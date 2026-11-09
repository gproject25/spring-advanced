package org.example.expert.domain.comment.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

	@Mock
	private CommentRepository commentRepository;

	@InjectMocks
	private CommentAdminService commentAdminService;

	@Test
	@DisplayName("관리자 권한으로 댓글 삭제 성공")
	void deleteComment_관리자댓글삭제_success() {
		// given
		long commentId = 1L;
		when(commentRepository.existsById(commentId)).thenReturn(true);

		// when
		commentAdminService.deleteComment(commentId);

		// then
		verify(commentRepository).existsById(commentId);
		verify(commentRepository).deleteById(commentId);
	}

	@Test
	@DisplayName("존재하지 않는 댓글 삭제 실패")
	void deleteComment_notFound_fail() {

		// given
		long commentId = 1L;
		when(commentRepository.existsById(commentId)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> commentAdminService.deleteComment(commentId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("댓글을 찾을 수 없습니다.");

		verify(commentRepository).existsById(commentId);
		verify(commentRepository, never()).deleteById(anyLong());
	}



}