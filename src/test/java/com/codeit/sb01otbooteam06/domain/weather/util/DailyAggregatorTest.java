package com.codeit.sb01otbooteam06.domain.weather.util;

import static com.codeit.sb01otbooteam06.domain.weather.util.DailyAggregator.aggregate;
import static org.assertj.core.api.BDDAssertions.then;

import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageItem;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyAggregatorTest {

    @Test
    @DisplayName("KMA 항목을 일 단위로 집계한다")
    void aggregateDailyItems() {
        // given
        List<KmaVillageItem> items = List.of(
            // 기온‧습도 3시각치
            new KmaVillageItem("20250720","0500",
                "20250721","0300","TMP","24", 63,123),
            new KmaVillageItem("20250720","0500",
                "20250721","0900","TMP","28", 63,123),
            new KmaVillageItem("20250720","0500",
                "20250721","1500","TMP","30", 63,123),
            // 최저‧최고
            new KmaVillageItem("20250720","0500",
                "20250721","1200","TMN","22", 63,123),
            new KmaVillageItem("20250720","0500",
                "20250721","1200","TMX","31", 63,123),
            // 습도
            new KmaVillageItem("20250720","0500",
                "20250721","0300","REH","80", 63,123),
            // 강수
            new KmaVillageItem("20250720","0500",
                "20250721","0300","PCP","1.5mm", 63,123),
            new KmaVillageItem("20250720","0500",
                "20250721","0300","POP","55", 63,123),
            // 하늘상태‧강수형태
            new KmaVillageItem("20250720","0500",
                "20250721","0300","SKY","4", 63,123),
            new KmaVillageItem("20250720","0500",
                "20250721","0300","PTY","1", 63,123),
            // 풍속
            new KmaVillageItem("20250720","0500",
                "20250721","0300","WSD","5", 63,123)
        );

        // when
        DailyAggregator.DailyAgg result = aggregate(items);

        // then
        then(result.tmpAvg()).isEqualTo(27.3);      // (24+28+30)/3, 소수 1자리 반올림
        then(result.tmpMin()).isEqualTo(22.0);
        then(result.tmpMax()).isEqualTo(31.0);
        then(result.rehAvg()).isEqualTo(80.0);
        then(result.pcpSum()).isEqualTo(1.5);
        then(result.popMax()).isEqualTo(55.0);
        then(result.sky()).isEqualTo(SkyStatus.CLOUDY);
        then(result.pty()).isEqualTo(PrecipitationType.RAIN);
        then(result.wsdAvg()).isEqualTo(5.0);
    }
}
