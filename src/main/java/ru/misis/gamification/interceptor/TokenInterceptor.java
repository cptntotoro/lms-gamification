package ru.misis.gamification.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.lang.reflect.Method;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        TokenCheck tokenCheck = method.getAnnotation(TokenCheck.class);
        if (tokenCheck == null) {
            tokenCheck = handlerMethod.getBeanType().getAnnotation(TokenCheck.class);
        }
        if (tokenCheck == null) {
            return true;
        }

        String headerName = tokenCheck.headerName();
        String token = request.getHeader(headerName);

        if (validateToken(token)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        String errorMessage = String.format("{\"error\": \"'%s' is not valid\"}", headerName);
        response.getWriter().write(errorMessage);
        return false;
    }

    private boolean validateToken(String token) {
        return token != null && !token.isBlank() && token.contains("123");
    }
}