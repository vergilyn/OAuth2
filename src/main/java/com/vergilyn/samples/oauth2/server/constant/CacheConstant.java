package com.vergilyn.samples.oauth2.server.constant;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/24
 */
public class CacheConstant {

    /**
     * type: string-json</br>
     * key: auth_code_{client_id}:{auth_code}</br>
     * value: 服务器上(开通授权客户端的信息)的client_id、client_secret、及生成的auth_code的json对象</br>
     * expired: 10min</br>
     * use: 获取access_token时确保auth_code、client_id、client_secret匹配, 且一个code在有效期内只能生成一个access_token.</br>
     * (因为为了验证匹配, 所以json中才保存了client_id、client_secret)</br>
     */
    public final static String REDIS_AUTH_CODE = "auth_code_%s:%s";
    public final static int EXPIRED_AUTH_CODE = 10;

    /**
     * type: string-json</br>
     * key: access_token_{client_id}:{access_token}</br>
     * value: {@link com.vergilyn.samples.oauth2.server.bean.AuthTokenCache}</br>
     * expired: 7days</br>
     * use: 用户资源API调用凭证</br>
     * problem:</br>
     *   1. 无法从redis-key看出是哪个用户(client请求refreshToken不可能传username, 一般只传refreshCode、clientId、grantType)</br>
     */
    public final static String REDIS_ACCESS_TOKEN = "access_token_%s:%s";
    public final static int EXPIRED_ACCESS_TOKEN = 7;
    /**
     * type: string</br>
     * key: refresh_token_{client_id}:{refresh_token}</br>
     * value: {@link com.vergilyn.samples.oauth2.server.bean.AuthTokenCache}</br>
     * expired: 30days(此值失效时, 对应的access_token一定要一起失效)</br>
     * use:</br>
     *   1. 提供给client刷新用户授权的有效期;</br>
     *   2. 用户手动取消对某client的授权;</br>
     * problem:</br>
     *   1. 无法从redis-key看出是哪个用户(client请求refreshToken不可能传username, 一般只传refreshCode、clientId、grantType)</br>
     */
    public final static String REDIS_REFRESH_TOKEN = "refresh_token_%s:%s";
    public final static int EXPIRED_REFRESH_TOKEN = 30;



    public static String keyAuthCode(String clientId, String authCode){
        return String.format(REDIS_AUTH_CODE, clientId, authCode);
    }

    public static String keyAccessCode(String clientId, String accessToken){
        return String.format(REDIS_ACCESS_TOKEN, clientId, accessToken);
    }
    public static String keyRefreshCode(String clientId, String refreshToken){
        return String.format(REDIS_REFRESH_TOKEN, clientId, refreshToken);
    }
}
