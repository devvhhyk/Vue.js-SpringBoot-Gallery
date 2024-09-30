package com.example.backend.service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String getToken(String key, Object value); // JWT 토큰 생성

    Claims getClams(String token); // JWT 토큰에서 클레임 추출

    boolean isValid(String token);

    int getId(String token);
}
