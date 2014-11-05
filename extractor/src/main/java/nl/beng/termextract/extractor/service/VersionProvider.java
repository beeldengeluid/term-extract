package nl.beng.termextract.extractor.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Provides the version of the project. Maven exposes the project version during
 * build in src/main/resources/application.properties. This version provider is
 * a singleton.
 * 
 * @author AMJ001
 * 
 */
public final class VersionProvider {

	private static final String VERSION_UNKNOWN = "onbekend";

	private static final String APPLICATION_VERSION = "application.version";

	private static final String APPLICATION_PROPERTIES = "application.properties";

	private static final Logger LOGGER = Logger.getLogger(VersionProvider.class
			.getName());

	private static final VersionProvider provider = new VersionProvider();
	private String version;

	private VersionProvider() {
		Properties properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(APPLICATION_PROPERTIES));
			version = properties.getProperty(APPLICATION_VERSION);
		} catch (IOException e) {
			LOGGER.warn("Resource 'application.properties' was not found or error while reading current version. "
					+ e.getMessage());
			version = VERSION_UNKNOWN;
		}
	}

	public static String getVersion() {
		return provider.version;
	}
}