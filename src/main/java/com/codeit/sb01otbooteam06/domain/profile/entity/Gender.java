package com.codeit.sb01otbooteam06.domain.profile.entity;

public enum Gender {
  MALE, FEMALE, OTHER;

  public static Gender from(String value) {
    if (value == null) {
      throw new IllegalArgumentException("성별(gender) 값이 null 입니다.");
    }
    try {
      return Gender.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 성별(gender) 값입니다: " + value);
    }
  }
}