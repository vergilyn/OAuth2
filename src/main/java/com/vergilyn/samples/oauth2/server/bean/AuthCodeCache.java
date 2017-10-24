package com.vergilyn.samples.oauth2.server.bean;

/**
 * 缓存auth_code的校验json对象;</br>
 * clientId、clientSecret、authCode是必须的(校验一致性).其余根据实现看还需要哪些数据.
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/24
 */
public class AuthCodeCache {
    private String authCode;
    private String clientId;
    private String clientSecret;
    // username为了构建redis的key结构
    private String username;

    public AuthCodeCache() {
    }

    public AuthCodeCache(ClientAuthBean clientAuth, String authCode, String username) {
        this.clientId = clientAuth.getClientId();
        this.clientSecret = clientAuth.getClientSecret();
        this.authCode = authCode;
        this.username = username;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getUsername() {
        return username;
    }
}
