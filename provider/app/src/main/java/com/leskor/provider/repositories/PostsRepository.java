package com.leskor.provider.repositories;

import java.util.List;
import com.leskor.provider.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository extends JpaRepository<Post, String> {
    Page<Post> findAll(Pageable pageable);
    List<Post> findByTopic(String topic, Pageable pageable);
    List<Post> findByRegion(String region, Pageable pageable);
    List<Post> findByTopicAndRegion(String topic, String region, Pageable pageable);
}
