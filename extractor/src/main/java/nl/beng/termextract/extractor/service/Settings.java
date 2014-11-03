package nl.beng.termextract.extractor.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nl.beng.termextract.extractor.service.impl.algorithm.AbstractAnalyzer;

import org.apache.lucene.analysis.util.CharArraySet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Settings {

	@Value("${nl.beng.termextract.algorithm.min.gram}")
	private int minGram;
	@Value("${nl.beng.termextract.algorithm.max.gram}")
	private int maxGram;
	@Value("${nl.beng.termextract.algorithm.min.token.frequency}")
	private int minTokenFrequency;
	@Value("${nl.beng.termextract.algorithm.norm.frequency}")
	private double minNormalizedFrequency;

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

	public int getMinGram() {
		return minGram;
	}

	public void setMinGram(int minGram) {
		this.minGram = minGram;
	}

	public int getMaxGram() {
		return maxGram;
	}

	public void setMaxGram(int maxGram) {
		this.maxGram = maxGram;
	}

	public CharArraySet getStopwordsSet() {
		return stopwordsSet;
	}

	public int getMinTokenFrequency() {
		return minTokenFrequency;
	}

	public void setMinTokenFrequency(int minTokenFrequency) {
		this.minTokenFrequency = minTokenFrequency;
	}

	public double getMinNormalizedFrequency() {
		return minNormalizedFrequency;
	}

	public void setMinNormalizedFrequency(double minNormalizedFrequency) {
		this.minNormalizedFrequency = minNormalizedFrequency;
	}

	public Map<String, Integer> getWordFrequencyMap() {
		return wordFrequencyMap;
	}

}
