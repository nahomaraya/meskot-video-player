//package com.neu.finalproject.meskot.interceptor;
//
//import org.apache.shiro.SecurityUtils;
//import org.springframework.stereotype.Component;
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import com.fasterxml.jackson.core.JsonProcessingException;
//
//
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.http.client.ClientHttpRequestExecution;
//import org.springframework.http.client.ClientHttpRequestInterceptor;
//import org.springframework.http.client.ClientHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import javax.security.auth.Subject;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Slf4j
//@Getter
//@Setter
//@Component
//public class AuthInterceptor implements HandlerInterceptor {
//
//    @Override
//    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler)
//            throws Exception {
//
//        Subject subject = SecurityUtils.getSubject();
//
//        if (!subject.isAuthenticated()) {
//            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//
//        return true;
//    }
//}
