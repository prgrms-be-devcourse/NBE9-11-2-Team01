package com.team01.backend.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = -1794542998L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPost post = new QPost("post");

    public final com.team01.backend.global.entity.QBaseEntity _super = new com.team01.backend.global.entity.QBaseEntity(this);

    public final com.team01.backend.domain.user.entity.QUser author;

    public final com.team01.backend.domain.board.entity.QBoard board;

    public final com.team01.backend.domain.category.entity.QCategory category;

    public final ListPath<com.team01.backend.domain.comment.entity.Comment, com.team01.backend.domain.comment.entity.QComment> comments = this.<com.team01.backend.domain.comment.entity.Comment, com.team01.backend.domain.comment.entity.QComment>createList("comments", com.team01.backend.domain.comment.entity.Comment.class, com.team01.backend.domain.comment.entity.QComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath title = createString("title");

    public QPost(String variable) {
        this(Post.class, forVariable(variable), INITS);
    }

    public QPost(Path<? extends Post> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPost(PathMetadata metadata, PathInits inits) {
        this(Post.class, metadata, inits);
    }

    public QPost(Class<? extends Post> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.team01.backend.domain.user.entity.QUser(forProperty("author")) : null;
        this.board = inits.isInitialized("board") ? new com.team01.backend.domain.board.entity.QBoard(forProperty("board")) : null;
        this.category = inits.isInitialized("category") ? new com.team01.backend.domain.category.entity.QCategory(forProperty("category")) : null;
    }

}

