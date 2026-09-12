package it.cityvoice.api.features.comments.repositories;

import it.cityvoice.api.features.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, UUID> {
}
