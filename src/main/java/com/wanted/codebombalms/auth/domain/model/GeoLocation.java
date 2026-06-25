package com.wanted.codebombalms.auth.domain.model;

public record GeoLocation(String country, String city) {

    private static final GeoLocation UNKNOWN = new GeoLocation(null, null);

    /** GeoIP 조회 실패(사설 IP·미등록 등) 시 사용 */
    public static GeoLocation unknown() {
        return UNKNOWN;
    }

    public boolean isUnknown() {
        return country == null && city == null;
    }
}
