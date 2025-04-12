package com.moviereservationapi.notification.model;

import com.moviereservationapi.notification.Enum.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("notificationDetails")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDetails {

    @Id
    private String id;
    private LocalDateTime sentAt;
    private Channel channel;
    private String sentTo;
}
