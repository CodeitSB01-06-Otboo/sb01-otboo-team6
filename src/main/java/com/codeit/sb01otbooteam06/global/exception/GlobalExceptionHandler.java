package com.codeit.sb01otbooteam06.global.exception;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse>
  handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
    List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
    ErrorResponse errorResponse = new ErrorResponse(fieldErrors, HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<ErrorResponse>
  handleRuntimeException(Exception exception) {
    ErrorResponse errorResponse = new ErrorResponse(exception,
        HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * otboo 커스텀 예외에 대한 처리를 합니다.
   *
   * @param exception
   * @return 해당 exception에 대한 에러 응답
   */
  @ExceptionHandler(OtbooException.class)
  protected ResponseEntity<ErrorResponse>
  handleRuntimeException(OtbooException exception) {
    log.error("커스텀 예외 발생: code = {}, message = {}", exception.getErrorCode(),
        exception.getMessage());
    HttpStatus status = determineHttpStatus(exception);
    ErrorResponse errorResponse = new ErrorResponse(exception, status.value());
    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * otboo 예외에 대한 상태코드를 처리합니다.
   *
   * @param exception
   * @return 해당 exception에 대한 상태코드
   */
  private HttpStatus determineHttpStatus(OtbooException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    return switch (errorCode) {

      //400 Bad Request
      case ILLEGAL_ARGUMENT_ERROR,
           INVALID_REQUEST,
           USER_NOT_FOUND,
           CLOTHES_NOT_FOUND,
           ATTRIBUTEDEF_ALREADY_EXISTS,
           ATTRIBUTEDEF_NOT_FOUND,
           PROFILE_NOT_FOUND,
           FEED_NOT_FOUND,
           UNAUTHORIZED_FEED_ACCESS,
           WEATHER_NOT_FOUND-> HttpStatus.BAD_REQUEST;


      //500 Internal Server Error
      case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }


}
