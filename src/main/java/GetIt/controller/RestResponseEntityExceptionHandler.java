package GetIt.controller;

import GetIt.exceptions.*;
import GetIt.model.response.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {UpdateTranscriptException.class, TranscriptNotFoundException.class})
    protected ResponseEntity<Object> handleNotFoundException(RuntimeException ex, WebRequest webRequest) {
        return handleException(ex, webRequest, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {EmptyExpressionException.class, EmptyYoutubeUrlException.class, InvalidTimeSlotException.class})
    protected ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest webRequest) {
        return handleException(ex, webRequest, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InternalServerErrorException.class, Exception.class})
    protected ResponseEntity<Object> handleInternalServerError(RuntimeException ex, WebRequest webRequest) {
        return handleException(ex, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> handleException(RuntimeException ex, WebRequest webRequest, HttpStatus badRequest) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return handleExceptionInternal(ex, errorResponse, new HttpHeaders(), badRequest, webRequest);
    }
}