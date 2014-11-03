package nl.beng.termextract.extractor.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Settings {
	@JsonProperty("min_gram")
	@JsonInclude(value = Include.NON_NULL)
	private Integer minGram;
	@JsonProperty("max_gram")
	@JsonInclude(value = Include.NON_NULL)
	private Integer maxGram;
	@JsonProperty("min_token_frequency")
	@JsonInclude(value = Include.NON_NULL)
	private Integer minTokenFrequency;
	@JsonProperty("min_normalized_frequency")
	@JsonInclude(value = Include.NON_NULL)
	private Double minNormalizedFrequency;

	public Integer getMinGram() {
		return minGram;
	}

	public void setMinGram(Integer minGram) {
		this.minGram = minGram;
	}

	public Integer getMaxGram() {
		return maxGram;
	}

	public void setMaxGram(Integer maxGram) {
		this.maxGram = maxGram;
	}

	public Integer getMinTokenFrequency() {
		return minTokenFrequency;
	}

	public void setMinTokenFrequency(Integer minTokenFrequency) {
		this.minTokenFrequency = minTokenFrequency;
	}

	public Double getMinNormalizedFrequency() {
		return minNormalizedFrequency;
	}

	public void setMinNormalizedFrequency(Double minNormalizedFrequency) {
		this.minNormalizedFrequency = minNormalizedFrequency;
	}
}
