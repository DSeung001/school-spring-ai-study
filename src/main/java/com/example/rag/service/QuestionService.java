package com.example.rag.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.rag.dto.QuestionSourceRow;
import com.example.rag.model.Question;
import com.example.rag.model.Source;
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
        return toQuestions(mapper.findAllWithSources());
    }

    @Transactional(readOnly = true)
    public List<Question> search(String keyword) {
        // 검색어가 없거나 공백뿐이면 전체 목록
        if (keyword == null || keyword.isBlank()) return findAll();
        return toQuestions(mapper.searchWithSources(likePattern(keyword.strip())));
    }

    @Transactional(readOnly = true)
    public Question findById(long id) {
        List<Question> found = toQuestions(mapper.findByIdWithSources(id));
        if (found.isEmpty()) throw notFound();
        return found.getFirst();
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

    // JOIN 결과는 근거 개수만큼 같은 질문이 반복되므로 질문 번호별로 하나로 묶음.
    private List<Question> toQuestions(List<QuestionSourceRow> rows) {
        Map<Long, Question> questions = new LinkedHashMap<>(); // 질문 번호 순서 유지
        Map<Long, List<Source>> sources = new HashMap<>();
        for (QuestionSourceRow row : rows) {
            if (!questions.containsKey(row.questionId())) {
                questions.put(row.questionId(), new Question(row.questionId(), row.content(), row.answer()));
                sources.put(row.questionId(), new ArrayList<>());
            }
            if (row.hasSource()) { // 근거 없는 질문은 근거 컬럼이 NULL인 행 하나
                sources.get(row.questionId()).add(row.toSource());
            }
        }
        for (Question question : questions.values()) {
            question.setSources(sources.get(question.getId()));
        }
        return new ArrayList<>(questions.values());
    }

    // LIKE의 %, _는 '아무 글자'라는 뜻이므로 검색어에 든 %, _는 앞에 \를 붙여 글자 그대로 찾음.
    private String likePattern(String keyword) {
        String escaped = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private Question makeQuestion(long id, String content) {
        GeneratedAnswer generated = answerService.answer(content);
        return new Question(id, content, generated.answer(), generated.sources());
    }
}
