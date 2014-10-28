package nl.beng.gtaa.indexer;

import java.util.List;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;

public class Indexer {

	private static final Logger logger = LoggerFactory.getLogger(Indexer.class);

	/*
	 * @Autowired private OaiHarvester harvester;
	 * 
	 * @Autowired private ElasticsearchTemplate template;
	 */// @Value("${nl.beng.gtaa.indexer.oai.server.url}")
	private String oaiPmhServerUrl = "http://openskos.org/oai-pmh";
	// @Value("${nl.beng.gtaa.indexer.oai.metadata.prefix}")
	private String metaDataPrefix = "oai_rdf";

	public void index() {
		logger.info("Start harvesting oai metadata. ");
		OaiPmhServer server = new OaiPmhServer(oaiPmhServerUrl);
		try {
			RecordsList recordsList = server.listRecords(metaDataPrefix);
			while (recordsList.size() > 0) {
				indexRecords(recordsList);
				recordsList = server.listRecords(recordsList
						.getResumptionToken());
			}

		} catch (OAIException e) {
			logger.error("Error during harvesting oai metadata. ", e);
		}
		finally {
			logger.info("Finished harvesting oai metadata. ");
		}
	}

	private void indexRecords(RecordsList recordsList) {
		List<Record> records = recordsList.asList();
		for (Record record : records) {
				GtaaDocument gtaaDocument = createGtaaDocument(record);
		}
		logger.info("harvested '" + recordsList.size() + "' records.");
	}
	
	private GtaaDocument createGtaaDocument(Record record) {
		if (record.getMetadata() != null) {
		}
		return null;
	}

	// this variant respods with the esdoc list without indexing it
	private ESDoc elementToESDoc(Record elt) {
		if (elt.getMetadata() != null) {
			Element data = (Element) elt.getMetadata().elements().get(0);

			String uri = data.attribute("about").getText();

			List<Element> altLabelList = data.elements("altLabel");
			String altLabel = "";
			for (int i = 0; i < altLabelList.size(); i++) {
				if (i > 0) {
					altLabel += " ";
				}
				altLabel += altLabelList.get(i).getText();
			}

			List<Element> prefLabelList = data.elements("prefLabel");
			String prefLabel = "";
			for (int i = 0; i < prefLabelList.size(); i++) {
				if (i > 0) {
					prefLabel += " ";
				}
				prefLabel += prefLabelList.get(i).getText();

			}
			List<Element> csList = data.elements("inScheme");
			String cs = "";
			for (int j = 0; j < csList.size(); j++) {
				if (j > 0) {
					cs += " ";
				}
				cs += csList.get(j).attribute("resource").getValue();
			}

			// System.out.println("found uri: " + uri + " pl "+ prefLabel +
			// " al: " + altLabel + " cs: "+ cs);
			ESDoc esd = new ESDoc();

			esd.uri = uri;
			esd.preflabel = prefLabel;
			esd.altlabel = altLabel;
			esd.conceptSchemes = cs;
			return (esd);
		} else
			return null;
	}


	public static void main(String[] args) {
		Indexer indexer = new Indexer();
		indexer.index();
	}

}
