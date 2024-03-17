package com.leskor.provider.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leskor.provider.entities.TopPost;

@Repository
public interface TopPostsRepository extends JpaRepository<TopPost, Integer> {
	List<TopPost> findByRegion(String region);

	List<TopPost> findByRegionAndTopic(String region, String topic);
}
