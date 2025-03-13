package com.example.main.config;

import com.example.main.dto.UserDto;
import com.example.main.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get the user ID from the request (in a real app, this would come from a JWT token or session)
        String userId = request.getHeader("User-Id");
        
        // Skip authentication for login and register endpoints
        String path = request.getRequestURI();
        if (path.contains("/api/users/authenticate") || path.contains("/api/users") && request.getMethod().equals("POST")) {
            return true;
        }
        
        // Check if user is banned
        if (userId != null) {
            Long userIdLong = Long.parseLong(userId);
            if (userService.getUserById(userIdLong).map(UserDto::isBanned).orElse(false)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Your account has been banned");
                return false;
            }
        }
        
        return true;
    }
} 