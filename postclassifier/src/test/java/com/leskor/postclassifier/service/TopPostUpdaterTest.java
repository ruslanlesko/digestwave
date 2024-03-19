package com.leskor.postclassifier.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.postclassifier.db.TopPostRepository;
import com.leskor.postclassifier.model.TopPost;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TopPostUpdaterTest {
    private static final String TOPIC = "TECH";

    @Mock
    TopPostRepository topPostRepository;

    private TopPostUpdater topPostUpdater;

    @BeforeEach
    void setUp() {
        topPostUpdater = new TopPostUpdater(topPostRepository);
    }

    @Test
    void testSaveTopPosts() {
        List<TopPost> topPosts = List.of(
                new TopPost(0, TOPIC, "region_1", "1111", 0),
                new TopPost(0, TOPIC, "region_2", "2222", 1),
                new TopPost(0, TOPIC, "region_2", "0000", 2));

        List<TopPost> existing = List.of(new TopPost(1, TOPIC, "region_2", "5555", 0));

        when(topPostRepository.findAllByTopicAndRegion(TOPIC, "region_1")).thenReturn(List.of());
        when(topPostRepository.findAllByTopicAndRegion(TOPIC, "region_2")).thenReturn(existing);
        doNothing().when(topPostRepository).deleteAll(List.of());
        doNothing().when(topPostRepository).deleteAll(existing);
        when(topPostRepository.saveAll(topPosts)).thenReturn(topPosts);

        topPostUpdater.updateTopPosts(topPosts);

        verify(topPostRepository, times(2)).findAllByTopicAndRegion(anyString(), anyString());
        verify(topPostRepository, times(2)).deleteAll(any());
        verify(topPostRepository, times(1)).saveAll(topPosts);
    }
}