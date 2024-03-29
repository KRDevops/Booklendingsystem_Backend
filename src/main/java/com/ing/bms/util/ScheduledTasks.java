package com.ing.bms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ing.bms.service.SchedulerService;

@Component
public class ScheduledTasks {
	private static Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
	@Autowired
	SchedulerService schedulerService;

	@Scheduled(cron = "0 */1 * ? * *")
	public void run() {
		logger.info("Executing Scheduler");
		schedulerService.updateStatus();

	}
}
