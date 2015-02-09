package nl.beng.termextract.extractor.repository.namedentity.impl;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.ResourceBundle.getBundle;
import static nl.beng.termextract.extractor.repository.namedentity.NamedEntityType.MISC;
import static nl.beng.termextract.extractor.repository.namedentity.NamedEntityType.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
        List<NamedEntity> foundEntities = new ArrayList<>();
        Map<String, Entity> entitiesWithoutDBPediaType = newHashMap();
        Map<String, Entity> entitiesWithUnknownDBPediaType = newHashMap();
        try {
            List<Entity> textRazorEntities = client.analyze(text).getResponse().getEntities();
            if (textRazorEntities != null) {
                for (Entity entity : textRazorEntities) {
                    if (entity.getType() != null) {
                        foundEntities.add(convert(entity, entitiesWithUnknownDBPediaType));
                    } else {
                        entitiesWithoutDBPediaType.put(entity.getMatchedText(), entity);
                    }
                }
            }
        } catch (NetworkException | AnalysisException e) {
            throw new NamedEntityExtractionException(e.getMessage(), e);
        }
        logEntitiesTypeConversionResults(entitiesWithoutDBPediaType, entitiesWithUnknownDBPediaType, foundEntities);
        return foundEntities;
    }

    private NamedEntity convert(Entity entity, Map<String, Entity> entitiesWithUnknownDBPediaType) throws NamedEntityExtractionException {
        NamedEntity namedEntity = new NamedEntity();
        namedEntity.setText(entity.getMatchedText());

        String convertedType = convertType(entity.getType());
        if (convertedType != null) {
            namedEntity.setType(valueOf(convertedType));
        } else {
            namedEntity.setType(MISC);
            entitiesWithUnknownDBPediaType.put(entity.getMatchedText(), entity);
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

    private void logEntitiesTypeConversionResults(Map<String, Entity> entitiesWithoutDBPediaType, Map<String, Entity> entitiesWithUnknownDBPediaType,
            List<NamedEntity> foundEntities) {
        if (!entitiesWithoutDBPediaType.isEmpty()) {
            LOG.warn("Skipped {} unique entities without DBPedia type.", entitiesWithoutDBPediaType.size());
            int numberOfEntitiesWithoutFreebaseType = 0;
            for (String entityText : entitiesWithoutDBPediaType.keySet()) {
                if (entitiesWithoutDBPediaType.get(entityText).getFreebaseTypes() == null) {
                    numberOfEntitiesWithoutFreebaseType++;
                }
            }
            LOG.warn("Of which {} entities don't have Freebase types either... ", numberOfEntitiesWithoutFreebaseType);
            for (String entityText : entitiesWithoutDBPediaType.keySet()) {
                LOG.warn("\tEntity text: {} \t\t\t\t Freebase types: {}", entityText, entitiesWithoutDBPediaType.get(entityText).getFreebaseTypes());
            }
        }
        if (!entitiesWithUnknownDBPediaType.isEmpty()) {
            LOG.warn("Found {} unique entit(y)(ies) with unknown DBPedia type:", entitiesWithUnknownDBPediaType.size());
            for (String entityText : entitiesWithUnknownDBPediaType.keySet()) {
                LOG.warn("\tTextRazor entity type(s): {}, matched text: {}", entitiesWithUnknownDBPediaType.get(entityText).getType(), entityText);
            }
        }
        LOG.info("Converted types of {} TextRazor entities.", foundEntities.size());
        LOG.info("Converted types of {} unique TextRazor entities.", newHashSet(foundEntities).size());
    }
}
