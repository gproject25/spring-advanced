package org.example.expert.domain.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.common.PasswordEncoder;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("회원가입 성공")
	void signup_회원가입통합테스트_success() throws Exception {
		//given
		String request = """
                {
                    "email": "test@test.com",
                    "password": "Password1234",
                    "userRole": "USER"
                }
                """;

		//when & then
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("test@test.com"))
			.andExpect(jsonPath("$.message").value("가입 성공"));
	}

	@Test
	@DisplayName("이미 존재하는 이메일로 회원가입 시도 시 실패")
	void signup_중복이메일_fail() throws Exception {
		//given
		User user = UserFixture.createUser();
		user.changePassword(passwordEncoder.encode("Password1234"));
		userRepository.save(user);

		String request = """
                {
                    "email": "user@test.com",
                    "password": "Password1234",
                    "userRole": "USER"
                }
                """;

		//when & then
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("로그인 성공")
	void signin_로그인통합테스트_success() throws Exception {
		//given
		User user = UserFixture.createUser();
		user.changePassword(passwordEncoder.encode("Password1234"));
		userRepository.save(user);

		String request = """
                {
                    "email": "user@test.com",
                    "password": "Password1234"
                }
                """;

		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bearerToken").exists());
	}

	@Test
	@DisplayName("비밀번호 불일치 시 로그인 실패")
	void signin_wrongPassword_fail() throws Exception {
		User user = UserFixture.createUser();
		user.changePassword(passwordEncoder.encode("Password1234"));
		userRepository.save(user);

		String request = """
                {
                    "email": "user@test.com",
                    "password": "wrongPassword"
                }
                """;

		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isBadRequest());
	}

}