package org.example.expert.domain.manager.controller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.common.JwtUtil;
import org.example.expert.data.TodoFixture;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
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

//통합 테스트
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ManagerControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private ManagerRepository managerRepository;

	@Autowired
	private TodoRepository todoRepository;

	@Autowired
	private UserRepository userRepository;

	private User user;
	private User user2;
	private Manager manager;
	private Todo todo1;
	private String token;

	@BeforeEach
	void setUp() {
		user = UserFixture.createUser();
		user2 = UserFixture.createUser2();
		userRepository.save(user);
		userRepository.save(user2);

		token = jwtUtil.createToken(user2.getId(), user2.getEmail(), user2.getUserRole());
		todo1 = TodoFixture.createTodoWithtoutId(user2);
		todoRepository.save(todo1);

		manager = new Manager(user, todo1);
		managerRepository.save(manager);
	}

	@Test
	@DisplayName("POST /todos/{todoId}/managers - 담당자 등록 성공")
	void saveManager_Manager등록_success() throws Exception {
		//given
		String requestBody = """
                {
                    "managerUserId" : %d
                }
                """.formatted(user.getId());

		//when & then
		mockMvc.perform(post("/todos/{todoId}/managers", todo1.getId())
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.user.id").value(user.getId()));

	}

	@Test
	@DisplayName("POST /todos/{todoId}/managers - 작성자가 아닌 사용자의 등록 실패")
	void saveManager_forbidden_fail() throws Exception {

		// given
		String otherToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

		String requestBody = """
            {
                "managerUserId": %d
            }
            """.formatted(user2.getId());

		// when & then
		mockMvc.perform(post("/todos/{todoId}/managers", todo1.getId())
				.header("Authorization", otherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("일정을 생성한 유저만 담당자를 지정할 수 있습니다."));
	}

	@Test
	@DisplayName("GET /todos/{todoId}/managers - 담당자 목록 조회 성공")
	void getManagers_Page조회_success() throws Exception {

		// given

		// when & then
		mockMvc.perform(get("/todos/{todoId}/managers", todo1.getId())
				.header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	@DisplayName("DELETE /todos/{todoId}/managers/{managerId} - 담당자 삭제 성공")
	void deleteManager_success() throws Exception {

		// given

		// when & then
		mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todo1.getId(), manager.getId())
				.header("Authorization", token))
			.andExpect(status().isOk());

		// 실제로 삭제되었는지 확인
		assertThat(managerRepository.findById(manager.getId())).isEmpty();
	}

	@Test
	@DisplayName("DELETE /todos/{todoId}/managers/{managerId} - 작성자가 아닌 사용자의 삭제 실패")
	void deleteManager_forbidden() throws Exception {

		// given
		String otherToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

		// when & then
		mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todo1.getId(), manager.getId())
				.header("Authorization", otherToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message")
				.value("해당 일정을 만든 유저가 유효하지 않습니다."));
	}

}