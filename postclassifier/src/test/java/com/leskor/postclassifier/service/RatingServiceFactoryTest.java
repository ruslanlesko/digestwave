package com.leskor.postclassifier.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RatingServiceFactoryTest {
    @Mock
    AiRatingService aiRatingService;

    @Mock
    SimpleRatingService simpleRatingService;

    private RatingServiceFactory ratingServiceFactory;

    @BeforeEach
    void setUp() {
        ratingServiceFactory = new RatingServiceFactory(aiRatingService, simpleRatingService);
    }

    @Test
    void getRatingServiceReturnsAiServiceForIntRegion() {
        assertEquals(aiRatingService, ratingServiceFactory.getRatingService("INT"));
    }

    @Test
    void getRatingServiceReturnsSimpleServiceForAnyOtherRegion() {
        assertEquals(simpleRatingService, ratingServiceFactory.getRatingService("CA"));
    }
}