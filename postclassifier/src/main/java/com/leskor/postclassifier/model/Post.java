package com.leskor.postclassifier.model;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("posts")
public record Post(String hash, String title, String topic, String region, @Column("publication_time") long publicationTime) {
}
