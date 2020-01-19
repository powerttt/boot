package com.github.boot.commons;

import com.github.boot.commons.utils.PropertyUtil;
import com.github.boot.commons.constants.JwtKeys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

/**
 *   iss: jwt签发者
 *     sub: jwt所面向的用户
 *     aud: 接收jwt的一方
 *     exp: jwt的过期时间，这个过期时间必须要大于签发时间
 *     nbf: 定义在什么时间之前，该jwt都是不可用的.
 *     iat: jwt的签发时间
 *     jti: jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击。
 *
 * @Author tongning
 * @Date 2019/10/20 0020
 * function:<
 * <p>
 * >
 */
public class JWTUtils {

    private String key;
    private Long expiration;
    private Long refresh;

    public JWTUtils() {
        this.key = PropertyUtil.getProperty("jwt.config.key");
        this.expiration = Long.valueOf(PropertyUtil.getProperty("jwt.config.expiration"));
        this.refresh = Long.valueOf(PropertyUtil.getProperty("jwt.config.refresh"));
    }

    /**
     * 生成JWT
     */
    public String createJWT(String id, String subject, Map<String, String> map) {
        JwtBuilder builder = Jwts.builder().setId(id)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, key);
        map.forEach(builder::claim);
        return builder.compact();
    }

    /**
     * 解析JWT
     */
    public Claims parseJWT(String jwtStr) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(jwtStr).getBody();
    }

    /**
     * 生成jwt
     */
    public String createJWT(Claims claims) {
        JwtBuilder builder = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, key);
        claims.forEach(builder::claim);
        return builder.compact();
    }


    /**
     * 判断刷新JWT
     * 过期时间小于24小时，刷新
     */
    public String refresh(String headerToken) {
        String token = headerToken.substring(JwtKeys.BEARER.length());
        Claims claims = parseJWT(token);
        if (claims.getExpiration().getTime() - System.currentTimeMillis() < refresh) {
            claims.setExpiration(new Date(System.currentTimeMillis() + expiration));
            JwtBuilder builder = Jwts.builder();
            claims.forEach((key, value) -> builder.claim(key, value));
            String refreshToken = builder.signWith(SignatureAlgorithm.HS256, key).compact();
            return refreshToken;
        }
        return token;
    }

    public String toString(Claims claims) {
        StringBuilder sb = new StringBuilder("{");
        claims.forEach((k, v) -> {
            sb.append("\"").append(k).append("\"")
                    .append("\"").append(v).append("\"")
                    .append(",");
        });
        sb.replace(sb.length() - 1, sb.length() - 1, "");
        sb.append("}");
        return sb.toString();
    }
}
