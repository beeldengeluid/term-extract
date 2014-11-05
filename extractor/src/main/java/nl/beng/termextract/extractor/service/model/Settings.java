package nl.beng.termextract.extractor.service.model;

import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.Properties;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {
	private static final Logger logger = LoggerFactory
			.getLogger(Settings.class);

	private static final String TOKENIZER_MIN_GRAM = "tokenizer.min.gram";
	private static final String TOKENIZER_MAX_GRAM = "tokenizer.max.gram";
	private static final String TOKENIZER_MIN_NORM_FREQUENCY = "tokenizer.min.norm.frequency";
	private static final String TOKENIZER_MIN_TOKEN_FREQUENCY = "tokenizer.min.token.frequency";
	private static final String TOKENIZER_MIN_SCORE = "tokenizer.min.score";
	private static final String NAMEDENTITY_MIN_TOKEN_FREQUENCY = "namedentity.{0}.min.token.frequency";
	private static final String NAMEDENTITY_MIN_SCORE = "namedentity.{0}.min.score";

	private Properties properties = new Properties();

	public Settings() {
	}

	public Settings(Properties properties) {
		this.properties.putAll(properties);
		for (Entry<Object, Object> property : this.properties.entrySet()) {
			logger.info("default property: '" + property.toString() + "'");
		}
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Integer getTokenizerMinTokenFrequency() {
		return Integer.valueOf((String) this.properties
				.get(TOKENIZER_MIN_TOKEN_FREQUENCY));
	}

	public Float getTokenizerMinNormFrequency() {
		return Float.valueOf((String) this.properties
				.get(TOKENIZER_MIN_NORM_FREQUENCY));
	}

	public Integer getTokenizerMinGram() {
		return Integer
				.valueOf((String) this.properties.get(TOKENIZER_MIN_GRAM));
	}

	public Integer getTokenizerMaxGram() {
		return Integer
				.valueOf((String) this.properties.get(TOKENIZER_MAX_GRAM));
	}

	public Float getTokenizerMinScore() {
		return Float.valueOf((String) this.properties.get(TOKENIZER_MIN_SCORE));
	}

	public Float getNamedEntityMinScore(NamedEntityType namedEntityType) {
		String propertyName = MessageFormat.format(NAMEDENTITY_MIN_SCORE,
				namedEntityType.toValue());
		return Float.valueOf((String) this.properties.get(propertyName));
	}

	public Integer getNamedEntityMinTokenFrequency(
			NamedEntityType namedEntityType) {
		String propertyName = MessageFormat.format(
				NAMEDENTITY_MIN_TOKEN_FREQUENCY, namedEntityType.toValue());
		return Integer.valueOf((String) this.properties.get(propertyName));
	}
}
