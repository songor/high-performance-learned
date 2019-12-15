# high-performance-learned

* 分层设计

  * 接入层

    View Object：与前端对接的模型，隐藏内部实现，供展示的聚合模型

  * 业务层

    Domain Model：领域模型，业务核心模型，拥有生命周期，贫血并以服务输出能力

  * 数据层

    Data Object：数据模型，同数据库映射，用以 ORM 方式操作数据库的能力模型

* 