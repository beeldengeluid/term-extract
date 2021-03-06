package nl.beng.termextract.extractor.repository.namedentity.impl;

import java.util.List;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntity;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.repository.namedentity.NamedEntityType;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-app.xml", "classpath:service-settings.xml" })
public class XtasRepositoryTest {

	@Autowired
	private LocalXtasRepository localXtasRepository;
	@Autowired
	private LabsXtasRepository labsXtasRepository;

	@Test
	public void testLocalXtasExtract() {
		try {
			List<NamedEntity> namedEntities = localXtasRepository.extract("Mooie goal van Ruud van Nistelrooy. 1-0 voor Manchester United. John Jones Mary and Mr. J. J. Jones ran to Washington.");
			Assert.assertThat(namedEntities, Matchers.hasSize(9));
			Assert.assertThat(namedEntities.get(0).getText(), Matchers.is("Ruud"));
			Assert.assertThat(namedEntities.get(0).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(1).getText(), Matchers.is("Nistelrooy"));
			Assert.assertThat(namedEntities.get(1).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(2).getText(), Matchers.is("Manchester"));
			Assert.assertThat(namedEntities.get(2).getType(), Matchers.equalTo(NamedEntityType.LOCATION));
			Assert.assertThat(namedEntities.get(3).getText(), Matchers.is("United"));
			Assert.assertThat(namedEntities.get(3).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
			Assert.assertThat(namedEntities.get(4).getText(), Matchers.is("John Jones"));
			Assert.assertThat(namedEntities.get(4).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(5).getText(), Matchers.is("Mary"));
			Assert.assertThat(namedEntities.get(5).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(6).getText(), Matchers.is("and"));
			Assert.assertThat(namedEntities.get(6).getType(), Matchers.equalTo(NamedEntityType.MISC));
			Assert.assertThat(namedEntities.get(7).getText(), Matchers.is("J. J. Jones"));
			Assert.assertThat(namedEntities.get(7).getType(), Matchers.equalTo(NamedEntityType.PERSON));
			Assert.assertThat(namedEntities.get(8).getText(), Matchers.is("Washington"));
			Assert.assertThat(namedEntities.get(8).getType(), Matchers.equalTo(NamedEntityType.LOCATION));			
		} catch (NamedEntityExtractionException e) {
			Assert.fail();
		}
		
	}
	
	@Test
	public void testLabsXtasExtract() {
        try {
            List<NamedEntity> namedEntities = localXtasRepository.extract("Mooie goal van Ruud van Nistelrooy. 1-0 voor Manchester United. John Jones Mary and Mr. J. J. Jones ran to Washington.");
            Assert.assertThat(namedEntities, Matchers.hasSize(9));
            Assert.assertThat(namedEntities.get(0).getText(), Matchers.is("Ruud"));
            Assert.assertThat(namedEntities.get(0).getType(), Matchers.equalTo(NamedEntityType.PERSON));
            Assert.assertThat(namedEntities.get(1).getText(), Matchers.is("Nistelrooy"));
            Assert.assertThat(namedEntities.get(1).getType(), Matchers.equalTo(NamedEntityType.PERSON));
            Assert.assertThat(namedEntities.get(2).getText(), Matchers.is("Manchester"));
            Assert.assertThat(namedEntities.get(2).getType(), Matchers.equalTo(NamedEntityType.LOCATION));
            Assert.assertThat(namedEntities.get(3).getText(), Matchers.is("United"));
            Assert.assertThat(namedEntities.get(3).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
            Assert.assertThat(namedEntities.get(4).getText(), Matchers.is("John Jones"));
            Assert.assertThat(namedEntities.get(4).getType(), Matchers.equalTo(NamedEntityType.PERSON));
            Assert.assertThat(namedEntities.get(5).getText(), Matchers.is("Mary"));
            Assert.assertThat(namedEntities.get(5).getType(), Matchers.equalTo(NamedEntityType.PERSON));
            Assert.assertThat(namedEntities.get(6).getText(), Matchers.is("and"));
            Assert.assertThat(namedEntities.get(6).getType(), Matchers.equalTo(NamedEntityType.MISC));
            Assert.assertThat(namedEntities.get(7).getText(), Matchers.is("J. J. Jones"));
            Assert.assertThat(namedEntities.get(7).getType(), Matchers.equalTo(NamedEntityType.PERSON));
            Assert.assertThat(namedEntities.get(8).getText(), Matchers.is("Washington"));
            Assert.assertThat(namedEntities.get(8).getType(), Matchers.equalTo(NamedEntityType.LOCATION));
        } catch (NamedEntityExtractionException e) {
            Assert.fail();
        }
    }
	
	@Test
	public void testITagsFirstExtract() {
	    try {
            List<NamedEntity> namedEntities = localXtasRepository.extract("Stichting Lekker Dier");
            Assert.assertThat(namedEntities, Matchers.hasSize(3));
            Assert.assertThat(namedEntities.get(0).getText(), Matchers.is("Stichting"));
            Assert.assertThat(namedEntities.get(0).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
            Assert.assertThat(namedEntities.get(1).getText(), Matchers.is("Lekker Dier"));
            Assert.assertThat(namedEntities.get(1).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
            Assert.assertThat(namedEntities.get(2).getText(), Matchers.is("Stichting Lekker Dier"));
            Assert.assertThat(namedEntities.get(2).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
        } catch (NamedEntityExtractionException e) {
            Assert.fail();
        }
	}
}
