package com.project.main.service;

import com.project.main.exception.TooManyRequestsException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class VerificationRateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_EMAIL_SEND_PER_HOUR = 3;
    private static final int MAX_VERIFY_WRONG_ATTEMPTS = 5;
    private static final int MAX_IP_VERIFY_ATTEMPTS = 20;

    private static final Duration EMAIL_TTL = Duration.ofHours(1);
    private static final Duration VERIFY_TTL = Duration.ofHours(1);
    private static final Duration USER_LOCK_TTL = Duration.ofMinutes(15);

    public VerificationRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkCanSendEmail(String ip) {
        String ipKey = "rate:email:ip:" + ip;
        Long count = getCount(ipKey);
        if (count != null && count >= MAX_EMAIL_SEND_PER_HOUR) {
            throw new TooManyRequestsException("Too many email requests. Try again later.");
        }
    }

    public void recordEmailSent(String ip, Long userId) {
        incrementWithTtl("rate:email:ip:" + ip, EMAIL_TTL);
        incrementWithTtl("rate:email:user:" + userId, EMAIL_TTL);
    }


    public void checkCanVerifyCode(String ip, Long userId) {
        String userLockKey = "lock:verify:user:" + userId;
        String userKey = "rate:verify:user:" + userId;
        String ipKey = "rate:verify:ip:" + ip;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(userLockKey))) {
            throw new TooManyRequestsException("Too many failed attempts. Try again later.");
        }

        Long userCount = getCount(userKey);
        if (userCount != null && userCount >= MAX_VERIFY_WRONG_ATTEMPTS) {
            redisTemplate.opsForValue().setIfAbsent(userLockKey, "locked", USER_LOCK_TTL);
            throw new TooManyRequestsException("Too many failed attempts. Try again later.");
        }

        Long ipCount = getCount(ipKey);
        if (ipCount != null && ipCount >= MAX_IP_VERIFY_ATTEMPTS) {
            throw new TooManyRequestsException("Too many verification attempts from your IP. Try again later.");
        }
    }

    public void recordFailedVerifyAttempt(String ip, Long userId) {
        incrementWithTtl("rate:verify:user:" + userId, VERIFY_TTL);
        incrementWithTtl("rate:verify:ip:" + ip, VERIFY_TTL);
    }

    public void clearVerifyAttemptsOnSuccess(Long userId) {
        redisTemplate.delete("rate:verify:user:" + userId);
        redisTemplate.delete("lock:verify:user:" + userId);
    }

    private Long getCount(String key) {
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val) : 0L;
    }

    private void incrementWithTtl(String key, Duration ttl) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttl);
        }
    }
}