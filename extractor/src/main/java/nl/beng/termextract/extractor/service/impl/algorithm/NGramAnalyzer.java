package nl.beng.termextract.extractor.service.impl.algorithm;

import java.io.Reader;

import nl.beng.termextract.extractor.service.Settings;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

public class NGramAnalyzer extends AbstractAnalyzer {

	private Settings settings;
	private CharArraySet stopwordsSet;

	public NGramAnalyzer(CharArraySet stopwordsSet, Settings settings) {
		this.stopwordsSet = stopwordsSet;
		this.settings = settings;
	}

	@Override
	protected Reader initReader(String fieldName, Reader reader) {
		return super.initReader(fieldName, reader);
	}

	@Override
	protected TokenStreamComponents createComponents(String f, Reader reader) {
		Tokenizer source = new StandardTokenizer(LUCENE_VERSION, reader);
		ShingleFilter shingleFilter = new ShingleFilter(source,
				this.settings.getMinGram(), this.settings.getMaxGram());
		TokenStream filter = new LowerCaseFilter(LUCENE_VERSION, shingleFilter);
		filter = new StopFilter(LUCENE_VERSION, filter,
				this.stopwordsSet);
		return new TokenStreamComponents(source, filter);
	}

}
