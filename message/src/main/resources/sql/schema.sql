SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_attachment
-- ----------------------------
DROP TABLE IF EXISTS `tb_attachment`;
CREATE TABLE `tb_attachment`
(
    `id`            int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time` datetime    NOT NULL COMMENT '创建时间',
    `name`          varchar(64) NOT NULL COMMENT '名称',
    `type`          smallint    NOT NULL COMMENT '类型:10.站内信，20:邮件',
    `content_type`  varchar(32) NOT NULL COMMENT '媒体内容类型',
    `meta`          json DEFAULT NULL COMMENT '元数据信息',
    `message_id`    int(20) NOT NULL COMMENT '消息 id',
    PRIMARY KEY (`id`),
    KEY             `ix_message_id` (`message_id`)
) ENGINE=InnoDB COMMENT='消息附件';

-- ----------------------------
-- Table structure for tb_batch_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_batch_message`;
CREATE TABLE `tb_batch_message`
(
    `id`             int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`  datetime NOT NULL COMMENT '创建时间',
    `complete_time`  datetime DEFAULT NULL COMMENT '完成时间',
    `execute_status` tinyint(4) NOT NULL COMMENT '状态:0.执行中、1.执行成功，99.执行失败',
    `count`          smallint NOT NULL COMMENT '总数',
    `success_number` smallint DEFAULT NULL COMMENT '成功发送数量',
    `fail_number`    smallint DEFAULT NULL COMMENT '失败发送数量',
    `type`           smallint NOT NULL COMMENT '类型:10.站内信,20.邮件,30.短信',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='批量消息';

-- ----------------------------
-- Table structure for tb_email_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_email_message`;
CREATE TABLE `tb_email_message`
(
    `id`              int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`   datetime(3) NOT NULL COMMENT '创建时间',
    `update_version`  int(20) NOT NULL DEFAULT '1' COMMENT '更新版本号',
    `type`            varchar(64)  NOT NULL COMMENT '类型',
    `from_email`      varchar(128) NOT NULL COMMENT '发送邮件',
    `to_email`        varchar(128) NOT NULL COMMENT '收取邮件',
    `title`           varchar(64)  NOT NULL COMMENT '标题',
    `content`         text         NOT NULL COMMENT '内容',
    `retry_count`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `max_retry_count` tinyint(4) NOT NULL DEFAULT '0' COMMENT '最大重试次数',
    `last_send_time`  datetime(3) DEFAULT NULL COMMENT '最后发送时间',
    `execute_status`  tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
    `success_time`    datetime(3) DEFAULT NULL COMMENT '发送成功时间',
    `exception`       text COMMENT '异常信息',
    `remark`          varchar(256) DEFAULT NULL COMMENT '备注',
    `has_attachment`  tinyint(4) DEFAULT NULL COMMENT '是否存在附件:0.否,1.是',
    `batch_id`        int(20) DEFAULT NULL COMMENT '批量消息 id',
    PRIMARY KEY (`id`) USING BTREE,
    KEY               `ix_from_user` (`from_email`) USING BTREE,
    KEY               `ix_to_user` (`to_email`) USING BTREE
) ENGINE=InnoDB COMMENT='邮件消息';

-- ----------------------------
-- Table structure for tb_site_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_site_message`;
CREATE TABLE `tb_site_message`
(
    `id`              int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`   datetime(3) NOT NULL COMMENT '创建时间',
    `update_version`  int(20) NOT NULL DEFAULT '1' COMMENT '更新版本号',
    `channel`         varchar(32)  DEFAULT NULL COMMENT '渠道名称',
    `type`            varchar(64) NOT NULL COMMENT '类型',
    `to_user_id`      int(20) NOT NULL COMMENT '收到的用户 id',
    `title`           varchar(64) NOT NULL COMMENT '标题',
    `content`         text        NOT NULL COMMENT '内容',
    `is_push`         tinyint(4) NOT NULL COMMENT '是否推送消息：0.否，1.是',
    `is_read`         tinyint(4) NOT NULL COMMENT '是否已读：0.否，1.是',
    `read_time`       datetime(3) DEFAULT NULL COMMENT '读取时间',
    `meta`            json         DEFAULT NULL COMMENT '元数据信息',
    `retry_count`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `max_retry_count` tinyint(4) NOT NULL DEFAULT '0' COMMENT '最大重试次数',
    `last_send_time`  datetime(3) DEFAULT NULL COMMENT '最后发送时间',
    `exception`       text COMMENT '异常信息',
    `execute_status`  tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
    `success_time`    datetime(3) DEFAULT NULL COMMENT '发送成功时间',
    `has_attachment`  tinyint(4) DEFAULT NULL COMMENT '是否存在附件:0.否,1.是',
    `batch_id`        int(20) DEFAULT NULL COMMENT '批量消息 id',
    `remark`          varchar(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    KEY               `ix_to_user_id` (`to_user_id`) USING BTREE
) ENGINE=InnoDB COMMENT='站内信消息';

-- ----------------------------
-- Table structure for tb_sms_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_sms_message`;
CREATE TABLE `tb_sms_message`
(
    `id`              int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`   datetime(3) NOT NULL COMMENT '创建时间',
    `update_version`  int(20) NOT NULL DEFAULT '1' COMMENT '更新版本号',
    `type`            varchar(64)  NOT NULL COMMENT '类型',
    `channel`         varchar(32)  DEFAULT NULL COMMENT '渠道名称',
    `phone_number`    varchar(24)  NOT NULL COMMENT '手机号码',
    `content`         varchar(128) NOT NULL COMMENT '内容',
    `retry_count`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `max_retry_count` tinyint(4) NOT NULL DEFAULT '0' COMMENT '最大重试次数',
    `last_send_time`  datetime     DEFAULT NULL COMMENT '最后发送时间',
    `execute_status`  tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
    `success_time`    datetime     DEFAULT NULL COMMENT '成功时间',
    `exception`       text COMMENT '异常信息',
    `batch_id`        int(20) DEFAULT NULL COMMENT '批量消息 id',
    `remark`          varchar(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    KEY               `ix_phone_number` (`phone_number`) USING BTREE
) ENGINE=InnoDB COMMENT='短信消息';

SET
FOREIGN_KEY_CHECKS = 1;
