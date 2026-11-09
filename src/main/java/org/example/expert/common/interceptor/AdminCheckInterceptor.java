package org.example.expert.common.interceptor;

import java.io.IOException;

import org.example.expert.common.LogResponse;
import org.example.expert.common.exception.ErrorResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminCheckInterceptor implements HandlerInterceptor {

	private final LogResponse logResponse;
	private final UserService userService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
		Object id = request.getAttribute("userId");
		String url = request.getRequestURI();

		//1. 인증 여부 확인
		if (id == null) {
			logResponse.adminApiFail("인증되지 않은 사용자", null, null, url);
			throw new AuthException("인증이 필요합니다.");
		}
		Long userId = (Long) id;

		//2. 관리자 권한 확인
		 UserRole userRole = userService.getUser(userId).getUserRole();
		if(userRole != UserRole.ADMIN)
		{
			logResponse.adminApiFail("관리자 권한 없음", userId, userRole, url);
			throw new AuthException("관리자만 접근할 수 있습니다.");
		}

		//3. 관리자 API 접근 성공 로그 기록
		logResponse.adminApiSuccess(userId, userRole, url);

		return true;
	}
}
