package com.wanted.codebombalms.auth.domain.service;

import com.wanted.codebombalms.auth.domain.model.GeoLocation;

public interface GeoIpResolver {

    /**
     * IP 주소로 국가/도시를 조회한다.
     * 조회 불가(사설 IP·미등록·오류) 시 {@link GeoLocation#unknown()} 반환 (예외 던지지 않음).
     */
    GeoLocation resolve(String ipAddress);
}
