package com.streamit.groupchatapp.model;


import com.streamit.groupchatapp.model.enums.ChannelRole;
import com.streamit.groupchatapp.model.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "channel_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"})
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelRole role;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onJoin() {
        this.joinedAt = LocalDateTime.now();
    }

}
