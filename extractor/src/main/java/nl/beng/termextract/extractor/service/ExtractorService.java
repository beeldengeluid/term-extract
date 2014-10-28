package nl.beng.termextract.extractor.service;

import java.util.List;
import java.util.Set;

import nl.beng.termextract.extractor.model.Term;

public interface ExtractorService {

	/**
	 * Extracts per text in <code>texts</code> the terms in the text.
	 * 
	 * @param texts
	 * @return A list with a set of {@link Term} 
	 */
	List<Set<Term>> extract(List<String> texts);

}
