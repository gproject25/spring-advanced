package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.example.expert.client.WeatherClient;
import org.example.expert.data.AuthUserFixture;
import org.example.expert.data.TodoFixture;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

	@Mock
	private TodoRepository todoRepository;

	@Mock
	private WeatherClient weatherClient;

	@InjectMocks
	private TodoService todoService;

	private User user;
	private AuthUser authUser;
	private Todo todo1;
	private Todo todo2;

	@BeforeEach
	public void setUp() {
		user = UserFixture.createUser();
		authUser = AuthUserFixture.createAuthUserGivenUser(user);
		todo1 = TodoFixture.createTodo(user);
		todo2 = TodoFixture.createTodo2(user);
	}

	@Test
	@DisplayName("Todo 저장 성공 시 Todo DTO 반환")
	void saveTodo_저장성공_success(){
		//given
		TodoSaveRequest request = new TodoSaveRequest("테스트 제목", "테스트 내용");

		when(weatherClient.getTodayWeather()).thenReturn("Cloudy");
		when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		//when
		TodoSaveResponse response = todoService.saveTodo(authUser, request);

		//then
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("테스트 제목");
		assertThat(response.getContents()).isEqualTo("테스트 내용");
		assertThat(response.getWeather()).isEqualTo("Cloudy");
		assertThat(response.getUser().getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	@DisplayName("Todo 목록 조회 성공 시 TodoResponse Page 반환")
	void getTodos_Page조회_success(){
		//given
		Pageable pageable = PageRequest.of(0, 10);
		Page<Todo> todoPage = new PageImpl<>(List.of(todo1, todo2), pageable, 2);

		when(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).thenReturn(todoPage);

		//when
		Page<TodoResponse> response = todoService.getTodos(1, 10);

		//then
		assertThat(response).isNotNull();
		assertThat(response.getTotalElements()).isEqualTo(2);
		assertThat(response.getContent()).hasSize(2);
		assertThat(response.getContent().get(0).getTitle()).isEqualTo("title");
		assertThat(response.getContent().get(1).getTitle()).isEqualTo("title2");
	}

	@Test
	@DisplayName("Todo 단 건 조회 성공 시 TodoResponse DTO 반환")
	void getTodo_단건조회_success(){
		//given
		when(todoRepository.findByIdWithUser(todo1.getId())).thenReturn(Optional.of(todo1));

		//when
		TodoResponse response = todoService.getTodo(todo1.getId());

		//then
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("title");
		assertThat(response.getContents()).isEqualTo("contents");
	}

	@Test
	@DisplayName("존재하지 않는 Todo 조회 시 예외 발생")
	void getTodo_단건조회_fail(){
		//given
		Long id = 99L;

		//when
		when(todoRepository.findByIdWithUser(id)).thenReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> todoService.getTodo(id))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("Todo not found");
	}

}