package tech.zerofiltre.blog.domain.article.model;

import tech.zerofiltre.blog.domain.user.model.*;

import java.time.*;
import java.util.*;

public class Article {
    private long id;
    private String title;
    private String thumbnail;
    private String content;
    private User author;
    private LocalDateTime publishedAt;
    private List<Reaction> reactions;
    private Status status;
    private List<Tag> tags;

    public Article() {
    }

    public Article(long id, String title, String thumbnail, String content, User author, LocalDateTime publishedAt, List<Reaction> reactions, Status status, List<Tag> tags) {
        this.id = id;
        this.title = title;
        this.thumbnail = thumbnail;
        this.content = content;
        this.author = author;
        this.publishedAt = publishedAt;
        this.reactions = reactions;
        this.status = status;
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
