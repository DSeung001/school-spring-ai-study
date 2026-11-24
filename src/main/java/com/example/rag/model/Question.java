package com.example.rag.model;

public class Question {
    private final long id;
    private final String content;
    private final String answer;

    public Question(long id, String content, String answer) {
        this.id = id;
        this.content = content;
        this.answer = answer;
    }
    public long getId() { return id; }
    public String getContent() { return content; }
    public String getAnswer() { return answer; }
}
