package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@Builder
public class AlertDto {
    private Long id;
    private String title;
    private String body;
    private String routeValue;
    private String idType;
    private String idType2;
    private String idValue;
    private String idValue2;
    private ZonedDateTime receiveTime;
    private Boolean isRead;
}
