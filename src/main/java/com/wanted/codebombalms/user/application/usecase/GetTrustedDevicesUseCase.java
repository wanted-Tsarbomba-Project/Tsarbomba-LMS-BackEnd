package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.auth.domain.model.TrustedDevice;

import java.util.List;

public interface GetTrustedDevicesUseCase {

    List<TrustedDevice> getTrustedDevices(Long userId);
}
