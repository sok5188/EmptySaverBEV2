package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}
