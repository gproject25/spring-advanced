package org.example.expert.domain.comment.service;

import org.example.expert.data.AuthUserFixture;
import org.example.expert.data.TodoFixture;
import org.example.expert.data.UserFixture;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
	@DisplayName("존재하지 않는 Todo에 댓글 등록 시 예외가 발생한다.")
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, 1L, request);
        });

        // then
        assertEquals("Todo를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
	@DisplayName("존재하는 Todo에 댓글 등록 시 CommentSaveResponse를 반환한다.")
    public void comment를_정상적으로_등록한다() {
        // given
        AuthUser authUser = AuthUserFixture.createAuthUser();
        User user = User.fromAuthUser(authUser);
        Todo todo = TodoFixture.createTodo(user);

		CommentSaveRequest request = new CommentSaveRequest("contents");
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todo.getId(), request);

        // then
        assertNotNull(result);
    }

	@Test
	@DisplayName("댓글 목록 조회 성공")
	void getComments_success() {

		// given
		long todoId = 1L;

		User user1 = UserFixture.createUser();
		User user2 = UserFixture.createUser2();

		Comment comment1 = new Comment("첫 번째 댓글", user1, null);
		Comment comment2 = new Comment("두 번째 댓글", user2, null);
		ReflectionTestUtils.setField(comment1, "id", 1L);
		ReflectionTestUtils.setField(comment2, "id", 2L);

		List<Comment> comments = List.of(comment1, comment2);

		when(commentRepository.findByTodoIdWithUser(todoId)).thenReturn(comments);

		// when
		List<CommentResponse> result = commentService.getComments(todoId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getContents()).isEqualTo("첫 번째 댓글");
		assertThat(result.get(1).getContents()).isEqualTo("두 번째 댓글");
	}
}
