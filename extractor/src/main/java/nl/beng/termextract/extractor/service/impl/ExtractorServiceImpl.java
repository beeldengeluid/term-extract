package nl.beng.termextract.extractor.service.impl;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Integer.parseInt;
import static org.apache.commons.io.IOUtils.readLines;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;
import nl.beng.termextract.extractor.repository.gtaa.GtaaRepository;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityType;
import nl.beng.termextract.extractor.repository.namedentity.impl.CltlRepository;
import nl.beng.termextract.extractor.repository.namedentity.impl.LabsXtasRepository;
import nl.beng.termextract.extractor.repository.namedentity.impl.LocalXtasRepository;
import nl.beng.termextract.extractor.repository.namedentity.impl.TextRazorRepository;
import nl.beng.termextract.extractor.service.ExtractionException;
import nl.beng.termextract.extractor.service.ExtractorService;
import nl.beng.termextract.extractor.service.VersionProvider;
import nl.beng.termextract.extractor.service.impl.algorithm.AbstractAnalyzer;
import nl.beng.termextract.extractor.service.impl.algorithm.NGramAnalyzer;
import nl.beng.termextract.extractor.service.model.ExtractResponse;
import nl.beng.termextract.extractor.service.model.Match;
import nl.beng.termextract.extractor.service.model.Settings;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

@Service
public class ExtractorServiceImpl implements ExtractorService, ApplicationContextAware {

    public static final String XTAS_LOCAL_REPOSITORY_NAME = "xtas-local";
    public static final String XTAS_904LABS_REPOSITORY_NAME = "xtas";
    public static final String CLTL_REPOSITORY_NAME = "cltl";
    public static final String TEXTRAZOR_REPOSITORY_NAME = "textrazor";
    private static final Logger LOG = getLogger(ExtractorServiceImpl.class);

	private GtaaRepository gtaaRepository;
    private Settings defaultSettings;
    private Map<String, NamedEntityRecognitionRepository> namedEntityRepositories = newHashMap();

    private Map<String, Integer> wordFrequencyMap = newHashMap();
	private CharArraySet stopwordsSet;
	
	private static final Map<GtaaType, String> gtaaOutput = new HashMap<>();
	static {
	    gtaaOutput.put(GtaaType.GEOGRAFISCHENAMEN, "GTAA_geografischenamen");
	    gtaaOutput.put(GtaaType.NAMEN, "GTAA_namen");
	    gtaaOutput.put(GtaaType.ONDERWERPEN, "GTAA_onderwerpen");
	    gtaaOutput.put(GtaaType.PERSOONSNAMEN, "GTAA_personen");
	}

    @Autowired
    public ExtractorServiceImpl(GtaaRepository gtaaRepository, Settings defaultSettings) {
        this.gtaaRepository = gtaaRepository;
        this.defaultSettings = defaultSettings;
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        namedEntityRepositories.put(XTAS_LOCAL_REPOSITORY_NAME, appContext.getBean(LocalXtasRepository.class));
        namedEntityRepositories.put(XTAS_904LABS_REPOSITORY_NAME, appContext.getBean(LabsXtasRepository.class));
        namedEntityRepositories.put(CLTL_REPOSITORY_NAME, appContext.getBean(CltlRepository.class));
        namedEntityRepositories.put(TEXTRAZOR_REPOSITORY_NAME, appContext.getBean(TextRazorRepository.class));
    }

	@Value("${nl.beng.termextract.tokenizer.wordfrequency.file}")
    public void setWordFrequencyMap(Resource wordFrequencies) throws NumberFormatException, IOException {
        try (InputStream is = wordFrequencies.getInputStream()) {
            for (String line : readLines(is, "UTF-8")) {
                String[] words = line.split(";");
                wordFrequencyMap.put(words[0], parseInt(words[1]));
            }
		}
	}

	@Value("${nl.beng.termextract.tokenizer.stopwords}")
	public void setStopwords(final String stopwords) throws IOException {
		this.stopwordsSet = new CharArraySet(AbstractAnalyzer.LUCENE_VERSION,
				Arrays.asList(stopwords.split(",")), false);
	}

	@Override
	public ExtractResponse extract(String text) throws ExtractionException {
		return doExtract(text, this.defaultSettings);
	}

	@Override
	public ExtractResponse extract(String text, Settings settings)
			throws ExtractionException {
		if (settings != null) {
			return doExtract(text, mergeSettings(settings, defaultSettings));
		} else {
			return doExtract(text, defaultSettings);
		}
	}

	private Settings mergeSettings(Settings overrideSettings,
			Settings defaultSettings) {
		Settings mergedSettings = new Settings();
		Properties mergedProperties = new Properties();
		mergedProperties.putAll(defaultSettings.getProperties());
		mergedProperties.putAll(overrideSettings.getProperties());
		mergedSettings.setProperties(mergedProperties);
		return mergedSettings;
	}

	private ExtractResponse doExtract(String text, Settings settings)
			throws ExtractionException {
        NamedEntityRecognitionRepository namedEntityRecognitionRepository = getNamedEntityRecognitionRepository(settings.getNamedEntityRepository());
		ExtractResponse response = new ExtractResponse();
		List<Match> matches = new LinkedList<>();
		Multiset<GtaaDocument> gtaaMatches = HashMultiset.create();
		Multiset<String> tokens = null;
		List<NamedEntity> namedEntities = null;
		try {
			tokens = extractTokens(text, new NGramAnalyzer(this.stopwordsSet,
					settings));
			tokens = extractFrequentTokens(tokens, settings);
			tokens = extractUncommonTokens(tokens, settings);
			gtaaMatches.addAll(findGtaaMatches(tokens, settings));
			namedEntities = namedEntityRecognitionRepository.extract(text);
			gtaaMatches.addAll(findMatchingTerms(namedEntities, settings));
			for (Entry<GtaaDocument> gtaaMatch : gtaaMatches.entrySet()) {
				matches.add(createMatch(gtaaMatch.getElement()));
			}
			sortMatches(matches);
			response.setMatches(matches);
			response.setVersion(VersionProvider.getVersion());
			response.setSettings(settings.getProperties());
			response.setTotal(matches.size());
		} catch (NamedEntityExtractionException e) {
			String message = "Could not extract entities from named entity recognition repository.";
            LOG.error(message, e);
			throw new ExtractionException(message, e);

		}
		return response;
	}

    private NamedEntityRecognitionRepository getNamedEntityRecognitionRepository(String repoName) {
        NamedEntityRecognitionRepository repository = namedEntityRepositories.get(repoName);
        if (repository == null) {
            throw new IllegalArgumentException("No NamedEntityRecognitionRepository found for name: [" + repoName + "]");
		}
        return repository;
	}

	private void sortMatches(List<Match> matches) {
		Collections.sort(matches, new Comparator<Match>() {

			@Override
			public int compare(Match match1, Match match2) {
				return match2.getScore().compareTo(match1.getScore());
			}
		});

	}

	private Match createMatch(GtaaDocument document) {
		Match match = new Match();
		match.setType(gtaaOutput.get(document.getType()));
		match.setUri(document.getUri());
		match.setPrefLabel(document.getPrefLabel());
		match.setAltLabel(document.getAltLabel());
		match.setScore(document.getScore());
		if (document.getConceptSchemes() != null) {
			match.setConceptSchemes(document.getConceptSchemes());
		}
		return match;
	}

	private Multiset<GtaaDocument> findMatchingTerms(
			List<NamedEntity> namedEntities, Settings settings) {
        LOG.info("Start findMatchingTerms(namedEntities)");
		Multiset<GtaaDocument> frequentGtaaMatches = HashMultiset.create();
		Multiset<GtaaDocument> gtaaMatches = HashMultiset.create();
		for (NamedEntity namedEntity : namedEntities) {
			Float minScore = settings.getNamedEntityMinScore(namedEntity
					.getType());
			switch (namedEntity.getType()) {
			case PERSON:
				gtaaMatches.addAll(gtaaRepository.find(namedEntity.getText(),
						minScore, GtaaType.PERSOONSNAMEN));
				break;
			case LOCATION:
				gtaaMatches.addAll(gtaaRepository.find(namedEntity.getText(),
						minScore, GtaaType.GEOGRAFISCHENAMEN));
				break;
			case ORGANIZATION:
				gtaaMatches.addAll(gtaaRepository.find(namedEntity.getText(),
						minScore, GtaaType.NAMEN));
				break;
			case MISC:
				gtaaMatches
						.addAll(gtaaRepository.find(namedEntity.getText(),
								minScore, GtaaType.PERSOONSNAMEN,
								GtaaType.ONDERWERPEN));
				break;
			default:
				;
			}
		}
		frequentGtaaMatches = extractFrequentMatches(gtaaMatches, settings);
        LOG.info(frequentGtaaMatches.entrySet().size() + " matches found");
        LOG.info("End findMatchingTerms(namedEntities)");
		return frequentGtaaMatches;
	}

	private Multiset<GtaaDocument> extractFrequentMatches(
			Multiset<GtaaDocument> gtaaMatches, Settings settings) {
		Multiset<GtaaDocument> frequentGtaaMatches = HashMultiset.create();
		for (Entry<GtaaDocument> match : gtaaMatches.entrySet()) {
			if (match.getCount() >= settings
					.getNamedEntityMinTokenFrequency(getNamedEntityType(match
							.getElement()))) {
				frequentGtaaMatches.add(match.getElement(), match.getCount());
			}
		}
		return frequentGtaaMatches;
	}

	private NamedEntityType getNamedEntityType(GtaaDocument document) {
		switch (document.getType()) {
		case PERSOONSNAMEN:
			return NamedEntityType.PERSON;
		case GEOGRAFISCHENAMEN:
			return NamedEntityType.LOCATION;
		case NAMEN:
			return NamedEntityType.ORGANIZATION;
		default:
			return NamedEntityType.MISC;
		}
	}

	private Multiset<GtaaDocument> findGtaaMatches(Multiset<String> tokens,
			Settings settings) {
        LOG.info("Start findMatchingTerms(tokens)");
		Multiset<GtaaDocument> gtaaMatches = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			gtaaMatches.addAll(gtaaRepository.find(token.getElement(),
					settings.getTokenizerMinScore(), GtaaType.ONDERWERPEN));
		}
        LOG.info(gtaaMatches.size() + " gtaa matches");
        LOG.info("End findMatchingTerms(tokens)");
		return gtaaMatches;
	}

	private Multiset<String> extractUncommonTokens(Multiset<String> tokens,
			Settings settings) {
        LOG.info("Start extractUncommonTokens()");
		Multiset<String> uncommonTokens = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			Integer wordFrequency = this.wordFrequencyMap.get(token
					.getElement());
			wordFrequency = wordFrequency == null ? 1 : wordFrequency;

			double normfrequency = token.getCount() / wordFrequency;
			if (normfrequency >= settings.getTokenizerMinNormFrequency()) {
                LOG.debug("Uncommon token found: '" + token.getElement()
						+ "' normfrequency '" + normfrequency + "'");
				uncommonTokens.add(token.getElement(), token.getCount());
			} else {
                LOG.debug("Common token skipped: '" + token.getElement()
						+ "' normfrequency '" + normfrequency + "'");
			}
		}
        LOG.info(uncommonTokens.size() + " uncommon tokens");
        LOG.info("End extractUncommonTokens()");
		return uncommonTokens;
	}

	private Multiset<String> extractFrequentTokens(Multiset<String> tokens,
			Settings settings) {
        LOG.info("Start extractFrequentTokens()");
		Multiset<String> frequentTokens = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			if (token.getCount() >= settings.getTokenizerMinTokenFrequency()) {
				frequentTokens.add(token.getElement(), token.getCount());
                LOG.debug(MessageFormat.format(
						"frequent token found: ''{0}'' ({1})",
						token.getElement(), token.getCount()));
			}
		}
        LOG.info(frequentTokens.size() + " frequent tokens");
        LOG.info("End extractFrequentTokens()");
		return frequentTokens;
	}

	private Multiset<String> extractTokens(final String text, Analyzer analyzer)
			throws ExtractionException {
        LOG.info("Start extractTokens()");
		Multiset<String> tokens = HashMultiset.create();
		TokenStream tokenStream = null;
		try {
			tokenStream = analyzer.tokenStream(null, text);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				CharTermAttribute attribute = tokenStream
						.getAttribute(CharTermAttribute.class);
				if (attribute != null) {
					String token = attribute.toString();
					tokens.add(token);
				}
			}
		} catch (IOException e) {
			String message = "Terms could not be extracted.";
            LOG.error(message, e);
			throw new ExtractionException(message, e);
		} finally {
			analyzer.close();
            LOG.info(tokens.size() + " tokens");
            LOG.info("End extractTokens()");
		}
		return tokens;
	}
}
