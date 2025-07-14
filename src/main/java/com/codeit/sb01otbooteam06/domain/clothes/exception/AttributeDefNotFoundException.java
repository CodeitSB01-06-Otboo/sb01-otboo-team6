package com.codeit.sb01otbooteam06.domain.clothes.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import java.util.UUID;

public class AttributeDefNotFoundException extends AttributeDefException {

  public AttributeDefNotFoundException() {
    super(ErrorCode.ATTRIBUTEDEF_NOT_FOUND);
  }

  public AttributeDefNotFoundException withId(UUID attributeDefId) {
    AttributeDefNotFoundException exception = new AttributeDefNotFoundException();
    exception.addDetail("attributeDefId", attributeDefId);
    return exception;
  }

}
