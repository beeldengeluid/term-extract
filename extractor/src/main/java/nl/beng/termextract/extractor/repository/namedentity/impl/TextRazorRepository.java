package nl.beng.termextract.extractor.repository.namedentity.impl;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.ResourceBundle.getBundle;
import static nl.beng.termextract.extractor.repository.namedentity.NamedEntityType.MISC;
import static nl.beng.termextract.extractor.repository.namedentity.NamedEntityType.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.Entity;

@Repository
public class TextRazorRepository implements NamedEntityRecognitionRepository {
    public static final ResourceBundle ENTITY_MAP = getBundle("textrazor_entities");
    private static final Logger LOG = getLogger(TextRazorRepository.class);

    @Autowired
    private TextRazor client;

    @Override
    public List<NamedEntity> extract(String text) throws NamedEntityExtractionException {
        Set<NamedEntity> foundEntities = newHashSet();
        try {
            List<Entity> textRazorEntities = client.analyze(text).getResponse().getEntities();
            if (textRazorEntities != null) {
                for (Entity entity : textRazorEntities) {
                    if (entity.getType() != null) {
                        foundEntities.add(convert(entity));
                    } else {
                        LOG.warn("No DBPedia types found for entity with text: [{}]", entity.getMatchedText());
                    }
                }
            }
        } catch (NetworkException | AnalysisException e) {
            throw new NamedEntityExtractionException(e.getMessage(), e);
        }
        return new ArrayList<NamedEntity>(foundEntities);
    }

    private NamedEntity convert(Entity entity) throws NamedEntityExtractionException {
        NamedEntity namedEntity = new NamedEntity();
        namedEntity.setText(entity.getMatchedText());

        String convertedType = convertType(entity.getType());
        if (convertedType != null) {
            namedEntity.setType(valueOf(convertedType));
        } else {
            namedEntity.setType(MISC);
            LOG.warn("No GTAA type found for any of TextRazor types: " + entity.getType() + " setting default type MISC...");
        }
        return namedEntity;
    }

    private String convertType(List<String> types) {
        String convertedType = null;
        for (String type : types) {
            if (ENTITY_MAP.containsKey(type)) {
                convertedType = ENTITY_MAP.getString(type);
                if (valueOf(convertedType) != MISC) {
                    break;
                }
            }
        }
        return convertedType;
    }
}
