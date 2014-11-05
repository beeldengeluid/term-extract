package nl.beng.termextract.extractor.service;

import nl.beng.termextract.extractor.service.model.ExtractResponse;
import nl.beng.termextract.extractor.service.model.Settings;

public interface ExtractorService {

	/**
	 * Extracts the terms in the text with the given settings.
	 * 
	 * @param text
	 * @param settings
	 * @return
	 * @throws ExtractionException
	 */
	ExtractResponse extract(String text, Settings settings)
			throws ExtractionException;

	/**
	 * Extract the terms in the text
	 * 
	 * @param text
	 * @return
	 * @throws ExtractionException
	 */
	ExtractResponse extract(String text) throws ExtractionException;

}
