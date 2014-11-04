package nl.beng.termextract.extractor.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;
import nl.beng.termextract.extractor.repository.gtaa.GtaaRepository;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;
import nl.beng.termextract.extractor.service.ExtractionException;
import nl.beng.termextract.extractor.service.ExtractorService;
import nl.beng.termextract.extractor.service.Match;
import nl.beng.termextract.extractor.service.Settings;
import nl.beng.termextract.extractor.service.impl.algorithm.AbstractAnalyzer;
import nl.beng.termextract.extractor.service.impl.algorithm.NGramAnalyzer;

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

	private static final Logger logger = LoggerFactory
			.getLogger(ExtractorServiceImpl.class);

	@Autowired
	private GtaaRepository gtaaRepository;
	@Autowired
	private NamedEntityRecognitionRepository namedEntityRecognitionRepository;

	@Autowired
	private Settings defaultSettings;

	private Map<String, Integer> wordFrequencyMap;
	private CharArraySet stopwordsSet;

	@Value("${nl.beng.termextract.algorithm.wordfrequency.file}")
	public void setWordFrequencyMap(final String wordFrequencyFileName)
			throws NumberFormatException, IOException {
		wordFrequencyMap = new HashMap<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(wordFrequencyFileName));
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

	@Value("${nl.beng.termextract.algorithm.stopwords}")
	public void setStopwords(final String stopwords) throws IOException {
		this.stopwordsSet = new CharArraySet(AbstractAnalyzer.LUCENE_VERSION,
				Arrays.asList(stopwords.split(",")), false);
	}

	@Override
	public Set<Match> extract(String text) throws ExtractionException {
		return doExtract(text, defaultSettings);
	}

	@Override
	public Set<Match> extract(String text, Settings settings)
			throws ExtractionException {
		if (settings != null) {
			if (settings.getMinGram() == null) {
				settings.setMinGram(defaultSettings.getMinGram());
			}
			if (settings.getMaxGram() == null) {
				settings.setMaxGram(defaultSettings.getMaxGram());
			}
			if (settings.getMinTokenFrequency() == null) {
				settings.setMinTokenFrequency(defaultSettings
						.getMinTokenFrequency());
			}
			if (settings.getMinNormalizedFrequency() == null) {
				settings.setMinNormalizedFrequency(defaultSettings
						.getMinNormalizedFrequency());
			}
			return doExtract(text, settings);
		} else {
			return doExtract(text, defaultSettings);
		}
	}

	private Set<Match> doExtract(String text, Settings settings)
			throws ExtractionException {
		Set<Match> matches = new HashSet<>();
		Multiset<GtaaDocument> gtaaMatches = HashMultiset.create();
		Multiset<String> tokens = null;
		List<NamedEntity> namedEntities = null;
		try {
			tokens = extractTokens(text, new NGramAnalyzer(this.stopwordsSet,
					settings));
			tokens = extractFrequentTokens(tokens, settings);
			tokens = extractUncommonTokens(tokens, settings);
			gtaaMatches.addAll(findGtaaMatches(tokens));
			namedEntities = namedEntityRecognitionRepository.extract(text);
			gtaaMatches.addAll(findMatchingTerms(namedEntities, settings));
			for (Entry<GtaaDocument> gtaaMatch : gtaaMatches.entrySet()) {
				matches.add(createMatch(gtaaMatch.getElement()));
			}
		} catch (NamedEntityExtractionException e) {
			String message = "Could not extract entities from named entity recognition repository.";
			logger.error(message, e);
			throw new ExtractionException(message, e);

		}
		return matches;
	}

	private Match createMatch(GtaaDocument document) {
		Match match = new Match();
		match.setType(document.getType().toValue());
		match.setUri(document.getUri());
		match.setPrefLabel(document.getPrefLabel());
		match.setAltLabel(document.getAltLabel());
		if (document.getConceptScheme() != null) {
			match.setConceptSchemes(document.getConceptScheme().split(" "));
		}
		return match;
	}

	private Multiset<GtaaDocument> findMatchingTerms(
			List<NamedEntity> namedEntities, Settings settings) {
		logger.info("Start findMatchingTerms(namedEntities)");
		Multiset<GtaaDocument> frequentGtaaMatches = HashMultiset.create();
		Multiset<GtaaDocument> gtaaMatches = HashMultiset.create();
		for (NamedEntity namedEntity : namedEntities) {
			switch (namedEntity.getType()) {
			case PERSON:
				gtaaMatches.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.PERSOONSNAMEN));
				break;
			case LOCATION:
				gtaaMatches.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.GEOGRAFISCHENAMEN));
				break;
			case ORGANIZATION:
				gtaaMatches.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.NAMEN));
				break;
			case MISC:
				gtaaMatches.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.PERSOONSNAMEN, GtaaType.ONDERWERPEN));
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
			if (match.getCount() >= settings.getMinTokenFrequency()) {
				frequentGtaaMatches.add(match.getElement(), match.getCount());
			}
		}
		return frequentGtaaMatches;
	}

	private Multiset<GtaaDocument> findGtaaMatches(Multiset<String> tokens) {
		logger.info("Start findMatchingTerms(tokens)");
		Multiset<GtaaDocument> gtaaMatches = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			gtaaMatches.addAll(gtaaRepository.find(token.getElement(),
					GtaaType.ONDERWERPEN));
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
			if (normfrequency < settings.getMinNormalizedFrequency()) {
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
			if (token.getCount() >= settings.getMinTokenFrequency()) {
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
