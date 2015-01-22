package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class LocalXtasRepository extends XtasRepository {
    private static final int NUMBER_OF_TOKEN_LINE_COLUMNS = 10;
    private static final int TOKEN_FIELD_POS = 1;
    private static final int TOKEN_ENCODING_FIELD_POS = 6;
    
    @Value("${nl.beng.termextract.namedentity.xtas.local.url}")
    public void setXtasUrl(String xtasUrl) throws MalformedURLException {
        this.xtasUrl = new URL(xtasUrl);
    }
    
    public LocalXtasRepository() {
        super("raw");
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
