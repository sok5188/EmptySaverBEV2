package com.example.emptySaver.domain.entity.category;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("sports")
public class Sports extends Category{
    @Enumerated(EnumType.STRING)
    private SportsType sportType;
}
