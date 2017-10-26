package com.vergilyn.samples.oauth2.sina.bean;

/**
 * 当请求授权Endpoint：https://api.weibo.com/2/oauth2/authoriz，返回方式是：跳转到redirect_uri，并在uri 的query parameter中附带错误的描述信息。
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/26
 */
public class CodeRespParam {
    /* 正确授权参数:
     *  code	string	用于第二步调用oauth2/access_token接口，获取授权后的access token。
     *  state	string	如果传递参数，会回传该参数。
     */
    private String code;
    private String state;

    /* 授权错误参数:
     *  error: 错误码
     *  error_code: 错误的内部编号
     *  error_description: 错误的描述信息
     *  error_url: 可读的网页URI，带有关于错误的信息，用于为终端用户提供与错误有关的额外信息。
     */
    private String error;
    private String error_code;
    private String error_description;
    private String error_uri;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getError_description() {
        return error_description;
    }

    public void setError_description(String error_description) {
        this.error_description = error_description;
    }

    public String getError_uri() {
        return error_uri;
    }

    public void setError_uri(String error_uri) {
        this.error_uri = error_uri;
    }

    @Override
    public String toString() {
        return "CodeRespParam{" + "code='" + code + '\'' + ", state='" + state + '\'' + ", error='" + error + '\'' + ", error_code='" + error_code + '\'' + ", error_description='" + error_description + '\'' + ", error_uri='" + error_uri + '\'' + '}';
    }
}
