package com.leskor.postclassifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.leskor.postclassifier.exceptions.OllamaUnavailableException;
import com.leskor.postclassifier.model.TopPost;

@WebMvcTest(Controller.class)
class ControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RatingService ratingService;

	@Test
	void testTopPosts_HappyScenario() throws Exception {
		when(ratingService.ratePosts(anyString(), anyString())).thenReturn(List.of());
		when(ratingService.ratePosts("PROGRAMMING", "INT"))
				.thenReturn(List.of(new TopPost(1, "PROGRAMMING", "INT", "3333", 0)));

		mockMvc.perform(post("/top")
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.regionPosts[0].topicPosts[0].topPosts[0].hash").value("3333"));
	}

	@Test
	void testTopPosts_WhenExceptionOccurs() throws Exception {
		doThrow(new OllamaUnavailableException("Service Unavailable"))
				.when(ratingService).ratePosts(anyString(), anyString());

		mockMvc.perform(post("/top")
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isServiceUnavailable());
	}
}