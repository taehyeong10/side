package com.make.side.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "texts", createIndex = true)
public class TextDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long memberId;

    @Field(type = FieldType.Text)
    private String text;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    public TextDocument() {
    }

    public TextDocument(Long memberId, String text) {
        this.memberId = memberId;
        this.text = text;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
