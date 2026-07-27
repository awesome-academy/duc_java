package com.tripgoapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// "dev" profile supplies a JWT secret fallback so this doesn't require JWT_SECRET to be
// exported just to load the context locally; base application.properties requires it explicitly.
@SpringBootTest
@ActiveProfiles("dev")
class TripGoApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
