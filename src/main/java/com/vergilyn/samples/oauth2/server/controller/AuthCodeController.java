package com.vergilyn.samples.oauth2.server.controller;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.vergilyn.samples.oauth2.data.OAuth2Database;
import com.vergilyn.samples.oauth2.server.bean.AuthCodeCache;
import com.vergilyn.samples.oauth2.server.bean.ClientAuthBean;
import com.vergilyn.samples.oauth2.server.bean.UserBean;
import com.vergilyn.samples.oauth2.server.constant.CacheConstant;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/23
 */
@Controller
@RequestMapping("/oauth2")
public class AuthCodeController {
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * <pre>
     * 获取OAuth2[授权码]请求;
     * 必须: clientId、redirectURI绝对地址、response_type = code;
     * ex.
     *  template http://localhost:{port}/oauth2/authorize?client_id={AppKey}&response_type=code&redirect_uri={redirectURI}
     *  right-test http://localhost:8080/oauth2/authorize?client_id=100001&response_type=code&redirect_uri=http%3A%2F%2Fwww.baidu.com
     *  error-test http://localhost:8080/oauth2/authorize?client_id=233333&response_type=code&redirect_uri=http%3A%2F%2Fwww.baidu.com
     * (当然,也可以传递别的参数到redirect_uri)
     * </pre>
     * @param request
     * @return 返回授权码(code)有效期xx分钟, 客户端只能使用一次[与client_id和redirect_uri一一对应关系]
     * @throws OAuthSystemException
     */
    @RequestMapping(value = "/auth_code", method = {RequestMethod.GET, RequestMethod.POST})
    public String fetchAuthorizeCode(HttpServletRequest request) throws OAuthSystemException {

        OAuthAuthzRequest oauthRequest;
        try {
            // 1. 构建OAuth2请求
            oauthRequest = new OAuthAuthzRequest(request);
            /* 2. 校验请求 (具体考虑需要校验的内容), 参考校验内容:
             *   1) requestURL是否合法, 指定[白名单]的请求才能获取[授权码].
             *   2) redirectURL是否合法.
             *   3) clientId是否合法. (重要)
             *   (备注: 没有 帐号/密码 的校验, 因为登陆是在自身系统中, 获取授权码的接口不需要)
             */
            boolean isCheckFailure = checkRequest(request, oauthRequest);

            // 校验失败, 返回错误code、msg到redirectURL。
            if (isCheckFailure) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setError(OAuthError.CodeResponse.ACCESS_DENIED)
                        .setErrorDescription("校验失败, 请检查参数是否正确!")
                        .buildJSONMessage();
                return "redirect:" + oauthRequest.getRedirectURI();
            }

            /* 3. 检验用户是否已登陆、帐号&密码, 校验失败返回[授权登陆页面]; 需要username、password
             * (相当于:
             *   a. 客户端发起请求, 一定会跳转到此login.html, 然后用户进行授权(username、password、授权客户端能用的权限)
             *   b. 客户授权后, 又请求此方法, 返回[授权码]给客户端.
             *   c. 客户端得到[授权码], 再用[授权码]向[资源服务器]请求受保护资源
             *   d. 资源服务器会验证访问令牌的有效性，如果成功则下发受保护资源。
             *  )
             */
            boolean isLoginFailure = checkLogin(request);
            if (isLoginFailure) { // 返回[授权登陆页面]
                request.setAttribute("response_type", oauthRequest.getResponseType());
                request.setAttribute("client_id", oauthRequest.getClientId());
                request.setAttribute("redirect_uri", oauthRequest.getRedirectURI());
//                request.setAttribute("scope", oauthRequest.getScopes());
                return "/oauth2/login";
            }

            /* 4. 生成授权码; (授权码可以有别的算法)
             *  授权码尽可能在短时间有效; 且一个authCode, 只能使用一次得到一个accessToken.
             */
            String username = request.getParameter("username");
            String clientId = oauthRequest.getClientId();
            String authCode = "";
            String responseType = oauthRequest.getResponseType();
            if (ResponseType.CODE.toString().equals(responseType)) {
                OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
                authCode = oauthIssuerImpl.authorizationCode();
                // (授权码: 加入缓存, 当client请求获取accessToken校验. key: authCode; value: clientId; 失效时长尽可能短)
                ClientAuthBean authBean = OAuth2Database.CLIENT.get(clientId);
                AuthCodeCache codeCache = new AuthCodeCache(authBean, authCode, username);
                redisTemplate.opsForValue().set(CacheConstant.keyAuthCode(clientId, authCode), JSON.toJSONString(codeCache), 10, TimeUnit.MINUTES);
            }

            // 4.1 构建授权响应
            OAuthResponse oauthResponse = OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND)
                    .setCode(authCode)
                    .location(oauthRequest.getRedirectURI())
                    .buildQueryMessage();

            // 申请[授权码成功], 重定向到: redirectURL
            return "redirect:" + oauthResponse.getLocationUri();
        } catch (OAuthProblemException e) {
            OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .error(e) // OAuthProblemException ex
                    .buildJSONMessage();
            return "/oauth2/error";
        }
    }

    private boolean checkLogin(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UserBean userBean = OAuth2Database.USER.get(username);

        boolean rs = !(StringUtils.isNotEmpty(password)
                && OAuth2Database.USER.containsKey(username)
                && userBean.getPassword().equalsIgnoreCase(password));

        return rs;
    }

    private boolean checkRequest(HttpServletRequest request, OAuthAuthzRequest oauthRequest) {
        boolean rs = false;

        String clientId = oauthRequest.getClientId();
        rs = OAuth2Database.CLIENT.containsKey(clientId);

        return !rs;
    }


}
