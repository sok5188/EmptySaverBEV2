package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Post;
import com.example.emptySaver.domain.entity.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    @EntityGraph(attributePaths = "member")
    Optional<Post> findWithMemberById(Long id);

    @EntityGraph(attributePaths = "team")
    Optional<Post> findWithTeamById(Long id);

    List<Post> findByTeam(Team team);
}
