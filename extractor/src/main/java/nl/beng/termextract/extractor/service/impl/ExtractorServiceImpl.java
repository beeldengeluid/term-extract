package nl.beng.termextract.extractor.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

@Service
public class ExtractorServiceImpl implements ExtractorService {

	private static final String XTAS_LOCAL_REPOSITORY_NAME = "xtas-local";
	private static final String XTAS_904LABS_REPOSITORY_NAME = "xtas";
	private static final String CLTL_REPOSITORY_NAME = "cltl";
	private static final Logger logger = LoggerFactory
			.getLogger(ExtractorServiceImpl.class);

	@Autowired
	private GtaaRepository gtaaRepository;
	@Autowired
	private LabsXtasRepository labsXtasRepository;
	@Autowired
	private LocalXtasRepository localXtasRepository;
	@Autowired
	private CltlRepository cltlRepository;

	@Autowired
	private Settings defaultSettings;

	private Map<String, Integer> wordFrequencyMap;
	private CharArraySet stopwordsSet;

	@Value("${nl.beng.termextract.tokenizer.wordfrequency.file}")
	public void setWordFrequencyMap(final String wordFrequencyFileName)
			throws NumberFormatException, IOException {
		wordFrequencyMap = new HashMap<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(Thread
					.currentThread().getContextClassLoader()
					.getResourceAsStream(wordFrequencyFileName)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] words = line.split(";");
				wordFrequencyMap.put(words[0], Integer.parseInt(words[1]));
			}
		} finally {
			if (reader != null) {
				reader.close();
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
		NamedEntityRecognitionRepository namedEntityRecognitionRepository = getNamedEntityRecognitionRepository(settings);
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
			logger.error(message, e);
			throw new ExtractionException(message, e);

		}
		return response;
	}

	private NamedEntityRecognitionRepository getNamedEntityRecognitionRepository(
			Settings settings) throws ExtractionException {
		String repositoryName = settings.getNamedEntityRepository();
		switch (repositoryName) {
		case CLTL_REPOSITORY_NAME:
			return cltlRepository;
		case XTAS_LOCAL_REPOSITORY_NAME:
			return localXtasRepository;
		case XTAS_904LABS_REPOSITORY_NAME:
		    return labsXtasRepository;
		default:
			throw new ExtractionException("Unknown named entity repository '"
					+ repositoryName + "'");
		}

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
		match.setType(document.getType().toValue());
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
		logger.info("Start findMatchingTerms(namedEntities)");
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
		logger.info(frequentGtaaMatches.entrySet().size() + " matches found");
		logger.info("End findMatchingTerms(namedEntities)");
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
		logger.info("Start findMatchingTerms(tokens)");
		Multiset<GtaaDocument> gtaaMatches = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			gtaaMatches.addAll(gtaaRepository.find(token.getElement(),
					settings.getTokenizerMinScore(), GtaaType.ONDERWERPEN));
		}
		logger.info(gtaaMatches.size() + " gtaa matches");
		logger.info("End findMatchingTerms(tokens)");
		return gtaaMatches;
	}

	private Multiset<String> extractUncommonTokens(Multiset<String> tokens,
			Settings settings) {
		logger.info("Start extractUncommonTokens()");
		Multiset<String> uncommonTokens = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			Integer wordFrequency = this.wordFrequencyMap.get(token
					.getElement());
			wordFrequency = wordFrequency == null ? 1 : wordFrequency;

			double normfrequency = token.getCount() / wordFrequency;
			if (normfrequency >= settings.getTokenizerMinNormFrequency()) {
				logger.debug("Uncommon token found: '" + token.getElement()
						+ "' normfrequency '" + normfrequency + "'");
				uncommonTokens.add(token.getElement(), token.getCount());
			} else {
				logger.debug("Common token skipped: '" + token.getElement()
						+ "' normfrequency '" + normfrequency + "'");
			}
		}
		logger.info(uncommonTokens.size() + " uncommon tokens");
		logger.info("End extractUncommonTokens()");
		return uncommonTokens;
	}

	private Multiset<String> extractFrequentTokens(Multiset<String> tokens,
			Settings settings) {
		logger.info("Start extractFrequentTokens()");
		Multiset<String> frequentTokens = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			if (token.getCount() >= settings.getTokenizerMinTokenFrequency()) {
				frequentTokens.add(token.getElement(), token.getCount());
				logger.debug(MessageFormat.format(
						"frequent token found: ''{0}'' ({1})",
						token.getElement(), token.getCount()));
			}
		}
		logger.info(frequentTokens.size() + " frequent tokens");
		logger.info("End extractFrequentTokens()");
		return frequentTokens;
	}

	private Multiset<String> extractTokens(final String text, Analyzer analyzer)
			throws ExtractionException {
		logger.info("Start extractTokens()");
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
			logger.error(message, e);
			throw new ExtractionException(message, e);
		} finally {
			analyzer.close();
			logger.info(tokens.size() + " tokens");
			logger.info("End extractTokens()");
		}
		return tokens;
	}

}
