//package com.neu.finalproject.meskot.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class UserRealm extends AuthorizingRealm {
//
//    @Autowired
//    private UserService userService;
//
//    @Override
//    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
//            throws AuthenticationException {
//        UsernamePasswordToken loginToken = (UsernamePasswordToken) token;
//
//        User user = userService.findByUsername(loginToken.getUsername());
//        if (user == null) {
//            throw new UnknownAccountException("User not found");
//        }
//
//        return new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName());
//    }
//
//    // Optional: Role/permission checks later
//    @Override
//    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
//        return null;
//    }
//}