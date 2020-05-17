package com.theteam.questerium.quartz;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.util.Calendar;

public class QuartzUtils {
	public static SimpleTriggerFactoryBean createIntervalTrigger(
			String name,
			long startDelayMs,
			long intervalMs,
			JobDetail jobDetail
	) {
		SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setName(name);
		trigger.setStartDelay(startDelayMs);
		trigger.setRepeatInterval(intervalMs);
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		trigger.setJobDetail(jobDetail);
		trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
		return trigger;
	}

	public static CronTriggerFactoryBean createCronTrigger(
			String name,
			String cronExpr,
			JobDetail jobDetail
	) {
		CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
		trigger.setName(name);
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		trigger.setStartTime(calendar.getTime());
		trigger.setStartDelay(0L);
		trigger.setCronExpression(cronExpr);
		trigger.setJobDetail(jobDetail);
		trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
		return trigger;
	}

	public static JobDetailFactoryBean createJobDetail(String name, Class<? extends Job> jobClass) {
		JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
		jobDetail.setName(name);
		jobDetail.setJobClass(jobClass);
		jobDetail.setDurability(true);
		return jobDetail;
	}
}
