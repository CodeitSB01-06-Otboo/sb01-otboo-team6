package com.codeit.sb01otbooteam06.domain.notification.event;

import com.codeit.sb01otbooteam06.domain.user.entity.User;

public record ClothesAttributeAddedEvent(User receiver, String attributeSummary) {

}
