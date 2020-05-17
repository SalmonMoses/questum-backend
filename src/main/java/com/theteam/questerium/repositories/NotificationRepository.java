package com.theteam.questerium.repositories;

import com.theteam.questerium.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	@Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.userType = :userType AND n.isRead = false " +
			"ORDER BY n.createdAt DESC")
	List<Notification> findAllUnreadForUser(long userId, String userType);

	@Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.userType = :userType AND n.isSent = false  " +
			"ORDER BY n.createdAt DESC")
	List<Notification> findAllUnsentForUser(long userId, String userType);

	@Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.userType = :userType ORDER BY n.createdAt " +
			"DESC")
	List<Notification> findAllForUser(long userId, String userType);

	@Query(value = "DELETE FROM Notification n WHERE n.createdAt < :before AND n.isRead = true AND n.isSent = true")
	@Modifying
	@Transactional
	int deleteNotificationsCreatedBeforeNDays(Timestamp before);
}
