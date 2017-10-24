# OAuth2.0
需要redis作为缓存。
redis-key:
    
    1. 缓存auth_code
    type: string-json
    key: auth_code
    value: 服务器上(开通授权客户端的信息)的client_id、client_secret、及生成的auth_code的json对象
    expired: 10min
    use: 获取access_token时确保auth_code、client_id、client_secret匹配, 且一个code在有效期内只能生成一个access_token.
      (因为为了验证匹配, 所以json中才保存了client_id、client_secret)
    2.     
    
ClientAuthBean有2个作用(只是测试不想写太多, 所以就用的一个JavaBean)
    1. 缓存auth_code, 并保存匹配校验信息
    2. 缓存access_token、refresh_token