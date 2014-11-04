package nl.beng.termextract.extractor.service;

import java.util.Set;

public interface ExtractorService {

	/**
	 * Extracts the terms in the text with the given settings.
	 * 
	 * @param text
	 * @param settings
	 * @return
	 * @throws ExtractionException
	 */
	Set<Match> extract(String text, Settings settings)
			throws ExtractionException;

	/**
	 * Extract the terms in the text
	 * 
	 * @param text
	 * @return
	 * @throws ExtractionException
	 */
	Set<Match> extract(String text) throws ExtractionException;

}
