package com.codeit.sb01otbooteam06.domain.user.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;

import java.util.UUID;

public class UserNotFoundException extends OtbooException {

    public UserNotFoundException(UUID userId) {
        super(ErrorCode.USER_NOT_FOUND, new RuntimeException("사용자를 찾을 수 없습니다. id=" + userId));
    }

    public UserNotFoundException(String email) {
        super(ErrorCode.USER_NOT_FOUND, new RuntimeException("사용자를 찾을 수 없습니다. email=" + email));
    }
}
