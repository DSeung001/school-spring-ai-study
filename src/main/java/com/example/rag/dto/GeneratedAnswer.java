package com.example.rag.dto;

import com.example.rag.model.Source;

import java.util.List;

public record GeneratedAnswer(String answer, List<Source> sources) {
}
