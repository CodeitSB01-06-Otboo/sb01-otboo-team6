package com.codeit.sb01otbooteam06.domain.feed.service;

import java.util.UUID;

public interface FeedLikeService {

  void likeFeed(UUID feedId);

  void unlikeFeed(UUID feedId);

}
