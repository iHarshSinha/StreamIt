package com.streamit.groupchatapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    /**
     * Example values: "admin", "manager", "member", "user"
     */
    @Column(nullable = false)
    private String role;

    @Column
    private String profileImageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Owning side of Many-to-Many.
     * Join table: user_groups(user_id, channel_id)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_channels",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id")
    )
    @Builder.Default
    private Set<Channel> channels = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Optional helper methods (recommended)
    public void joinChannel(Channel channel) {
        this.channels.add(channel);
        channel.getUsers().add(this);
    }

    public void leaveChannel(Channel channel) {
        this.channels.remove(channel);
        channel.getUsers().remove(this);
    }
}
