package com.streamit.groupchatapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * This is optional.
     * If you want a public-friendly ID (like "grp_83jd9"), keep it.
     * Otherwise remove it and just use `id`.
     */
    @Column(unique = true)
    private String channelId;

    @Column(nullable = false)
    private String channelName;

    @Column(length = 500)
    private String channelDescription;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;

    /**
     * Inverse side of Many-to-Many.
     * It maps back to User.channels.
     */
    @ManyToMany(mappedBy = "channels", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // avoids infinite recursion when returning JSON
    private Set<User> users = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
    }
}
