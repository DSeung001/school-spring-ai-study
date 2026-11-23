package com.example.rag.service;

import java.util.List;
import com.example.rag.model.Question;
import com.example.rag.repository.QuestionMapper;
import com.example.rag.dto.GeneratedAnswer;
import com.example.rag.repository.SourceMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuestionService {
    private final QuestionMapper mapper;
    private final TransactionTemplate transaction;
    private final AnswerService answerService;
    private final SourceMapper sourceMapper;

    public QuestionService(QuestionMapper mapper, TransactionTemplate transaction, AnswerService answerService, SourceMapper sourceMapper) {
        this.mapper = mapper;
        this.transaction = transaction;
        this.answerService = answerService;
        this.sourceMapper = sourceMapper;
    }

    public Question create(String content) {
        // 답변 생성이 실패하면 DB 저장을 시작하지 않음.
        Question question = makeQuestion(0, content.strip());
        return transaction.execute(status -> {
            mapper.insert(question); // AUTO_INCREMENT 번호가 question.id에 들어옴
            saveSources(question);
            return question;
        });
    }

    @Transactional(readOnly = true)
    public List<Question> findAll() {
        List<Question> questions = mapper.findAll();
        for (Question question : questions) {
            question.setSources(sourceMapper.findByQuestionId(question.getId()));
        }
        return questions;
    }

    public Question update(long id, String content) {
        if (mapper.findById(id) == null) throw notFound();
        // 외부 모델 호출은 쓰기 트랜잭션 전에 끝냄.
        Question updated = makeQuestion(id, content.strip());
        return transaction.execute(status -> {
            if (mapper.update(updated) == 0) throw notFound();
            sourceMapper.deleteByQuestionId(id);
            saveSources(updated);
            return updated;
        });
    }

    public void delete(long id) {
        if (mapper.delete(id) == 0) throw notFound();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다.");
    }

    private void saveSources(Question question) {
        for (int i = 0; i < question.getSources().size(); i++) {
            sourceMapper.insert(question.getId(), i, question.getSources().get(i));
        }
    }

    private Question makeQuestion(long id, String content) {
        GeneratedAnswer generated = answerService.answer(content);
        return new Question(id, content, generated.answer(), generated.sources());
    }
}
