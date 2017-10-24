package com.vergilyn.samples.oauth2.server.controller;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.vergilyn.samples.oauth2.data.OAuth2Database;
import com.vergilyn.samples.oauth2.server.bean.AuthCodeCache;
import com.vergilyn.samples.oauth2.server.bean.AuthTokenCache;
import com.vergilyn.samples.oauth2.server.bean.ClientAuthBean;
import com.vergilyn.samples.oauth2.server.constant.CacheConstant;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author VergiLyn
 * @blog http://www.cnblogs.com/VergiLyn/
 * @date 2017/10/24
 */
@Controller
@RequestMapping("/oauth2")
public class AuthTokenController {
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * title: 获取令牌(AccessToken) [验证client_id、client_secret、auth_code的正确性];
     * <p>
     *   注意事项:
     *   <li>1. 保证auth_code、client_id、client_secret的一致性</li>
     *   <li>2. 保证auth_code只能使用一次, 获得一个access_token;</li>
     *   <li>3. access_token有效时长: 7days, refresh_token有效时长: 30days;</li>
     * </p>
     * <p>
     *   remark:
     *      1. access_token: api调用的凭证, 有效时长较refresh_token短;
     *      2. refresh_token: 用于client调用刷新access_token;
     *          a. refresh_token失效时, 表示access_token一定已失效, 且只能由用户重新发起授权.
     *              (即client无法自己获取到access_token)
     *          b. refresh_token未失效时, 但access_token已失效时, 可由client发起(不是用户)重新获取一个有效的access_code.
     *              (且refresh_token的失效时长重置. 这样client定期调用refresh_token就可以永久获得用户的授权, 除非用户手动取消对其授权.
     *               备注: 用户手动取消授权, 让refresh_token、access_token同时失效.
     *              )
     * </p>
     *
     * @param request
     * @url http://localhost:{port}/oauth2/access_token?client_id={AppKey}&client_secret={AppSecret}&grant_type=authorization_code&redirect_uri={YourSiteUrl}&code={code}
     * @return
     */
    @RequestMapping(value = "/access_token",method = RequestMethod.POST)
    public HttpEntity accessToken(HttpServletRequest request) throws OAuthSystemException {
        OAuthIssuer oauthIssuer = null;
        OAuthTokenRequest oauthRequest = null;
        OAuthResponse response = null;
        try {
            oauthIssuer = new OAuthIssuerImpl(new MD5Generator());
            oauthRequest = new OAuthTokenRequest(request);

            // (省略)1.1 client校验redirectURI是否合法; (参考: server校验写法);
            // (省略)1.2 client校验client_id(或AppKey)
            // 1.3 client校验client_secret
            boolean isSecret = checkSecret(oauthRequest,request);
            if(!isSecret){
                response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription("validate client error.")
                                .buildJSONMessage();
                return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));

            }

            String clientId = oauthRequest.getClientId();
            String clientSecret = oauthRequest.getClientSecret();
            String code = oauthRequest.getCode();
            String codeCacheKey = CacheConstant.keyAuthCode(clientId,code);
            AuthCodeCache codeCache = null;
            // 验证AUTHORIZATION_CODE
            if (GrantType.AUTHORIZATION_CODE.name().equalsIgnoreCase(oauthRequest.getGrantType())) {
                // 校验auth_code、client_id、client_secret的一致性.
                // 1. server根据client_id获取到server上保存的允许授权登陆的client信息.
                // 2. client_id、client_secret、auth_code的匹配性.
                String json = redisTemplate.opsForValue().get(codeCacheKey);
                if (StringUtils.isEmpty(json)) { // client_id、authCode不匹配, 或已超时
                    response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("invalid client_id or auth_code.")
                            .buildJSONMessage();
                    return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
                }

                codeCache = JSON.parseObject(json, AuthCodeCache.class);
                boolean rs = codeCache.getClientSecret().equalsIgnoreCase(clientSecret);
                if(!rs){ // clientSecret不匹配
                    response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("validate params does not match .")
                            .buildJSONMessage();
                    return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
                }

            }
            // username(userId)不会通过请求传递到此, 所以在之前auth_code的缓存中才保留此值.
            String username = codeCache.getUsername();

            // 生成accessToken、refreshToken; 例如sina就没有提供refreshToken功能
            String accessToken = oauthIssuer.accessToken();
            String refreshToken = oauthIssuer.refreshToken();
            // 缓存access_token、refreshToken; 缓存的value看设计而定。
            AuthTokenCache tokenCache = new AuthTokenCache(clientId, clientSecret, accessToken, refreshToken, username);
            String authJson = JSON.toJSONString(tokenCache);
            redisTemplate.opsForValue().set(CacheConstant.keyAccessCode(clientId, accessToken), authJson,7, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(CacheConstant.keyRefreshCode(clientId, refreshToken), authJson,30, TimeUnit.DAYS);

            //正确生成accessToken、refreshToken后, 清除授权码: 确保一个code只能使用一次
            redisTemplate.delete(codeCacheKey);

            //构建oauth2授权返回信息
            response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken)
                    .setExpiresIn("24h")
                    .buildJSONMessage();

            return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
        } catch (OAuthProblemException ex) {
            //构建错误响应
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .error(ex)
                    .buildJSONMessage();
            return new ResponseEntity(res.getBody(), HttpStatus.valueOf(res.getResponseStatus()));
        }
    }


    /**
     * <pre>
     *     获取令牌(AccessToken) [验证client_id、client_secret、auth_code的正确性];
     *     1. 保证auth_code只能使用一次, 获得一个access_token;
     *     2. access_token的有效设置时长.
     * </pre>
     *
     * @param request
     * @url http://localhost:{port}/oauth2/access_token?client_id={AppKey}&client_secret={AppSecret}&grant_type=authorization_code&redirect_uri={YourSiteUrl}&code={code}
     * @return
     */
    @RequestMapping(value = "/refresh_token",method = RequestMethod.POST)
    public HttpEntity refreshToken(HttpServletRequest request) throws OAuthSystemException {
        return null;
    }

    /**
     *
     * @param oauthRequest
     * @param request
     * @return true: 匹配
     */
    private boolean checkSecret(OAuthTokenRequest oauthRequest, HttpServletRequest request) {
        String clientSecret = StringUtils.defaultString(oauthRequest.getClientSecret(), "");
        String clientId = StringUtils.defaultString(oauthRequest.getClientId(), "");
        // client的client_id、client_secret与server回传的是否一致; (代码忽略)
        // (client存有client_id、client_secret(一对或多对)。)

        // 测试只保存client_id与client_secret一致;
        ClientAuthBean clientAuthBean = OAuth2Database.CLIENT.get(clientId);
        return clientSecret.equalsIgnoreCase(clientAuthBean.getClientSecret());
    }


}
