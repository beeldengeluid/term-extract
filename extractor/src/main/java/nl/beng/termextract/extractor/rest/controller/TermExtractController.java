package nl.beng.termextract.extractor.rest.controller;

import nl.beng.termextract.extractor.rest.model.ExtractRequest;
import nl.beng.termextract.extractor.service.ExtractionException;
import nl.beng.termextract.extractor.service.ExtractorService;
import nl.beng.termextract.extractor.service.model.ExtractResponse;
import nl.beng.termextract.extractor.service.model.Settings;

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

	@RequestMapping(method = { RequestMethod.GET }, produces = REST_RESPONSE_TYPE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ExtractResponse extract(@RequestParam(value = "text") String text)
			throws ExtractionException {
		logger.debug("GET Start term extraction....");
		try {
			return extractorService.extract(text);
		} finally {
			logger.debug("GET End term extraction.");
		}
	}

	@RequestMapping(method = { RequestMethod.POST }, headers = REST_REQUEST_TYPE, produces = REST_RESPONSE_TYPE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ExtractResponse extractPost(@RequestBody final ExtractRequest request)
			throws ExtractionException {
		logger.debug("Start term extraction....");
		try {
			if (request.getSettings() != null) {
				Settings settings = new Settings();
				settings.setProperties(request.getSettings());
				return extractorService.extract(request.getText(), settings);
			} else {
				return extractorService.extract(request.getText());
			}
		} finally {
			logger.debug("End term extraction.");
		}
	}

}
