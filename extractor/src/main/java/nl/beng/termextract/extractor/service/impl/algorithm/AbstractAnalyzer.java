package nl.beng.termextract.extractor.service.impl.algorithm;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

public abstract class AbstractAnalyzer extends Analyzer {
	public static final Version LUCENE_VERSION = Version.LUCENE_48;

	public AbstractAnalyzer(ReuseStrategy reuseStrategy) {
		super(reuseStrategy);
	}

	public AbstractAnalyzer() {
		this(GLOBAL_REUSE_STRATEGY);
	}

}
