package nl.beng.termextract.extractor.repository.gtaa.impl;

import java.util.LinkedList;
import java.util.List;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;
import nl.beng.termextract.extractor.repository.gtaa.GtaaRepository;

import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class GtaaElasticsearchRepository implements GtaaRepository {

	private static final String ALT_LABEL_FIELDNAME = "altLabel";

	private static final String PREF_LABEL_FIELDNAME = "prefLabel";

	@Autowired
	private ElasticsearchTemplate template;

	@Value("${nl.beng.termextract.algorithm.gtaa.match.min.score}")
	private float minScore;

	@Override
	public List<GtaaDocument> find(String token, GtaaType... types) {
		NativeSearchQueryBuilder query = null;
		BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
		List<FilterBuilder> filters = new LinkedList<>();
		OrFilterBuilder orFilter = null;

		for (GtaaType type : types) {
			filters.add(FilterBuilders.termFilter("type", type.toValue()));

		}
		orFilter = FilterBuilders.orFilter(filters
				.toArray(new FilterBuilder[] {}));
		boolFilter.must(orFilter);
		query = new NativeSearchQueryBuilder().withQuery(
				QueryBuilders.filteredQuery(
						QueryBuilders.queryString(token)
								.field(PREF_LABEL_FIELDNAME, 10)
								.field(ALT_LABEL_FIELDNAME, 2), boolFilter))
				.withMinScore(minScore);
		return template.queryForList(query.build(), GtaaDocument.class);
	}
}
