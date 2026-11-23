package com.example.rag.controller;

import com.example.rag.dto.SpeechRequest;
import com.example.rag.service.VoiceService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {
    private final VoiceService service;
    public VoiceController(VoiceService service) { this.service = service; }

    @PostMapping(value = "/tts", produces = "audio/wav")
    public ResponseEntity<byte[]> tts(@Valid @RequestBody SpeechRequest request) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("audio/wav"))
                .body(service.speak(request.text()));
    }
}
