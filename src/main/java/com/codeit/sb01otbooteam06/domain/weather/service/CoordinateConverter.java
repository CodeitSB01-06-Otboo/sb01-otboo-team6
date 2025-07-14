package com.codeit.sb01otbooteam06.domain.weather.service;

import org.springframework.stereotype.Component;

@Component
public class CoordinateConverter {

    /** 격자(A1=1,1) 원점 기준값 ─ 기상청 공식(단기예보 가이드) */
    private static final double RE = 6371.00877;   // 지구 반경(km)
    private static final double GRID = 5.0;        // 격자 간격(km)
    private static final double SLAT1 = 30.0;      // 투영 위도1
    private static final double SLAT2 = 60.0;      // 투영 위도2
    private static final double OLON = 126.0;      // 기준 경도
    private static final double OLAT = 38.0;       // 기준 위도
    private static final double XO = 43;           // 기준점 X(열)
    private static final double YO = 136;          // 기준점 Y(행)

    private static final double DEGRAD = Math.PI / 180.0;
    private static final double RADDEG = 180.0 / Math.PI;

    public record Grid(int gridX, int gridY) { }

    public Grid latLonToGrid(double lat, double lon) {

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) /
            Math.log(Math.tan(Math.PI*0.25 + slat2*0.5) /
                Math.tan(Math.PI*0.25 + slat1*0.5));

        double sf = Math.tan(Math.PI*0.25 + slat1*0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI*0.25 + olat*0.5);
        ro = re * sf / Math.pow(ro, sn);

        /* ↘ 실제 변환 */
        double ra = Math.tan(Math.PI*0.25 + lat*DEGRAD*0.5);
        ra = re * sf / Math.pow(ra, sn);

        double theta = lon*DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0*Math.PI;
        if (theta < -Math.PI) theta += 2.0*Math.PI;
        theta *= sn;

        int x = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int y = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        return new Grid(x, y);
    }
}
