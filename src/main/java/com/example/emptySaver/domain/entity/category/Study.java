package com.example.emptySaver.domain.entity.category;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("study")
public class Study extends Category{
    //ex, toeic, coding, ...
    private String studyCategory;



}
