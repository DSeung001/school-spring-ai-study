package com.example.rag.model;

public class Question {
    private long id;
    private String content;
    private String answer;

    // MyBatis가 조회 결과를 담을 때 사용하는 생성자와 setter
    public Question() { }
    public Question(long id, String content, String answer) {
        this.id = id;
        this.content = content;
        this.answer = answer;
    }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
