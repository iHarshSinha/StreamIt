package com.streamit.groupchatapp.repository;

import com.streamit.groupchatapp.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

}
