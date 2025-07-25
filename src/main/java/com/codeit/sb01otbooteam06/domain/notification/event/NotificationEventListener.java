package com.codeit.sb01otbooteam06.domain.notification.event;


import com.codeit.sb01otbooteam06.domain.follow.entity.Follow;
import com.codeit.sb01otbooteam06.domain.follow.repository.FollowRepository;
import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.repository.NotificationRepository;
import com.codeit.sb01otbooteam06.domain.notification.util.NotificationCreator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Async("eventTaskExecutor")
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationRepository notificationRepository;
  private final FollowRepository followRepository;
  private final ApplicationEventPublisher eventPublisher;

  @EventListener
  public void handleFeedLiked(FeedLikeCreatedEvent event) {
    if (event.sender().getId().equals(event.receiver().getId())) {
      return;
    }

    Notification notification = NotificationCreator.ofFeedLike(event.sender(), event.receiver(),
        event.feedContent());
    notificationRepository.save(notification);
    eventPublisher.publishEvent(new NotificationCreateEvent(NotificationDto.from(notification)));
  }

  @EventListener
  public void handleFeedCommented(FeedCommentCreatedEvent event) {
    if (event.sender().getId().equals(event.receiver().getId())) {
      return;
    }

    Notification notification = NotificationCreator.ofFeedComment(event.sender(), event.receiver(),
        event.feedContent());
    notificationRepository.save(notification);
    eventPublisher.publishEvent(new NotificationCreateEvent(NotificationDto.from(notification)));
  }

  @EventListener
  public void handleRoleChanged(UserRoleChangeEvent event) {
    Notification notification = NotificationCreator.ofRoleChanged(event.receiver(),
        event.previousRole(), event.newRole());
    notificationRepository.save(notification);
    eventPublisher.publishEvent(new NotificationCreateEvent(NotificationDto.from(notification)));
  }

  @EventListener
  public void handleClothesAttributeAdded(ClothesAttributeAddedEvent event) {
    Notification notification = NotificationCreator.ofClothesAttributeAdded(event.receiver(),
        event.attributeSummary());
    notificationRepository.save(notification);
    eventPublisher.publishEvent(new NotificationCreateEvent(NotificationDto.from(notification)));
  }

  @EventListener
  public void handleFolloweeFeedPosted(FolloweeFeedPostedEvent event) {
    List<Follow> follows = followRepository.findFollowers(event.followee().getId(), null,
        Pageable.unpaged());

    List<Notification> notifications = follows.stream()
        .map(Follow::getFollower)
        .filter(follower -> !follower.getId().equals(event.followee().getId()))
        .map(follower -> NotificationCreator.ofFolloweeFeedPosted(follower, event.followee(),
            event.feedContent()))
        .toList();

    notificationRepository.saveAll(notifications);
    notifications.forEach(n ->
        eventPublisher.publishEvent(new NotificationCreateEvent(NotificationDto.from(n)))
    );
  }

  @EventListener
  public void handleUserFollowed(UserFollowMeEvent event) {
    if (event.follower().getId().equals(event.following().getId())) {
      return;
    }

    Notification notification = NotificationCreator.ofUserFollowMe(event.follower(),
        event.following());
    notificationRepository.save(notification);
    eventPublisher.publishEvent(new NotificationCreateEvent(NotificationDto.from(notification)));
  }

  @EventListener
  public void DirectMessageReceived(DirectMessageReceivedEvent event) {
    if (event.sender().getId().equals(event.receiver().getId())) {
      return;
    }

    Notification notification = NotificationCreator.ofDirectMessageReceive(
        event.sender(), event.receiver(), event.messageContent());
    notificationRepository.save(notification);
    eventPublisher.publishEvent(new NotificationCreateEvent(NotificationDto.from(notification)));
  }


}
