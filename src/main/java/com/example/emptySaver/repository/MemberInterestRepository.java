package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberInterest;
import com.example.emptySaver.domain.entity.category.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberInterestRepository extends JpaRepository<MemberInterest,Long> {

    @EntityGraph(attributePaths = "member")
    List<MemberInterest> findWithMemberByCategory(Category category);

    @EntityGraph(attributePaths = "category")
    List<MemberInterest> findWithCategoryByMember(Member member);
}
