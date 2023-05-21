package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.Team;
import com.example.emptySaver.domain.entity.category.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team,Long> {
    @EntityGraph(attributePaths = "owner")
    List<Team> findByOwner(Member owner);


    List<Team> findByCategory(Category categoryByLabel);

    @Override
    @EntityGraph(attributePaths = "category")
    List<Team> findAll();
    @EntityGraph(attributePaths = "category")
    Optional<Team> findFirstWithCategoryById(Long id);

    @EntityGraph(attributePaths = "category")
    List<Team> findWithCategoryByCategoryIn(List<Category> categories);

    @EntityGraph(attributePaths = "owner")
    Optional<Team> findWithOwnerById(Long id);

}
