package com.tripgoapi.application.port.out;

public interface LoginAttemptLimiterPort {

    boolean isBlocked(String email);

    void onLoginFailed(String email);

    void onLoginSucceeded(String email);
}
