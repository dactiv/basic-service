SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_authentication_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_authentication_info`;
CREATE TABLE `tb_authentication_info`
(
    `id`            int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
    `user_id`       int(20) NOT NULL COMMENT '用户 id',
    `type`          varchar(32) NOT NULL COMMENT '用户类型',
    `ip`            varchar(32) NOT NULL COMMENT 'ip 地址',
    `device`        json         DEFAULT NULL COMMENT '设备信息',
    `province`      varchar(64)  DEFAULT NULL COMMENT '省',
    `city`          varchar(64)  DEFAULT NULL COMMENT '市',
    `area`          varchar(64)  DEFAULT NULL COMMENT '区域',
    `sync_status`   tinyint(4) DEFAULT '0' COMMENT '同步 es 状态：0.处理中，1.成功，99.失败',
    `retry_count`   tinyint(4) DEFAULT '0' COMMENT '重试次数',
    `remark`        varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='认证信息表';

-- ----------------------------
-- Table structure for tb_console_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_console_user`;
CREATE TABLE `tb_console_user`
(
    `id`            int(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
    `email`         varchar(64)  DEFAULT NULL COMMENT '邮箱',
    `password`      char(64)    NOT NULL COMMENT '密码',
    `status`        tinyint(4) NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
    `username`      varchar(32) NOT NULL COMMENT '登录帐号',
    `gender`        tinyint     NOT NULL COMMENT '性别:10.男,20.女',
    `real_name`     varchar(16) NOT NULL COMMENT '真实姓名',
    `groups_info`   json         DEFAULT NULL COMMENT '组信息',
    `resource_map`  json         DEFAULT NULL COMMENT '资源信息',
    `remark`        varchar(128) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_username` (`username`) USING BTREE,
    UNIQUE KEY `ux_email` (`email`) USING BTREE
) ENGINE=InnoDB COMMENT='后台用户表';


-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
DROP TABLE IF EXISTS `tb_group`;
CREATE TABLE `tb_group`
(
    `id`            int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
    `name`          varchar(32) NOT NULL COMMENT '名称',
    `authority`     varchar(64)  DEFAULT NULL COMMENT 'spring security role 的 authority 值',
    `sources`       json        NOT NULL COMMENT '来源',
    `parent_id`     int(20) DEFAULT NULL COMMENT '父类 id',
    `removable`     tinyint(4) NOT NULL COMMENT '是否可删除:0.否、1.是',
    `modifiable`    tinyint(4) NOT NULL COMMENT '是否可修改:0.否、1.是',
    `status`        tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:0.禁用、1.启用',
    `resource_map`  json         DEFAULT NULL COMMENT '资源信息',
    `remark`        varchar(128) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_name` (`name`) USING BTREE,
    UNIQUE KEY `ux_authority` (`authority`)
) ENGINE=InnoDB COMMENT='用户组表';

-- ----------------------------
-- Table structure for tb_member_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_member_user`;
CREATE TABLE `tb_member_user`
(
    `id`                int(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `creation_time`     datetime(3) NOT NULL COMMENT '创建时间',
    `registration_time` datetime(3) NOT NULL COMMENT '注册时间',
    `update_version`    int(20) NOT NULL DEFAULT '1' COMMENT '更新版本号',
    `username`          varchar(64) NOT NULL COMMENT '登录帐号',
    `gender`            tinyint     NOT NULL COMMENT '性别:10.男,20.女',
    `password`          char(64)    NOT NULL COMMENT '密码',
    `email`             varchar(64) DEFAULT NULL COMMENT '邮箱',
    `phone`             varchar(24) DEFAULT NULL COMMENT '备注',
    `status`            tinyint(4) NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
    `initialization`    json        DEFAULT NULL COMMENT '初始化信息',
    `groups_info`       json        DEFAULT NULL COMMENT '组信息',
    `resource_map`      json        DEFAULT NULL COMMENT '资源信息',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_username` (`username`) USING BTREE,
    UNIQUE KEY `ux_email` (`email`) USING BTREE,
    UNIQUE KEY `ux_phone` (`phone`) USING BTREE
) ENGINE=InnoDB COMMENT='会员用户表';


SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `tb_group` (`id`, `creation_time`, `name`, `authority`, `sources`, `parent_id`, `removable`, `modifiable`,`status`, `resource_map`, `remark`) VALUES (1, '2021-09-13 13:24:01.080', '超级管理员', 'ADMIN', '[\"CONSOLE\", \"SYSTEM\", \"ALL\"]', NULL, 0, 1, 1, '{}', NULL);

INSERT INTO `tb_console_user` (`id`, `creation_time`, `email`, `password`, `status`, `username`, `real_name`,`groups_info`, `resource_map`, `remark`)
VALUES (1, '2021-08-18 09:40:46.953', 'admin@domian.com','$2a$10$U2787VFuFP9NMyxwdsP1bOmtvofTgwU5nLcdV7Gj3ZyhdiZO.T8mG', 1, 'admin', '超级管理员', '[{\"id\": 1, \"name\": \"超级管理员\", \"authority\": \"ADMIN\"}]', '{}', NULL);