package com.vergilyn.samples.oauth2.server.bean;

/**
 * server的用户表;</br>
 * accessToken、refreshToken不一定要保存到数据库, 可以只存在于缓存中(具体看设计);
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/24
 */
public class UserBean {
//    private String id;
    private String username;
    private String password;
    private String nickname;

    public UserBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
