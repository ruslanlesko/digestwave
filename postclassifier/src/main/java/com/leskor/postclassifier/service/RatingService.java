package com.leskor.postclassifier.service;

import com.leskor.postclassifier.model.TopPost;
import java.util.List;

public interface RatingService {
    List<TopPost> ratePosts(String topic, String region);
}
