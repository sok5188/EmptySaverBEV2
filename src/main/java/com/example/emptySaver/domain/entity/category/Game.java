package com.example.emptySaver.domain.entity.category;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter@Setter
@DiscriminatorValue("game")
public class Game extends Category{
    //ex, mmorpg, fps ...
    private String gameGenre;
}
