package nl.beng.termextract.extractor.rest.model;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ExtractRequest {

	@JsonInclude(value = Include.NON_NULL)
	private Properties settings;
	private String text;

	public Properties getSettings() {
		return settings;
	}

	public void setSettings(Properties settings) {
		this.settings = settings;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
