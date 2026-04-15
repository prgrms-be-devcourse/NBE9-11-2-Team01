package com.team01.backend.domain.board.entity;

import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "boards")
public class Board extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Setter
    private boolean isDeleted;

    public Board(String name, String description){
        this.name = name;
        this.description = description;
        this.isDeleted = false;
    }

    public void update(String name, String description) { // 게시판 수정
        this.name = name;
        this.description = description;
    }
}
