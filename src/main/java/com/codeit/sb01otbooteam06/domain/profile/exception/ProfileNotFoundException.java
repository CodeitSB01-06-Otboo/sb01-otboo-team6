package com.codeit.sb01otbooteam06.domain.profile.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;

import java.util.UUID;

public class ProfileNotFoundException extends OtbooException {

  public ProfileNotFoundException(UUID userId) {
    super(ErrorCode.PROFILE_NOT_FOUND);
    addDetail("userId", userId.toString());
  }
}
