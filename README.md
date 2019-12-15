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

    * Spring Boot spring-configuration-metadata.json

      server.tomcat.accept-count

      server.tomcat.max-connections

      server.tomcat.max-threads

      server.tomcat.min-spare-threads

    * 

  * 

* 