package nl.beng.termextract.extractor.repository.namedentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NamedEntityType {

	PERSON, ORGANIZATION, LOCATION, MISC;

	private static Map<String, NamedEntityType> namesMap = new HashMap<String, NamedEntityType>();

	static {
		namesMap.put("person", PERSON);
		namesMap.put("organization", ORGANIZATION);
		namesMap.put("location", LOCATION);
		namesMap.put("misc", MISC);
	}

	@JsonCreator
	public static NamedEntityType forValue(String value) {
		return namesMap.get(StringUtils.lowerCase(value));
	}

	@JsonValue
	public String toValue() {
		for (Entry<String, NamedEntityType> entry : namesMap.entrySet()) {
			if (entry.getValue() == this)
				return entry.getKey();
		}

		return null;
	}

}
