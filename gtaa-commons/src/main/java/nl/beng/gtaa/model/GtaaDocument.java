package nl.beng.gtaa.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class GtaaDocument {

	@JsonProperty
	private String prefLabel;
	@JsonProperty
	private String altLabel;
	@JsonProperty
	private String conceptScheme;
	@JsonProperty
	private String uri;
	
}
