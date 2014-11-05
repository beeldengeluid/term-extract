package nl.beng.termextract.extractor.repository.gtaa.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;
import nl.beng.termextract.extractor.repository.gtaa.GtaaRepository;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class GtaaElasticsearchRepository implements GtaaRepository {

	private static final String ALT_LABEL_FIELDNAME = "altLabel";

	private static final String PREF_LABEL_FIELDNAME = "prefLabel";

	@Autowired
	private ElasticsearchTemplate template;

	@Override
	public List<GtaaDocument> find(String token, Float minScore,
			GtaaType... types) {

		BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
		List<FilterBuilder> filters = new LinkedList<>();
		OrFilterBuilder orFilter = null;

		for (GtaaType type : types) {
			filters.add(FilterBuilders.termFilter("type", type.toValue()));

		}
		orFilter = FilterBuilders.orFilter(filters
				.toArray(new FilterBuilder[] {}));
		boolFilter.must(orFilter);
		final NativeSearchQuery query = new NativeSearchQueryBuilder()
				.withQuery(
						QueryBuilders.filteredQuery(
								QueryBuilders.queryString(token)
										.field(PREF_LABEL_FIELDNAME, 10)
										.field(ALT_LABEL_FIELDNAME, 2),
								boolFilter)).withMinScore(minScore).build();

		// We do some custom mapping since we want to store the score in the
		// GtaaDocument
		return template.query(query,
				new ResultsExtractor<List<GtaaDocument>>() {

					@Override
					public List<GtaaDocument> extract(
							org.elasticsearch.action.search.SearchResponse response) {
						List<GtaaDocument> matches = new LinkedList<GtaaDocument>();
						ResultsMapper mapper = new DefaultResultMapper(
								new MappingElasticsearchConverter(
										new SimpleElasticsearchMappingContext())
										.getMappingContext());

						FacetedPage<GtaaDocument> page = mapper.mapResults(
								response, GtaaDocument.class,
								query.getPageable());
						matches = page.getContent();
						mapScore(matches, response);
						return matches;
					}

					private void mapScore(List<GtaaDocument> matches,
							SearchResponse response) {
						Map<String, Float> idScoreMap = new HashMap<>();
						if (response.getHits() != null) {
							for (SearchHit searchHit : response.getHits()
									.getHits()) {
								idScoreMap.put(searchHit.getId(),
										searchHit.getScore());
							}
						}
						for (GtaaDocument document : matches) {
							Float score = idScoreMap.get(document.getId());
							if (score != null) {
								document.setScore(score);
							}
						}

					}
				});

	}
}
