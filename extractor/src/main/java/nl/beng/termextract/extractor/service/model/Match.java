package nl.beng.termextract.extractor.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Match {

	private String uri;
	private String type;
	@JsonProperty(value = "pref_label")
	@JsonInclude(value = Include.NON_NULL)
	private String prefLabel;
	@JsonProperty(value = "alt_label")
	@JsonInclude(value = Include.NON_NULL)
	private String altLabel;
	@JsonProperty(value = "concept_schemes")
	@JsonInclude(value = Include.NON_NULL)
	private String[] conceptSchemes;
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private Float score;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPrefLabel() {
		return prefLabel;
	}

	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}

	public String getAltLabel() {
		return altLabel;
	}

	public void setAltLabel(String altLabel) {
		this.altLabel = altLabel;
	}

	public String[] getConceptSchemes() {
		return conceptSchemes;
	}

	public void setConceptSchemes(String[] conceptSchemes) {
		this.conceptSchemes = conceptSchemes;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "Match [uri=" + uri + ", score=" + score + ", prefLabel="
				+ prefLabel + ", altLabel=" + altLabel + ", type=" + type
				+ ", conceptSchemes=" + conceptSchemes + "]";
	}
}
