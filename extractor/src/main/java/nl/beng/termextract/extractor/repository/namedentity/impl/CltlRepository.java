package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityType;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class CltlRepository implements NamedEntityRecognitionRepository {

	private static final Logger logger = LoggerFactory
			.getLogger(CltlRepository.class);

	private URL treeTaggerUrl;
	private URL openerUrl;

	@Value(value = "${nl.beng.termextract.namedentity.cltl.treetagger.url}")
	public void setTreeTaggerUrl(String treeTaggerUrl)
			throws MalformedURLException {
		this.treeTaggerUrl = new URL(treeTaggerUrl);
	}
	@Value(value = "${nl.beng.termextract.namedentity.cltl.opener.url}")
	public void setOpenerUrl(String openerUrl) throws MalformedURLException {
		this.openerUrl = new URL(openerUrl);
	}

	@Override
	public List<NamedEntity> extract(String text)
			throws NamedEntityExtractionException {
		logger.info("Start extract(text)");
		Document document = null;
		List<NamedEntity> namedEntities = new LinkedList<>();
		String kafResult = postData(treeTaggerUrl, text);
		String nerResult = postData(openerUrl, kafResult);
		if (nerResult.length() > 0) {
			try {
				document = DocumentHelper.parseText(nerResult);
				List<Element> entities = document.selectNodes("//entity");

				for (Element entity : entities) {

					NamedEntity namedEntity = new NamedEntity();
					String type = entity.attributeValue("type");
					if ("PERSON".equalsIgnoreCase(type)) {
						namedEntity.setType(NamedEntityType.PERSON);
					} else if ("LOCATION".equalsIgnoreCase(type)) {
						namedEntity.setType(NamedEntityType.LOCATION);
					} else if ("ORGANIZATION".equalsIgnoreCase(type)) {
						namedEntity.setType(NamedEntityType.ORGANIZATION);
					} else {
						namedEntity.setType(NamedEntityType.MISC);
					}

					Element refs = entity.element("references");
					for (Iterator<Node> it = refs.nodeIterator(); it.hasNext();) {
						Node node = (Node) it.next();
						if (node.getNodeType() == Node.COMMENT_NODE) {
							namedEntity.setText(node.getText());
							break;
						}
					}
					logger.debug("Named entity extracted '" + namedEntity + "'" );
					namedEntities.add(namedEntity);
				}
			} catch (DocumentException e) {
				String message = "Could parse response.";
				logger.error(message, e);
				throw new NamedEntityExtractionException(message, e);
			}
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
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
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

}
