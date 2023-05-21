package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    //알림을 받은 Member
    private Member member;

    private String title;
    private String body;
    private String routeValue;
    private String idType;
    private String idValue;
    private LocalDateTime receiveTime;
    private Boolean isRead;
    @Builder(builderMethodName = "init")
    public Notification(Member member, String title, String body, String routeValue, String idType, String idValue){
        this.member=member;
        this.title=title;
        this.body=body;
        this.routeValue=routeValue;
        this.idType=idType;
        this.idValue = idValue;
        receiveTime=LocalDateTime.now();
        isRead=false;
    }
}
