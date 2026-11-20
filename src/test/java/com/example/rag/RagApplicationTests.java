package com.example.rag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
class RagApplicationTests {

private QuestionService service() { return new QuestionService(); }
@Test void crudAndValidation() throws Exception {
var s=service(); var mvc=MockMvcBuilders.standaloneSetup(new QuestionController(s)).setControllerAdvice(new ApiErrorHandler()).build();
mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"   \"}")).andExpect(status().isBadRequest());
assertTrue(s.findAll().isEmpty());
mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\" 질문 \"}")).andExpect(status().isCreated());
assertEquals("질문",s.findAll().getFirst().getContent());
mvc.perform(put("/api/questions/1").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"수정\"}")).andExpect(status().isOk());
assertEquals("수정",s.findAll().getFirst().getContent());
mvc.perform(delete("/api/questions/1")).andExpect(status().isNoContent()).andExpect(content().string(""));
mvc.perform(delete("/api/questions/1")).andExpect(status().isNotFound());
}


}
