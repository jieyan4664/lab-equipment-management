package com.lab.backed.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌工具类
 * 用于生成、解析和验证 JWT 令牌
 */
public class JwtUtil {
    // 1.生成密钥（HS256）
    private static final SecretKey KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 生成token
    public static String createToken() {
        return Jwts.builder()
                .setSubject("admin")
                .signWith(KEY) // 签名密钥
                .compact();
    }

    // 解析token【重点：不用verifyWith，改用parserBuilder+setSigningKey】
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY) // 替代 verifyWith(KEY)
                .build() // 构建JwtParser
                .parseClaimsJws(token)
                .getBody();
    }
}
