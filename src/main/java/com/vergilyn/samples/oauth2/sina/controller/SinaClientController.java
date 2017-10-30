package com.vergilyn.samples.oauth2.sina.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.vergilyn.samples.oauth2.sina.bean.CodeRespParam;
import com.vergilyn.samples.oauth2.sina.config.SinaOAuthConfig;
import com.vergilyn.samples.oauth2.sina.constant.SinaCache;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/26
 */
@Controller
@RequestMapping("/sina")
public class SinaClientController {
    @Value("${app.name}")
    String appName;
    @Value("${app.description}")
    String desc;
    @Autowired
    SinaOAuthConfig config;
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 应用服务的登陆页面, 放置sina的第三方登陆。
     * @param request
     * @return
     * @throws OAuthSystemException
     */
    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request) throws OAuthSystemException {
        request.setAttribute("appName", appName);
        request.setAttribute("desc", desc);
        request.setAttribute("sinaURL", config.toSinaURL());

        return "/oauth2/sina/login";
    }


    @RequestMapping(value = "/auth_callback")
    public String authCallback(HttpServletRequest request, CodeRespParam codeResp) throws Exception {
        String respBody = "";
        try {
            // 记录用于调试分析
            redisTemplate.opsForValue().set(SinaCache.keyCodeResp(), JSON.toJSONString(codeResp));

            // 1. 未得到auth_code
            if(StringUtils.isEmpty(codeResp.getCode())){
                request.setAttribute("resp", codeResp);
                /* FIXME 根据实际情况看返回位置, 例举:
                 *  1. 返回之前点击跳转到授权登陆的页面, 并提示错误原因. (推荐)
                 *  2. 返回到 登录页面 (可能跟1一样, 也可能不一样)
                 *  3. 网站的首页
                 */
                return "/oauth2/sina/code_error";
            }

            // 2. 正确获得auth_code, 用auth_code换取access_token
            // 不一定需要这么写, http或webservice请求也一样。
            OAuthClientRequest oauthClientRequest = OAuthClientRequest
                    .tokenLocation(config.getTokenURL())
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(config.getAppKey())
                    .setClientSecret(config.getAppSecret())
                    .setRedirectURI(config.getRedirectURI())
                    .setCode(codeResp.getCode())
                    .buildQueryMessage();

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(oauthClientRequest, OAuth.HttpMethod.POST);
            String accessToken = oAuthResponse.getAccessToken();
            String refreshToken= oAuthResponse.getRefreshToken();
            Long expiresIn = oAuthResponse.getExpiresIn();
            // 看sina接口文档说明
            String uid = oAuthResponse.getParam("uid");
            respBody = oAuthResponse.getBody();

            // 记录用于调试分析
            redisTemplate.opsForValue().set(SinaCache.keyTokenResp(), respBody);

            request.setAttribute("infoURL", config.toInfoURL(accessToken, uid));

        } catch (OAuthProblemException e) {
            e.printStackTrace();
            respBody = e.getLocalizedMessage();
        }
        request.setAttribute("respBody", respBody);

        return "/oauth2/sina/access_token";
    }

}
