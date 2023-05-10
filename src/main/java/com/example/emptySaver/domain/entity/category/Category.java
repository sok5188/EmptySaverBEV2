package com.example.emptySaver.domain.entity.category;

import com.example.emptySaver.domain.entity.MemberInterest;
import com.example.emptySaver.domain.entity.Team;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@Entity
public abstract class Category {

    @Id@GeneratedValue
    @Column(name = "category_id")
    private Long id;
//   추후 카테고리로 회원을 찾아야 하는 경우 사용
//    @OneToMany(mappedBy = "category")
//    private List<Member_Interest> memberInterestList;
    @OneToMany(mappedBy = "category")
    private List<Team> categoryTeamList;

    @Column(name="category_name")
    private String name;

}
