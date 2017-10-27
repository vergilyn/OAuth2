package com.vergilyn.samples.oauth2.sina.config;

import java.net.URLEncoder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 具体参考sina接口文档: http://open.weibo.com/wiki/%E6%8E%88%E6%9D%83%E6%9C%BA%E5%88%B6
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/26
 */
@Configuration
@ConfigurationProperties(prefix = "sina.oauth2")
@Component
public class SinaOAuthConfig {
    private String appKey;
    private String appSecret;

    private String redirectURI;
    // 客户端提供: 取消授权回调页
    private String cancleURI;

    // 请求用户授权Token
    private String codeURL;
    // 获取授权过的Access Token
    private String tokenURL;
    // 用access_token、uid获取用户授权信息
    private String infoURL;
    // 授权回收接口
    private String revokeURL;
    // 授权信息查询接口
    private String tokenInfoURL;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    public String getCancleURI() {
        return cancleURI;
    }

    public void setCancleURI(String cancleURI) {
        this.cancleURI = cancleURI;
    }

    public String getCodeURL() {
        return codeURL;
    }

    public void setCodeURL(String codeURL) {
        this.codeURL = codeURL;
    }

    public String getTokenURL() {
        return tokenURL;
    }

    public void setTokenURL(String tokenURL) {
        this.tokenURL = tokenURL;
    }

    public String getInfoURL() {
        return infoURL;
    }

    public void setInfoURL(String infoURL) {
        this.infoURL = infoURL;
    }

    public String getRevokeURL() {
        return revokeURL;
    }

    public void setRevokeURL(String revokeURL) {
        this.revokeURL = revokeURL;
    }

    public String getTokenInfoURL() {
        return tokenInfoURL;
    }

    public void setTokenInfoURL(String tokenInfoURL) {
        this.tokenInfoURL = tokenInfoURL;
    }

    public String toSinaURL(){
        String url = this.codeURL;
        url += "?client_id=" + this.appKey;
        url += "&redirect_uri=" + URLEncoder.encode(this.redirectURI);

        return url;
    }

    public String toInfoURL(String accessToken, String uid){
        String url = this.infoURL;
        url += "?access_token=" + accessToken;
        url += "&uid=" + uid;

        return url;
    }
}
