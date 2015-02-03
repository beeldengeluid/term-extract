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
public class TextRazorTest {

    @Autowired
    private TextRazorRepository textRazorRepository;

    @Test
    @Ignore("This test is actually an integration test because it will call the TextRazor service.")
    public void testExtract() throws NamedEntityExtractionException {
        List<NamedEntity> namedEntities = textRazorRepository
                .extract("Mooie goal van Ruud van Nistelrooy. 1-0 voor Manchester United. John Jones Mary and Mr. J. J. Jones ran to Washington.");
        Assert.assertThat(namedEntities, Matchers.hasSize(7));
        Assert.assertThat(namedEntities.get(0).getText(), Matchers.is("Mooie"));
        Assert.assertThat(namedEntities.get(0).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
        Assert.assertThat(namedEntities.get(1).getText(), Matchers.is("Ruud van Nistelrooy"));
        Assert.assertThat(namedEntities.get(1).getType(), Matchers.equalTo(NamedEntityType.PERSON));
        Assert.assertThat(namedEntities.get(2).getText(), Matchers.is("Manchester"));
        Assert.assertThat(namedEntities.get(2).getType(), Matchers.equalTo(NamedEntityType.LOCATION));
        Assert.assertThat(namedEntities.get(3).getText(), Matchers.is("Manchester United"));
        Assert.assertThat(namedEntities.get(3).getType(), Matchers.equalTo(NamedEntityType.ORGANIZATION));
        Assert.assertThat(namedEntities.get(4).getText(), Matchers.is("John Jones Mary"));
        Assert.assertThat(namedEntities.get(4).getType(), Matchers.equalTo(NamedEntityType.PERSON));
        Assert.assertThat(namedEntities.get(5).getText(), Matchers.is("Mr. J. J. Jones"));
        Assert.assertThat(namedEntities.get(5).getType(), Matchers.equalTo(NamedEntityType.PERSON));
        Assert.assertThat(namedEntities.get(6).getText(), Matchers.is("Washington"));
        Assert.assertThat(namedEntities.get(6).getType(), Matchers.equalTo(NamedEntityType.LOCATION));
    }

    @Test
    @Ignore("This test is actually an integration test because it will call the TextRazor service.")
    public void testExtractEmptyString() throws NamedEntityExtractionException {
        List<NamedEntity> namedEntities = textRazorRepository.extract("");
        Assert.assertTrue(namedEntities.isEmpty());
    }
}
