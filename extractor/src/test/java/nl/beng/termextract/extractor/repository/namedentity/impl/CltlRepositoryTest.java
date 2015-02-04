package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityType;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-app.xml", "classpath:service-settings.xml" })
public class CltlRepositoryTest {

	@Autowired
	private CltlRepository cltlRepository;

	@Test
	@Ignore
	public void testExtract() {
		try {
			List<NamedEntity> namedEntities = cltlRepository.extract("Mooie goal van Ruud van Nistelrooy. 1-0 voor Manchester United. John Jones Mary and Mr. J. J. Jones ran to Washington.");
			Assert.assertThat(namedEntities, Matchers.hasSize(6));
			Assert.assertThat(namedEntities.get(0).getText(), Matchers.is("Ruud van Nistelrooy"));
			Assert.assertThat(namedEntities.get(0).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(1).getText(), Matchers.is("Manchester United"));
			Assert.assertThat(namedEntities.get(1).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
			Assert.assertThat(namedEntities.get(2).getText(), Matchers.is("John Jones Mary"));
			Assert.assertThat(namedEntities.get(2).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(3).getText(), Matchers.is("Mr"));
			Assert.assertThat(namedEntities.get(3).getType(), Matchers.equalTo(NamedEntityType.MISC));
			Assert.assertThat(namedEntities.get(4).getText(), Matchers.is("J. Jones"));
			Assert.assertThat(namedEntities.get(4).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(5).getText(), Matchers.is("Washington"));
			Assert.assertThat(namedEntities.get(5).getType(), Matchers.equalTo(NamedEntityType.LOCATION));
		} catch (NamedEntityExtractionException e) {
		    e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		
	}
}
