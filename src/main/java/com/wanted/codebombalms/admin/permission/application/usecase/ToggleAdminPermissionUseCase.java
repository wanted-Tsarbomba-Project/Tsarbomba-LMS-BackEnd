package com.wanted.codebombalms.admin.permission.application.usecase;

import com.wanted.codebombalms.admin.permission.application.command.ToggleAdminPermissionCommand;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPermissionResult;

// master가 admin 계정의 단일 권한을 부여하거나 회수하는 usecase다.
public interface ToggleAdminPermissionUseCase {

    // command에 담긴 권한 타입과 부여 여부대로 admin 권한을 변경한다.
    AdminAccountPermissionResult togglePermission(ToggleAdminPermissionCommand command);
}
