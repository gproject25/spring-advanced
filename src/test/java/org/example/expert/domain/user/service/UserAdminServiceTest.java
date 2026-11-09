package org.example.expert.domain.user.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.example.expert.data.UserFixture;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserAdminService userAdminService;

	@Test
	@DisplayName("사용자의 권한 변경 성공시 UserResponse DTO 반환")
	public void changeUserRole_Success(){
		//given
		long userId = 1L;
		User user = UserFixture.createUserAdminRole();
		UserRoleChangeRequest request = new UserRoleChangeRequest("USER");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		//when
		UserResponse response = userAdminService.changeUserRole(userId, request);

		//then
		assertThat(response).isNotNull();
		assertThat(response.getUserRole()).isEqualTo(UserRole.USER);
		assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
		assertThat(response.getId()).isEqualTo(user.getId());
	}

	@Test
	@DisplayName("존재하지 않는 사용자의 권한 변경 시 예외 발생")
	void changeUserRole_UserNotFound_Fail() {

		// given
		long userId = 1L;
		UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userAdminService.changeUserRole(userId, request))
			.isInstanceOf(InvalidRequestException.class);

		verify(userRepository).findById(userId);
	}

}