package com.codeit.sb01otbooteam06.domain.weather.mapper;

import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageItem;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherMetrics;
import com.codeit.sb01otbooteam06.domain.weather.entity.Precipitation;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class WeatherMapper {

    /* enum 코드를 이용해 값 매핑 */
    public static WeatherMetrics toMetrics(List<KmaVillageItem> items) {
        Map<String, KmaVillageItem> map =
            items.stream().collect(Collectors.toMap(KmaVillageItem::category, i -> i));

        // SKY, PTY
        SkyStatus sky = Optional.ofNullable(map.get("SKY"))
            .map(i -> SkyStatus.fromCode(Integer.parseInt(i.fcstValue())))
            .orElse(null);

        PrecipitationType pty = Optional.ofNullable(map.get("PTY"))
            .map(i -> PrecipitationType.fromCode(Integer.parseInt(i.fcstValue())))
            .orElse(null);

        // 온도
        Temperature temp = Temperature.from(
            parseDouble(map.get("TMP")),
            parseDouble(map.get("TMN")),
            parseDouble(map.get("TMX")));

        // 강수
        Precipitation precip = Precipitation.from(
            parseDoubleOrZero(map.get("PCP")),               // mm
            map.get("PCP") == null ? null : map.get("PCP").fcstValue(), // “강수없음” 그대로 저장
            parseDouble(map.get("POP")));                    // %

        // 풍속·풍향
        Double speed = num(map.get("WSD"));
        Wind wind = Wind.from(
            speed,
            toBeaufort(speed),          // Beaufort util 제거→내부 static 메서드
            num(map.get("VEC")),
            num(map.get("UUU")),
            num(map.get("VVV")));

        return new WeatherMetrics(
            sky, pty, temp, precip, wind,
            parseDouble(map.get("REH")),                     // 습도
            parseDoubleOrZero(map.get("SNO")),               // 적설
            parseDouble(map.get("LGT")));                    // 낙뢰
    }

    private static Double parseDouble(KmaVillageItem i) {
        if (i == null) {
            return null;
        }
        try {
            return Double.parseDouble(i.fcstValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDoubleOrZero(KmaVillageItem i) {
        Double v = parseDouble(i);
        return v != null ? v : 0.0;
    }

    private static <E> E enumFrom(KmaVillageItem i, IntFunction<E> mapper) {
        return i == null ? null : mapper.apply(Integer.parseInt(i.fcstValue()));
    }

    private static Double num(KmaVillageItem i) {
        if (i == null) {
            return null;
        }
        try {
            return Double.parseDouble(i.fcstValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double numZero(KmaVillageItem i) {
        Double v = num(i);
        return v != null ? v : 0.0;
    }

    /**
     * 풍속(m/s) → 보퍼트 단계(0‧1‧…‧12). <br> 기상청 기준(약식):  0.3 미만 0단계, 32.7 이상 12단계
     */
    private static Integer toBeaufort(Double ms) {
        if (ms == null) {
            return null;
        }
        double v = ms;
        if (v < 0.3) {
            return 0;
        } else if (v < 1.6) {
            return 1;
        } else if (v < 3.4) {
            return 2;
        } else if (v < 5.5) {
            return 3;
        } else if (v < 8.0) {
            return 4;
        } else if (v < 10.8) {
            return 5;
        } else if (v < 13.9) {
            return 6;
        } else if (v < 17.2) {
            return 7;
        } else if (v < 20.8) {
            return 8;
        } else if (v < 24.5) {
            return 9;
        } else if (v < 28.5) {
            return 10;
        } else if (v < 32.7) {
            return 11;
        } else {
            return 12;
        }
    }
}
