package nl.beng.gtaa.indexer;

import java.util.LinkedList;
import java.util.List;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.ErrorResponseException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;

@Component(value = "indexer")
public class Indexer {

	private static final String INITIAL_FROM_DATE = "2012-10-01T12:00:00";
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
	private static final String DATE_ACCEPTED_ELEMENT = "dateAccepted";
	private static final Logger logger = LoggerFactory.getLogger(Indexer.class);
	private static final String INDEX_SETTINGS_PATH = "gtaa_settings.json";
	private static final String INDEX_MAPPING_PATH = "gtaa_mappings.json";

	protected static final DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd'T'HH:mm:ss").toFormatter().withZoneUTC();

	@Autowired
	private ElasticsearchTemplate template;
	@Value("${nl.beng.gtaa.oai.server.url}")
	private String oaiPmhServerUrl;
	@Value("${nl.beng.gtaa.oai.metadata.prefix}")
	private String metaDataPrefix;
	@Value("${nl.beng.gtaa.elasticsearch.index.name}")
	private String indexName;
	@Value("${nl.beng.gtaa.oai.indexer.buffer.days}")
	private int buffer;
	@Value("${nl.beng.gtaa.oai.indexer.update.period.days}")
	private int updatePeriod;
	@Value("${nl.beng.gtaa.oai.set}")
	private String gtaaSet;
	@Autowired
	private Client client;

	@Scheduled(cron = "${nl.beng.gtaa.oai.indexer.cron}")
	public void update() {
		if (!template.indexExists(indexName)) {
			template.createIndex(indexName, ElasticsearchTemplate
					.readFileFromClasspath(INDEX_SETTINGS_PATH));
			template.putMapping(indexName, GtaaDocument.DOCUMENT_NAME,
					ElasticsearchTemplate
							.readFileFromClasspath(INDEX_MAPPING_PATH));
		}
		logger.info("Start harvesting oai metadata in update mode. ");
		OaiPmhServer server = new OaiPmhServer(oaiPmhServerUrl);
		String untilDate = null;
		int bufferForThisRun = buffer;
		try {
			String fromDate = INITIAL_FROM_DATE;
			do {
				IndexStatus indexStatus = getIndexStatus();
				if (indexStatus != null
						&& !StringUtils.isBlank(indexStatus.getLastFromDate())) {
					fromDate = getLastFromDate(indexStatus, bufferForThisRun);
					// only use buffer in first iteration, after that set it to
					// 0.
					bufferForThisRun = 0;
				}
				untilDate = getUntilDate(fromDate);
				harvest(server, fromDate, untilDate);

			} while (!isAfterToday(untilDate));
		} catch (OAIException e) {
			logger.error("Error during harvesting oai metadata. ", e);
		} finally {
			logger.info("Finished harvesting oai metadata in update mode. ");
		}

	}

	private String getLastFromDate(IndexStatus indexStatus, int buffer) {
		// We extract three days from the from date that was recorded to be quit
		// sure nothing got modified with a date before the from date.
		return dateFormat.print(dateFormat.parseDateTime(
				indexStatus.getLastFromDate()).minusDays(buffer));
	}

	private boolean isAfterToday(String dateString) {
		DateTime today = new DateTime();
		return dateFormat.parseDateTime(dateString).isAfter(today);
	}

	private void harvest(OaiPmhServer server, String fromDate, String untilDate)
			throws OAIException {
		logger.info("harvesting from '" + fromDate + "' until '" + untilDate
				+ "' ");
		try {
			RecordsList recordsList = server.listRecords(metaDataPrefix,
					fromDate, untilDate, gtaaSet);
			while (recordsList.size() > 0 && recordsList != null) {
				List<GtaaDocument> gtaaDocuments = createGtaaDocuments(recordsList);
				logger.info("harvested '" + gtaaDocuments.size()
						+ "' documents.");
				logger.info("Indexing '" + gtaaDocuments.size()
						+ "' documents...");
				index(gtaaDocuments);
				logger.info("Indexed '" + gtaaDocuments.size() + "' documents.");
				logger.info("harvesting....");
				if (recordsList.getResumptionToken() == null) {
					break;
				}
				recordsList = server.listRecords(recordsList
						.getResumptionToken());
			}
			updateIndexStatus(untilDate);
		} catch (ErrorResponseException e) {
			if (ErrorResponseException.NO_RECORDS_MATCH.equalsIgnoreCase(e
					.getCode())) {
				{
					updateIndexStatus(untilDate);
				}
			} else {
				throw e;
			}
		}
	}

	private String getUntilDate(String fromDate) {
		return dateFormat.print(dateFormat.parseDateTime(fromDate).plusDays(
				updatePeriod));
	}

	private void updateIndexStatus(String untilDate) {
		IndexQuery query = new IndexQuery();
		IndexStatus status = new IndexStatus();
		if (isAfterToday(untilDate)) {
			status.setLastFromDate(dateFormat.print(new DateTime()));
		} else {
			status.setLastFromDate(untilDate);
		}
		query.setId(status.getId());
		query.setIndexName(indexName);
		query.setObject(status);
		template.index(query);
	}

	private IndexStatus getIndexStatus() {
		GetResponse response = client
				.prepareGet(indexName, IndexStatus.DOCUMENT_NAME, IndexStatus.ID).execute()
				.actionGet();
		ResultsMapper mapper = new DefaultResultMapper();
		return mapper.mapResult(response, IndexStatus.class);
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
			Element dateAcceptedElement = data.element(DATE_ACCEPTED_ELEMENT);
			if (uriAttribute != null
					&& StringUtils.isNotBlank(uriAttribute.getText())) {
				String conceptScheme = extractConceptScheme(data
						.elements(IN_SCHEME_ELEMENT));
				GtaaType type = extractGtaaType(conceptScheme);
				if (type != null && dateAcceptedElement != null) {
					gtaaDocument = new GtaaDocument();
					String uri = uriAttribute.getText();
					gtaaDocument.setType(type);
					gtaaDocument.setId(uri.replaceAll("/", "_"));
					gtaaDocument.setUri(uri);
					gtaaDocument.setAltLabel(extractTextFromElements(data
							.elements(ALT_LABEL_ELEMENT)));
					gtaaDocument.setPrefLabel(extractTextFromElements(data
							.elements(PREF_LABEL_ELEMENT)));
					if (StringUtils.isNotBlank(conceptScheme)) {
						gtaaDocument
								.setConceptSchemes(conceptScheme.split(" "));
					}
				}
			}

		}
		return gtaaDocument;
	}

	private GtaaType extractGtaaType(String conceptScheme) {
		if (StringUtils.isNotBlank(conceptScheme)) {
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
			if (StringUtils.isNotBlank(element.getText())) {
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
			indexer.update();
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}
}
