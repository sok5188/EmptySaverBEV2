package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class AlertDto {
    private String title;
    private String body;
    private String routeValue;
    private String idType;
    private String idValue;
    private LocalDateTime receiveTime;
    private Boolean isRead;
}
