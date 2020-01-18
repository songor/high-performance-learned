# high-performance-learned

* 分层设计

  * 接入层

    View Object：与前端对接的模型，隐藏内部实现，供展示的聚合模型

  * 业务层

    Domain Model：领域模型，业务核心模型，拥有生命周期，贫血并以服务输出能力

  * 数据层

    Data Object：数据模型，同数据库映射，用以 ORM 方式操作数据库的能力模型

* 安全地传输密码（TODO）

  * 哈希散列
  * 对称加密
  * 非对称加密
  * https

* 跨域问题（TODO）

  * JSONP
  * CORS
  * 代理法

### 云端部署，性能压测

* 系统构建

  CentOS 7.4 64位

* Java 环境安装

  * WinSCP 拷贝 jdk-8u231-linux-x64.rpm 至 ESC /tmp

  * chmod 777 jdk-8u231-linux-x64.rpm

  * rpm -ivh jdk-8u231-linux-x64.rpm

  * vim ~/.bash_profile

    JAVA_HOME=/usr/java/{folder_name}

    PATH=$PATH:$JAVA_HOME/bin

  * source ~/.bash_profile

  * java -version

* 数据库环境安装

  * yum install mysql*
  * yum install mariadb-server
  * systemctl start mariadb.service
  * ps -ef | grep mysql
  * netstat -anp | grep 3306
  * mysqladmin -u root password root
  * mysql -uroot -proot
  * show databases;

* mysqldump

  * cd C:\developer\mysql-8.0.18\bin
  * mysqldump -uroot -proot seckill > C:\developer\high-performance-learned\seckill.sql
  * WinSCP 拷贝 seckill.sql 至 ESC /tmp
  * mysql -uroot -proot < /tmp/seckill.sql

* deploy 脚本

  nohup java -jar seckill.jar --spring.config.addition-location=application.properties

  ./deploy.sh &

* jmeter 性能压测

  Thread Group / HTTP Request / View Results Tree / Aggregate Report

* 并发容量问题

  * 查看进程树

    pstree -p 16636

    pstree -p 16636 | wc -l

  * 查看服务器负载

    top -H

    关注 load average / %Cpu(s): us, sy

  * server 端并发线程数

    Spring Boot spring-configuration-metadata.json

    server.tomcat.accept-count 100

    Maximum queue length for incoming connection requests when all possible request processing threads are in use.

    server.tomcat.max-connections 10000

    Maximum number of connections that the server accepts and processes at any given time. Once the limit has been reached, the operating system may still accept connections based on the \"acceptCount\" property.

    server.tomcat.max-threads 200

    server.tomcat.min-spare-threads 10

  * 优化方向

    * 单 Web 容器上限

      线程数量：4 核 CPU 8G 内存单进程调度线程数 800 - 1000 以上后即花费巨大的时间在 CPU 调度上

      等待队列长度：队列做缓冲池用，但也不能无限长，消耗内存，出队入队也消耗 CPU

    * MySQL 数据库 QPS 容量

      主键查询：千万级别数据 1 - 10 毫秒

      唯一索引查询：千万级别数据 10 - 100 毫秒

      非唯一索引查询：千万级别数据 100 - 1000 毫秒

      无索引：百万条数据 1000 毫秒 +

    * MySQL 数据库 TPS 容量

      更新、删除：同查询

      插入：1w - 10w TPS（依赖配置优化）

* 定制化内嵌 tomcat

  * keepAliveTimeOut
  * maxKeepAliveRequests
  * implements WebServerFactoryCustomizer\<ConfigurableWebServerFactory\>

### 分布式扩展

* Nginx 反向代理负载均衡 & 服务端水平对称部署 & MySQL 开放远端连接

  * scp -r /var/www/ root@172.24.129.190:/var/

    scp /tmp/jdk-8u212-linux-x64.rpm  root@172.24.129.190:/tmp

    scp -r /var/www/ root@172.24.129.191:/var/

    scp /tmp/jdk-8u212-linux-x64.rpm  root@172.24.129.191:/tmp

  * yum install telnet

    telnet 172.24.129.189 3306

    use mysql

    show tables;

    select host,user,password from user;

    grant all privileges on \*.\* to root@172.24.129.190 identified by 'root';

    grant all privileges on \*.\* to root@172.24.129.191 identified by 'root';

    flush privileges;

  * 外部化 spring.datasource.url 配置

  * Nginx 安装

    netstat -an | grep 80

    wget https://openresty.org/download/openresty-1.15.8.2.tar.gz

    tar -xvzf openresty-1.15.8.2.tar.gz

    cd openresty-1.15.8.2

    yum install pcre-devel openssl-devel gcc curl

    ./configure

    make

    make install

* Nginx 使用

  * 静态 Web 服务器

    * 启动 Nginx

      cd /usr/local/openresty/nginx/

      sbin/nginx -c conf/nginx.conf

      拷贝静态资源文件至 /usr/local/openresty/nginx/html

    * 配置静态资源 location

      location /resources/ {
          alias  /usr/local/openresty/nginx/html/resources/;
      }

      mv *.html resources/（除 50x.html 和 index.html）

      mv host.js resources/

      cp -r static resources/

      rm -rf static/

    * 修改 host.js

      var global_host = "39.104.118.251";
      
    * 无缝重启

      sbin/nginx -s reload

  * 动静分离服务器

    /resources 路径为静态资源使用；其他路径为动态资源使用

  * 反向代理服务器

    * 配置 upstream server

      upstream seckill_application_server {
          server 172.24.129.190:8080 weight=1;
          server 172.24.129.191:8080 weight=1;
      }

    * 配置动态资源 location

      location / {
          proxy_pass http://seckill_application_server;
          proxy_set_header Host $http_host:$proxy_port;
          proxy_set_header X-Real-IP $remote_addr;
          proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      }

    * 开启 tomcat access log 验证

      server.tomcat.accesslog.enabled=true
      server.tomcat.accesslog.directory=/var/www/seckill/accesslog
      server.tomcat.accesslog.pattern=%h %l %u %t "%r" %s %b %D

* Nginx 开启 Keepalive

  worker_processes  10;

  events {
      worker_connections  10240;
  }

  upstream seckill_application_server {
      server 172.24.129.190:8080 weight=1;
      server 172.24.129.191:8080 weight=1;
      keepalive 30;
  }

  location / {
      proxy_pass http://seckill_application_server;
      proxy_set_header Host $http_host:$proxy_port;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_http_version 1.1;
      proxy_set_header Connection "";
  }

* JMeter 拾遗
  * [Apache JMeter: a Powerful Tool for Performance and Load Testing](https://www.logicify.com/en/blog/apache-jmeter-a-powerful-tool-for-performance-and-load-testing/)
  * [Apache JMeter](https://jmeter.apache.org/usermanual/get-started.html)
  * setenv.sh -> export HEAP="-Xms2G -Xmx2G -XMaxMetaspaceSize=512m"
  * jmeter -n -t [jmx file] -l [results file] -e -o [Path to web report folder]
  * jmeter -Jusers=500 -Jseconds=5 -Jcounts=50 -Jip=39.104.166.225 -Jport=8080 -n -t seckill_item_get.jmx -l seckill_item_get.jtl -e -o report
  
* Nginx 高性能原因

  * epoll 多路复用

    Java NIO -> Linux 2.6 内核之前使用 Linux select 模型，之后使用 Linux epoll 模型

    Java BIO -> Linux select 模型（变更触发轮询查找） -> Linux epoll 模型（变更触发回调函数）

  * master worker 进程模型

    * master 和 worker 是父子进程
    * accept_mutex
    * master 负责 CONNECT（epoll），worker 负责 ACCEPT、SEND、RECV（epoll）
    * 每个 worker 进程只有一个线程

  * 协程机制（类比 nodejs 异步编程模型）

    * 依附于线程的内存模型，切换开销小（没有 CPU 切换开销，仅内存切换开销）
    * 遇阻塞归还执行权，代码同步编写
    * 无需加锁

* 分布式会话

  * 基于 cookie 传输 sessionid（tomcat 容器 session 实现迁移到 redis）
    * [redis Windows](https://github.com/microsoftarchive/redis/releases)
    * [redis Installation](https://redis.io/download)
    * vim redis.conf -> bind 172.24.129.189
    * src/redis-server ./redis.conf &
    * application.properties -> spring.redis.host=172.24.129.189
  * 基于 token 传输类似 sessionid

### 多级缓存

* 缓存设计原则

  * 快速存取设备，内存
  * 将缓存推到离用户最近的节点
  * 缓存同步、清理

* Redis 缓存

  * 单机版
  * sentinel 机制
  * cluster 机制

* 本地热点缓存

  * 热点数据
  * 脏读非常不敏感
  * 内存可控
  * Guava Cache
    * 可控的大小和超时时间
    * 可配置的 LRU 策略
    * 线程安全

* Nginx Proxy Cache

  * Nginx 反向代理

  * 依靠内存缓存文件地址

  * 依靠文件系统存储索引级文件

  * nginx.conf

    ```nginx
    proxy_cache_path /usr/local/openresty/nginx/tmp_cache levels=1:2 keys_zone=tmp_cache:100m inactive=7d max_size=10g;
    
    location / {
        proxy_cache tmp_cache;
        proxy_cache_key $uri;
        proxy_cache_valid 200 206 304 302 7d;
    }
    ```

* Nginx Lua 缓存

  * 协程机制

  * Lua 协程

    coroutine.yield()、coroutine.resume()

  * Nginx 协程

    * Nginx 的每一个 Worker 进程都是在 epoll 或 kqueue 这种事件模型之上，封装成协程
    * 每一个请求都有一个协程进行处理
    * 即使 ngx_lua 需要运行 Lua，相对于 C 有一定的开销，但依旧能保证高并发能力

  * Nginx 协程机制（同步编程模型）

    * Nginx 每个工作进程创建一个 Lua VM
    * 工作进程内的所有协程共享一个 Lua VM
    * 每个外部请求由一个 Lua 协程处理，之间数据隔离
    * Lua 代码调用 IO 等异步接口时，协程被挂起，保存上下文数据，不阻塞工作进程
    * IO 等异步操作完成后还原协程上下文，代码继续执行

  * Nginx 处理阶段

    * NGX_HTTP_POST_READ_PHASE
    * NGX_HTTP_SERVER_REWRITE_PHASE -> rewrite_handler
    * NGX_HTTP_FIND_CONFIG_PHASE
    * NGX_HTTP_REWRITE_PHASE -> rewrite_handler
    * NGX_HTTP_POST_REWRITE_PHASE
    * NGX_HTTP_PREACCESS_PHASE -> limit_conn_handler limit_req_handler
    * NGX_HTTP_ACCESS_PHASE -> auth_basic_handler access_handler
    * NGX_HTTP_POST_ACCESS_PHASE
    * NGX_HTTP_TRY_FILES_PHASE
    * NGX_HTTP_CONTENT_PHASE -> static_handler
    * NGX_HTTP_LOG_PHASE -> log_handler

  * Nginx Lua 插载点

    * Initialization Phase

      init_by_lua

      init_worker_by_lua

    * Rewrite / Access Phase

      ssl_certificate_by_lua

      set_by_lua

      rewrite_by_lua

      access_by_lua

    * Content Phase

      balancer_by_lua content_by_lua

      header_filter_by_lua

      body_filter_by_lua

    * Log Phase

      log_by_lua

  * OpenResty

    * init_by_lua

      vim /usr/local/openresty/lua/custom_init.lua

      ```lua
      ngx.log(ngx.NOTICE, "custom init lua");
      ```

      vim /usr/local/openresty/nginx/conf/nginx.conf

      ```nginx
      init_by_lua_file ../lua/custom_init.lua;
      ```

    * content_by_lua

      vim /usr/local/openresty/nginx/conf/nginx.conf

      ```nginx
      location /helloworld {
          default_type "text/html";
          content_by_lua_file ../lua/helloworld.lua;
      }
      ```

      vim /usr/local/openresty/lua/helloworld.lua

      ```lua
      ngx.say("hello world lua");
      ```

    * [概览](http://openresty.org/cn/)

    * openresty hello world

      vim /usr/local/openresty/lua/item_2.lua

      ```lua
      ngx.exec("/item/get?id=2");
      ```

      vim /usr/local/openresty/nginx/conf/nginx.conf

      ```nginx
      location /item_2 {
          content_by_lua_file ../lua/item_2.lua;
      }
      ```

    * shared dic（共享内存字典，所有 worker 线程可见，LRU 淘汰）

      vim /usr/local/openresty/lua/item_detail.lua

      ```lua
      function get_cache(key)
          local ngx_cache = ngx.shared.item_detail_cache
          local value = ngx_cache:get(key)
          return value
      end
      
      function set_cache(key,value,expire)
          if not expire then
              expire = 0
          end
          local ngx_cache = ngx.shared.item_detail_cache
          local succ,err,forcible = ngx_cache:set(key,value,expire)
          return succ
      end
      
      local args = ngx.req.get_uri_args()
      local id = args["id"]
      local item_model = get_cache("item_"..id)
      if item_model == nil then
          local resp = ngx.location.capture("/item/get?id="..id)
          item_model = resp.body
          set_cache("item_"..id,item_model,60)
      end
      ngx.say(item_model)
      ```

      vim /usr/local/openresty/nginx/conf/nginx.conf

      ```nginx
      lua_shared_dict item_detail_cache 128m;
      
      location /lua/item/get {
          default_type "application/json";
          content_by_lua_file ../lua/item_detail.lua;
      }
      ```

    * openresty redis 支持（master slave 模式；只读不写；下游更新 redis，脏数据影响小）

      /usr/local/openresty/lualib/resty/redis.lua

      vim /usr/local/openresty/lua/item_detail_redis.lua

      ```lua
      local args = ngx.req.get_uri_args()
      local id = args["id"]
      local redis = require "resty.redis"
      local cache = redis:new()
      local ok,err = cache:connect("172.24.129.189",6379)
      local item_model = cache:get("item_"..id)
      if item_model == ngx.null or item_model == nil then
          local resp = ngx.location.capture("/item/get?id="..id)
          item_model = resp.body
      end
      ngx.say(item_model)
      ```

      vim /usr/local/openresty/nginx/conf/nginx.conf

      ```nginx
      location /lua/item/get {
          default_type "application/json";
          #content_by_lua_file ../lua/item_detail.lua;
          content_by_lua_file ../lua/item_detail_redis.lua;
      }
      ```

### 页面静态化

* 静态资源 CDN（content delivery network）

* cache control 响应头

  * private：客户端可以缓存

  * public：客户端和代理服务器都可以缓存

  * max-age=xxx：缓存内容将在 xxx 秒后失效

  * no-cache：使用缓存内容前强制向服务端验证是否可用

    * ETag & If-None-Match

      ETag：资源唯一标识

    * Last-modified & If-Modified-Since

      List-modified：资源最后被修改的时间

  * no-store：不缓存内容

* 浏览器三种刷新方式

  * 回车刷新或 a 链接

    看 cache control 对应的 max-age 是否有效，有效则直接使用缓存内容，否则若 cache-control 为 no-cache，则进入缓存协商逻辑

  * F5 刷新或 command + R 刷新

    去掉 cache-control 中的 max-age 或直接设置 max-age 为 0，然后进入缓存协商逻辑

  * ctrl + F5 或 command + shift + R 刷新

    去掉 cache-control 中的 max-age 或直接设置 max-age 为 0，并且去掉协商头，强制刷新

* CDN 自定义缓存策略

  * 自定义目录过期时间
  * 自定义后缀名过期时间
  * 自定义目录、后缀名对应权重
  * 通过界面或 API 强制 CDN 刷新

* 静态资源部署策略

  * css、js、img 等元素使用带版本号部署，例如 xxx.js?v=1.0（html 置为 no-cache） -> 不便利且维护困难
  * css、js、img 等元素使用带摘要部署，例如 xxx.js?v=45edw -> 存在先部署 html 还是先部署资源的覆盖问题
  * css、js、img 等元素使用摘要做文件名部署，例如 45edw.js，新老版本并存且可回滚，资源部署完后再部署 html
  * 对应静态资源保持生命周期内不会变，max-age 可设置很长
  * html 文件设置 no-cache 或较短 max-age，以便于更新
  * html 文件仍然设置较长的 max-age，依靠动态获取版本号请求发送到后端，异步下载最新版本号的 html 后展示
  * 动态请求也可以静态化成 json 资源推送到 CDN 上
  * 依靠异步请求获取后端节点对应资源状态
  * 通过跑批推送 CDN 内容

* 全页面静态化

  * html、css、js 静态资源 CDN + ajax 动态请求 CDN
  * 在服务端完成 html、css 甚至 js 的 load 渲染成纯 html 文件后直接以静态资源的方式部署到 CDN 上
  * [phantomjs](https://phantomjs.org/)
    * 使用 init_view 和 get_init 方式防止多次初始化（item_detail_2.html）
    * 轮询生成内容（item_detail_2.js）
    * 将全静态化页面推送到 CDN（http://39.104.234.19/resources/item_detail_2.html?id=2）

### 缓存库存

* 交易验证优化（Redis）

  * 用户风控策略优化

  * 活动校验策略优化

    引入活动发布流程，紧急下线能力

* 缓存库存模型优化

  * 库存行锁优化

    ALTER TABLE item_stock ADD UNIQUE INDEX item_id_index (item_id);

  * 扣减库存缓存化

    活动发布同步库存进缓存，下单交易减缓存库存（数据库记录不一致）

  * 异步同步数据库

    活动发布同步库存进缓存，下单交易减缓存库存，异步消息扣减数据库库存

    问题：异步消息发送失败（Producer）、扣减操作执行失败（Consumer）、正确回补库存

  * 最终一致性保证

* 异步消息队列 [RocketMQ](https://rocketmq.apache.org/docs/quick-start/)

  * wget http://ftp.cuhk.edu.hk/pub/packages/apache.org/rocketmq/4.6.0/rocketmq-all-4.6.0-bin-release.zip

    yum install unzip

    unzip rocketmq-all-4.6.0-bin-release.zip

  * vim runserver.sh & vim runbroker.sh & vim tools.sh 修改 JAVA_OPT

    nohup sh bin/mqnamesrv &

    nohup sh bin/mqbroker -n localhost:9876 &

  * export NAMESRV_ADDR=localhost:9876

    sh bin/tools.sh org.apache.rocketmq.example.quickstart.Producer

    sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer

  * vim tools.sh -> 修改 JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${BASE_DIR}/lib:${JAVA_HOME}/jre/lib/ext:/usr/java/jdk1.8.0_212-amd64/jre/lib/ext"

    ./mqadmin updateTopic -n localhost:9876 -t seckill_stock -c DefaultCluster

  * 配置安全组规则，添加 9876/9876 和 10911/10911

  * windows

    配置环境变量 ROCKETMQ_HOME

    start mqnamesrv.cmd

    修改 runbroker.cmd -> set "JAVA_OPT=%JAVA_OPT% -cp "%CLASSPATH%""

    start mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true

    start mqadmin updateTopic -n localhost:9876 -t seckill_stock -c DefaultCluster

* 分布式事务（ACID、CAP、BASE）

* 业务系统热点问题（TODO）

### 事务型消息

* [RocketMQ transaction example](https://rocketmq.apache.org/docs/transaction-example/)

* 库存流水

  * 数据类型
    * 主业务数据 master data
    * 操作型数据 log data -> 异步操作

  * 业务场景决定高可用技术
  
    少卖 / 超卖 -> Redis 失效不回源 / 回源数据库
  
  * 库存售罄

* 销量逻辑异步化 / 下单逻辑异步化

### 流量削峰技术

* 缺陷

  秒杀下单接口会被脚本不停地刷

  秒杀验证逻辑和秒杀下单接口强关联，代码冗余度高

  秒杀验证逻辑复杂，对交易系统产生无关联负载

* 秒杀令牌的原理和使用方式

  * 原理

    秒杀接口需要依靠令牌才能进入

    秒杀的令牌由秒杀活动模块负责生成

    秒杀活动模块对秒杀令牌生成全权处理，逻辑收口

    秒杀下单前需要先获得秒杀令牌

  * 缺陷

    秒杀令牌只要活动一开始就无限制生成，影响系统性能

* 秒杀大闸的原理和使用方式

  * 原理

    依靠秒杀令牌的授权原理定制发牌逻辑，做到大闸功能

    根据秒杀商品初始库存颁发对应数量令牌，控制大闸流量

    用户风控策略前置到秒杀令牌发放中

    库存售罄判断前置到秒杀令牌发放中

  * 缺陷

    浪涌流量涌入后系统无法应付

    多库存、多商品等令牌限制能力弱

* 队列泄洪的原理和使用方式

  * 原理

    排队有些时候比并发更高效（例如 redis 单线程模型，InnoDB mutex key 等）

    依靠排队去限制并发流量

    依靠排队和下游拥塞窗口程度调整队列释放流量大小

  * 本地、分布式

    本地：将队列维护在本地内存中（线程池）-> 避免网络开销、高可用性

    分布式：将队列设置在外部 redis 中

    分布式 + 本地

### 防刷限流技术

* 验证码生成与验证技术

  * 验证码

    包装秒杀令牌前置，需要验证码来错峰

    数学公式验证码生成器

* 限流原理与实现

  * 目的

    流量远比你想象的要多

    系统活着比挂了要好

    宁愿让少数人能用，也不要让所有人都不能用

  * 限流方案

    限并发

    令牌桶算法：应对突发流量

    漏桶算法：平滑网络流量以固定的速率流入

  * 限流粒度

    接口维度

    总维度

  * 限流范围

    集群限流：依赖 redis 或其他中间件技术做统一计数器，往往会产生性能瓶颈

    单机限流：负载均衡的前提下单机平均限流效果更好

  * Guava RateLimiter

* 防黄牛技术

  令牌、排队、限流均只能控制总流量，无法控制黄牛流量

  * 传统防刷

    限制一个会话（session、token）同一秒、分钟接口调用多少次 -> 多会话接入绕开

    限制同一个 ip 同一秒、分钟接口调用多少次 -> 数量不好控制，容易误伤

  * 黄牛为什么难防

    模拟器作弊：模拟硬件设备，可修改设备信息

    设备牧场作弊：工作室里一批移动设备

    人工作弊：靠佣金吸引兼职人员刷单

  * app 加固技术

    防止反编译；H5 和 app 端接口隔离，H5 端有更多限制

  * 设备指纹

    采集终端设备各项参数，启动应用时生成唯一设备指纹

    根据对应指纹的参数猜测出模拟器等可疑设备概率

  * 凭证系统

    根据设备指纹下发凭证

    关键业务链路上带上凭证并由业务系统到凭证服务器上验证

    凭证服务器根据对应凭证所等价的设备指纹参数并根据实时行为风控系统判定对应凭证的可疑度分数，若分数低于某个数值则由业务系统返回固定错误码，拉起前端验证码验身，验身成功后加入凭证服务器对应分数

### 总结

* 核心知识点

  * 分布式
    * nginx 反向代理
    * 分布式会话管理
  * 查询多级缓存
    * redis 缓存
    * 本地缓存
    * 静态请求 cdn
    * 动态请求缓存
    * 页面静态化
  * 交易泄压
    * 缓存库存
    * 交易异步化
    * 异步化事务
  * 流量错峰
    * 秒杀令牌
    * 秒杀大闸
    * 队列泄洪
  * 防刷限流
    * 验证码
    * 限流器
    * 防黄牛
  * 性能测试
    * jmeter 压测
    * 压测优化

* 项目框架

  结构分层、业务逻辑分层、领域模型分层

* 性能压测框架

  tomcat 容器优化通用方案，管道（keepalive）优化通用方案

* 分布式扩展

  nginx 负载均衡设计

  水平扩展 vs 垂直扩展（nginx、redis、MySQL）

* 多级缓存

  读不到（预热机制） vs 脏读（业务层面容忍度）

  越近越好的缓存

* 页面静态化

  CDN 的美妙设计

  一切皆页面，一切皆静态

* 缓存库存

  交易验证（性能和正确性的权衡）

  库存模型（性能和可用性的权衡）

* 事务型消息

  ACID vs CAP & BASE

  最终一致性方案

* 流量错峰技术

  防洪峰、防击穿（令牌）

  防浪费（库存映射）

  排队

* 防刷限流

  错峰（验证码）

  限流（Guava RateLimiter）

  防黄牛

### 登录态管理

* 分布式会话持久性管理（redis cluster、redis 集群）

  * 会话有效期

    Tomcat 默认为 30 分钟

    不与服务端发生交互的呆滞时间

  * 会话续命

    触发操作延长生命周期

    延长到 30 分钟

  * 安全性管理

    url query string，GET 请求参数 -> token 暴露在 URL 上，不安全

    自定义 header -> 仍然是明文传输，不安全

    用安全传输的 https -> 仅保证数据加密传输，无法解决浏览器端调试而盗取用户请求内容，模拟用户操作

    自定义协议 -> 由于无法调试 app，只能通过抓包，而自定义协议报文可能为二进制，使得无法被解析

* 强登录态与弱登录态

  * 强登录态（下单）

  * 无需登录（浏览）

  * 弱登录态（同时支持游客模式和登录模式）

    场景：千人千面的智能推荐

    续命能力：请求续命、Keepalive 续命

* SSO 单点登录

  非同域名下，访问手机 H5 wap application 和 PC 网站 application 会携带不同的 cookie_id，因此引入 SSO

  * 同域名

    www.app.com/wap/** 和 www.app.com/pc** -> 只需要确保使用相同的 cookie 存储

  * 根域名相同而子域名不同

    wap.app.com 和 www.app.com -> domain=/

  * 域名不相同

    app.com 和 bpp.com -> 引入 SSO 服务鉴权 sso_cookie

* 小结

  * OAuth 2.0

### MySQL 性能优化

* 通用性能优化

  缓存、异步、批处理

* MySQL 应用性能优化

  * 写操作（批量 insert，批量 update）

    sql 编译 n 次和 1 次的时间与空间复杂度

    网络消耗的时间复杂度

    磁盘寻址的复杂度

  * 读操作（索引）

  * 搜索

    模糊查询 like '%%' -> 搜索引擎

* MySQL 单机配置性能优化（4 核 8G）

  * max_connection=1000
  * write ahead log（undo / redo log）
    * innodb_file_per_table=1（寻址，data file）
    * innodb_buffer_pool_size=6G（60% - 80%，data buffer）
    * innodb_log_file_size=256M（undo / redo log）
    * innodb_log_buffer_size=16M（undo / redo buffer）
    * innodb_flush_log_at_trx_commit=2
    * innodb_data_file_path=ibdata1:1G;ibdata2:1G;ibdata3:1G:autoextend

* MySQL 分布式配置性能优化

  * 主从扩展

    * 实现

      开启 bin_log；设置主从同步账号，配置主从同步

    * 主从切换问题

      master 和 slave 无法保证强一致性

      undo / redo log 成功，bin log 失败，bin log 无法真实反映 master 情况

### MySQL 分布式架构

* 主从性能优化

  备份；读写分离

  半同步机制

* 多主多从

  * 数据分片

    hash + mod

  * 分片维度

    尽可能保证一次查询请求几乎命中在一个数据库上

    固定路由位（user_id）

    时间自增分片（月、年）

  * 分片冗余一致性保障

    application -> 用户订单 -> RocketMQ -> 商户订单

  * 无迁移扩展

    mod 位数据迁移

    弹性自增 -> 在原本的路由规则之上添加另外一套路由规则，如 order_id 时间戳 > 2020.01.01

* ACID，CAP，BASE

  * master slave -> AP
  * master slave semi sync（二阶段提交，事务协调者 + 多事务处理接收者）-> CP
  * zookeeper -> 单调一致性（半数以上节点一致 / 可用）