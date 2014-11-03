package nl.beng.gtaa.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GtaaType {

	ONDERWERPEN, PERSOONSNAMEN, NAMEN, GEOGRAFISCHENAMEN;

	private static Map<String, GtaaType> namesMap = new HashMap<String, GtaaType>();

	static {
		namesMap.put("onderwerpen", ONDERWERPEN);
		namesMap.put("persoonsnamen", PERSOONSNAMEN);
		namesMap.put("namen", NAMEN);
		namesMap.put("geografischenamen", GEOGRAFISCHENAMEN);
	}

	@JsonCreator
	public static GtaaType forValue(String value) {
		return namesMap.get(StringUtils.lowerCase(value));
	}

	@JsonValue
	public String toValue() {
		for (Entry<String, GtaaType> entry : namesMap.entrySet()) {
			if (entry.getValue() == this)
				return entry.getKey();
		}

		return null;
	}

}
