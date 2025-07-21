package com.codeit.sb01otbooteam06.domain.follow.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;

public class InvalidFollowerException extends OtbooException {
  public InvalidFollowerException() {
    super(ErrorCode.FOLLOWER_MISMATCH);
  }
}
