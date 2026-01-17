package com.streamit.groupchatapp.model;




import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String groupId;
    private String groupName;
    private String groupDescription;
    private LocalDateTime createdOn;
    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    private List<User> createdBy;
}
