package com.vergilyn.samples.oauth2.server.bean;

/**
 * 缓存access_token、refresh_token的json对象.(可以写成2个JavaBean, 看设计)</br>
 *
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/24
 */
public class AuthTokenCache {
    private String clientId;
    private String clientSecret;
    // username: 暂定作用是标识这个access_token/refresh_token是属于那个用户的授权.
    private String username;

    private String accessToken;
    private String refreshToken;

    public AuthTokenCache() {
    }

    public AuthTokenCache(String clientId, String clientSecret, String accessToken, String refreshToken, String username) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
