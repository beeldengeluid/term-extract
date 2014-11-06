package nl.beng.termextract.extractor.rest;

import nl.beng.termextract.extractor.repository.namedentity.NamedEntityExtractionException;
import nl.beng.termextract.extractor.rest.error.ErrorMessage;

import org.omg.CORBA.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: Danny Date: 10-7-14 Time: 16:39
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler({ NamedEntityExtractionException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorMessage handleNamedEntityExtractionException(
			@RequestBody NamedEntityExtractionException exception) {
		return new ErrorMessage(exception.getMessage());
	}

	@ExceptionHandler({ SystemException.class, Throwable.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessage handleSystemException(@RequestBody Throwable exception) {
		try {
			return new ErrorMessage(exception.getMessage());
		} finally {
			logger.error(exception.getMessage(), exception);
		}
	}
}
