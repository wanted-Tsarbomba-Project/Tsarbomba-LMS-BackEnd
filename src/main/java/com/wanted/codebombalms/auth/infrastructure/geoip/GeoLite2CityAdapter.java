package com.wanted.codebombalms.auth.infrastructure.geoip;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.wanted.codebombalms.auth.domain.model.GeoLocation;
import com.wanted.codebombalms.auth.domain.service.GeoIpResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

@Slf4j
@Component
public class GeoLite2CityAdapter implements GeoIpResolver {

    private static final String DB_PATH = "geoip/GeoLite2-City.mmdb";

    private final DatabaseReader reader;

    public GeoLite2CityAdapter() {
        try (InputStream is = new ClassPathResource(DB_PATH).getInputStream()) {
            this.reader = new DatabaseReader.Builder(is)
                    .locales(List.of("en"))
                    .build();
            log.info("GeoLite2 City DB 로드 완료: {}", DB_PATH);
        } catch (IOException e) {
            throw new IllegalStateException("GeoLite2 City DB 로드 실패: " + DB_PATH, e);
        }
    }

    @Override
    public GeoLocation resolve(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return GeoLocation.unknown();
        }
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            CityResponse response = reader.city(address);
            String country = response.getCountry().getName();
            String city = response.getCity().getName();
            if (country == null && city == null) {
                return GeoLocation.unknown();
            }
            return new GeoLocation(country, city);
        } catch (IOException | GeoIp2Exception e) {
            // 사설 IP·미등록·조회 오류 → 로그인 막지 않고 unknown
            return GeoLocation.unknown();
        }
    }
}
