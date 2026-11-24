package com.example.rag;

import com.example.rag.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionMemoryTests {
    @Autowired MockMvc mvc;

    @Test
    void registrationListAndValidationWithoutDatabase() throws Exception {
        mvc.perform(get("/api/questions")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\" 도서관 질문 \"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("도서관 질문"));
        mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("질문을 입력해 주세요."));
        mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + "가".repeat(201) + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        mvc.perform(get("/api/questions")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put("/api/questions/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"수정\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/")).andExpect(status().isNotFound());
    }

    @Test
    void memoryBelongsToOneServerInstance() {
        var first = new QuestionService();
        first.create("첫 질문");
        assertEquals(2, first.create("둘째 질문").getId());
        assertEquals(2, first.findAll().size());
        assertTrue(new QuestionService().findAll().isEmpty());
    }
}
