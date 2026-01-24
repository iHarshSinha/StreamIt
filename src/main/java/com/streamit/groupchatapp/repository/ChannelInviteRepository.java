package com.streamit.groupchatapp.repository;

import com.streamit.groupchatapp.model.ChannelInvite;
import com.streamit.groupchatapp.model.enums.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChannelInviteRepository extends JpaRepository<ChannelInvite, Long> {

    List<ChannelInvite> findByInvitedUserIdOrderByCreatedAtDesc(Long invitedUserId);

    List<ChannelInvite> findByInvitedUserIdAndStatusOrderByCreatedAtDesc(Long invitedUserId, InviteStatus status);

    Optional<ChannelInvite> findByChannelIdAndInvitedUserIdAndStatus(Long channelId, Long invitedUserId, InviteStatus status);

    List<ChannelInvite> findByStatusAndExpiresAtBefore(InviteStatus status, LocalDateTime time);
}
