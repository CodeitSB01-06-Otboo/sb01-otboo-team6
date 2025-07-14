package com.codeit.sb01otbooteam06.domain.clothes.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import java.util.UUID;

public class AttributeDefAlreadyExistsException extends AttributeDefException {

  public AttributeDefAlreadyExistsException() {
    super(ErrorCode.ATTRIBUTEDEF_ALREADY_EXISTS);
  }

  public AttributeDefAlreadyExistsException withId(UUID attributeDefId) {
    AttributeDefAlreadyExistsException exception = new AttributeDefAlreadyExistsException();
    exception.addDetail("attributeDefId", attributeDefId);
    return exception;
  }
}
