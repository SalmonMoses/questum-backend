package com.theteam.questerium.repositories;

import com.theteam.questerium.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	@Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.userType = :userType AND n.isRead = false")
	List<Notification> findAllUnreadForUser(long userId, String userType);

	@Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.userType = :userType AND n.isSent = false")
	List<Notification> findAllUnsentForUser(long userId, String userType);
}
