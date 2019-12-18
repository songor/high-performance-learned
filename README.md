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
  * mysqldump -uroot -proot seckill > C:\developer\high-performance-learned\seckill-sql\seckill.sql
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

* Nginx

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

* 
