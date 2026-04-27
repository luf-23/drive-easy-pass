package org.dep.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.security.JwtService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 放行不需要登录的路径
        if (path.startsWith("/auth/") || path.startsWith("/questions") || path.startsWith("/exam/venues") || path.startsWith("/exam/routes") || path.startsWith("/exam/schedules")) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "Please login first");
            return false;
        }

        try {
            CurrentUser currentUser = jwtService.parseToken(authorization.substring(7));
            request.setAttribute("currentUser", currentUser);
            return true;
        } catch (IllegalArgumentException ex) {
            writeUnauthorized(response, ex.getMessage());
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
