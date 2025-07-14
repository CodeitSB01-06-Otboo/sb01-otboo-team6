package com.codeit.sb01otbooteam06.domain.clothes.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;

public class AttributeDefException extends OtbooException {

  public AttributeDefException(ErrorCode errorCode) {
    super(errorCode);
  }

}