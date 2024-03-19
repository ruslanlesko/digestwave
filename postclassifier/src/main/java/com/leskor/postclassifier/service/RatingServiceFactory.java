package com.leskor.postclassifier.service;

import org.springframework.stereotype.Service;

@Service
public class RatingServiceFactory {
    private final AiRatingService aiRatingService;
    private final SimpleRatingService simpleRatingService;

    public RatingServiceFactory(AiRatingService aiRatingService,
                                SimpleRatingService simpleRatingService) {
        this.aiRatingService = aiRatingService;
        this.simpleRatingService = simpleRatingService;
    }

    public RatingService getRatingService(String region) {
        return "INT".equalsIgnoreCase(region) ? aiRatingService : simpleRatingService;
    }
}
