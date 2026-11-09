package org.example.expert.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.example.expert.common.JwtUtil;
import org.example.expert.common.PasswordEncoder;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("회원가입 성공 시 SignupReponse DTO 반환")
	public void signup_회원가입성공_success(){
		//given
		SignupRequest request = new SignupRequest("test@test.com", "Password1234", "USER");

		when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		//when
		SignupResponse response = authService.signup(request);

		//then
		assertThat(response.getEmail()).isEqualTo("test@test.com");
		assertThat(response.getMessage()).isEqualTo("가입 성공");
	}

	@Test
	@DisplayName("이미 존재하는 이메일로 회원가입 시도 시 실패")
	void signup_중복이메일_fail() {

		// given
		SignupRequest request = new SignupRequest("test@test.com", "Password1234", "USER");

		when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> authService.signup(request)).isInstanceOf(InvalidRequestException.class).hasMessage("이미 존재하는 이메일입니다.");
	}

	@Test
	@DisplayName("로그인 성공")
	void signin_로그인성공_success() {

		// given
		SigninRequest request = new SigninRequest("test@test.com", "Password1234");
		User user = UserFixture.createUser();

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
		when(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).thenReturn("testToken");

		// when
		SigninResponse response = authService.signin(request);

		// then
		assertThat(response.getBearerToken()).isEqualTo("testToken");
		verify(userRepository).findByEmail(request.getEmail());
	}



}