package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Comment;
import com.example.emptySaver.domain.entity.Post;
import com.example.emptySaver.domain.entity.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByTeam(Team team);
    List<Comment> findByPost(Post post);
    @EntityGraph(attributePaths = {"member","team"})
    Optional<Comment> findWithMemberAndTeamById(Long id);
    @EntityGraph(attributePaths = {"member","post"})
    Optional<Comment> findWithMemberAndPostById(Long id);
}
