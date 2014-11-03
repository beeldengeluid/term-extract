package nl.beng.termextract.extractor.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.gtaa.model.GtaaType;
import nl.beng.termextract.extractor.repository.gtaa.GtaaRepository;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityRecognitionRepository;
import nl.beng.termextract.extractor.service.ExtractionException;
import nl.beng.termextract.extractor.service.ExtractorService;
import nl.beng.termextract.extractor.service.Settings;
import nl.beng.termextract.extractor.service.Term;
import nl.beng.termextract.extractor.service.impl.algorithm.NGramAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Override
	public List<Set<Term>> extract(List<String> texts, Settings settings)
			throws ExtractionException {
		List<Set<Term>> extractedTermsList = new LinkedList<>();
		for (String text : texts) {
			Set<Term> terms = extractTerms(text, settings);

			extractedTermsList.add(terms);
		}
		return extractedTermsList;
	}

	private Set<Term> extractTerms(String text, Settings settings)
			throws ExtractionException {
		Set<Term> terms = new HashSet<>();
		Multiset<String> tokens = null;
		List<NamedEntity> namedEntities = null;
		tokens = extractTokens(text, new NGramAnalyzer(settings));
		tokens = extractFrequentTokens(tokens, settings);
		tokens = extractUncommonTokens(tokens, settings);
		terms.addAll(findMatchingTerms(tokens));
		try {
			namedEntities = namedEntityRecognitionRepository.extract(text);
		} catch (NamedEntityExtractionException e) {
			String message = "Could not extract entities from named entity recognition repository.";
			logger.error(message, e);
			throw new ExtractionException(message, e);
		}
		terms.addAll(findMatchingTerms(namedEntities));
		return terms;
	}

	private Set<Term> findMatchingTerms(List<NamedEntity> namedEntities) {
		logger.info("Start findMatchingTerms(namedEntities)");
		Set<Term> terms = new HashSet<>();
		Multiset<GtaaDocument> gtaaDocuments = HashMultiset.create();
		for (NamedEntity namedEntity : namedEntities) {
			switch (namedEntity.getType()) {
			case PERSON:
				gtaaDocuments.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.PERSOONSNAMEN));
				break;
			case LOCATION:
				gtaaDocuments.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.GEOGRAFISCHENAMEN));
				break;
			case ORGANIZATION:
				gtaaDocuments.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.NAMEN));
				break;
			case MISC:
				gtaaDocuments.addAll(gtaaRepository.find(namedEntity.getText(),
						GtaaType.PERSOONSNAMEN, GtaaType.ONDERWERPEN));
				break;
			default:
				;
			}
			for (GtaaDocument gtaaDocument : gtaaDocuments.elementSet()) {
				Term term = new Term();
//				term.setFrequency(gtaaDocuments.count(gtaaDocument));
				term.addGtaaMatch(gtaaDocument);
				terms.add(term);
			}
		}
		logger.info(terms.size() + " terms extracted");
		logger.info("End findMatchingTerms(namedEntities)");
		return terms;
	}

	private Set<Term> findMatchingTerms(Multiset<String> tokens) {
		logger.info("Start findMatchingTerms(tokens)");
		Set<Term> terms = new HashSet<>();
		for (Entry<String> token : tokens.entrySet()) {
			List<GtaaDocument> gtaaDocuments = gtaaRepository.find(
					token.getElement(), GtaaType.ONDERWERPEN);
			if (gtaaDocuments.size() > 0) {
				logger.debug("found GTAADocument for term '"
						+ token.getElement() + "' " + gtaaDocuments);
				Term term = new Term();
///				term.setFrequency(token.getCount());
				term.setGtaaMatches(new HashSet<>(gtaaDocuments));
				terms.add(term);
			}
		}
		logger.info(terms.size() + " tokens matched");
		logger.info("End findMatchingTerms(tokens)");
		return terms;
	}

	private Multiset<String> extractUncommonTokens(Multiset<String> tokens,
			Settings settings) {
		logger.info("Start extractUncommonTokens()");
		Multiset<String> uncommonTokens = HashMultiset.create();
		for (Entry<String> token : tokens.entrySet()) {
			Integer wordFrequency = settings.getWordFrequencyMap().get(
					token.getElement());
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
