package com.codeit.sb01otbooteam06.domain.weather.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;

public class WeatherException extends OtbooException {

    public WeatherException(ErrorCode errorCode) {
        super(errorCode);
    }
}
