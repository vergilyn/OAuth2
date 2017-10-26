package com.vergilyn.samples.oauth2.sina.config;

import java.net.URLEncoder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
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
    private String cancleURI;
    /**
     * client_id	true	string	申请应用时分配的AppKey。
     * redirect_uri	true	string	授权回调地址，站外应用需与设置的回调地址一致，站内应用需填写canvas page的地址。
     */
    private String codeURL;
    /**
     *                  必选      类型    说明
     * client_id	    true	string	申请应用时分配的AppKey。
     * client_secret	true	string	申请应用时分配的AppSecret。
     * grant_type	    true	string	请求的类型，填写"authorization_code"
     *
     * grant_type为authorization_code时
     * code	            true	string	调用authorize获得的code值。
     * redirect_uri	    true	string	回调地址，需需与注册应用里的回调地址一致。
     */
    private String tokenURL;

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

    public String toSinaURL(){
        String url = this.codeURL;
        url += "?client_id=" + this.appKey;
        url += "&redirect_uri=" + URLEncoder.encode(this.redirectURI);

        return url;
    }

}
