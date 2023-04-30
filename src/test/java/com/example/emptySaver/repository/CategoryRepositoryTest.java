package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.category.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
@DataJpaTest
class CategoryRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private CategoryRepository categoryRepository;

    @Nested
    @DisplayName("save Test")
    class SaveTest{
        @Test
        public void 게임저장(){
            //given
            Game game=new Game();
            game.setGameGenre(GameType.FPS);
            game.setName("SuddenAttack");
            em.persistAndFlush(game);
            //when
            em.clear();
            Optional<Category> save = categoryRepository.findById(game.getId());
            //then
            assertThat(save.isPresent()).isTrue();
            assertThat(save.get().getName()).isEqualTo(game.getName());
        }

        @Test
        public void 영화저장(){
            Movie movie=new Movie();
            movie.setName("바람");
            movie.setMovieGenre(MovieType.ACTION);
            em.persistAndFlush(movie);
            //when
            em.clear();
            Optional<Category> save = categoryRepository.findById(movie.getId());

            assertThat(save.isPresent()).isTrue();
            assertThat(save.get().getName()).isEqualTo(movie.getName());
        }

        @Test void 스터디저장(){
            Study study=new Study();
            study.setName("백엔드 개발 스터디");
            study.setStudyType(StudyType.LANGUAGE);
            em.persistAndFlush(study);

            em.clear();
            Optional<Category> save = categoryRepository.findById(study.getId());

            assertThat(save.isPresent()).isTrue();
            assertThat(save.get().getName()).isEqualTo(study.getName());
        }

    }
}