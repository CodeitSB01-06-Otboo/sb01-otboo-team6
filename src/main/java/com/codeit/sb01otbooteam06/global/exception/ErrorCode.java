package com.codeit.sb01otbooteam06.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  //global
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
  INVALID_REQUEST("잘못된 요청입니다"),
  ILLEGAL_ARGUMENT_ERROR("잘못된 인수가 전달되었습니다."),

  //Clothes
  CLOTHES_NOT_FOUND("의상이 없습니다."),

  //AttributeDefs
  ATTRIBUTEDEF_ALREADY_EXISTS("의상 속성이 이미 존재합니다."),
  ATTRIBUTEDEF_NOT_FOUND("의상 속성이 존재하지 않습니다."),


  //weather
  WEATHER_NOT_FOUND("현재 위치의 날씨 데이터가 없습니다."),
  //USER
  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),

  //Profile
  PROFILE_NOT_FOUND("프로필을 찾을 수 없슴니다."),

  //Feed
  FEED_NOT_FOUND("피드를 찾을 수 없습니다."),
  UNAUTHORIZED_FEED_ACCESS("해당 피드에 대한 접근 권한이 없습니다.");



  private final String message;

}
