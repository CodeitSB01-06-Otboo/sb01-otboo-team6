package com.codeit.sb01otbooteam06.domain.clothes.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;

public class ClothesException extends OtbooException {

  public ClothesException(ErrorCode errorCode) {
    super(errorCode);
  }
}
