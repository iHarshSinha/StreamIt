package com.streamit.groupchatapp.repository;

import com.streamit.groupchatapp.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

    public interface ChannelRepository extends JpaRepository<Channel, Long> {

        @Query("""
        select c
        from Channel c
        where
            c.type = 'PUBLIC'
            or exists (
                select 1
                from ChannelMembership cm
                where cm.channel.id = c.id
                  and cm.user.id = :userId
            )
        """)
        List<Channel> findAllForUserOrPublic(@Param("userId") Long userId);
    }

