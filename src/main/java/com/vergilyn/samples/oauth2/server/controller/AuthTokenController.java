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
     * 获取AccessToken
     * <p>参数:
     *     <ul>
     *         <li>client_id、client_secret、code</li>
     *         <li>grant_type: 固定"authorization_code", 不区分大小写</li>
     *         <li>redirect_uri</li>
     *     </ul>
     * </p>
     * <p>注意事项:
     *  <ol>
     *   <li>保证auth_code、client_id、client_secret的一致性</li>
     *   <li>保证auth_code只能使用一次, 获得一个access_token;</li>
     *   <li>access_token有效时长: 7days, refresh_token有效时长: 30days;</li>
     *  </ol>
     * </p>
     * <p>remark:
     *  <ul>
     *      <li>1. access_token: api调用的凭证, 有效时长较refresh_token短;</li>
     *      <li>2. refresh_token: 用于client调用刷新/获取access_token;<br/>
     *          a. refresh_token失效时, 表示access_token一定已失效, 且只能由用户重新发起授权.<br/>
     *               (即client无法自己获取到access_token)<br/>
     *               <br/>
     *          b. refresh_token未失效时, 但access_token已失效时, 可由client发起(不是用户)根据有效的refresh_token获取一个有效的access_code.<br/>
     *              (且同时可以重置refresh_token的有效期. 这样client定期调用refresh_token就可以永久获得用户的授权, 除非用户手动取消对其授权.<br/>
     *               备注: 用户手动取消授权, 让refresh_token、access_token同时失效.)<br/>
     *      </li>
     *  </ul>
     * </p>
     * <p>ex.
     *     http://localhost:{port}/oauth2/access_token?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code&redirect_uri={redirect_uri}&code={code}
     * </p>
     * @param request
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
                            .setError(OAuthError.TokenResponse.INVALID_REQUEST)
                            .setErrorDescription("invalid client_id or auth_code.")
                            .buildJSONMessage();
                    return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
                }

                codeCache = JSON.parseObject(json, AuthCodeCache.class);
                boolean rs = codeCache.getClientSecret().equalsIgnoreCase(clientSecret);
                if(!rs){ // clientSecret不匹配
                    response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                            .setErrorDescription("client does not match .")
                            .buildJSONMessage();
                    return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
                }

            }
            // username(userId)不会通过请求传递到此, 所以在之前auth_code的缓存中才保留此值.
            String username = codeCache.getUsername();

            /* (可能需要) 避免server重复授权.
             *   用户在client_a的授权未失效, 但用户在client_a登陆时却要重新授权.
             *   重新获取auth_code, 用auth_code获取access_token、refresh_token.
             *   但server记录要保证唯一: 某个用户在某一个网站的授权信息只能有一份。
             *   (此例子已避免, 因为redis-key不会重复, 就算重新授权之前的access_token、refresh_token的cache会被覆盖)
             */

            // 生成accessToken、refreshToken; 例如sina就没有提供refreshToken功能
            String accessToken = oauthIssuer.accessToken();
            String refreshToken = oauthIssuer.refreshToken();
            // 缓存access_token、refreshToken; 缓存的value看设计而定。
            AuthTokenCache tokenCache = new AuthTokenCache(clientId, clientSecret, accessToken, refreshToken, username);
            String authJson = JSON.toJSONString(tokenCache);
            redisTemplate.opsForValue().set(CacheConstant.keyAccessCode(clientId, accessToken), authJson,CacheConstant.EXPIRED_ACCESS_TOKEN, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(CacheConstant.keyRefreshCode(clientId, refreshToken), authJson,CacheConstant.EXPIRED_REFRESH_TOKEN, TimeUnit.DAYS);

            //正确生成accessToken、refreshToken后, 清除授权码: 确保一个code只能使用一次
            redisTemplate.delete(codeCacheKey);

            //构建oauth2授权返回信息
            response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken)
                    .setExpiresIn(CacheConstant.EXPIRED_ACCESS_TOKEN+"")
                    .buildJSONMessage();

            return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
        } catch (OAuthProblemException ex) {
            ex.printStackTrace();
            //构建错误响应
            response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .error(ex)
                    .buildJSONMessage();
            return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
        }
    }


    /**
     * 根据refresh_token更新或重新获取access_token.
     * <p>参数:
     *   <ul>
     *       <li>client_id</li>
     *       <li>refresh_token</li>
     *       <li>grant_type: 固定为"refresh_token", 不区分大小写</li>
     *       <li>(可选)client_secret: 以下代码中必须, 只是用于校验客户端是否合法</li>
     *   </ul>
     * </p>
     * <p>我在网上看到有2类调用refresh_token的结果:
     *  <ol>
     *    <li>每次调用refresh_token都生成新的refresh_token、access_token返回给client(有效期全部重置)</li>
     *    <li>refresh_token、access_token始终保持不变, 但每次重置它们的有效期.<br/>
     *      每次重置refresh_token的有效期可以让client保证用户授权永不过期.(client定期调用refresh_token延长有效期)
     *    </li>
     *  </ol>
     * </p>
     * <p>ex.
     *     http://localhost:{port}/oauth2/refresh_token?client_id={client_id}&grant_type=refresh_token&refresh_token={refresh_token}
     * </p>
     * @return 实际应用中更多返回的是json
     */
    @RequestMapping(value = "/refresh_token",method = RequestMethod.POST)
    public HttpEntity refreshToken(HttpServletRequest request) throws OAuthSystemException {
        OAuthIssuer oauthIssuer;
        OAuthTokenRequest oauthRequest = null;
        OAuthResponse response = null;
        try {
            oauthRequest = new OAuthTokenRequest(request);
            oauthIssuer = new OAuthIssuerImpl(new MD5Generator());

            // (忽略) 1. 校验client的合法性(client_id、client_secret是否匹配);

            // 2. 校验grant_type: 只允许refresh_token
            String grantType = oauthRequest.getGrantType();
            if(!GrantType.REFRESH_TOKEN.name().equalsIgnoreCase(grantType)){
                response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("invalid grant_type.")
                        .buildJSONMessage();
                return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }

            // 3. 校验refresh_token的有效性
            String clientId = oauthRequest.getClientId();
            String refreshToken = oauthRequest.getRefreshToken();
            String refreshKey = CacheConstant.keyRefreshCode(clientId, refreshToken);
            String refreshJson = redisTemplate.opsForValue().get(refreshKey);
            if(StringUtils.isEmpty(refreshJson)){ // 无效的refreshToken
                response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setError(OAuthError.TokenResponse.INVALID_REQUEST)
                        .setErrorDescription("invalid refresh_token.")
                        .buildJSONMessage();
                return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }
            AuthTokenCache refreshBean = JSON.parseObject(refreshJson, AuthTokenCache.class);

            /* 4.根据refresh_token可能的操作:
             *   1. access_token未超时, 重置或延长access_token的有效期.
             *   2. access_token已超时, 生成新的access_token返回给client.(也可以不生成新的access_token)
             *     a) access_token、refresh_token都保持不变.
             *     b) 生成新的access_token, 并且生成新的refresh_token一起返回给client.
             *     (我也不知道a\b方案哪种较好 >.<!)
             */
            String accessKey = CacheConstant.keyAccessCode(clientId, refreshToken);
            String newAccessToken = refreshBean.getAccessToken(); // 沿用之前的access_token
            if(redisTemplate.hasKey(accessKey)){
                // 4.1 access_token未超时, 重置/延长有效期
                redisTemplate.expire(accessKey, CacheConstant.EXPIRED_ACCESS_TOKEN, TimeUnit.DAYS);
                // 同时重置refresh_token的有效期(具体看需求)
                redisTemplate.expire(refreshKey, CacheConstant.EXPIRED_REFRESH_TOKEN, TimeUnit.DAYS);
            }else{
                // 4.2 access_token已超时, 重新设置可用的access_token.
                // newAccessToken= oauthIssuer.refreshToken(); // 或新生成一个access_token
                String clientSecret = oauthRequest.getClientSecret();
                AuthTokenCache accessCache = new AuthTokenCache(clientId, clientSecret, newAccessToken, refreshToken, refreshBean.getUsername());
                redisTemplate.opsForValue().set(accessKey, JSON.toJSONString(accessCache),CacheConstant.EXPIRED_ACCESS_TOKEN, TimeUnit.DAYS);
            }

            // 5. 构建响应: refresh_token、access_token都未改变
            response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(newAccessToken)
                    .setExpiresIn(CacheConstant.EXPIRED_ACCESS_TOKEN+"")
                    .setRefreshToken(refreshBean.getRefreshToken())
                    .buildJSONMessage();
            return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
        } catch (OAuthProblemException ex) {
            response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .error(ex)
                    .buildJSONMessage();
            return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
        }

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
