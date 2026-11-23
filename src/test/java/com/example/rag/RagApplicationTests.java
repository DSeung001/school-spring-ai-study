package com.example.rag;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.rag.index-on-startup=false")
class RagApplicationTests {

	@Test
	void contextLoads() {
	}

}
