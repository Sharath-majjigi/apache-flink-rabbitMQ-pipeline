package com.iudx.eventstream.model;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event implements Serializable {

    Instant timestamp;
    String userId;
    String eventType;
    String productId;
    int sessionDuration;
}
