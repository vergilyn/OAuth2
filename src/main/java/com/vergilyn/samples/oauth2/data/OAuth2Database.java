package com.vergilyn.samples.oauth2.data;

import java.util.HashMap;
import java.util.Map;

import com.vergilyn.samples.oauth2.server.bean.ClientAuthBean;
import com.vergilyn.samples.oauth2.server.bean.UserBean;

/**
 * 模拟数据库数据.
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/23
 */
public class OAuth2Database {
    /**
     * key: client_id, value: client_secret
     */
    public final static Map<String,ClientAuthBean> CLIENT;
    static{
        CLIENT = new HashMap<>();
        ClientAuthBean ca = new ClientAuthBean("100001","client_a","a_secret");
        ClientAuthBean cb = new ClientAuthBean("100002","client_b","b_secret");
        ClientAuthBean cc = new ClientAuthBean("100003","client_c","c_secret");
        CLIENT.put(ca.getClientId(),ca);
        CLIENT.put(cb.getClientId(),cb);
        CLIENT.put(cc.getClientId(),cc);
    }

    /**
     * key: username, value: password
     */
    public final static Map<String,UserBean> USER;
    static {
        USER = new HashMap<>();
        UserBean ua = new UserBean("root", "123456", "root_nickname");
        UserBean ub = new UserBean("tigger", "tigger", "tigger_nickname");
        USER.put(ua.getUsername(), ua);
        USER.put(ub.getUsername(), ub);
    }
}
