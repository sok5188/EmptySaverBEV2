package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.category.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository<T extends Category> extends JpaRepository<T, Long> {
    @Query("from Game")
    List<Game> findAllGame();
    @Query("from Sports")
    List<Sports> findAllSports();
    @Query("from Movie")
    List<Movie> findAllMovie();
    @Query("from Study")
    List<Study> findAllStudy();
    @Override
    Optional<T> findById(Long categoryId);
}
