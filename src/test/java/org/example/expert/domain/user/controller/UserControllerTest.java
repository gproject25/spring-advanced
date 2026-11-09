package org.example.expert.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.common.JwtUtil;
import org.example.expert.common.PasswordEncoder;
import org.example.expert.data.UserFixture;
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
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	private String token;
	private User user;

	@BeforeEach
	void setUp(){
		user = UserFixture.createUserAdminRole();
		user.changePassword(passwordEncoder.encode("Password1234"));
		userRepository.save(user);
		token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

	}

	@Test
	@DisplayName("GET /users/{userId} - 사용자 조회 성공")
	void getUser_통합테스트_success() throws Exception {
		//given
		User targetUser = UserFixture.createUser();
		userRepository.save(targetUser);

		//when & then
		mockMvc.perform(get("/users/{userId}", targetUser.getId())
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(targetUser.getId()))
			.andExpect(jsonPath("$.email").value(targetUser.getEmail()))
			.andExpect(jsonPath("$.userRole").value(targetUser.getUserRole().name()));
	}

	@Test
	@DisplayName("PUT /users - 비밀번호 변경 성공")
	void changePassword_통합테스트_success() throws Exception {
		//given
		String requestBody = """
        {
            "oldPassword": "Password1234", 
            "newPassword": "NewPassword1234"
        }
        """;

		//when & then
		mockMvc.perform(put("/users")
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
        	.andExpect(status().isOk());
	}

	@Test
	@DisplayName("PUT /users - 기존 비밀번호로 변경 실패")
	void changePassword_통합테스트_fail() throws Exception {

		// given
		String requestBody = """
        {
            "oldPassword": "WrongPassword1234",
            "newPassword": "NewPassword1234"
        }
        """;

		// when & then
		mockMvc.perform(put("/users")
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(requestBody))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("잘못된 비밀번호입니다."));
	}

}