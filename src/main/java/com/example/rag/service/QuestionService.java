package com.example.rag.service;

import com.example.rag.model.Question;

import java.util.List;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// Spring이 이 객체를 하나만 만들어 모든 요청이 함께 씀 → 아래 Map도 공유되므로 synchronized를 붙임
@Service
public class QuestionService {
    // 서버 메모리에만 있음. 재시작하면 비어 있음
    private final TreeMap<Long, Question> questions = new TreeMap<>();
    private long nextId = 1;

    public synchronized Question create(String content) {
        Question question = makeQuestion(nextId, content.strip());
        questions.put(nextId, question);
        nextId++;
        return question;
    }
    public synchronized List<Question> findAll() {
        return List.copyOf(questions.values());
    }

    private Question makeQuestion(long id, String content) {
        return new Question(id, content, "질문을 받았습니다. AI는 3일차에 연결합니다.");
    }
}
