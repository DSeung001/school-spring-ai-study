package com.example.schoolqa;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoiceService {
    private final RestClient ttsClient;

    public VoiceService(@Value("${app.voice.tts-url}") String ttsUrl) {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(120));
        this.ttsClient = RestClient.builder().baseUrl(ttsUrl).requestFactory(factory).build();
    }
    public byte[] speak(String text) {
        try {
            byte[] audio = ttsClient.post().uri("/tts").contentType(MediaType.APPLICATION_JSON)
                    .body(new SpeechRequest(text)).retrieve().body(byte[].class);
            if (audio == null || audio.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "음성이 비어 있습니다.");
            }
            return audio;
        } catch (RestClientResponseException error) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "TTS 서버가 음성을 만들지 못했습니다. 입력 내용과 서버 로그를 확인해 주세요.");
        } catch (ResourceAccessException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "TTS 서버에 연결하지 못했습니다. 8001 포트 실행 상태를 확인해 주세요.");
        }
    }
}
