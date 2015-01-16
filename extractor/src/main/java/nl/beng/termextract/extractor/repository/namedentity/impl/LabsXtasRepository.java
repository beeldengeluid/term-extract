package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;
import nl.beng.termextract.extractor.repository.namedentity.Saf;
import nl.beng.termextract.extractor.repository.namedentity.Token;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class LabsXtasRepository extends XtasRepository implements NamedEntityRecognitionRepository{
    private static String xtasOutputType = "saf";
    
    @Value("${nl.beng.termextract.namedentity.xtas.904labs.url}")
    public void setXtasUrl(String xtasUrl) throws MalformedURLException {
        this.xtasUrl = new URL(xtasUrl);
    }
    @Value("${nl.beng.termextract.namedentity.xtas.apikey}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    @Value("${nl.beng.termextract.namedentity.xtas.apicontext}")
    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }
    
    @Override
    public List<NamedEntity> extract(String text) throws NamedEntityExtractionException {
        List<NamedEntity> namedEntities = new LinkedList<>();
        logger.info("Start extract(text)");
        try {
            String taskId = returnTaskId(text, xtasOutputType);
            Saf result = getData(new URL(xtasUrl, taskId));
            namedEntities = parseXtasResponse(result);
        } catch (Exception e) {
            String message = "Error during xtas extraction.";
            logger.error(message, e);
            throw new NamedEntityExtractionException(message, e);
        }
        
        logger.info("End extract(text)");
        return namedEntities;
    }
    
    public List<NamedEntity> parseXtasResponse(Saf saf) {
        List<NamedEntity> namedEntities = new LinkedList<>();
        if (saf != null && saf.getTokens() != null) {
            for (Token token: saf.getTokens()) {
                if (StringUtils.isNotBlank(token.getWord()) && StringUtils.isNotBlank(token.getNe())) {
                    parse(token.getWord(), token.getNe(), namedEntities);
                }
            }
        }
        return namedEntities;
    }
    
    private Saf getData(final URL url) throws NamedEntityExtractionException {
        ObjectMapper mapper = new ObjectMapper();
        Saf saf = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        OutputStream outputStream = null;       
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            saf = mapper.readValue(connection.getInputStream(), Saf.class);
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

        return saf;
    }
}
