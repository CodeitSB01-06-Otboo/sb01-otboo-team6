package com.codeit.sb01otbooteam06.domain.weather.entity;

/**
 * KMA PTY(강수 형태) 코드 매핑
 * 0 : 강수 없음 (NONE)
 * 1 : 비          (RAIN)
 * 2 : 비/눈       (RAIN_SNOW)  ― 진눈개비
 * 3 : 눈          (SNOW)
 * 4 : 소나기      (SHOWER)
 * 5 : 빗방울      (DRIZZLE)     ― 초단기 실황 전용
 * 6 : 빗방울/눈날림 (DRIZZLE_SNOW)
 * 7 : 눈날림      (FLURRY)
 */
public enum PrecipitationType {
    NONE(0),
    RAIN(1),
    RAIN_SNOW(2),
    SNOW(3),
    SHOWER(4),
    DRIZZLE(5),
    DRIZZLE_SNOW(6),
    FLURRY(7);

    private final int code;

    PrecipitationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /** KMA 코드 → Enum */
    public static PrecipitationType fromCode(int code) {
        return switch (code) {
            case 0 -> NONE;
            case 1 -> RAIN;
            case 2 -> RAIN_SNOW;
            case 3 -> SNOW;
            case 4 -> SHOWER;
            case 5 -> DRIZZLE;
            case 6 -> DRIZZLE_SNOW;
            case 7 -> FLURRY;
            default -> throw new IllegalArgumentException("Unknown PTY code: " + code);
        };
    }
}