package com.vergilyn.samples.oauth2.sina.controller;

import javax.servlet.http.HttpServletRequest;

import com.vergilyn.samples.oauth2.sina.bean.CodeRespParam;
import com.vergilyn.samples.oauth2.sina.config.SinaOAuthConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request) throws OAuthSystemException {
        request.setAttribute("appName", appName);
        request.setAttribute("desc", desc);
        request.setAttribute("sinaURL", config.toSinaURL());

        return "/oauth2/sina/login";
    }


    @RequestMapping(value = "/auth_callback")
    public String authCallback(HttpServletRequest request, CodeRespParam codeResp) throws Exception {
        OAuthAuthzResponse oauthAuthzResponse = null;
        try {
            System.out.println(codeResp);

            // 1. 当授权失败引导用户返回到授权页
            if(StringUtils.isEmpty(codeResp.getCode())){
                System.out.print("error: " + codeResp.getError());
                System.out.print(" ,error_code: " + codeResp.getError_code());
                System.out.println(" ,error_desc: " + codeResp.getError_description());
                return "redirect:" + config.toSinaURL();
            }

            // 2. 正确获得auth_code, 用auth_code换取access_token
            OAuthClientRequest oauthClientRequest = OAuthClientRequest
                    .tokenLocation(config.getTokenURL())
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(config.getAppKey())
                    .setClientSecret(config.getAppSecret())
                    .setRedirectURI(config.getRedirectURI())
                    .setCode(codeResp.getCode()) //TODO 测试
                    .buildQueryMessage();

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(oauthClientRequest, OAuth.HttpMethod.POST);
            String accessToken = oAuthResponse.getAccessToken();
            String refreshToken= oAuthResponse.getRefreshToken();
            Long expiresIn = oAuthResponse.getExpiresIn();

            System.out.println("accessToken: " + accessToken);
            System.out.println("refreshToken: " + refreshToken);
            System.out.println("expiresIn: " + expiresIn);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
