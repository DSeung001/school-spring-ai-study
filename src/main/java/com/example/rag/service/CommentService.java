package com.example.rag.service;

import java.util.List;
import com.example.rag.model.Comment;
import com.example.rag.repository.CommentMapper;
import com.example.rag.repository.QuestionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {
    private final CommentMapper mapper;
    private final QuestionMapper questionMapper;

    public CommentService(CommentMapper mapper, QuestionMapper questionMapper) {
        this.mapper = mapper;
        this.questionMapper = questionMapper;
    }

    @Transactional
    public Comment create(long questionId, String content) {
        checkQuestion(questionId);
        Comment comment = new Comment(questionId, content.strip());
        mapper.insert(comment); // AUTO_INCREMENT 번호가 comment.id에 들어옴
        // 작성 시각은 DB가 정하므로 다시 조회해 응답에 담음.
        return mapper.findById(comment.getId());
    }

    @Transactional(readOnly = true)
    public List<Comment> findByQuestionId(long questionId) {
        checkQuestion(questionId);
        return mapper.findByQuestionId(questionId);
    }

    public void delete(long id) {
        if (mapper.delete(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
        }
    }

    // 없는 질문에 댓글을 달거나 조회하면 404. FK 오류(503)가 나기 전에 먼저 확인함.
    private void checkQuestion(long questionId) {
        if (questionMapper.findById(questionId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다.");
        }
    }
}
