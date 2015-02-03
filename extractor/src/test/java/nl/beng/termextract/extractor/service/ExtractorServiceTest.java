package nl.beng.termextract.extractor.service;

import static java.lang.Integer.valueOf;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static nl.beng.gtaa.model.GtaaType.PERSOONSNAMEN;
import static nl.beng.termextract.extractor.repository.namedentity.NamedEntityType.PERSON;
import static nl.beng.termextract.extractor.service.impl.ExtractorServiceImpl.TEXTRAZOR_REPOSITORY_NAME;
import static nl.beng.termextract.extractor.service.model.Settings.NAMEDENTITY_MIN_TOKEN_FREQUENCY;
import static nl.beng.termextract.extractor.service.model.Settings.NAMEDENTITY_REPOSITORY;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Arrays;

import nl.beng.gtaa.model.GtaaDocument;
import nl.beng.termextract.extractor.repository.gtaa.GtaaRepository;
import nl.beng.termextract.extractor.service.model.ExtractResponse;
import nl.beng.termextract.extractor.service.model.Settings;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Response;

/**
 * Test with mocked beans for elasticsearch- and external service beans.
 * @author Bas de Vos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-app.xml", "classpath:service-settings.xml", "classpath:mocked-beans.xml" })
public class ExtractorServiceTest {

    @Autowired
    private ExtractorService service;
    @Autowired
    private Settings defaultSettings;
    @Autowired
    private TextRazor textRazorClient;
    @Autowired
    private GtaaRepository gtaaRepository;

    @Test
    public void testExtractWithTextRazor() throws ExtractionException, NetworkException, AnalysisException {
        // testdata that textRazorClient mock will return
        AnalyzedText analyzedText = new AnalyzedText();
        Response response = new Response();
        Entity entity = new Entity();
        setField(entity, "type", asList("Athlete", "Person"));
        setField(entity, "matchedText", "Frank de Boer");
        setField(response, "entities", asList(entity));
        setField(analyzedText, "response", response);
        when(textRazorClient.analyze(Mockito.isA(String.class))).thenReturn(analyzedText);

        // testdata that gtaaRepository mock will return
        GtaaDocument gtaaDocument = new GtaaDocument("test");
        gtaaDocument.setType(PERSOONSNAMEN);
        when(gtaaRepository.find("Frank de Boer", defaultSettings.getNamedEntityMinScore(PERSON), PERSOONSNAMEN)).thenReturn(Arrays.asList(gtaaDocument));

        // configure settings so that textrazor repository will be used and Person entities will always be added to the gtaa results
        defaultSettings.getProperties().setProperty(NAMEDENTITY_REPOSITORY, TEXTRAZOR_REPOSITORY_NAME);
        defaultSettings.getProperties().setProperty(format(NAMEDENTITY_MIN_TOKEN_FREQUENCY, PERSON.toValue()), "1");

        // the actual test
        ExtractResponse extractResponse = service.extract("In Amsterdam gaat Frank de Boer graag naar de Marqt.", defaultSettings);
        Assert.assertEquals(valueOf(1), extractResponse.getTotal());
    }
}
