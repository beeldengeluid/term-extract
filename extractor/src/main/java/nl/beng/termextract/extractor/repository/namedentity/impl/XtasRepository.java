package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class XtasRepository implements NamedEntityRecognitionRepository {

	private static final int NUMBER_OF_TOKEN_LINE_COLUMNS = 10;
	private static final int TOKEN_FIELD_POS = 1;
	private static final int TOKEN_ENCODING_FIELD_POS = 6;

	private static final String RUN_FROG_CONTEXT = "/run/frog";

	private static final Logger logger = LoggerFactory
			.getLogger(XtasRepository.class);

	private URL xtasUrl;

	@Value(value = "${nl.beng.termextract.namedentity.xtas.url}")
	public void setXtasUrl(String xtasUrl) throws MalformedURLException {
		this.xtasUrl = new URL(xtasUrl);
	}

	@Override
	public List<NamedEntity> extract(String text)
			throws NamedEntityExtractionException {
		logger.info("Start extract(text)");
		List<NamedEntity> namedEntities = new LinkedList<>();
		String taskId = null;
		try {
			taskId = postData(new URL(xtasUrl, RUN_FROG_CONTEXT),
					"{\"data\": \"" + text + "\"}");
			logger.debug("xtas taskid:" + taskId);
			String result = getData(new URL(xtasUrl, "/result/" + taskId));
			namedEntities = parseXtasResponse(result);
		} catch (MalformedURLException e) {
			String message = "Error during xtas extraction.";
			logger.error(message, e);
			throw new NamedEntityExtractionException(message, e);
		}
		logger.info("End extract(text)");
		return namedEntities;
	}

	private List<NamedEntity> parseXtasResponse(String xtasResponseString)
			throws NamedEntityExtractionException {
		List<NamedEntity> namedEntities = new LinkedList<>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			logger.debug(xtasResponseString);
			String[] responseArray = mapper.readValue(xtasResponseString,
					String[].class);
			for (String responseItem : responseArray) {
				if (StringUtils.isNotBlank(responseItem)) {
					String responseItemFields[] = responseItem.split("	");
					if (responseItemFields.length == NUMBER_OF_TOKEN_LINE_COLUMNS) {
						String tokens = responseItemFields[TOKEN_FIELD_POS];
						String tokenEncoding = responseItemFields[TOKEN_ENCODING_FIELD_POS];
						namedEntities.addAll(parse(tokens, tokenEncoding));
					}
				}
			}
		} catch (Exception e) {
			String message = "Unable to parse xtas response '"
					+ xtasResponseString + "'";
			logger.error(message, e);
			throw new NamedEntityExtractionException(message, e);
		}
		return namedEntities;
	}

	private List<NamedEntity> parse(String tokens, String tokenEncoding) {
		List<NamedEntity> namedEntities = new LinkedList<>();
		String[] tokenArray = tokens.split("_");
		String[] tokenEncodingArray = tokenEncoding.split("_");
		if (tokenArray.length != tokenEncodingArray.length) {
			logger.warn("Tokens '" + tokens + "' do not match token '"
					+ tokenEncoding + "' encoding.");
			return null;
		}

		int index = 0;
		NamedEntity namedEntity = null;
		for (String token : tokenArray) {
			String encoding = tokenEncodingArray[index];
			if (encoding.indexOf("-") == 1) {
				String tag = encoding.substring(0, encoding.indexOf("-"));
				String type = encoding.substring(encoding.indexOf("-") + 1);
				if (!StringUtils.isBlank(type)) {
					if (tag.equals("B")) {
						if (namedEntity != null) {
							// we already have a named entity
							namedEntities.add(namedEntity);
						}
						NamedEntityType namedEntityType = extractNamedEntityType(type);
						if (namedEntityType != null) {
							namedEntity = new NamedEntity();
							namedEntity.setType(namedEntityType);
							namedEntity.setText(token);
						}
					} else if (tag.equals("I")) {
						if (namedEntity != null) {
							namedEntity.setText(namedEntity.getText() + " "
									+ token);
						}
					}
				}
			}
			index++;
		}
		if (namedEntity != null) {
			namedEntities.add(namedEntity);

		}
		return namedEntities;
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
			String postData = inputString;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length",
					String.valueOf(postData.length()));

			// Write data
			outputStream = connection.getOutputStream();
			outputStream.write(postData.getBytes());

			// Read response
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
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			// Read response
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
