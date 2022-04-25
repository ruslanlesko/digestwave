package com.leskor.provider.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    public String hash;

    @Column(name = "publication_time")
    public long publicationTime;

    @Column(name = "site_code")
    public String siteCode;

    public String title;
    public String content;

    @Column(name = "image_url")
    public String imageURL;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getPublicationTime() {
        return publicationTime;
    }

    public void setPublicationTime(long publicationTime) {
        this.publicationTime = publicationTime;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
