package nl.beng.termextract.extractor.service.model;

import java.util.List;
import java.util.Properties;

public class ExtractResponse {

	private String version;
	private Integer total;
	private Properties settings;
	private List<Match> matches;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Match> getMatches() {
		return matches;
	}

	public void setMatches(List<Match> matches) {
		this.matches = matches;
	}

	public Properties getSettings() {
		return settings;
	}

	public void setSettings(Properties settings) {
		this.settings = settings;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}
