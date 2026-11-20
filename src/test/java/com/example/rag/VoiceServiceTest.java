package com.example.rag;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class VoiceServiceTest {
    @Test void sendsTextAndReturnsWavBytes() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] wav = "RIFF-test-WAVE".getBytes(StandardCharsets.UTF_8);
        var received = new java.util.concurrent.atomic.AtomicReference<String>();
        server.createContext("/tts", exchange -> {
            received.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.getResponseHeaders().set("Content-Type", "audio/wav");
            exchange.sendResponseHeaders(200, wav.length);
            exchange.getResponseBody().write(wav);
            exchange.close();
        });
        server.start();
        try {
            var service = new VoiceService("http://127.0.0.1:" + server.getAddress().getPort());
            assertArrayEquals(wav, service.speak("안녕하세요"));
            assertTrue(received.get().contains("안녕하세요"));
        } finally { server.stop(0); }
    }
    @Test void upstreamErrorBecomesBadGateway() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tts", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();
        try {
            var service = new VoiceService("http://127.0.0.1:" + server.getAddress().getPort());
            var error = assertThrows(ResponseStatusException.class, () -> service.speak("테스트"));
            assertEquals(502, error.getStatusCode().value());
        } finally { server.stop(0); }
    }
}
