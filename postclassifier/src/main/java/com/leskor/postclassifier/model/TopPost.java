package com.leskor.postclassifier.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("top_posts")
public record TopPost(@Id int id, String topic, String region, String hash, int rating) {
}
