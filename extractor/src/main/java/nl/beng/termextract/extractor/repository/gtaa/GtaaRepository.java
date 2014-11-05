package nl.beng.termextract.extractor.repository.gtaa;

import java.util.List;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;

public interface GtaaRepository {

	List<GtaaDocument> find(final String token, Float minScore, GtaaType...types);

}
