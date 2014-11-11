package nl.beng.gtaa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(type = GtaaDocument.DOCUMENT_NAME, indexName = "gtaa")
public class GtaaDocument {

	public static final String DOCUMENT_NAME = "gtaa_document";
	
	@JsonProperty
	@Id
	private String id;
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private String prefLabel;
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private String altLabel;
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private String[] conceptSchemes;
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private String uri;
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private GtaaType type;
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private Float score;

	public GtaaDocument() {
	}

	public GtaaDocument(String id) {
		this.id = id;
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

	public GtaaType getType() {
		return type;
	}

	public void setType(GtaaType type) {
		this.type = type;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GtaaDocument other = (GtaaDocument) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GtaaDocument [id=" + id + ", score=" + score + ", prefLabel="
				+ prefLabel + ", altLabel=" + altLabel + ", conceptScheme="
				+ conceptSchemes + ", uri=" + uri + ", type=" + type + "]";
	}

}
