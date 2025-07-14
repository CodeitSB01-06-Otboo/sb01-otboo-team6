package com.codeit.sb01otbooteam06.domain.weather.exception;

import com.codeit.sb01otbooteam06.global.exception.ErrorCode;

public class WeatherNotFoundException extends WeatherException {

    public WeatherNotFoundException() {
        super(ErrorCode.WEATHER_NOT_FOUND);
    }

    /** 위·경도 정보로 상세 메시지 보강 */
    public WeatherNotFoundException withLatLon(double lat, double lon) {
        WeatherNotFoundException ex = new WeatherNotFoundException();
        ex.addDetail("latitude", lat);
        ex.addDetail("longitude", lon);
        return ex;
    }
}
