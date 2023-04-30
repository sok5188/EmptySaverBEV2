package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Friend{
    @Id@GeneratedValue
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name="owner_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member owner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member friendMember;

    public void addFriend(Member owner, Member friend){
        this.owner = owner;
        this.friendMember=friend;
        owner.getFriends().add(this);
    }

}
