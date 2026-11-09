package org.example.expert.domain.comment.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.common.JwtUtil;
import org.example.expert.data.TodoFixture;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TodoRepository todoRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private JwtUtil jwtUtil;

	private User user;
	private User user2;
	private Todo todo;
	private Comment comment;
	private String token;

	@BeforeEach
	void setUp() {
		// given
		user = userRepository.save(UserFixture.createUserAdminRole());
		user2 = userRepository.save(UserFixture.createUser2());
		token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
		todo = todoRepository.save(TodoFixture.createTodoWithtoutId(user));
		comment = commentRepository.save(new Comment("테스트 댓글", user, todo));
	}

	@Test
	@DisplayName("POST /todos/{todoId}/comments - 댓글 등록 성공")
	void saveComment_댓글등록_success() throws Exception {

		// given
		String requestBody = """
            {
                "contents": "테스트 댓글입니다."
            }
            """;

		// when & then
		mockMvc.perform(post("/todos/{todoId}/comments", todo.getId())
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.contents").value("테스트 댓글입니다."));
	}

	@Test
	@DisplayName("GET /todos/{todoId}/comments - 댓글 목록 조회 성공")
	void getComments_success() throws Exception {

		// given
		Comment comment = commentRepository.save(new Comment("테스트 댓글입니다.", user, todo));

		// when & then
		mockMvc.perform(get("/todos/{todoId}/comments", todo.getId())
				.header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[1].contents").value("테스트 댓글입니다."));
	}

}