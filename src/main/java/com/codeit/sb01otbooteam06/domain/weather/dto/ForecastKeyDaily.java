package com.codeit.sb01otbooteam06.domain.weather.dto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * (baseDate + baseTime + fcstDate) + (gridX, gridY)
 * – 예보 날짜마다 하나의 UUID가 나오도록 설계.
 */
public record ForecastKeyDaily(
    Instant baseInstant,
    LocalDate fcstDate,
    int gridX, int gridY) {

    /* -------- 정적 팩토리 -------- */
    public static ForecastKeyDaily from(String baseDate, String baseTime, LocalDate fcstDate) {
        return new ForecastKeyDaily(
            toInstant(baseDate, baseTime),
            fcstDate,
            0, 0);                      // gridX/Y는 나중에 setter로 주입하거나
    }

    public ForecastKeyDaily withGrid(int x, int y) {
        return new ForecastKeyDaily(baseInstant, fcstDate, x, y);
    }

    /* -------- UUID -------- */
    public UUID toUuid() {
        String raw = baseInstant + "|" + fcstDate + "|" + gridX + "|" + gridY;
        return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    }

    /* ---- 내부 날짜→Instant 변환 ---- */
    private static Instant toInstant(String yyyymmdd, String hhmm) {
        LocalDate d = LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE);
        String time = String.format("%04d", Integer.parseInt(hhmm));
        LocalTime t = LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmm"));
        return ZonedDateTime.of(d, t, ZoneId.of("Asia/Seoul")).toInstant();
    }
}
