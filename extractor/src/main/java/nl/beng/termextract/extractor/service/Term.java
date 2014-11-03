package nl.beng.termextract.extractor.service;

import java.util.HashSet;
import java.util.Set;

import nl.beng.gtaa.model.GtaaDocument;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Term {

	@JsonProperty(value = "gtaa_matches")
	private Set<GtaaDocument> gtaaMatches;

	public Set<GtaaDocument> getGtaaMatches() {
		return gtaaMatches;
	}

	public void setGtaaMatches(Set<GtaaDocument> gtaaMatches) {
		this.gtaaMatches = gtaaMatches;
	}

	@Override
	public String toString() {
		return "Term [gtaaMatches=" + gtaaMatches
				+ "]";
	}

	public void addGtaaMatch(GtaaDocument gtaaDocument) {
		if (gtaaMatches == null) {
			gtaaMatches = new HashSet<>();
		}
		gtaaMatches.add(gtaaDocument);
	}

}
