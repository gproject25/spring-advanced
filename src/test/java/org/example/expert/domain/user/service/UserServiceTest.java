package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.example.expert.common.PasswordEncoder;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	private Long userId;
	private User user;

	@BeforeEach
	public void setUp() {
		userId = 1L;
		user = UserFixture.createUser();

	}

	@Test
	@DisplayName("특정 상요자 조회 성공 시 UserResponse Dto 반환")
	public void getUser_응답DTO반환_success() {
		//given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		//when
		UserResponse response = userService.getUser(userId);

		//then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(user.getId());
		assertThat(response.getEmail()).isEqualTo(user.getEmail());
		assertThat(response.getUserRole()).isEqualTo(user.getUserRole());
		verify(userRepository).findById(userId);
	}

	@Test
	@DisplayName("기존 비밀번호 확인 후 새로운 비밀번호 변경 성공")
	public void changePassword_AllCasePass_success(){
		//given
		UserChangePasswordRequest request = new UserChangePasswordRequest("Password1234", "NewPassword1234");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Password1234", user.getPassword())).thenReturn(true);
		when(passwordEncoder.matches("NewPassword1234", user.getPassword())).thenReturn(false);
		when(passwordEncoder.encode("NewPassword1234")).thenReturn("encodedNewPassword");

		//when
		userService.changePassword(userId, request);

		//then
		assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
	}

	@Test
	@DisplayName("새 비밀번호가 기존 비밀번호와 같으면 예외 발생")
	public void changePassword_기존비밀번호동일_fail() {
		// given
		UserChangePasswordRequest request = new UserChangePasswordRequest("Password1234", "Password1234");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Password1234", user.getPassword())).thenReturn(true);

		//when & then
		assertThatThrownBy(() -> userService.changePassword(userId, request))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
	}

	@Test
	@DisplayName("기존 비밀번호가 일치하지 않으면 예외 발생")
	public void changePassword_틀린비밀번호_fail(){
		//given
		UserChangePasswordRequest request = new UserChangePasswordRequest("WrongPassword", "Password1234");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("WrongPassword", user.getPassword())).thenReturn(false);

		//when & then
		assertThatThrownBy(() -> userService.changePassword(userId, request))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("잘못된 비밀번호입니다.");
	}
}