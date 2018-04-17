package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class XtasRepository implements NamedEntityRecognitionRepository {
	private static final String RUN_FROG_CONTEXT = "/run/frog";
	private static final String RESULT_PATH = "/result/";
	// 5 minutes timeout
	private static final int TIMEOUT = 300000;

	protected static final Logger logger = LoggerFactory
			.getLogger(XtasRepository.class);

	protected URL xtasUrl;
	protected String apiKey;
	protected String apiContext;
	private ObjectMapper jsonMapper = new ObjectMapper();
	private String xtasOutputType;
	
	public XtasRepository(String outputType) {
	    this.xtasOutputType = outputType;
	}
	
	private static class XtasPayload {
	    @JsonProperty
	    private String data;
	    @JsonProperty
	    private Arguments arguments;

        public XtasPayload(String text, String output) {
            this.data = text;
            arguments = new Arguments(output);
        }
        
        private static class Arguments {
            @JsonProperty
            private String output;
            
            public Arguments(String output) {
                this.output = output;
            }
        }
	}
	
	abstract List<NamedEntity> parseXtasResponse(String xtasResponse) throws Exception;
	
	@Override
    public List<NamedEntity> extract(String text) throws NamedEntityExtractionException {
        List<NamedEntity> namedEntities = new LinkedList<>();
        logger.info("Start extract(text)");
        try {
            String taskId = returnTaskId(text, xtasOutputType);
            String result = getData(new URL(xtasUrl, taskId));
            namedEntities = parseXtasResponse(result);
        } catch (Exception e) {
            String message = "Error during xtas extraction.";
            logger.error(message, e);
            throw new NamedEntityExtractionException(message, e);
        }
        
        logger.info("End extract(text)");
        return namedEntities;
    }
	
	protected String returnTaskId(String text, String xtasOutputType)
			throws Exception {
		String taskId = null;
		String resultPath = RESULT_PATH;
		String contextPlusKey = RUN_FROG_CONTEXT;
		boolean useNewXtas = StringUtils.isNotBlank(apiKey) && StringUtils.isNotBlank(apiContext);
		if (useNewXtas) {
		    contextPlusKey = apiContext + contextPlusKey + apiKey;
		    resultPath = apiContext + resultPath;
		}
		
		XtasPayload xtasPayloadObj = new XtasPayload(text, xtasOutputType); // just to make sure there is escaped quotes in there
		String xtasPayloadJson = jsonMapper.writeValueAsString(xtasPayloadObj);
        logger.debug("payload:" + xtasPayloadJson);
		    
		taskId = postData(new URL(xtasUrl, contextPlusKey), xtasPayloadJson);
		logger.debug("xtas taskid:" + taskId);
		if (useNewXtas) {
		    taskId = taskId + apiKey;
		}			
		return resultPath + taskId;
	}

	protected void parse(String tokens, String tokenEncoding, List<NamedEntity> namedEntities) {
		String[] tokenArray = tokens.split("_");
		String[] tokenEncodingArray = tokenEncoding.split("_");
		int index = 0;
		boolean mergeWithPrevious = false;
		NamedEntity namedEntity = null;
		if (tokenArray.length != tokenEncodingArray.length) {
			logger.warn("Tokens '" + tokens + "' do not match token '"
					+ tokenEncoding + "' encoding.");
			return;
		}
		for (String token : tokenArray) {
			String encoding = tokenEncodingArray[index];
			if (encoding.indexOf("-") == 1) {
				String tag = encoding.substring(0, encoding.indexOf("-"));
				String type = encoding.substring(encoding.indexOf("-") + 1);
				if (!StringUtils.isBlank(type)) {
					if (tag.equals("B")) {
						if (namedEntity != null) {
							logger.debug("Named entity extracted '" + namedEntity + "'" );
							// we already have a named entity
							namedEntities.add(namedEntity);
							if (mergeWithPrevious) {
							    namedEntities.add(merge(namedEntities.get(namedEntities.size()-2), namedEntity));
							    mergeWithPrevious = false;
							}
						}
						namedEntity = createNamedEntity(token, type);
					} else if (tag.equals("I")) {
						if (namedEntity != null) {
							namedEntity.setText(namedEntity.getText() + " "
									+ token);
						} else {
						    namedEntity = createNamedEntity(token, type);
						    mergeWithPrevious = true;
						}
					}
				}
			}
			index++;
		}
		if (namedEntity != null) {
			logger.debug("Named entity extracted '" + namedEntity + "'" );
			namedEntities.add(namedEntity);
			if (mergeWithPrevious) {
                namedEntities.add(merge(namedEntities.get(namedEntities.size()-2), namedEntity));
			}
		}
	}
	
	private NamedEntity createNamedEntity(String token, String type) {
	    NamedEntity namedEntity = null;
	    NamedEntityType namedEntityType = extractNamedEntityType(type);
        if (namedEntityType != null) {
            namedEntity = new NamedEntity();
            namedEntity.setType(namedEntityType);
            namedEntity.setText(token);
        }
        return namedEntity;
	}
	
	private NamedEntity merge(NamedEntity previous, NamedEntity current) {
	    NamedEntity namedEntity = new NamedEntity();
	    namedEntity.setText(previous.getText() + " " + current.getText());
	    namedEntity.setType(previous.getType());
	    logger.debug("Named entity extracted '" + namedEntity + "'" );
	    return namedEntity;
	}

	private NamedEntityType extractNamedEntityType(String type) {
		switch (type) {
		case "PER":
			return NamedEntityType.PERSON;
		case "LOC":
			return NamedEntityType.LOCATION;
		case "ORG":
			return NamedEntityType.ORGANIZATION;
		case "PRO":
		case "EVE":
		case "MISC":
			return NamedEntityType.MISC;
		default:
			return null;
		}
	}

	private String postData(final URL url, final String inputString)
			throws NamedEntityExtractionException {
		StringBuilder response = new StringBuilder();
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		OutputStream outputStream = null;
		try {
			byte[] postData = inputString.getBytes("UTF-8");
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json"); // might need charset=utf-8 added in case they change the server platform (isn't accepted at time of writing this)
			connection.setRequestProperty("Content-Length",
					String.valueOf(postData.length));
			outputStream = connection.getOutputStream();
			outputStream.write(postData);
			outputStream.flush();
			
			reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null)
				response.append(line);
		} catch (IOException e) {
			String message = "Could not read from url '" + url + "'";
			logger.error(message, e);
			throw new NamedEntityExtractionException(message, e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}

		return response.toString();
	}
	
	private String getData(final URL url) throws NamedEntityExtractionException {
        StringBuilder response = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        OutputStream outputStream = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null)
                response.append(line);
        } catch (IOException e) {
            String message = "Could not read from url '" + url + "'";
            logger.error(message, e);
            throw new NamedEntityExtractionException(message, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return response.toString();
    }

}
