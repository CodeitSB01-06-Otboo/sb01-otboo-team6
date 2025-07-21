package com.codeit.sb01otbooteam06.global.s3.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;

public class S3UploadException extends OtbooException {

  public S3UploadException(ErrorCode errorCode) {
    super(errorCode);
  }

  public S3UploadException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
