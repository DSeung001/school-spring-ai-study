package com.example.schoolqa;

public class Question {
    private final long id;
    private final String content;
    private final String answer;
    private final java.util.List<Source> sources;

    public Question(long id, String content, String answer) {
        this(id, content, answer, java.util.List.of());
    }
    public Question(long id, String content, String answer, java.util.List<Source> sources) {
        this.id = id;
        this.content = content;
        this.answer = answer;
        this.sources = java.util.List.copyOf(sources);
    }
    public long getId() { return id; }
    public String getContent() { return content; }
    public String getAnswer() { return answer; }
    public java.util.List<Source> getSources() { return sources; }
}
