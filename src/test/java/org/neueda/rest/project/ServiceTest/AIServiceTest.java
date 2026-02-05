package org.neueda.rest.project.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neueda.rest.project.service.AIService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AIServiceTest {

	private AIService aiService;
	private RestTemplate mockRestTemplate;

	@BeforeEach
	public void setUp() throws Exception {
		aiService = new AIService();
		mockRestTemplate = mock(RestTemplate.class);

		// inject mocked RestTemplate into private final field
		Field rtField = AIService.class.getDeclaredField("restTemplate");
		rtField.setAccessible(true);
		rtField.set(aiService, mockRestTemplate);
	}

	@Test
	public void explain_returnsOriginal_whenApiKeyBlank() {
		// apiKey is null by default on plain instantiation -> treated as blank
		String input = "Original text";

		String out = aiService.explain(input);

		assertEquals(input, out);
		verifyNoInteractions(mockRestTemplate);
	}

	@Test
	public void explain_returnsAiContent_whenApiResponds() throws Exception {
		// set apiKey to enable AI path
		Field apiKeyField = AIService.class.getDeclaredField("apiKey");
		apiKeyField.setAccessible(true);
		apiKeyField.set(aiService, "dummy-key");

		// prepare mocked AI response structure
		Map<String, Object> message = Map.of("content", "AI rewritten content");
		Map<String, Object> choice = Map.of("message", message);
		List<Object> choices = List.of(choice);
		Map<String, Object> body = Map.of("choices", choices);

		when(mockRestTemplate.postForEntity(anyString(), any(), eq(Map.class)))
				.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		String input = "Some original response";
		String out = aiService.explain(input);

		assertEquals("AI rewritten content", out);
		verify(mockRestTemplate).postForEntity(anyString(), any(), eq(Map.class));
	}

	@Test
	public void explain_returnsOriginal_whenRestThrows() throws Exception {
		Field apiKeyField = AIService.class.getDeclaredField("apiKey");
		apiKeyField.setAccessible(true);
		apiKeyField.set(aiService, "dummy-key");

		when(mockRestTemplate.postForEntity(anyString(), any(), eq(Map.class)))
				.thenThrow(new RuntimeException("connection failed"));

		String input = "Keep this";
		String out = aiService.explain(input);

		assertEquals(input, out);
		verify(mockRestTemplate).postForEntity(anyString(), any(), eq(Map.class));
	}
}
