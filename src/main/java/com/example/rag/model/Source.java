package com.example.rag.model;

public record Source(String title, String chunkId, String text, double score) {
}
