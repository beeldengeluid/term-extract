package nl.beng.gtaa.indexer;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(type = IndexStatus.DOCUMENT_NAME, indexName = "gtaa")
public class IndexStatus {

	public static final String DOCUMENT_NAME = "index_status";

	public static final String ID = "1";

	private String lastFromDate;

	@Id
	private String id = ID;

	public String getLastFromDate() {
		return lastFromDate;
	}

	public void setLastFromDate(String lastFromDate) {
		this.lastFromDate = lastFromDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
