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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class LocalXtasRepository extends XtasRepository implements NamedEntityRecognitionRepository{
    private static final int NUMBER_OF_TOKEN_LINE_COLUMNS = 10;
    private static final int TOKEN_FIELD_POS = 1;
    private static final int TOKEN_ENCODING_FIELD_POS = 6;
    private static String xtasOutputType = "raw";
    
    @Value("${nl.beng.termextract.namedentity.xtas.local.url}")
    public void setXtasUrl(String xtasUrl) throws MalformedURLException {
        this.xtasUrl = new URL(xtasUrl);
    }

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
    
    public List<NamedEntity> parseXtasResponse(String xtasResponseString)
            throws NamedEntityExtractionException {
        List<NamedEntity> namedEntities = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.debug(xtasResponseString);
            String[] responseArray = mapper.readValue(xtasResponseString,
                    String[].class);
            for (String responseItem : responseArray) {
                if (StringUtils.isNotBlank(responseItem)) {
                    String responseItemFields[] = responseItem.split("\t", -1);
                    if (responseItemFields.length == NUMBER_OF_TOKEN_LINE_COLUMNS) {
                        String tokens = responseItemFields[TOKEN_FIELD_POS];
                        String tokenEncoding = responseItemFields[TOKEN_ENCODING_FIELD_POS];
                        parse(tokens, tokenEncoding, namedEntities);
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
}
