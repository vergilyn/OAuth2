# OAuth2.0

## 1. 参考
  理论参考:
  1. [理解OAuth 2.0 - 阮一峰](http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)
  2. [第十七章 OAuth2集成——《跟我学Shiro》](http://jinnianshilongnian.iteye.com/blog/2038646)
  3. [实现OAuth2.0服务端【授权码模式(Authorization Code)】](http://blog.csdn.net/u014386474/article/details/51602264)

  代码参考:
  1. [https://github.com/zhangkaitao/shiro-example](https://github.com/zhangkaitao/shiro-example) (shiro-example-chapter17 server&client)
  2. [https://github.com/zhouyongtao/homeinns-web](https://github.com/zhouyongtao/homeinns-web)


## 2. 运行环境
  1. spring-boot v1.4.6 + thymeleaf
  2. apache oltu v1.0.2(server + client)
  3. redis
  4. fastjson v1.2.24, commons-lang3 v3.5
  5. 没有配置数据库, 用静态常量模拟的数据库数据.
  `com.vergilyn.samples.oauth2.data.OAuth2Database`

## 3. 代码结构
### 3.1 服务器划分
  1. server服务端, port:8081
  2. client客户端, port:8082
  3. sina客户端, port:8080 (说明: 对接sina微博登陆)
### 3.2 授权登陆逻辑
建议先看QQ的文档: [获取Access_Token](http://wiki.connect.qq.com/%E4%BD%BF%E7%94%A8authorization_code%E8%8E%B7%E5%8F%96access_token)  
  1. client发起授权登陆请求(GET):  
  http://localhost:{port}/oauth2/auth_code?client_id={AppKey}&response_type=code&redirect_uri={redirectURI}  
  2. server验证client_id、grant_type, 跳转到授权登陆页面;
  3. 用户在授权登陆页面输入username&password, 授权范围, 点击\[授权并登陆];  
  (url同1, 但为POST, 且多传递了一些参数username、password、scope等)
  4. server生成auth_code(有效期10min), 返回到client先前给予redirectURI;  
  (相当于server用参数auth_code去请求client的redirectURI)
  5. client收到server的请求,client再用auth_code去请求获取access_token&refresh_token:  
  http://localhost:{port}/oauth2/access_token?client_id={client_id}&client_secret={client_secret}&grant_type=authorization_code&redirect_uri={redirect_uri}&code={code}
  6. server校验auth_code、client_id、client_secrect, 生成access_token&refresh_token返回给client;  
  (access_token有效期7days, refresh_token有效期30days; 每次调用refreshTokenUrl, 重置有效期)  
  
到此授权完成, client得到请求resources的access_token.


### 3.3 思考: access_token 与 refresh_token
首先分清职责, access_token才是获取用户resources的凭证;而refresh_token是用来获取有效的access_token的.  
refresh_token目的: 让client能主动延长用户的有效授权期.  
我所了解的, 一般情况下, server都会提供refresh_token给client, 但不提供更改refresh_token的方法.   
refresh_token的作用, 在其有效期内, 不用用户去重新确认授权登陆, client可以通过refresh_token获取有效的access_token.  
不提供更新refresh_token的目的是, server默认用户在client的授权有效期只有30days, 在其失效后只能由用户重新发起授权.  


为什么要这么做?  
> 摘自QQ文档, 利用refresh_token续期access_token    
    Step3: 权限自动续期，获取Access Token  
    Access_Token的有效期默认是3个月，过期后需要用户重新授权才能获得新的Access_Token。本步骤可以实现授权自动续期，避免要求用户再次授权的操作，提升用户体验.  

我查阅的一些说明refresh_token、access_token都在说是为安全考虑, 但我在看了QQ文档后的一种理解:  
&nbsp;&nbsp;client去QQ官网申请, 填写审核资料获取client_id、client_secret. 
特别会填写一个"有效地址", (个人猜测)这个"有效地址"就是redirectURI的主域名. 
然后每次请求都校验redirectURI与client_id对应填写的"有效地址"是否匹配.  
这样能避免的问题:  
&nbsp;&nbsp;假设client_B拿到了用户root在client_A的access_token、refresh_token, client_A的id、secret, redirectURI也不能跳转到client_B的网站内.  
(以上感觉是自己在瞎想...)
  
另外, 我并未在QQ的文档中找到refresh_token的有效期, 但个人感觉不应该无限.   
因为, client会一直持有refresh_token, 如果用户不去server取消授权, 意味着一直可以通过这个refresh_token获取到有效的access_token.  
对用户体验不好, 所以refresh_token也应该存在有效时长.  

**这有个重要的问题: refresh_token、access_token什么时候会改变?**  
&nbsp;&nbsp;在我的测试代码中, 这2个值始终保持不变(有效期重置). 这产生的问题是:  
  1) 假设access_token不变, 当这个值泄漏后, 表示黑客可以用这个access_token一直获取resources.  
  (所以, 有一种做法是, 用户每次在client登陆或重新授权后, server都返回新的access_token给client.  
  这样能避免一个access_token长期可以获取resources)  
  
  2) 或许正是因为1), access_token长期有效导致不安全, 所以OAuth2才推出了refresh_token(我看到有博客这么说, 不知道对不对).  
  即, client用access_token请求resources, server校验此access_token是否有效. server返回无效, client则通过有效的refresh_token获取一个有效的access_token.  
  又产生了一个问题, refresh_token也泄漏了怎么办?  
  同1)的做法, 不仅返回新的access_token, 同时返回新的refresh_token.
  
梳理下来才发现每次返回新值可能更好.  
用户授权后每次在client登陆时, server都返回新的access_token、refresh_token给client.  
那么即使同时泄漏token, 只要用户重新在client登陆一次, 那么泄漏的access_token和refresh_token就无效了.    
  
  


  