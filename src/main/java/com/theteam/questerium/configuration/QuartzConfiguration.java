package com.theteam.questerium.configuration;

import com.theteam.questerium.quartz.QuartzUtils;
import com.theteam.questerium.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class QuartzConfiguration {
	private final long DAY_IN_MILLISECONDS = TimeUnit.DAYS.toMillis(1);

	@Bean
	public SimpleTriggerFactoryBean deleteNotificationsJobFactory(@Qualifier("deleteNotificationsJob") JobDetail jobDetail) {
		return QuartzUtils.createIntervalTrigger("deleteNotificationsJobTrigger",
		                                         0L,
		                                         DAY_IN_MILLISECONDS,
		                                         jobDetail);
	}

	@Bean
	@Qualifier("deleteNotificationsJob")
	public JobDetailFactoryBean deleteNotificationsJob() {
		return QuartzUtils.createJobDetail("deleteNotificationsJob", DeleteNotificationsJob.class);
	}

	public static class DeleteNotificationsJob implements Job {
		@Autowired
		NotificationService notificationService;

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			log.info("Executing DeleteNotificationsJob");
			int deletedNotificationsNumber = notificationService.deleteNotificationsCreatedBefore30Days();
			log.info("DeleteNotificationsJob deleted {} notifications", deletedNotificationsNumber);
		}
	}
}
