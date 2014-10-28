package nl.beng.termextract.extractor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.beng.termextract.extractor.model.Term;
import nl.beng.termextract.extractor.service.ExtractorService;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:searchengine-test-app.xml",
"classpath:mediamanagement-app.xml" })
public class ExtractorServiceTest {

	@Autowired
	ExtractorService extractor;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testExtract() {
		List<String> texts = new ArrayList<>();
		texts.add("De kat krabt de krullen van de trap.");
		texts.add("De Wereld Draait Door met Mathijs van Nieuwkerk.");
		List<Set<Term>> extractedTermsList = extractor.extract(texts);
		Assert.assertNotNull(extractedTermsList);
		Assert.assertThat(extractedTermsList, Matchers.hasSize(2));
		Assert.assertNotNull(extractedTermsList.get(0));
		Assert.assertThat(extractedTermsList.get(0), Matchers.hasSize(2));
		Assert.assertNotNull(extractedTermsList.get(1));
		Assert.assertThat(extractedTermsList.get(1), Matchers.hasSize(3));
	}

}
