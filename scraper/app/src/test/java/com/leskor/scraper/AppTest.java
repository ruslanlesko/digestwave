package com.leskor.scraper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppTest {
    @Test
    void appStarts() {
        App classUnderTest = new App();
        assertNotNull(classUnderTest, "app should be non-null");
    }
}
