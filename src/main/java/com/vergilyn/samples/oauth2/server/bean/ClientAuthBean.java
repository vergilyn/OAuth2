package com.vergilyn.samples.oauth2.server.bean;

/**
 * 已申请授权登陆的client校验信息
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/24
 */
public class ClientAuthBean {

//  private String id;
    private String clientId;
    private String clientName;
    private String clientSecret;
//  private String secretSalt; // 加密盐, 正式环境需要考虑

    public ClientAuthBean() {
    }

    public ClientAuthBean(String clientId, String clientName, String clientSecret) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
