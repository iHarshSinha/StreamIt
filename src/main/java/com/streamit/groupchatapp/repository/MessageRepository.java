package com.streamit.groupchatapp.repository;

import com.streamit.groupchatapp.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
SELECT m FROM Message m
JOIN FETCH m.sender s
WHERE m.channel.id = :channelId
AND (:cursor IS NULL OR m.id < :cursor)
ORDER BY m.id DESC
""")
    List<Message> fetchMessages(@Param("channelId") Long channelId,
                                @Param("cursor") Long cursor,
                                Pageable pageable);

}
