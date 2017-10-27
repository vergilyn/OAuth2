package com.vergilyn.samples.oauth2.sina.constant;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/27
 */
public class SinaCache {

    /**
     * type: string-json</br>
     * key: sina_code_resp:{timestamp}</br>
     * value: string-json</br>
     * expired: null</br>
     * use: 调试记录请求结果
     */
    private final static String SINA_CODE_RESP = "sina_code_resp:%d";

    /**
     * type: string-json</br>
     * key: sina_token_resp:{timestamp}</br>
     * value: string-json</br>
     * expired: null</br>
     * use: 调试记录请求结果
     */
    private final static String SINA_TOKEN_RESP = "sina_token_resp:%d";

    public static String keyCodeResp(){
        return String.format(SINA_CODE_RESP, System.currentTimeMillis());
    }

    public static String keyTokenResp(){
        return String.format(SINA_TOKEN_RESP, System.currentTimeMillis());
    }
}
