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

  


