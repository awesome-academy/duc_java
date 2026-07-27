package com.tripgoapi.application.port.out;

public interface LoginAttemptLimiterPort {

    boolean isBlocked(String email, String ipAddress);

    void onLoginFailed(String email, String ipAddress);

    void onLoginSucceeded(String email, String ipAddress);
}
