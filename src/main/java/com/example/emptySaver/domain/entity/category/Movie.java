package com.example.emptySaver.domain.entity.category;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter@Setter
@DiscriminatorValue("movie")
public class Movie extends Category{
    // sf, fantasy, adventure ...
    private String movieGenre;
}
