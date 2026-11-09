package org.example.expert.common.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.expert.common.LogResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// Request (userRole 수정)->
// JwtFilter ->
// AdminCheckInterceptor + Log->
// AdminAOP Log

@Aspect
@Component
@RequiredArgsConstructor
public class AdminCheckAOP {
	private final ObjectMapper objectMapper;
	private final LogResponse logResponse;

	@Around("""
        execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))
        || execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))
        """)
	public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {

		//실행 전
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

		Long userId = (Long) request.getAttribute("userId");
		UserRole userRole = UserRole.valueOf((String) request.getAttribute("userRole"));
		String url = request.getRequestURI();

		Object requestBody = extractRequestBody(joinPoint);
		String requestJson = objectMapper.writeValueAsString(requestBody);

		logResponse.adminApiRequest(userId, userRole, url, requestJson);

		//메서드 실행
		Object result = joinPoint.proceed();

		//실행 후
		String responseJson = objectMapper.writeValueAsString(result);
		logResponse.adminApiResponse(userId, userRole, url, responseJson);

		return result;

	}

	// RequestBody
	private Object extractRequestBody(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod(); //ex: method = changeUserRole // deleteComment

		Annotation[][] parameterAnnotations = method.getParameterAnnotations(); //@PathVariable + @RequestBody
		Object[] args = joinPoint.getArgs(); //실제 요청 값

		for (int i = 0; i < parameterAnnotations.length; i++) {
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation instanceof RequestBody) {
					return args[i]; //@RequestBody 값만 추출
				}
			}
		}
		return null;
	}
}
