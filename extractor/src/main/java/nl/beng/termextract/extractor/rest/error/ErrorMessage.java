package nl.beng.termextract.extractor.rest.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: Danny Date: 10-7-14 Time: 16:42
 */
public class ErrorMessage {
	@JsonProperty
	private String errorCode;
	@JsonProperty
	private String message;

	@JsonCreator
	public ErrorMessage(String errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

}
