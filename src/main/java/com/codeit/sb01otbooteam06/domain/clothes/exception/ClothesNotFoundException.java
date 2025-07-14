package com.codeit.sb01otbooteam06.domain.clothes.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import java.util.UUID;

public class ClothesNotFoundException extends ClothesException {

  public ClothesNotFoundException() {
    super(ErrorCode.CLOTHES_NOT_FOUND);
  }

  public ClothesNotFoundException withId(UUID clothesId) {
    ClothesNotFoundException exception = new ClothesNotFoundException();
    exception.addDetail("clothesId", clothesId);
    return exception;
  }

}
