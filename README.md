# basic-service

使用 spring boot、spring cloud、nacos、rabbitmq、redis、minio 等框架搭建的为服务基础服务。

## 功能介绍

1. authentication： 认证服务，用于统一的登陆/登出，操作审计，权限等业务管理功能。
2. captcha: 验证码服务，用于发送验证码使用，包含短信，邮箱，图片验证码的一些基础业务功能。
3. config: 配置管理服务，主要用于对整个系统的枚举、数据字典、环境变量，通讯加解密的管理进行一些简单的业务开发。
4. file-manager: 文件管理服务，使用 minio 对一些文件进行 curd 的业务功能开发。
5. gateway: 整个为服务的网关，使用 spring gateway 进行接口自动路由，对一些常用的链接进行数据的加密解密等业务进行开发。
6. message: 消息管理，对站内信，短信，邮件等业务进行一些简单的业务开发。

整个项目的前端在 [basic-admin](https://github.com/dactiv/basic-admin) 进行实现