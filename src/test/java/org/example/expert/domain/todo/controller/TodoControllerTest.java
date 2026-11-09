package org.example.expert.domain.todo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.client.WeatherClient;
import org.example.expert.common.JwtUtil;
import org.example.expert.data.TodoFixture;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

//통합 테스트
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class TodoControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TodoRepository todoRepository;

	@MockBean
	private WeatherClient weatherClient;

	private User user;
	private String token;
	private Todo todo1;

	@BeforeEach
	void setUp() {
		user = UserFixture.createUser();
		userRepository.save(user);
		token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
		todo1 = TodoFixture.createTodoWithtoutId(user);
		todoRepository.save(todo1);
	}

	@Test
	@DisplayName("POST /todos - Todo 저장 성공")
	void saveTodo_통합테스트_success() throws Exception {

		// given
		String requestBody = """
                {
                    "title": "테스트 제목",
                    "contents": "테스트 내용"
                }
                """;

		when(weatherClient.getTodayWeather()).thenReturn("Sunny");

		// when & then
		mockMvc.perform(post("/todos")
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("테스트 제목"))
			.andExpect(jsonPath("$.contents").value("테스트 내용"))
			.andExpect(jsonPath("$.weather").value("Sunny"));
	}

	@Test
	@DisplayName("GET /todos - Todo 목록 조회 성공")
	void getTodos_통합테스트_success() throws Exception {

		// given

		// when & then
		mockMvc.perform(get("/todos")
			.header("Authorization", token)
			.param("page", "1")
			.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].title").value("title"))
			.andExpect(jsonPath("$.content[0].contents").value("contents"))
			.andExpect(jsonPath("$.content[0].weather").value("Sunny"));
	}

	@Test
	@DisplayName("GET /todos/{todoId} - Todo 단건 조회 성공")
	void getTodo_통합테스트_success() throws Exception {

		// given

		// when & then
		mockMvc.perform(get("/todos/{todoId}", todo1.getId())
			.header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(todo1.getId()))
			.andExpect(jsonPath("$.title").value("title"))
			.andExpect(jsonPath("$.contents").value("contents"))
			.andExpect(jsonPath("$.weather").value("Sunny"))
			.andExpect(jsonPath("$.user.email").value(user.getEmail()));
	}

	@Test
	@DisplayName("GET /todos/{todoId} - 존재하지 않는 Todo 조회 실패")
	void getTodo_통합테스트_fail() throws Exception {

		// given
		long todoId = 99L;

		// when & then
		mockMvc.perform(get("/todos/{todoId}", todoId)
			.header("Authorization", token))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Todo not found"));
	}

}