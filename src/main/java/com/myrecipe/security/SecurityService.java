package com.myrecipe.security;

public interface SecurityService {
    boolean isAuthenticated();
    void autoLogin (String email, String password);
    String getAuthentication();
}
