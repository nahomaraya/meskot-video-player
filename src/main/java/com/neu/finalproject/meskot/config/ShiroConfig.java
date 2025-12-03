//package com.neu.finalproject.meskot.config;
//
//import com.neu.finalproject.meskot.security.UserRealm;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ShiroConfig {
//
//    @Bean
//    public SecurityManager securityManager(UserRealm userRealm) {
//        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
//        manager.setRealm(userRealm);
//        return manager;
//    }
//
//    @Bean
//    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
//        DefaultShiroFilterChainDefinition chain = new DefaultShiroFilterChainDefinition();
//
//        chain.addPathDefinition("/auth/**", "anon"); // Login & Register unrestricted
//        chain.addPathDefinition("/movies/**", "authc"); // require authentication
//        chain.addPathDefinition("/**", "anon");
//        return chain;
//    }
//}
