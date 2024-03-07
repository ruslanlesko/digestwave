package com.leskor.postclassifier.db;

import com.leskor.postclassifier.model.TopPost;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopPostRepository extends ListCrudRepository<TopPost, Integer> {
	List<TopPost> findAllByTopicAndRegion(String topic, String region);
}
