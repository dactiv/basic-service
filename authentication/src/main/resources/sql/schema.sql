
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_authentication_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_authentication_info`;
CREATE TABLE `tb_authentication_info`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`  datetime(3) NOT NULL COMMENT '创建时间',
    `user_id`        int(11) NOT NULL COMMENT '用户 id',
    `type`           varchar(32) NOT NULL COMMENT '用户类型',
    `ip`             varchar(32) NOT NULL COMMENT 'ip 地址',
    `device`         varchar(32)  DEFAULT NULL COMMENT '设备名称',
    `province`       varchar(64)  DEFAULT NULL COMMENT '省',
    `city`           varchar(64)  DEFAULT NULL COMMENT '市',
    `area`           varchar(64)  DEFAULT NULL COMMENT '区域',
    `sync_status`    tinyint(4) DEFAULT '0' COMMENT '同步 es 状态：0.处理中，1.成功，99.失败',
    `retry_count`    tinyint(4) DEFAULT '0' COMMENT '重试次数',
    `remark`         varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='认证信息表';

-- ----------------------------
-- Records of tb_authentication_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_console_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_console_user`;
CREATE TABLE `tb_console_user`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `creation_time`  datetime(3) NOT NULL COMMENT '创建时间',
    `email`          varchar(64)  DEFAULT NULL COMMENT '邮箱',
    `password`       char(64)    NOT NULL COMMENT '密码',
    `status`         tinyint(4) NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
    `username`       varchar(32) NOT NULL COMMENT '登录帐号',
    `real_name`      varchar(16) NOT NULL COMMENT '真实姓名',
    `remark`         varchar(128) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_username` (`username`) USING BTREE,
    UNIQUE KEY `ux_email` (`email`) USING BTREE
) ENGINE=InnoDB COMMENT='系统用户表';

-- ----------------------------
-- Records of tb_console_user
-- ----------------------------
BEGIN;
INSERT INTO `tb_console_user` VALUES (1, '2020-03-01 21:57:46.000', 'admin@domian.com', '$2a$10$U2787VFuFP9NMyxwdsP1bOmtvofTgwU5nLcdV7Gj3ZyhdiZO.T8mG', 1, 'admin', '超级管理员', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_console_user_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_console_user_resource`;
CREATE TABLE `tb_console_user_resource`
(
    `user_id`     int(11) NOT NULL COMMENT 'tb_console_user 外键',
    `resource_id` int(11) NOT NULL COMMENT 'tb_resource 外键',
    PRIMARY KEY (`user_id`, `resource_id`) USING BTREE
) ENGINE=InnoDB COMMENT='系统用户与资源关联表';

-- ----------------------------
-- Records of tb_console_user_resource
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
DROP TABLE IF EXISTS `tb_group`;
CREATE TABLE `tb_group`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`  datetime(3) NOT NULL COMMENT '创建时间',
    `name`           varchar(32)  NOT NULL COMMENT '名称',
    `authority`      varchar(64)  DEFAULT NULL COMMENT 'spring security role 的 authority 值',
    `source`         varchar(128) NOT NULL COMMENT '来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部',
    `parent_id`      int(11) DEFAULT NULL COMMENT '父类 id',
    `removable`      tinyint(4) NOT NULL COMMENT '是否可删除:0.否、1.是',
    `modifiable`     tinyint(4) NOT NULL COMMENT '是否可修改:0.否、1.是',
    `status`         tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:0.禁用、1.启用',
    `remark`         varchar(128) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_name` (`name`) USING BTREE
) ENGINE=InnoDB COMMENT='用户组表';

-- ----------------------------
-- Records of tb_group
-- ----------------------------
BEGIN;
INSERT INTO `tb_group` VALUES (1, '2020-03-01 21:56:25.000', '超级管理员', 'ADMIN', 'Console', NULL, 0, 1, 0, NULL);
INSERT INTO `tb_group` VALUES (2, '2020-03-01 21:56:25.000', '普通用户', 'ORDINARY', 'UserCenter', NULL, 0, 1, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_group_console_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_group_console_user`;
CREATE TABLE `tb_group_console_user`
(
    `group_id` int(11) NOT NULL COMMENT 'tb_group 表外键',
    `user_id`  int(11) NOT NULL COMMENT 'tb_console_user 表外键',
    PRIMARY KEY (`group_id`, `user_id`) USING BTREE
) ENGINE=InnoDB COMMENT='系统用户与用户组关联表';

-- ----------------------------
-- Records of tb_group_console_user
-- ----------------------------
BEGIN;
INSERT INTO `tb_group_console_user` VALUES (1, 1);
COMMIT;

-- ----------------------------
-- Table structure for tb_group_member_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_group_member_user`;
CREATE TABLE `tb_group_member_user`
(
    `group_id` int(11) NOT NULL COMMENT 'tb_group 表外键',
    `user_id`  int(11) NOT NULL COMMENT 'tb_console_user 表外键',
    PRIMARY KEY (`group_id`, `user_id`) USING BTREE
) ENGINE=InnoDB COMMENT='会员用户与组关联表';

-- ----------------------------
-- Records of tb_group_member_user
-- ----------------------------
BEGIN;
INSERT INTO `tb_group_member_user` VALUES (2, 1);
COMMIT;

-- ----------------------------
-- Table structure for tb_group_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_group_resource`;
CREATE TABLE `tb_group_resource`
(
    `group_id`    int(11) NOT NULL COMMENT 'tb_group 外键',
    `resource_id` int(11) NOT NULL COMMENT 'tb_resource_外键',
    PRIMARY KEY (`group_id`, `resource_id`) USING BTREE
) ENGINE=InnoDB COMMENT='用户组与资源关联表';

-- ----------------------------
-- Records of tb_group_resource
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_member_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_member_user`;
CREATE TABLE `tb_member_user`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `creation_time`  datetime(3) NOT NULL COMMENT '创建时间',
    `update_version` int(11) NOT NULL DEFAULT '1' COMMENT '更新版本号',
    `username`       varchar(64) NOT NULL COMMENT '登录帐号',
    `password`       char(64)    NOT NULL COMMENT '密码',
    `email`          varchar(64) DEFAULT NULL COMMENT '邮箱',
    `phone`          varchar(24) DEFAULT NULL COMMENT '备注',
    `status`         tinyint(4) NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_username` (`username`) USING BTREE,
    UNIQUE KEY `ux_email` (`email`) USING BTREE,
    UNIQUE KEY `ux_phone` (`phone`) USING BTREE
) ENGINE=InnoDB COMMENT='会员用户表';

-- ----------------------------
-- Records of tb_member_user
-- ----------------------------
BEGIN;
INSERT INTO `tb_member_user` VALUES (1, '2020-04-16 17:21:56.000', 1, 'maurice', '$2a$10$U2787VFuFP9NMyxwdsP1bOmtvofTgwU5nLcdV7Gj3ZyhdiZO.T8mG', '', '18776974353', 1);
COMMIT;

-- ----------------------------
-- Table structure for tb_member_user_initialization
-- ----------------------------
DROP TABLE IF EXISTS `tb_member_user_initialization`;
CREATE TABLE `tb_member_user_initialization`
(
    `id`              int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`   datetime(3) NOT NULL COMMENT '创建时间',
    `update_version`  int(11) NOT NULL DEFAULT '1' COMMENT '更新版本号',
    `user_id`         int(11) NOT NULL COMMENT '用户 id',
    `modify_password` tinyint(4) NOT NULL COMMENT '是否可更新密码：1.是、0.否',
    `modify_username` tinyint(4) NOT NULL COMMENT '是否客更新登录账户：1.是、0.否',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB COMMENT='会员用户初始化表';

-- ----------------------------
-- Records of tb_member_user_initialization
-- ----------------------------
BEGIN;
INSERT INTO `tb_member_user_initialization` VALUES (1, '2020-03-01 21:57:46.000', 1, 1, 0, 0, 0);
COMMIT;

-- ----------------------------
-- Table structure for tb_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_resource`;
CREATE TABLE `tb_resource`
(
    `id`               int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`    datetime(3) NOT NULL COMMENT '创建时间',
    `name`             varchar(32)  NOT NULL COMMENT '名称',
    `code`             varchar(128) NOT NULL COMMENT '唯一识别',
    `application_name` varchar(64)  NOT NULL COMMENT '应用名称',
    `type`             varchar(16)  NOT NULL COMMENT '类型:MENU.菜单类型、SECURITY.安全类型',
    `source`           varchar(16)  NOT NULL COMMENT '来源:Front.前端、Console.管理后台、UserCenter.用户中心、System.系统、Mobile.移动端、All.全部',
    `version`          varchar(16)  NOT NULL COMMENT '版本号',
    `value`            varchar(256) DEFAULT NULL COMMENT 'spring security 拦截值',
    `authority`        varchar(64)  DEFAULT NULL COMMENT 'spring security 资源的 authority 值',
    `icon`             varchar(32)  DEFAULT NULL COMMENT '图标',
    `parent_id`        int(11) DEFAULT NULL COMMENT '父类 id',
    `status`           tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态:0.禁用、1.启用',
    `sort`             tinyint(4) DEFAULT NULL COMMENT '顺序值',
    `remark`           varchar(128) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `ux_[code]_[application_name]_[type]_[source]_[parent_id]` (`code`,`application_name`,`source`,`parent_id`,`type`) USING BTREE
) ENGINE=InnoDB COMMENT='资源表';

-- ----------------------------
-- Records of tb_resource
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
