package com.leskor.postclassifier.db;

import com.leskor.postclassifier.model.Post;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends ListCrudRepository<Post, String> {
	List<Post> findTop20ByTopicAndRegionOrderByPublicationTimeDesc(String topic, String region);
}
