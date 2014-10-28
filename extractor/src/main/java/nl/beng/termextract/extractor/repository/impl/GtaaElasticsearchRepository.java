package nl.beng.termextract.extractor.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GtaaElasticsearchRepository {

	@Autowired
	private ElasticsearchTemplate template;

}
