package nl.beng.termextract.extractor.rest.model;

public class ExtractRequest {

	private Settings settings;
	private String text;

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
