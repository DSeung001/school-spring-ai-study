package com.example.rag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
class RagApplicationTests {
private final AnswerService answers = mock(AnswerService.class);
private QuestionService service() { when(answers.answer(anyString())).thenReturn("답변");return new QuestionService(answers); }
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
@Test void generationFailurePreservesExistingDataAndId() {
var s=service(); var original=s.create("기존");
when(answers.answer(anyString())).thenThrow(new IllegalStateException("model offline"));
assertThrows(IllegalStateException.class,()->s.update(original.getId(),"변경"));
assertThrows(IllegalStateException.class,()->s.create("신규"));
assertEquals(java.util.List.of(original),s.findAll());
doReturn("복구").when(answers).answer(anyString());
assertEquals(2L,s.create("복구").getId());
}

}
