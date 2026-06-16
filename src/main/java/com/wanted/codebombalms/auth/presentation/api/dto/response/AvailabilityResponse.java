package com.wanted.codebombalms.auth.presentation.api.dto.response;

public record AvailabilityResponse(
        boolean available
) {

    public static AvailabilityResponse of(boolean available) {
        return new AvailabilityResponse(available);
    }
}
