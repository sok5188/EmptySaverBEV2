package com.example.emptySaver.domain.entity.category;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter@Setter
@DiscriminatorValue("free")
public class Free extends Category{
    @Enumerated(EnumType.STRING)
    private FreeType freeType;
}
