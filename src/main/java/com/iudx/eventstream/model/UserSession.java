package com.iudx.eventstream.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSession{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id")
    String userId;

    @Column(name = "session_duration")
    int sessionDuration;
}
