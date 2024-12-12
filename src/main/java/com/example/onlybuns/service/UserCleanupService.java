package com.example.onlybuns.service;

import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class UserCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(UserCleanupService.class);

    @Autowired
    private UserInfoRepository userRepository;

    @Value("${user.cleanup.cutoff-days:30}")
    private int cutoffDays;

    @Value("${user.cleanup.cron:0 59 23 L * ?}")
    private String cronExpression;

    @Value("${user.cleanup.timezone:Europe/Belgrade}")
    private String timeZone;

    @Scheduled(cron = "${user.cleanup.cron}", zone = "${user.cleanup.timezone}")
    @Transactional
    public void deleteUnactivatedUsers() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
        LocalDateTime cutoffDate = now.minusDays(cutoffDays);

        List<UserInfo> usersToDelete = userRepository.findByIsActiveFalseAndRegistrationDateBefore(cutoffDate);
        if (!usersToDelete.isEmpty()) {
            userRepository.deleteAll(usersToDelete);
            logger.info("Deleted {} unactivated user(s) registered before {}.", usersToDelete.size(), cutoffDate);
        } else {
            logger.info("No unactivated users found for deletion.");
        }
    }
}
