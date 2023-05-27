package com.example.emptySaver.domain.entity;

import com.example.emptySaver.domain.entity.category.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class MemberInterest {
    @Id@GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @Builder(builderMethodName = "init")
    public MemberInterest(Member member, Category category){
        this.member=member;
        this.category=category;
        member.getMemberInterests().add(this);
    }


}
