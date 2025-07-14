package com.codeit.sb01otbooteam06.domain.dm.service;

import com.codeit.sb01otbooteam06.domain.dm.dto.DirectMessageDto;
import com.codeit.sb01otbooteam06.domain.dm.dto.DirectMessageListResponse;
import com.codeit.sb01otbooteam06.domain.dm.entity.DirectMessage;
import com.codeit.sb01otbooteam06.domain.dm.repository.DirectMessageRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DirectMessageService {

    private final DirectMessageRepository dmRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /* 전송 */
    @Transactional
    public UUID send(UUID senderId, UUID receiverId, String content) {
        User sender   = getUser(senderId);
        User receiver = getUser(receiverId);

        DirectMessage dm = dmRepository.save(DirectMessage.from(sender, receiver, content));
        log.info("[DM-DB ] saved id={}  {} → {}", dm.getId(), senderId, receiverId);

        String key = DirectMessage.generateKey(senderId, receiverId);
        messagingTemplate.convertAndSend("/sub/direct-messages_" + key,
            DirectMessageDto.from(dm));
        log.info("[DM-SEND] /sub/direct-messages_{}  payload#{}", key, dm.getId());
        return dm.getId();
    }


    /* DM 목록 ------------------------------------------------------------ */
    public DirectMessageListResponse list(UUID userId, UUID cursor, int size) {

        Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());

        Page<DirectMessageDto> page =
            dmRepository.findLatestPerRoom(userId, cursor, pageable)
                .map(DirectMessageDto::from);

        List<DirectMessageDto> list = page.getContent();
        UUID nextCursor  = page.hasNext() ? list.get(list.size()-1).id() : null;

        return new DirectMessageListResponse(
            list,
            nextCursor,
            nextCursor,
            page.hasNext(),
            list.size(),
            "createdAt",
            "DESCENDING");
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}