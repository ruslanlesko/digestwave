package com.leskor.provider.repositories;

import com.leskor.provider.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Post, String> {
    List<Post> findAllByOrderByPublicationTimeDesc();
    List<Post> findByTopicOrderByPublicationTimeDesc(String topic);
    List<Post> findByRegionOrderByPublicationTimeDesc(String region);
    List<Post> findByTopicAndRegionOrderByPublicationTimeDesc(String topic, String region);
}
