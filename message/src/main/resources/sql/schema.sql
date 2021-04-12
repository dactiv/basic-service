SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_email_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_email_message`;
CREATE TABLE `tb_email_message`  (
`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
`creation_time` datetime(0) NOT NULL COMMENT '创建时间',
`type` varchar(64) NOT NULL COMMENT '类型',
`from_user` varchar(128) NOT NULL COMMENT '发送用户邮件',
`to_user` varchar(128) NOT NULL COMMENT '收取用户邮件',
`title` varchar(64) NOT NULL COMMENT '标题',
`content` text NOT NULL COMMENT '内容',
`retry_count` tinyint(4) NOT NULL DEFAULT 0 COMMENT '重试次数',
`last_send_time` datetime(0) DEFAULT NULL COMMENT '最后发送时间',
`status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
`success_time` datetime(0) DEFAULT NULL COMMENT '发送成功时间',
`exception` varchar(256) DEFAULT NULL COMMENT '异常信息',
`remark` varchar(256) DEFAULT NULL COMMENT '备注',
PRIMARY KEY (`id`) USING BTREE,
INDEX `ix_from_user`(`from_user`) USING BTREE,
INDEX `ix_to_user`(`to_user`) USING BTREE
) ENGINE = InnoDB COMMENT = '邮件消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_site_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_site_message`;
CREATE TABLE `tb_site_message`  (
`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
`creation_time` datetime(0) NOT NULL COMMENT '创建时间',
`channel` varchar(32) DEFAULT NULL COMMENT '渠道名称',
`type` varchar(64) NOT NULL COMMENT '类型',
`from_user_id` int(11) NOT NULL COMMENT '发送的用户 id',
`to_user_id` int(11) NOT NULL COMMENT '收到的用户 id',
`title` varchar(64) NOT NULL COMMENT '标题',
`content` text NOT NULL COMMENT '内容',
`push_message` tinyint(4) NOT NULL COMMENT '是否推送消息：0.否，1.是',
`is_read` tinyint(4) NOT NULL COMMENT '是否已读：0.否，1.是',
`read_time` datetime(0) DEFAULT NULL COMMENT '读取时间',
`link` varchar(256) DEFAULT NULL COMMENT '链接',
`data` text DEFAULT NULL COMMENT '链接',
`retry_count` tinyint(4) NOT NULL DEFAULT 0 COMMENT '重试次数',
`last_send_time` datetime(0) DEFAULT NULL COMMENT '最后发送时间',
`exception` varchar(256) DEFAULT NULL COMMENT '异常信息',
`status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
`success_time` datetime(0) DEFAULT NULL COMMENT '发送成功时间',
`remark` varchar(256) DEFAULT NULL COMMENT '备注',
PRIMARY KEY (`id`) USING BTREE,
INDEX `ix_from_user_id`(`from_user_id`) USING BTREE,
INDEX `ix_to_user_id`(`to_user_id`) USING BTREE
) ENGINE = InnoDB COMMENT = '站内信消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_sms_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_sms_message`;
CREATE TABLE `tb_sms_message`  (
`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
`creation_time` datetime(0) NOT NULL COMMENT '创建时间',
`type` varchar(64) NOT NULL COMMENT '类型',
`channel` varchar(32) DEFAULT NULL COMMENT '渠道名称',
`phone_number` varchar(24) NOT NULL COMMENT '手机号码',
`content` varchar(128) NOT NULL COMMENT '内容',
`retry_count` tinyint(4) NOT NULL DEFAULT 0 COMMENT '重试次数',
`last_send_time` datetime(0) DEFAULT NULL COMMENT '最后发送时间',
`status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
`success_time` datetime(0) DEFAULT NULL COMMENT '成功时间',
`exception` varchar(256) DEFAULT NULL COMMENT '异常信息',
`remark` varchar(256) DEFAULT NULL COMMENT '备注',
PRIMARY KEY (`id`) USING BTREE,
INDEX `ix_phone_number`(`phone_number`) USING BTREE
) ENGINE = InnoDB COMMENT = '短信消息' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
