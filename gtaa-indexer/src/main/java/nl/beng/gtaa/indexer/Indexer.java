package nl.beng.gtaa.indexer;

import java.util.LinkedList;
import java.util.List;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Component;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;

@Component(value = "indexer")
public class Indexer {

	private static final String GTAA_GEOGRAFISCHE_NAMEN_SCHEME = "http://data.beeldengeluid.nl/gtaa/GeografischeNamen";
	private static final String GTAA_NAMEN_SCHEME = "http://data.beeldengeluid.nl/gtaa/Namen";
	private static final String GTAA_PERSOONSNAMEN_SCHEME = "http://data.beeldengeluid.nl/gtaa/Persoonsnamen";
	private static final String GTAA_ONDERWERPEN_SCHEME = "http://data.beeldengeluid.nl/gtaa/Onderwerpen";
	private static final String GTAA_SCHEME = "data.beeldengeluid.nl/gtaa";
	private static final String IN_SCHEME_ELEMENT = "inScheme";
	private static final String PREF_LABEL_ELEMENT = "prefLabel";
	private static final String ALT_LABEL_ELEMENT = "altLabel";
	private static final String RESOURCE_ATTRIBUTE = "resource";
	private static final String URI_ATTRIBUTE = "about";
	private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

	@Autowired
	private ElasticsearchTemplate template;
	@Value("${nl.beng.gtaa.oai.server.url}")
	private String oaiPmhServerUrl;
	@Value("${nl.beng.gtaa.oai.metadata.prefix}")
	private String metaDataPrefix;
	@Value("${nl.beng.gtaa.elasticsearch.index.name}")
	private String indexName;

	public void index() {
		logger.info("Start harvesting oai metadata. ");
		OaiPmhServer server = new OaiPmhServer(oaiPmhServerUrl);
		try {
			logger.info("harvesting....");
			RecordsList recordsList = server.listRecords(metaDataPrefix);
			while (recordsList.size() > 0) {
				List<GtaaDocument> gtaaDocuments = createGtaaDocuments(recordsList);
				logger.info("harvested '" + gtaaDocuments.size()
						+ "' documents.");
				logger.info("Indexing '" + gtaaDocuments.size()
						+ "' documents...");
				index(gtaaDocuments);
				logger.info("Indexed '" + gtaaDocuments.size() + "' documents.");
				logger.info("harvesting....");
				recordsList = server.listRecords(recordsList
						.getResumptionToken());

			}

		} catch (OAIException e) {
			logger.error("Error during harvesting oai metadata. ", e);
		} finally {
			logger.info("Finished harvesting oai metadata. ");
		}
	}

	private void index(List<GtaaDocument> documents) {
		List<IndexQuery> indexQueries = new LinkedList<IndexQuery>();
		for (GtaaDocument document : documents) {
			IndexQuery indexQuery = new IndexQuery();
			indexQuery.setObject(document);
			indexQuery.setId(document.getId());
			indexQuery.setIndexName(indexName);
			indexQueries.add(indexQuery);
		}
		if (indexQueries.size() > 0) {
			template.bulkIndex(indexQueries);
		}
	}

	private List<GtaaDocument> createGtaaDocuments(RecordsList recordsList) {
		List<GtaaDocument> gtaaDocuments = new LinkedList<GtaaDocument>();
		List<Record> records = recordsList.asList();
		for (Record record : records) {
			GtaaDocument gtaaDocument = createGtaaDocument(record);
			if (gtaaDocument != null) {
				gtaaDocuments.add(gtaaDocument);
				logger.debug(gtaaDocument.toString());
			}
		}
		return gtaaDocuments;
	}

	@SuppressWarnings("unchecked")
	private GtaaDocument createGtaaDocument(Record record) {
		GtaaDocument gtaaDocument = null;
		if (record.getMetadata() != null
				&& record.getMetadata().elements().size() > 0) {
			Element data = (Element) record.getMetadata().elements().get(0);
			Attribute uriAttribute = data.attribute(URI_ATTRIBUTE);
			if (uriAttribute != null
					&& StringUtils.isNotEmpty(uriAttribute.getText())) {
				String conceptScheme = extractConceptScheme(data
						.elements(IN_SCHEME_ELEMENT));
				GtaaType type = extractGtaaType(conceptScheme);
				if (type != null) {
					gtaaDocument = new GtaaDocument();
					String uri = data.attribute(URI_ATTRIBUTE).getText();
					gtaaDocument.setType(type);
					gtaaDocument.setId(uri.replaceAll("/", "_"));
					gtaaDocument.setUri(uri);
					gtaaDocument.setAltLabel(extractTextFromElements(data
							.elements(ALT_LABEL_ELEMENT)));
					gtaaDocument.setPrefLabel(extractTextFromElements(data
							.elements(PREF_LABEL_ELEMENT)));
					gtaaDocument.setConceptScheme(conceptScheme);
				}
			}

		}
		return gtaaDocument;
	}

	private GtaaType extractGtaaType(String conceptScheme) {
		if (StringUtils.isNotEmpty(conceptScheme)) {
			if (conceptScheme.contains(GTAA_SCHEME)) {
				for (String scheme : conceptScheme.split(" ")) {
					if (GTAA_ONDERWERPEN_SCHEME.equals(scheme)) {
						return GtaaType.ONDERWERPEN;
					} else if (GTAA_PERSOONSNAMEN_SCHEME.equals(scheme)) {
						return GtaaType.PERSOONSNAMEN;
					} else if (GTAA_NAMEN_SCHEME.equals(scheme)) {
						return GtaaType.NAMEN;
					} else if (GTAA_GEOGRAFISCHE_NAMEN_SCHEME.equals(scheme)) {
						return GtaaType.GEOGRAFISCHENAMEN;
					}

				}
			}
		}
		return null;
	}

	private String extractConceptScheme(List<Element> elements) {
		if (elements == null) {
			return null;
		}
		String extractedValue = null;
		for (Element element : elements) {
			Attribute resourceAttribute = element.attribute(RESOURCE_ATTRIBUTE);
			if (resourceAttribute != null
					&& StringUtils.isNotEmpty(resourceAttribute.getValue())) {
				if (extractedValue != null) {
					extractedValue += " ";
					extractedValue += resourceAttribute.getValue();
				} else {
					extractedValue = resourceAttribute.getValue();
				}
			}

		}
		return extractedValue;
	}

	private String extractTextFromElements(List<Element> elements) {
		if (elements == null) {
			return null;
		}
		String extractedValue = null;
		for (Element element : elements) {
			if (StringUtils.isNotEmpty(element.getText())) {
				if (extractedValue != null) {
					extractedValue += " ";
					extractedValue += element.getText();
				} else {
					extractedValue = element.getText();
				}
			}

		}
		return extractedValue;
	}

	public static void main(String[] args) {
		String contextFile = "./src/main/resources/application-context.xml";
		if (args.length >= 1) {
			contextFile = args[0];

		}
		FileSystemXmlApplicationContext context = null;
		try {
			context = new FileSystemXmlApplicationContext(contextFile);
			Indexer indexer = (Indexer) context.getBean("indexer");
			indexer.index();
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}
}
