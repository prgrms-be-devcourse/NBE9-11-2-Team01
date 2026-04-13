package com.team01.backend.domain.board.entity;

import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Board extends BaseEntity {
    private String name;
    private String description;

    public Board(String name, String description){
        this.name = name;
        this.description = description;
    }
}
