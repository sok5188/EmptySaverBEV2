package com.example.emptySaver.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FCMDto {
    private Long userId;
    private String title;
    private String body;
}
