package nl.beng.termextract.extractor.service;

import java.util.List;
import java.util.Set;

public interface ExtractorService {

	/**
	 * Extracts per text in <code>texts</code> the terms in the text.
	 * 
	 * @param texts
	 * @param settings
	 * @return A list with a set of {@link Term}
	 * @throws ExtractionException
	 */
	List<Set<Term>> extract(List<String> texts, Settings settings) throws ExtractionException;
}
