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
### 3.1 package划分  
  1. \*.sina.*: port=8080, 测试sina weibo登陆.  
  
  2. \*.server.*: port=8081, 本地模拟授权服务端. 
  3. \*.client.*: port=8082, 本地模拟授权客户端. 

### 3.2 sina weibo登陆测试
#### 3.2.1 准备工作
  1. 到http://open.weibo.com申请获取appKey、appSecret, 提供redirectURI。
  2. 修改application-sina.propeties中的配置。  
#### 3.2.2 启动服务
  1. 启动SinaClientApplication。
  2. 访问http://127.0.0.1:8080/sina/login;点击登陆界面中的\[跳转到sina-weibo登陆],跳转到sina的授权登陆页面。
  3. sina回调填写redirectURI, 页面展示得到sina回传的信息。  
  4. 如果(3.)中得到正确的access_token, 则会显示按钮\[根据access_token获取sina用户信息.], 点击会用access_token&uid去调用API获取资源。  
#### 3.2.3 授权逻辑
##### step.1 客户端引导用户到授权登陆页面
　　用户点击授权登陆(第三方登陆), 跳转到授权登陆页面.  
　　client请求(GET/POST): https://api.weibo.com/oauth2/authorize; 及API规定参数。  

##### step.2 用户被引导到sina的授权页面, 获取auth_code和access_token  
　　(登陆授权分离)
　　用户在sina授权页面, 先登陆 -\> 再授权, 获取auth_code。响应结果auth_code以query-param的形式请求redirectURI.  
　　client在redirectURI得到auth_code, 再用此auth_code去请求获取access_token(POST, https://api.weibo.com/oauth2/access_token)
  
##### step.3 client获取用户授权信息  
　　client用(step.2)得到access_token、uid, 去请求相应的API得到相应的授权信息。  

### 3.3 server&client测试  
　　server设计说明(只是个人理解后觉得好的方案):  

    1. 每次用户登陆(授权)时
        a) 返回新的access_token值及重置有效期;
        b) 返回新的refrsh_token值, 如果有授权操作, 重置有效期; 没有授权操作, 沿用旧的refresh_token的剩余有效时长。
	2. client调用refresh_token自动延续授权时:
        a) 返回新的access_token值及重置有效期;
        b) refresh_token值保持不变, 有效期看情况需不需要刷新。(个人觉得不刷新好)

具体server这样设计的原因参考博客: (暂时还未写, 代码实现也未改)