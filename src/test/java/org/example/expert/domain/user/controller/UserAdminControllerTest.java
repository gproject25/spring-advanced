package org.example.expert.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.common.JwtUtil;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
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
class UserAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	private String token;

	@Test
	@DisplayName("PATCH /admin/users/{userId} - UserRole 변경 성공")
	void changeUserRole_통합테스트_success() throws Exception {
		//given

		//관리자 토큰 생성
		User user = UserFixture.createUserAdminRole();
		userRepository.save(user);
		token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

		String requestBody =
			"""
				{
					"role" : "USER"
				}	
			""";

		User targetUser = UserFixture.createUser();
		userRepository.save(targetUser);

		//when & then
		mockMvc.perform(patch("/admin/users/{userId}", targetUser.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", token)
			.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userRole").value("USER"))
			.andExpect(jsonPath("$.id").value(targetUser.getId()));
	}

	@Test
	@DisplayName("PATCH /admin/users/{userId} - 일반 사용자 접근시 UserRole 변경 실패")
	void changeUserRole_통합테스트_fail() throws Exception {
		//given

		// 일반 사용자 토큰 생성
		User normalUser = UserFixture.createUser();
		userRepository.save(normalUser);
		String userToken = jwtUtil.createToken(normalUser.getId(), normalUser.getEmail(), normalUser.getUserRole());

		String requestBody =
			"""
				{
					"role" : "ADMIN"
				}	
			""";

		User targetUser = UserFixture.createUser2();
		userRepository.save(targetUser);

		//when & then
		mockMvc.perform(patch("/admin/users/{userId}", targetUser.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", userToken)
			.content(requestBody))
			.andExpect(status().isUnauthorized());
	}


}