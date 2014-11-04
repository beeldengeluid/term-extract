package nl.beng.termextract.extractor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Settings {

	@Value("${nl.beng.termextract.algorithm.min.gram}")
	private Integer minGram;
	@Value("${nl.beng.termextract.algorithm.max.gram}")
	private Integer maxGram;
	@Value("${nl.beng.termextract.algorithm.min.token.frequency}")
	private Integer minTokenFrequency;
	@Value("${nl.beng.termextract.algorithm.norm.frequency}")
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
