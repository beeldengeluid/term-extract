package nl.beng.termextract.extractor.rest.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nl.beng.termextract.extractor.rest.model.ExtractRequest;
import nl.beng.termextract.extractor.service.ExtractionException;
import nl.beng.termextract.extractor.service.ExtractorService;
import nl.beng.termextract.extractor.service.Settings;
import nl.beng.termextract.extractor.service.Term;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/termextract")
public class TermExtractController {

	private static final Logger logger = LoggerFactory
			.getLogger(TermExtractController.class);
	public static final String REST_RESPONSE_TYPE = "application/json;charset=UTF-8";
	public static final String REST_REQUEST_TYPE = "Content-Type=application/json;charset=UTF-8";

	@Autowired
	private ExtractorService extractorService;
	@Autowired
	private Settings defaultSettings;

	@RequestMapping(method = { RequestMethod.GET }, produces = REST_RESPONSE_TYPE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<Set<Term>> extract(@RequestParam(value = "text") String text)
			throws ExtractionException {
		return extractTerms(text, defaultSettings);
	}

	@RequestMapping(method = { RequestMethod.POST }, headers = REST_REQUEST_TYPE, produces = REST_RESPONSE_TYPE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<Set<Term>> extractPost(@RequestBody final ExtractRequest request)
			throws ExtractionException {
		if (request.getSettings().getMinGram() != null) {
			defaultSettings.setMinGram(request.getSettings().getMinGram());
		}
		if (request.getSettings().getMaxGram() != null) {
			defaultSettings.setMaxGram(request.getSettings().getMaxGram());
		}
		if (request.getSettings().getMinTokenFrequency() != null) {
			defaultSettings.setMinTokenFrequency(request.getSettings()
					.getMinTokenFrequency());
		}
		if (request.getSettings().getMinNormalizedFrequency() != null) {
			defaultSettings.setMinNormalizedFrequency(request.getSettings().getMinNormalizedFrequency());
		}
		return extractTerms(request.getText(), defaultSettings);
	}

	private List<Set<Term>> extractTerms(String text, Settings settings)
			throws ExtractionException {
		logger.debug("Start term extraction....");
		List<String> texts = new LinkedList<>();
		texts.add(text);
		try {
			return extractorService.extract(texts, settings);
		} finally {
			logger.debug("End term extraction.");
		}
	}

}
