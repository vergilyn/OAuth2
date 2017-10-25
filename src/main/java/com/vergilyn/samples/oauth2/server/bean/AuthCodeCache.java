package com.vergilyn.samples.oauth2.server.bean;

import java.util.Set;

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
    // 用户选择对client的授权范围
    private Set<String> scopes;
    private String state;

    // username为了构建redis的key结构
    private String username;

    public AuthCodeCache() {
    }

    public AuthCodeCache(ClientAuthBean clientAuth, String authCode, String username, Set<String> scopes, String state) {
        this.clientId = clientAuth.getClientId();
        this.clientSecret = clientAuth.getClientSecret();
        this.authCode = authCode;
        this.username = username;
        this.scopes = scopes;
        this.state = state;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setUsername(String username) {
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
