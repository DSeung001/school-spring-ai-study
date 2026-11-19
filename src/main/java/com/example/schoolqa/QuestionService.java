package com.example.schoolqa;

import java.util.List;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuestionService {
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

    public synchronized Question update(long id, String content) {
        requireQuestion(id);
        Question updated = makeQuestion(id, content.strip());
        questions.put(id, updated);
        return updated;
    }
    public synchronized void delete(long id) {
        requireQuestion(id);
        questions.remove(id);
    }
    private void requireQuestion(long id) {
        if (!questions.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다.");
        }
    }

    private Question makeQuestion(long id, String content) {
        return new Question(id, content, "질문을 받았습니다. AI는 3일차에 연결합니다.");
    }
}
