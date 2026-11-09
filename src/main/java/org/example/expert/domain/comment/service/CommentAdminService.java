package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentAdminService {

    private final CommentRepository commentRepository;

    @Transactional
    public void deleteComment(long commentId) {
		boolean existence = commentRepository.existsById(commentId);
		if(!existence){
			throw new InvalidRequestException("댓글을 찾을 수 없습니다.");
		}

        commentRepository.deleteById(commentId);
    }
}
