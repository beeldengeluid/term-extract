package nl.beng.termextract.extractor.service;


@SuppressWarnings("serial")
public class ExtractionException extends Exception {

	public ExtractionException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public ExtractionException(String message) {
		super(message);
	}

}
