package org.example.expert.domain.comment.controller;

import static org.assertj.core.api.Assertions.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentAdminControllerTest {
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
	@DisplayName("DELETE /admin/comments/{commentId} - 댓글 삭제 성공")
	void deleteComment_관리자댓글삭제성공_success() throws Exception {

		// given
		long commentId = comment.getId();

		// when & then
		mockMvc.perform(delete("/admin/comments/{commentId}", commentId)
				.header("Authorization", token))
			.andExpect(status().isOk());

		// 삭제되었는지 확인
		assertThat(commentRepository.existsById(commentId)).isFalse();
	}

	@Test
	@DisplayName("DELETE /admin/comments/{commentId} - 일반 사용자가 댓글 삭제 시도 시 실패")
	void deleteComment_사용자댓글삭제_fail() throws Exception {

		// given
		String userToken = jwtUtil.createToken(user2.getId(), user2.getEmail(), user2.getUserRole());
		long commentId = comment.getId();

		// when & then
		mockMvc.perform(delete("/admin/comments/{commentId}", commentId)
				.header("Authorization", userToken))
			.andExpect(status().isUnauthorized());
	}
}