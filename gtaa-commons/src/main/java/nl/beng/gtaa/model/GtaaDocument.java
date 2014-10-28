package nl.beng.gtaa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(type="GtaaDocument", indexName = "gtaa")
public class GtaaDocument {

	@JsonProperty
	private String prefLabel;
	@JsonProperty
	@Id
	private String id;
	@JsonProperty
	private String altLabel;
	@JsonProperty
	private String conceptScheme;
	@JsonProperty
	private String uri;

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

	public String getConceptScheme() {
		return conceptScheme;
	}

	public void setConceptScheme(String conceptScheme) {
		this.conceptScheme = conceptScheme;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "GtaaDocument [prefLabel=" + prefLabel + ", id=" + id
				+ ", altLabel=" + altLabel + ", conceptScheme=" + conceptScheme
				+ ", uri=" + uri + "]";
	}

}
