package com.codeit.sb01otbooteam06.domain.weather.util;

import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageItem;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import java.util.List;

/**
 * 기상청 getVilageFcst 응답 1일치(List<KmaVillageItem>)를
 * Weather 엔티티에 바로 매핑 가능한 DailyAgg로 요약한다.
 */
public final class DailyAggregator {

    private DailyAggregator() {}

    public record DailyAgg(
        Double tmpAvg,  Double tmpMin, Double tmpMax,
        Double rehAvg,
        Double pcpSum,  Double popMax,
        SkyStatus sky,  PrecipitationType pty,
        Double wsdAvg) { }

    public static DailyAgg aggregate(List<KmaVillageItem> items) {

        double tmpSum = 0; int tmpCnt = 0;
        Double tmx = null, tmn = null;

        double rehSum = 0; int rehCnt = 0;
        double wsdSum = 0; int wsdCnt = 0;

        double pcpSum = 0;
        double popMax = 0;

        SkyStatus sky = SkyStatus.CLEAR;
        PrecipitationType pty = PrecipitationType.NONE;

        for (KmaVillageItem it : items) {
            String cat = it.category();
            String val = it.fcstValue().trim();

            switch (cat) {
                case "TMP" -> {       // 시간별 기온
                    Double v = toDouble(val);
                    if (v != null) { tmpSum += v; tmpCnt++; }
                }
                case "TMX" -> tmx = toDouble(val);
                case "TMN" -> tmn = toDouble(val);

                case "REH" -> {
                    Double v = toDouble(val);
                    if (v != null) { rehSum += v; rehCnt++; }
                }
                case "WSD" -> {
                    Double v = toDouble(val);
                    if (v != null) { wsdSum += v; wsdCnt++; }
                }
                case "POP" -> {
                    Double v = toDouble(val);
                    if (v != null) popMax = Math.max(popMax, v);
                }
                case "PCP" -> pcpSum += parsePcp(val);
                case "PTY" -> pty = mergePty(pty, val);
                case "SKY" -> sky = mergeSky(sky, val);
            }
        }

        // 평균·최솟값·최댓값 계산
        Double tmpAvg = tmpCnt == 0 ? null : round(tmpSum / tmpCnt);
        Double tmpMin = tmn != null ? tmn
            : (tmpCnt == 0 ? null : round(tmpSum / tmpCnt)); // Fallback
        Double tmpMax = tmx != null ? tmx
            : (tmpCnt == 0 ? null : round(tmpSum / tmpCnt));

        Double rehAvg = rehCnt == 0 ? null : round(rehSum / rehCnt);
        Double wsdAvg = wsdCnt == 0 ? null : round(wsdSum / wsdCnt);

        return new DailyAgg(
            tmpAvg, tmpMin, tmpMax,
            rehAvg,
            round(pcpSum), round(popMax),
            sky, pty,
            wsdAvg);
    }

    /* ---------- util ---------- */

    private static Double toDouble(String s) {
        if (s.equals("-") || s.equals("-999")) return null;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return null; }
    }

    /** "1mm 미만", "강수없음" 등을 mm 실수로 변환 */
    private static double parsePcp(String s) {
        if (s.startsWith("강수없음")) return 0.0;
        if (s.contains("미만")) return 0.5;
        if (s.endsWith("mm")) s = s.replace("mm", "");
        Double v = toDouble(s);
        return v == null ? 0.0 : v;
    }

    private static PrecipitationType mergePty(PrecipitationType cur, String v) {
        try {
            PrecipitationType next = PrecipitationType.fromCode(Integer.parseInt(v));
            return next.ordinal() > cur.ordinal() ? next : cur;
        } catch (Exception e) {
            return cur;
        }
    }

    private static SkyStatus mergeSky(SkyStatus cur, String v) {
        try {
            int code = Integer.parseInt(v);
            return switch (code) {
                case 4 -> SkyStatus.CLOUDY;
                case 3 -> cur == SkyStatus.CLOUDY ? cur : SkyStatus.MOSTLY_CLOUDY;
                default -> cur;
            };
        } catch (Exception e) {
            return cur;
        }
    }

    private static Double round(double v) {
        return Math.round(v * 10) / 10.0;
    }
}