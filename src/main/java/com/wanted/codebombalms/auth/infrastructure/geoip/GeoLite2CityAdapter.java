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
import java.util.regex.Pattern;

@Slf4j
@Component
public class GeoLite2CityAdapter implements GeoIpResolver {

    private static final String DB_PATH = "geoip/GeoLite2-City.mmdb";

    /** IP 리터럴(IPv4 점표기 / IPv6 콜론포함)만 허용 — 호스트명 입력 시 DNS 조회·블로킹 차단 */
    private static final Pattern IP_LITERAL = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$|^[0-9a-fA-F:]*:[0-9a-fA-F:]*$");

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
        if (ipAddress == null || ipAddress.isBlank() || !IP_LITERAL.matcher(ipAddress).matches()) {
            return GeoLocation.unknown(); // 호스트명 등 비-IP 입력은 DNS 조회 없이 unknown
        }
        try {
            InetAddress address = InetAddress.getByName(ipAddress); // 리터럴만 도달 → DNS 미발생
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
