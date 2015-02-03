package nl.beng.termextract.extractor.repository.namedentity.impl;

import static java.util.ResourceBundle.getBundle;
import static nl.beng.termextract.extractor.repository.namedentity.NamedEntityType.valueOf;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.Entity;

@Repository
public class TextRazorRepository implements NamedEntityRecognitionRepository {
    private static final Logger logger = LoggerFactory.getLogger(TextRazorRepository.class);

    @Autowired
    private TextRazor client;
    private ResourceBundle entities = getBundle("textrazor_entities");

    @Override
    public List<NamedEntity> extract(String text) throws NamedEntityExtractionException {
        List<NamedEntity> foundEntities = new ArrayList<NamedEntity>();
        try {
            List<Entity> textRazorEntities = client.analyze(text).getResponse().getEntities();
            if (textRazorEntities != null) {
                for (Entity entity : textRazorEntities) {
                    foundEntities.add(convert(entity));
                }
            }
        } catch (NetworkException | AnalysisException e) {
            throw new NamedEntityExtractionException(e.getMessage(), e);
        }
        return foundEntities;
    }

    private NamedEntity convert(Entity entity) {
        NamedEntity namedEntity = new NamedEntity();
        namedEntity.setText(entity.getMatchedText());

        String convertedType = convertType(entity.getType());
        if (convertedType != null) {
            namedEntity.setType(valueOf(convertedType));
        } else {
            logger.warn("No GTAA type found for any of TextRazor types: {}", entity.getType());
        }
        return namedEntity;
    }

    private String convertType(List<String> types) {
        String convertedType = null;
        for (String type : types) {
            if (entities.containsKey(type)) {
                convertedType = entities.getString(type);
                break;
            }
        }
        return convertedType;
    }
}
