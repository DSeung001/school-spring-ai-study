package com.example.rag;

import java.util.List;

public record GeneratedAnswer(String answer, List<Source> sources) {
}
