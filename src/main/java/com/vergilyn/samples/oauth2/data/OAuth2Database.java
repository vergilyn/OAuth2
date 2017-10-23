package com.vergilyn.samples.oauth2.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟数据库数据.
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/23
 */
public class OAuth2Database {
    public final static Map<String,String> CLIENT;
    static{
        CLIENT = new HashMap<>();
        CLIENT.put("100001","CLIENT_ID_A");
        CLIENT.put("100002","CLIENT_ID_B");
        CLIENT.put("100003","CLIENT_ID_C");
    }

    public final static Map<String,String> USER;
    static {
        USER = new HashMap<>();
        USER.put("root","123456");
        USER.put("tigger","tigger");
    }
}
