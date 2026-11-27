package com.example.rag.dto;

import com.example.rag.model.Source;

// questions LEFT JOIN question_sources 결과의 한 행.
// 근거가 없는 질문은 근거 컬럼이 NULL이므로 Integer, Double처럼 null을 담을 수 있는 타입을 씀.
public record QuestionSourceRow(long questionId, String content, String answer,
        Integer sourceOrder, String title, String chunkId, String sourceText, Double score) {

    public boolean hasSource() { return sourceOrder != null; }

    public Source toSource() { return new Source(title, chunkId, sourceText, score); }
}
