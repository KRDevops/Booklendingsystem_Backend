package com.ing.bms.util;

import java.util.Calendar;

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
//    @Value("${cif}")
//	private String cif;
//	@Scheduled(fixedRate = 5000)
    
	@Scheduled(cron = "0 */3 * ? * *")
	public void run() {
//		schedulerService.update(Long.valueOf(cif));
		logger.info("Current time is :: " + Calendar.getInstance().getTime());
	}
}