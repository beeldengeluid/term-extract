package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class XtasRepository implements NamedEntityRecognitionRepository {

	private static final String RUN_FROG_CONTEXT = "/run/frog";

	private static final Logger logger = LoggerFactory
			.getLogger(XtasRepository.class);

	private URL xtasUrl;

	@Value(value = "${nl.beng.termextract.namedentity.xtas.url}")
	public void setXtasUrl(String xtasUrl) throws MalformedURLException {
		this.xtasUrl = new URL(xtasUrl);
	}

	@Override
	public List<NamedEntity> extract(String text)
			throws NamedEntityExtractionException {
		logger.info("Start extract(text)");
		List<NamedEntity> namedEntities = new LinkedList<>();
		String taskId = null;
		try {
			taskId = postData(new URL(xtasUrl, RUN_FROG_CONTEXT),
					"{\"data\": \"" + text + "\"}");
			System.out.println(taskId);
			String result = getData(new URL(xtasUrl, "/result/" + taskId));
			String[] resultItems = result.split(",");
			for (String resultItem : resultItems) {
				System.out.println(resultItem);
			}
		} catch (MalformedURLException e) {
			String message = "Error during xtas extraction.";
			logger.error(message, e);
			throw new NamedEntityExtractionException(message, e);
		}
		logger.info("End extract(text)");
		return namedEntities;
	}

	private String postData(final URL url, final String inputString)
			throws NamedEntityExtractionException {
		StringBuilder response = new StringBuilder();
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		OutputStream outputStream = null;
		try {
			String postData = inputString;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length",
					String.valueOf(postData.length()));

			// Write data
			outputStream = connection.getOutputStream();
			outputStream.write(postData.getBytes());

			// Read response
			reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line = null;
			while ((line = reader.readLine()) != null)
				response.append(line);
		} catch (IOException e) {
			String message = "Could not read from url '" + url + "'";
			logger.error(message, e);
			throw new NamedEntityExtractionException(message, e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}

		return response.toString();
	}

	private String getData(final URL url) throws NamedEntityExtractionException {
		StringBuilder response = new StringBuilder();
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		OutputStream outputStream = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			// Read response
			reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line = null;
			while ((line = reader.readLine()) != null)
				response.append(line);
		} catch (IOException e) {
			String message = "Could not read from url '" + url + "'";
			logger.error(message, e);
			throw new NamedEntityExtractionException(message, e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}

		return response.toString();
	}
}
