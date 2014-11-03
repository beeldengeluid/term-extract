package nl.beng.termextract.extractor.repository.namedentity;

import java.util.List;

public interface NamedEntityRecognitionRepository {

	/**
	 * Extract named entities from <code>text</code>.
	 * 
	 * @param text
	 * @return {@link NamedEntity}
	 * @throws NamedEntityExtractionException
	 */
	List<NamedEntity> extract(final String text)
			throws NamedEntityExtractionException;
}
