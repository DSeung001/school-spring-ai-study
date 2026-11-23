package com.example.rag.service;

import java.util.List;
import com.example.rag.model.Question;
import com.example.rag.repository.QuestionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuestionService {
    private final QuestionMapper mapper;
    private final TransactionTemplate transaction;

    public QuestionService(QuestionMapper mapper, TransactionTemplate transaction) {
        this.mapper = mapper;
        this.transaction = transaction;
    }

    public Question create(String content) {
        // 답변 생성이 실패하면 DB 저장을 시작하지 않음.
        Question question = makeQuestion(0, content.strip());
        return transaction.execute(status -> {
            mapper.insert(question); // AUTO_INCREMENT 번호가 question.id에 들어옴
            return question;
        });
    }

    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return mapper.findAll();
    }

    public Question update(long id, String content) {
        if (mapper.findById(id) == null) throw notFound();
        // 외부 모델 호출은 쓰기 트랜잭션 전에 끝냄.
        Question updated = makeQuestion(id, content.strip());
        return transaction.execute(status -> {
            if (mapper.update(updated) == 0) throw notFound();
            return updated;
        });
    }

    public void delete(long id) {
        if (mapper.delete(id) == 0) throw notFound();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다.");
    }

    private Question makeQuestion(long id, String content) {
        return new Question(id, content, "질문을 받았습니다. AI는 3일차에 연결합니다.");
    }
}
