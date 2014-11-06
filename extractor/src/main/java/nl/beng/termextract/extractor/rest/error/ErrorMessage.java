package nl.beng.termextract.extractor.rest.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorMessage {
	@JsonProperty
	private String message;

	@JsonCreator
	public ErrorMessage(String message) {
		this.message = message;
	}

}
