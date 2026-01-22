package com.streamit.groupchatapp.repository;

import com.streamit.groupchatapp.model.ChannelMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<ChannelMembership, Long> {
    boolean existsByChannelIdAndUserId(Long channelId, Long userId);

    void deleteByChannelIdAndUserId(Long channelId, Long userId);

    Optional<ChannelMembership> findByChannelIdAndUserId(Long channelId, Long userId);
}
