package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertDto<T> {
    private T friend;
    private T group;
    private T owner;
}
