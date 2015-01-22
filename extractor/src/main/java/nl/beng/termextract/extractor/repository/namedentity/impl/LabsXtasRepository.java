package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.Saf;
import nl.beng.termextract.extractor.repository.namedentity.Token;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class LabsXtasRepository extends XtasRepository{
    
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
    
    public LabsXtasRepository() {
        super("saf");
    }
    
    public List<NamedEntity> parseXtasResponse(String xtasResponseString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Saf saf = mapper.readValue(xtasResponseString, Saf.class);
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
}
