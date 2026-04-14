package com.team01.backend.domain.category.entity;

import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Category extends BaseEntity {
    private Long boardId;
    private String name;

    public Category(Long boardId, String name){
        this.boardId = boardId;
        this.name = name;
    }
}
