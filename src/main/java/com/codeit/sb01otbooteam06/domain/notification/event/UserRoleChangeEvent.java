package com.codeit.sb01otbooteam06.domain.notification.event;

import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;

public record UserRoleChangeEvent(User receiver, Role previousRole, Role newRole) {

}
