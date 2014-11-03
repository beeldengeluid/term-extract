package nl.beng.termextract.extractor.rest;

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
	private static final String CODE_INTERNAL_SYSTEM_ERROR = "error.internal.system.error";

	@ExceptionHandler({ SystemException.class, Throwable.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessage handleSystemException(@RequestBody Throwable exception) {
		try {
			return new ErrorMessage(CODE_INTERNAL_SYSTEM_ERROR,
					exception.getMessage());
		} finally {
			logger.error(exception.getMessage(), exception);
		}
	}
}
