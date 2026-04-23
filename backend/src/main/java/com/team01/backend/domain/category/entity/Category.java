package com.team01.backend.domain.category.entity;

import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "boardId", "name" }) })
public class Category extends BaseEntity {
    private Long boardId;
    private String name;

    public Category(Long boardId, String name){
        this.boardId = boardId;
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }
}
