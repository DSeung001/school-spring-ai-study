package com.example.rag.model;

import java.time.LocalDateTime;

// 작성자 칼럼이 없는 익명 댓글. 어느 질문의 댓글인지는 questionId로 구분함.
public class Comment {
    private long id;
    private long questionId;
    private String content;
    private LocalDateTime createdAt;

    // MyBatis가 조회 결과를 담을 때 사용하는 생성자와 setter
    public Comment() { }
    public Comment(long questionId, String content) {
        this.questionId = questionId;
        this.content = content;
    }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getQuestionId() { return questionId; }
    public void setQuestionId(long questionId) { this.questionId = questionId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
