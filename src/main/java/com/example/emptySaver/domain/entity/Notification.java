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
public class Notification implements Comparable<Notification>{
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
    private String idType2;
    private String idValue2;
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
    @Builder(builderMethodName = "longInit")
    public Notification(Member member, String title, String body, String routeValue, String idType
            , String idValue, String idType2, String idValue2){
        this.member=member;
        this.title=title;
        this.body=body;
        this.routeValue=routeValue;
        this.idType=idType;
        this.idValue = idValue;
        this.idType2=idType2;
        this.idValue2=idValue2;
        receiveTime=LocalDateTime.now();
        isRead=false;
    }

    @Override
    public int compareTo(Notification o) {
        return this.getReceiveTime().compareTo(o.getReceiveTime());
    }
}
