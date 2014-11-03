package nl.beng.termextract.extractor.service.impl.algorithm;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import nl.beng.termextract.extractor.service.Settings;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class NGramAnalyzerTest {

	@Autowired
	private Settings settings;

	@Test
	public void test() {
		String testableText = "Hello: World, Java-code example hello hello hello at at";
		String[] expectedTokens = new String[] { "hello world", "world java",
				"java code", "code example", "example hello", "hello hello",
				"hello hello" };
		NGramAnalyzer tokenizer = new NGramAnalyzer(settings);
		TokenStream tokenStream;
		try {
			tokenStream = tokenizer.tokenStream("dummy", new StringReader(
					testableText));
			tokenStream.reset();
			int count = 0;
			while (tokenStream.incrementToken()) {
				String text = tokenStream.getAttribute(CharTermAttribute.class)
						.toString();
				Assert.assertTrue(Arrays.asList(expectedTokens).contains(text));
				count++;
			}
			Assert.assertThat(count, Matchers.is(expectedTokens.length));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			tokenizer.close();
		}
	}
}
