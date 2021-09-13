SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_access_crypto
-- ----------------------------
DROP TABLE IF EXISTS `tb_access_crypto`;
CREATE TABLE `tb_access_crypto`
(
    `id`               int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`    datetime(3) NOT NULL COMMENT '创建时间',
    `name`             varchar(32) COLLATE utf8mb4_bin  NOT NULL COMMENT '名称',
    `type`             varchar(32) COLLATE utf8mb4_bin  NOT NULL COMMENT '类型',
    `value`            varchar(256) COLLATE utf8mb4_bin NOT NULL COMMENT '值',
    `request_decrypt`  tinyint(4) NOT NULL COMMENT '是否请求解密，0.否, 1.是',
    `response_encrypt` tinyint(4) NOT NULL COMMENT '是否响应加密，0.否, 1.是',
    `enabled`          tinyint(4) NOT NULL COMMENT '是否启用，1.是，0.否',
    `remark`           varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    KEY                `ix_value` (`value`) USING BTREE
) ENGINE=InnoDB COMMENT='访问加解密表';

-- ----------------------------
-- Records of tb_access_crypto
-- ----------------------------
BEGIN;
INSERT INTO `tb_access_crypto`
VALUES (1, '2020-08-06 14:40:51.000', '移动认证加解', 'mobile', '/authentication/login/**', 1, 1, 1, '移动端登录访问加解密');
COMMIT;

-- ----------------------------
-- Table structure for tb_access_crypto_predicate
-- ----------------------------
DROP TABLE IF EXISTS `tb_access_crypto_predicate`;
CREATE TABLE `tb_access_crypto_predicate`
(
    `id`               int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `creation_time`    datetime(3) NOT NULL COMMENT '创建时间',
    `name`             varchar(32) COLLATE utf8mb4_bin  NOT NULL COMMENT '名称',
    `value`            varchar(256) COLLATE utf8mb4_bin NOT NULL COMMENT '值',
    `remark`           varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
    `access_crypto_id` int(11) NOT NULL COMMENT '访问加解密 id',
    PRIMARY KEY (`id`) USING BTREE,
    KEY                `ix_access_crypto_id` (`access_crypto_id`) USING BTREE
) ENGINE=InnoDB COMMENT='访问加解密断言表';

-- ----------------------------
-- Records of tb_access_crypto_predicate
-- ----------------------------
BEGIN;
INSERT INTO `tb_access_crypto_predicate`
VALUES (1, '2020-08-06 14:40:51.000', 'Method', 'methods=POST', '必须是 post', 1);
INSERT INTO `tb_access_crypto_predicate`
VALUES (2, '2020-08-06 14:40:51.000', 'Header', 'header=X-ACCESS-TOKEN regexp=^\\S+$', '必须是 Header 的 X-ACCESS-TOKEN 有值',
        1);
INSERT INTO `tb_access_crypto_predicate`
VALUES (3, '2020-08-06 14:40:51.000', 'Header',
        'header=X-DEVICE-IDENTIFIED regexp=[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}',
        '必须是 Header 的 X-DEVICE-IDENTIFIED=UUID 值', 1);
COMMIT;

-- ----------------------------
-- Table structure for tb_data_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `tb_data_dictionary`;
CREATE TABLE `tb_data_dictionary`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
    `code`          varchar(256) COLLATE utf8mb4_bin NOT NULL COMMENT '键名称',
    `name`          varchar(64) COLLATE utf8mb4_bin  NOT NULL COMMENT '名称',
    `level`         varchar(32) COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '等级',
    `value`         text COLLATE utf8mb4_bin         NOT NULL COMMENT '值',
    `enabled`       tinyint(4) DEFAULT '1' COMMENT '是否启用:0.禁用,1.启用',
    `type_id`       int(11) NOT NULL COMMENT '对应字典类型',
    `parent_id`     int(11) DEFAULT NULL COMMENT '根节点为 null',
    `sort`          tinyint(4) DEFAULT NULL COMMENT '顺序值',
    `remark`        varchar(256) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_code` (`code`) USING BTREE,
    KEY             `ix_code` (`code`) USING BTREE,
    KEY             `ix_parent_id` (`parent_id`) USING BTREE
) ENGINE=InnoDB COMMENT='数据字典表';

-- ----------------------------
-- Records of tb_data_dictionary
-- ----------------------------
BEGIN;

INSERT INTO `tb_data_dictionary`
VALUES (1, '2020-03-29 14:20:36.000', 'system.crypto.access.type.server', '服务端加解密', NULL, 'server', 1, 5, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2, '2020-03-29 14:20:36.000', 'system.crypto.access.type.mobile', '移动端加解密', NULL, 'mobile', 1, 5, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.after', '时间之后', NULL, 'After', 1, 4, NULL, NULL,
        '在该日期时间之后发生的请求都将被匹配，如：datetime=2020-01-20T17:42:47.789，在 2020-01-20 17:42:47之后发生的请求都被匹配');
INSERT INTO `tb_data_dictionary`
VALUES (4, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.before', '时间之前', NULL, 'Before', 1, 4, NULL, NULL,
        '在该日期时间之后发生的请求都将被匹配，如：datetime=2020-01-20T17:42:47.789，在 2020-01-20 17:42:47之前发生的请求都被匹配');
INSERT INTO `tb_data_dictionary`
VALUES (5, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.between', '时间范围', NULL, 'Between', 1, 4, NULL,
        NULL,
        '在该日期时间范围发生的请求都将被匹配，如：datetime1=2020-01-20T17:42:47.789 datetime1=2020-03-20T17:42:47.789，在 2020-01-20 17:42:47 到 2020-03-20T17:42:47 发生的请求都被匹配');
INSERT INTO `tb_data_dictionary`
VALUES (6, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.cookie', '请求Cookie匹配', NULL, 'Cookie', 1, 4, NULL,
        NULL, '请求 Cookie 匹配，如：name=chocolate regexp=ch.p，表示 cookei 存在 chocolate 并且正则表达式对条件 ch.p 通过则匹配');
INSERT INTO `tb_data_dictionary`
VALUES (7, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.header', '请求头匹配', NULL, 'Header', 1, 4, NULL,
        NULL, '请求头匹配，如：name=X-REQUST-ID regexp=d+，表示 header 存在 X-REQUST-ID 并且正则表达式对条件 d+ 通过则匹配');
INSERT INTO `tb_data_dictionary`
VALUES (8, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.host', '访问主机匹配', NULL, 'Host', 1, 4, NULL, NULL,
        '访问主机匹配，如：patterns=**.somehost.org,**.anotherhost.org，表示访问来源是 somehost.org 或 **.anotherhost.org 时则匹配');
INSERT INTO `tb_data_dictionary`
VALUES (9, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.method', '请求方法匹配', NULL, 'Method', 1, 4, NULL,
        NULL, '请求方法匹配，如：methods=POST,GET，表示请求是 POST 或 GET，表示请求是 时则匹配');
INSERT INTO `tb_data_dictionary`
VALUES (10, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.path', '请求路径匹配', NULL, 'Path', 1, 4, NULL, NULL,
        '请求路径匹配，如：patterns=/foo/**,/bar/**，表示请求路径是带有/foo/前缀 或 /bar/前缀时则匹配');
INSERT INTO `tb_data_dictionary`
VALUES (11, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.query', '请求参数匹配', NULL, 'Query', 1, 4, NULL,
        NULL, '请求参数匹配，如：param=id regexp=d+，表示请求参数是 id 并且正则表达式对条件 d+ 通过则匹配');
INSERT INTO `tb_data_dictionary`
VALUES (12, '2020-03-29 14:20:36.000', 'system.crypto.access.predicate.remote-address', '访问IP匹配', NULL, 'RemoteAddr', 1,
        4, NULL, NULL,
        '访问IP匹配，如：sources=192.168.0.1/24,192.168.6.1/24 表示只有访问 IP 在 192.168.0.[1到24] 或 192.168.6.[1到24] 时则匹配');
INSERT INTO `tb_data_dictionary`
VALUES (13, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.none', '无', NULL, 'NONE', 1, 6, NULL,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (14, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.iso10126', 'ISO10126Padding', NULL,
        'ISO10126', 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (15, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.oaep', 'OAEPPadding', NULL, 'OAEP', 1, 6,
        NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (16, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.oaep-with-md5-and-mgf1',
        'OAEPWithMD5AndMGF1Padding', NULL, 'OAEPWithMd5AndMgf1', 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (17, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.oaep-with-sha1-and-mgf1',
        'OAEPWithSHA-1AndMGF1Padding', NULL, 'OAEPWithSha1AndMgf1', 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (18, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.oaep-with-sha256-and-mgf1',
        'OAEPWithSHA-256AndMGF1Padding', NULL, 'OAEPWithSha256AndMgf1', 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (19, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.oaep-with-sha-384-and-mgf1',
        'OAEPWithSHA-384AndMGF1Padding', NULL, 'OAEPWithSha384AndMgf1', 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (20, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.oaep-with-sha-512-and-mgf1',
        'OAEPWithSHA-512AndMGF1Padding', NULL, 'OAEPWithSha512AndMgf1', 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (21, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.pkcs1', 'PKCS1Padding', NULL, 'PKCS1', 1,
        6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (22, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.pkcs5', 'PKCS5Padding', NULL, 'PKCS5', 1,
        6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (23, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.padding-scheme.ssl3', 'SSL3Padding', NULL, 'SSL3', 1, 6,
        NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (24, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.mode.none', '无', NULL, 'NONE', 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (25, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.mode.cbc', 'CBC', NULL, 'CBC', 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (26, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.mode.cfb', 'CFB', NULL, 'CFB', 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (27, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.mode.ctr', 'CTR', NULL, 'CTR', 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (28, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.mode.ecb', 'ECB', NULL, 'ECB', 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (29, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.mode.ofb', 'OFB', NULL, 'OFB', 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (30, '2020-03-29 14:20:36.000', 'system.crypto.algorithm.mode.pcbc', 'PCBC', NULL, 'PCBC', 1, 7, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (31, '2020-03-29 14:20:36.000', 'system.email.captcha.login', '登录或注册', NULL,
        '您正在{0}，当前验证码为：{1}，请在{2}分钟内按页面提示提交验证码，切勿将验证码泄漏与他人，验证码提供给他人可能导致帐号被盗。', 1, 8, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (32, '2020-03-29 14:20:36.000', 'system.sms.captcha.login', '登录或注册', NULL,
        '【游戏圈】您正在{0}，当前验证码为：{1}，请在{2}分钟内按页面提示提交验证码，切勿将验证码泄漏与他人，验证码提供给他人可能导致帐号被盗。', 1, 9, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (33, '2020-03-29 14:20:36.000', 'system.notification.dynamic.at-user', '动态内容 @ 用户通知', NULL, '{0} 在他的动态中提到了您', 1,
        11, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (34, '2020-03-29 14:20:36.000', 'system.notification.dynamic.comment', '动态评论通知', NULL, '{0} 在评论了您的动态', 1, 11,
        NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (35, '2020-03-29 14:20:36.000', 'system.notification.dynamic.like', '动态点赞通知', NULL, '{0} 赞了您的动态', 1, 11, NULL,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (36, '2020-03-29 14:20:36.000', 'system.notification.dynamic.forward', '转发动态通知', NULL, '{0} 转发了您的动态', 1, 11,
        NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (37, '2020-03-29 14:20:36.000', 'system.notification.comment.at-user', '评论内容 @ 用户通知', NULL, '{0} 在评论内容时提到了你的', 1,
        12, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (38, '2020-03-29 14:20:36.000', 'system.notification.comment.reply', '回复评论通知', NULL, '{0} 回复了你的评论', 1, 12, NULL,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (39, '2020-03-29 14:20:36.000', 'system.notification.comment.like', '回复点赞通知', NULL, '{0} 点赞了你的评论', 1, 12, NULL,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (40, '2020-03-29 14:20:36.000', 'system.region.province.110000', '北京市', 'area', '110000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (41, '2020-03-29 14:20:36.000', 'system.region.area.110101', '东城区', NULL, '110101', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (42, '2020-03-29 14:20:36.000', 'system.region.area.110102', '西城区', NULL, '110102', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (43, '2020-03-29 14:20:36.000', 'system.region.area.110105', '朝阳区', NULL, '110105', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (44, '2020-03-29 14:20:36.000', 'system.region.area.110106', '丰台区', NULL, '110106', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (45, '2020-03-29 14:20:36.000', 'system.region.area.110107', '石景山区', NULL, '110107', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (46, '2020-03-29 14:20:36.000', 'system.region.area.110108', '海淀区', NULL, '110108', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (47, '2020-03-29 14:20:36.000', 'system.region.area.110109', '门头沟区', NULL, '110109', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (48, '2020-03-29 14:20:36.000', 'system.region.area.110111', '房山区', NULL, '110111', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (49, '2020-03-29 14:20:36.000', 'system.region.area.110112', '通州区', NULL, '110112', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (50, '2020-03-29 14:20:36.000', 'system.region.area.110113', '顺义区', NULL, '110113', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (51, '2020-03-29 14:20:36.000', 'system.region.area.110114', '昌平区', NULL, '110114', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (52, '2020-03-29 14:20:36.000', 'system.region.area.110115', '大兴区', NULL, '110115', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (53, '2020-03-29 14:20:36.000', 'system.region.area.110116', '怀柔区', NULL, '110116', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (54, '2020-03-29 14:20:36.000', 'system.region.area.110117', '平谷区', NULL, '110117', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (55, '2020-03-29 14:20:36.000', 'system.region.area.110118', '密云区', NULL, '110118', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (56, '2020-03-29 14:20:36.000', 'system.region.area.110119', '延庆区', NULL, '110119', 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (57, '2020-03-29 14:20:36.000', 'system.region.province.120000', '天津市', 'area', '120000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (58, '2020-03-29 14:20:36.000', 'system.region.area.120101', '和平区', NULL, '120101', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (59, '2020-03-29 14:20:36.000', 'system.region.area.120102', '河东区', NULL, '120102', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (60, '2020-03-29 14:20:36.000', 'system.region.area.120103', '河西区', NULL, '120103', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (61, '2020-03-29 14:20:36.000', 'system.region.area.120104', '南开区', NULL, '120104', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (62, '2020-03-29 14:20:36.000', 'system.region.area.120105', '河北区', NULL, '120105', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (63, '2020-03-29 14:20:36.000', 'system.region.area.120106', '红桥区', NULL, '120106', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (64, '2020-03-29 14:20:36.000', 'system.region.area.120110', '东丽区', NULL, '120110', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (65, '2020-03-29 14:20:36.000', 'system.region.area.120111', '西青区', NULL, '120111', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (66, '2020-03-29 14:20:36.000', 'system.region.area.120112', '津南区', NULL, '120112', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (67, '2020-03-29 14:20:36.000', 'system.region.area.120113', '北辰区', NULL, '120113', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (68, '2020-03-29 14:20:36.000', 'system.region.area.120114', '武清区', NULL, '120114', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (69, '2020-03-29 14:20:36.000', 'system.region.area.120115', '宝坻区', NULL, '120115', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (70, '2020-03-29 14:20:36.000', 'system.region.area.120116', '滨海新区', NULL, '120116', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (71, '2020-03-29 14:20:36.000', 'system.region.area.120117', '宁河区', NULL, '120117', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (72, '2020-03-29 14:20:36.000', 'system.region.area.120118', '静海区', NULL, '120118', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (73, '2020-03-29 14:20:36.000', 'system.region.area.120119', '蓟州区', NULL, '120119', 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (74, '2020-03-29 14:20:36.000', 'system.region.province.130000', '河北省', 'city', '130000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (75, '2020-03-29 14:20:36.000', 'system.region.city.130100', '石家庄市', 'area', '130100', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (76, '2020-03-29 14:20:36.000', 'system.region.area.130102', '长安区', NULL, '130102', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (77, '2020-03-29 14:20:36.000', 'system.region.area.130104', '桥西区', NULL, '130104', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (78, '2020-03-29 14:20:36.000', 'system.region.area.130105', '新华区', NULL, '130105', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (79, '2020-03-29 14:20:36.000', 'system.region.area.130107', '井陉矿区', NULL, '130107', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (80, '2020-03-29 14:20:36.000', 'system.region.area.130108', '裕华区', NULL, '130108', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (81, '2020-03-29 14:20:36.000', 'system.region.area.130109', '藁城区', NULL, '130109', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (82, '2020-03-29 14:20:36.000', 'system.region.area.130110', '鹿泉区', NULL, '130110', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (83, '2020-03-29 14:20:36.000', 'system.region.area.130111', '栾城区', NULL, '130111', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (84, '2020-03-29 14:20:36.000', 'system.region.area.130121', '井陉县', NULL, '130121', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (85, '2020-03-29 14:20:36.000', 'system.region.area.130123', '正定县', NULL, '130123', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (86, '2020-03-29 14:20:36.000', 'system.region.area.130125', '行唐县', NULL, '130125', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (87, '2020-03-29 14:20:36.000', 'system.region.area.130126', '灵寿县', NULL, '130126', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (88, '2020-03-29 14:20:36.000', 'system.region.area.130127', '高邑县', NULL, '130127', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (89, '2020-03-29 14:20:36.000', 'system.region.area.130128', '深泽县', NULL, '130128', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (90, '2020-03-29 14:20:36.000', 'system.region.area.130129', '赞皇县', NULL, '130129', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (91, '2020-03-29 14:20:36.000', 'system.region.area.130130', '无极县', NULL, '130130', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (92, '2020-03-29 14:20:36.000', 'system.region.area.130131', '平山县', NULL, '130131', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (93, '2020-03-29 14:20:36.000', 'system.region.area.130132', '元氏县', NULL, '130132', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (94, '2020-03-29 14:20:36.000', 'system.region.area.130133', '赵县', NULL, '130133', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (95, '2020-03-29 14:20:36.000', 'system.region.area.130181', '辛集市', NULL, '130181', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (96, '2020-03-29 14:20:36.000', 'system.region.area.130183', '晋州市', NULL, '130183', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (97, '2020-03-29 14:20:36.000', 'system.region.area.130184', '新乐市', NULL, '130184', 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (98, '2020-03-29 14:20:36.000', 'system.region.city.130200', '唐山市', 'area', '130200', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (99, '2020-03-29 14:20:36.000', 'system.region.area.130202', '路南区', NULL, '130202', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (100, '2020-03-29 14:20:36.000', 'system.region.area.130203', '路北区', NULL, '130203', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (101, '2020-03-29 14:20:36.000', 'system.region.area.130204', '古冶区', NULL, '130204', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (102, '2020-03-29 14:20:36.000', 'system.region.area.130205', '开平区', NULL, '130205', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (103, '2020-03-29 14:20:36.000', 'system.region.area.130207', '丰南区', NULL, '130207', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (104, '2020-03-29 14:20:36.000', 'system.region.area.130208', '丰润区', NULL, '130208', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (105, '2020-03-29 14:20:36.000', 'system.region.area.130209', '曹妃甸区', NULL, '130209', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (106, '2020-03-29 14:20:36.000', 'system.region.area.130224', '滦南县', NULL, '130224', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (107, '2020-03-29 14:20:36.000', 'system.region.area.130225', '乐亭县', NULL, '130225', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (108, '2020-03-29 14:20:36.000', 'system.region.area.130227', '迁西县', NULL, '130227', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (109, '2020-03-29 14:20:36.000', 'system.region.area.130229', '玉田县', NULL, '130229', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (110, '2020-03-29 14:20:36.000', 'system.region.area.130281', '遵化市', NULL, '130281', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (111, '2020-03-29 14:20:36.000', 'system.region.area.130283', '迁安市', NULL, '130283', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (112, '2020-03-29 14:20:36.000', 'system.region.area.130284', '滦州市', NULL, '130284', 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (113, '2020-03-29 14:20:36.000', 'system.region.city.130300', '秦皇岛市', 'area', '130300', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (114, '2020-03-29 14:20:36.000', 'system.region.area.130302', '海港区', NULL, '130302', 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (115, '2020-03-29 14:20:36.000', 'system.region.area.130303', '山海关区', NULL, '130303', 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (116, '2020-03-29 14:20:36.000', 'system.region.area.130304', '北戴河区', NULL, '130304', 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (117, '2020-03-29 14:20:36.000', 'system.region.area.130306', '抚宁区', NULL, '130306', 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (118, '2020-03-29 14:20:36.000', 'system.region.area.130321', '青龙满族自治县', NULL, '130321', 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (119, '2020-03-29 14:20:36.000', 'system.region.area.130322', '昌黎县', NULL, '130322', 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (120, '2020-03-29 14:20:36.000', 'system.region.area.130324', '卢龙县', NULL, '130324', 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (121, '2020-03-29 14:20:36.000', 'system.region.city.130400', '邯郸市', 'area', '130400', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (122, '2020-03-29 14:20:36.000', 'system.region.area.130402', '邯山区', NULL, '130402', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (123, '2020-03-29 14:20:36.000', 'system.region.area.130403', '丛台区', NULL, '130403', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (124, '2020-03-29 14:20:36.000', 'system.region.area.130404', '复兴区', NULL, '130404', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (125, '2020-03-29 14:20:36.000', 'system.region.area.130406', '峰峰矿区', NULL, '130406', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (126, '2020-03-29 14:20:36.000', 'system.region.area.130407', '肥乡区', NULL, '130407', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (127, '2020-03-29 14:20:36.000', 'system.region.area.130408', '永年区', NULL, '130408', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (128, '2020-03-29 14:20:36.000', 'system.region.area.130423', '临漳县', NULL, '130423', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (129, '2020-03-29 14:20:36.000', 'system.region.area.130424', '成安县', NULL, '130424', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (130, '2020-03-29 14:20:36.000', 'system.region.area.130425', '大名县', NULL, '130425', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (131, '2020-03-29 14:20:36.000', 'system.region.area.130426', '涉县', NULL, '130426', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (132, '2020-03-29 14:20:36.000', 'system.region.area.130427', '磁县', NULL, '130427', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (133, '2020-03-29 14:20:36.000', 'system.region.area.130430', '邱县', NULL, '130430', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (134, '2020-03-29 14:20:36.000', 'system.region.area.130431', '鸡泽县', NULL, '130431', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (135, '2020-03-29 14:20:36.000', 'system.region.area.130432', '广平县', NULL, '130432', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (136, '2020-03-29 14:20:36.000', 'system.region.area.130433', '馆陶县', NULL, '130433', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (137, '2020-03-29 14:20:36.000', 'system.region.area.130434', '魏县', NULL, '130434', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (138, '2020-03-29 14:20:36.000', 'system.region.area.130435', '曲周县', NULL, '130435', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (139, '2020-03-29 14:20:36.000', 'system.region.area.130481', '武安市', NULL, '130481', 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (140, '2020-03-29 14:20:36.000', 'system.region.city.130500', '邢台市', 'area', '130500', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (141, '2020-03-29 14:20:36.000', 'system.region.area.130502', '桥东区', NULL, '130502', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (142, '2020-03-29 14:20:36.000', 'system.region.area.130503', '桥西区', NULL, '130503', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (143, '2020-03-29 14:20:36.000', 'system.region.area.130521', '邢台县', NULL, '130521', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (144, '2020-03-29 14:20:36.000', 'system.region.area.130522', '临城县', NULL, '130522', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (145, '2020-03-29 14:20:36.000', 'system.region.area.130523', '内丘县', NULL, '130523', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (146, '2020-03-29 14:20:36.000', 'system.region.area.130524', '柏乡县', NULL, '130524', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (147, '2020-03-29 14:20:36.000', 'system.region.area.130525', '隆尧县', NULL, '130525', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (148, '2020-03-29 14:20:36.000', 'system.region.area.130526', '任县', NULL, '130526', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (149, '2020-03-29 14:20:36.000', 'system.region.area.130527', '南和县', NULL, '130527', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (150, '2020-03-29 14:20:36.000', 'system.region.area.130528', '宁晋县', NULL, '130528', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (151, '2020-03-29 14:20:36.000', 'system.region.area.130529', '巨鹿县', NULL, '130529', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (152, '2020-03-29 14:20:36.000', 'system.region.area.130530', '新河县', NULL, '130530', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (153, '2020-03-29 14:20:36.000', 'system.region.area.130531', '广宗县', NULL, '130531', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (154, '2020-03-29 14:20:36.000', 'system.region.area.130532', '平乡县', NULL, '130532', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (155, '2020-03-29 14:20:36.000', 'system.region.area.130533', '威县', NULL, '130533', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (156, '2020-03-29 14:20:36.000', 'system.region.area.130534', '清河县', NULL, '130534', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (157, '2020-03-29 14:20:36.000', 'system.region.area.130535', '临西县', NULL, '130535', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (158, '2020-03-29 14:20:36.000', 'system.region.area.130581', '南宫市', NULL, '130581', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (159, '2020-03-29 14:20:36.000', 'system.region.area.130582', '沙河市', NULL, '130582', 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (160, '2020-03-29 14:20:36.000', 'system.region.city.130600', '保定市', 'area', '130600', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (161, '2020-03-29 14:20:36.000', 'system.region.area.130602', '竞秀区', NULL, '130602', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (162, '2020-03-29 14:20:36.000', 'system.region.area.130606', '莲池区', NULL, '130606', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (163, '2020-03-29 14:20:36.000', 'system.region.area.130607', '满城区', NULL, '130607', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (164, '2020-03-29 14:20:36.000', 'system.region.area.130608', '清苑区', NULL, '130608', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (165, '2020-03-29 14:20:36.000', 'system.region.area.130609', '徐水区', NULL, '130609', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (166, '2020-03-29 14:20:36.000', 'system.region.area.130623', '涞水县', NULL, '130623', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (167, '2020-03-29 14:20:36.000', 'system.region.area.130624', '阜平县', NULL, '130624', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (168, '2020-03-29 14:20:36.000', 'system.region.area.130626', '定兴县', NULL, '130626', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (169, '2020-03-29 14:20:36.000', 'system.region.area.130627', '唐县', NULL, '130627', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (170, '2020-03-29 14:20:36.000', 'system.region.area.130628', '高阳县', NULL, '130628', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (171, '2020-03-29 14:20:36.000', 'system.region.area.130629', '容城县', NULL, '130629', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (172, '2020-03-29 14:20:36.000', 'system.region.area.130630', '涞源县', NULL, '130630', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (173, '2020-03-29 14:20:36.000', 'system.region.area.130631', '望都县', NULL, '130631', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (174, '2020-03-29 14:20:36.000', 'system.region.area.130632', '安新县', NULL, '130632', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (175, '2020-03-29 14:20:36.000', 'system.region.area.130633', '易县', NULL, '130633', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (176, '2020-03-29 14:20:36.000', 'system.region.area.130634', '曲阳县', NULL, '130634', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (177, '2020-03-29 14:20:36.000', 'system.region.area.130635', '蠡县', NULL, '130635', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (178, '2020-03-29 14:20:36.000', 'system.region.area.130636', '顺平县', NULL, '130636', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (179, '2020-03-29 14:20:36.000', 'system.region.area.130637', '博野县', NULL, '130637', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (180, '2020-03-29 14:20:36.000', 'system.region.area.130638', '雄县', NULL, '130638', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (181, '2020-03-29 14:20:36.000', 'system.region.area.130681', '涿州市', NULL, '130681', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (182, '2020-03-29 14:20:36.000', 'system.region.area.130682', '定州市', NULL, '130682', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (183, '2020-03-29 14:20:36.000', 'system.region.area.130683', '安国市', NULL, '130683', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (184, '2020-03-29 14:20:36.000', 'system.region.area.130684', '高碑店市', NULL, '130684', 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (185, '2020-03-29 14:20:36.000', 'system.region.city.130700', '张家口市', 'area', '130700', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (186, '2020-03-29 14:20:36.000', 'system.region.area.130702', '桥东区', NULL, '130702', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (187, '2020-03-29 14:20:36.000', 'system.region.area.130703', '桥西区', NULL, '130703', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (188, '2020-03-29 14:20:36.000', 'system.region.area.130705', '宣化区', NULL, '130705', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (189, '2020-03-29 14:20:36.000', 'system.region.area.130706', '下花园区', NULL, '130706', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (190, '2020-03-29 14:20:36.000', 'system.region.area.130708', '万全区', NULL, '130708', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (191, '2020-03-29 14:20:36.000', 'system.region.area.130709', '崇礼区', NULL, '130709', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (192, '2020-03-29 14:20:36.000', 'system.region.area.130722', '张北县', NULL, '130722', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (193, '2020-03-29 14:20:36.000', 'system.region.area.130723', '康保县', NULL, '130723', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (194, '2020-03-29 14:20:36.000', 'system.region.area.130724', '沽源县', NULL, '130724', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (195, '2020-03-29 14:20:36.000', 'system.region.area.130725', '尚义县', NULL, '130725', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (196, '2020-03-29 14:20:36.000', 'system.region.area.130726', '蔚县', NULL, '130726', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (197, '2020-03-29 14:20:36.000', 'system.region.area.130727', '阳原县', NULL, '130727', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (198, '2020-03-29 14:20:36.000', 'system.region.area.130728', '怀安县', NULL, '130728', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (199, '2020-03-29 14:20:36.000', 'system.region.area.130730', '怀来县', NULL, '130730', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (200, '2020-03-29 14:20:36.000', 'system.region.area.130731', '涿鹿县', NULL, '130731', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (201, '2020-03-29 14:20:36.000', 'system.region.area.130732', '赤城县', NULL, '130732', 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (202, '2020-03-29 14:20:36.000', 'system.region.city.130800', '承德市', 'area', '130800', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (203, '2020-03-29 14:20:36.000', 'system.region.area.130802', '双桥区', NULL, '130802', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (204, '2020-03-29 14:20:36.000', 'system.region.area.130803', '双滦区', NULL, '130803', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (205, '2020-03-29 14:20:36.000', 'system.region.area.130804', '鹰手营子矿区', NULL, '130804', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (206, '2020-03-29 14:20:36.000', 'system.region.area.130821', '承德县', NULL, '130821', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (207, '2020-03-29 14:20:36.000', 'system.region.area.130822', '兴隆县', NULL, '130822', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (208, '2020-03-29 14:20:36.000', 'system.region.area.130824', '滦平县', NULL, '130824', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (209, '2020-03-29 14:20:36.000', 'system.region.area.130825', '隆化县', NULL, '130825', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (210, '2020-03-29 14:20:36.000', 'system.region.area.130826', '丰宁满族自治县', NULL, '130826', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (211, '2020-03-29 14:20:36.000', 'system.region.area.130827', '宽城满族自治县', NULL, '130827', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (212, '2020-03-29 14:20:36.000', 'system.region.area.130828', '围场满族蒙古族自治县', NULL, '130828', 1, 16, 202, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (213, '2020-03-29 14:20:36.000', 'system.region.area.130881', '平泉市', NULL, '130881', 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (214, '2020-03-29 14:20:36.000', 'system.region.city.130900', '沧州市', 'area', '130900', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (215, '2020-03-29 14:20:36.000', 'system.region.area.130902', '新华区', NULL, '130902', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (216, '2020-03-29 14:20:36.000', 'system.region.area.130903', '运河区', NULL, '130903', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (217, '2020-03-29 14:20:36.000', 'system.region.area.130921', '沧县', NULL, '130921', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (218, '2020-03-29 14:20:36.000', 'system.region.area.130922', '青县', NULL, '130922', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (219, '2020-03-29 14:20:36.000', 'system.region.area.130923', '东光县', NULL, '130923', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (220, '2020-03-29 14:20:36.000', 'system.region.area.130924', '海兴县', NULL, '130924', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (221, '2020-03-29 14:20:36.000', 'system.region.area.130925', '盐山县', NULL, '130925', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (222, '2020-03-29 14:20:36.000', 'system.region.area.130926', '肃宁县', NULL, '130926', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (223, '2020-03-29 14:20:36.000', 'system.region.area.130927', '南皮县', NULL, '130927', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (224, '2020-03-29 14:20:36.000', 'system.region.area.130928', '吴桥县', NULL, '130928', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (225, '2020-03-29 14:20:36.000', 'system.region.area.130929', '献县', NULL, '130929', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (226, '2020-03-29 14:20:36.000', 'system.region.area.130930', '孟村回族自治县', NULL, '130930', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (227, '2020-03-29 14:20:36.000', 'system.region.area.130981', '泊头市', NULL, '130981', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (228, '2020-03-29 14:20:36.000', 'system.region.area.130982', '任丘市', NULL, '130982', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (229, '2020-03-29 14:20:36.000', 'system.region.area.130983', '黄骅市', NULL, '130983', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (230, '2020-03-29 14:20:36.000', 'system.region.area.130984', '河间市', NULL, '130984', 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (231, '2020-03-29 14:20:36.000', 'system.region.city.131000', '廊坊市', 'area', '131000', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (232, '2020-03-29 14:20:36.000', 'system.region.area.131002', '安次区', NULL, '131002', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (233, '2020-03-29 14:20:36.000', 'system.region.area.131003', '广阳区', NULL, '131003', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (234, '2020-03-29 14:20:36.000', 'system.region.area.131022', '固安县', NULL, '131022', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (235, '2020-03-29 14:20:36.000', 'system.region.area.131023', '永清县', NULL, '131023', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (236, '2020-03-29 14:20:36.000', 'system.region.area.131024', '香河县', NULL, '131024', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (237, '2020-03-29 14:20:36.000', 'system.region.area.131025', '大城县', NULL, '131025', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (238, '2020-03-29 14:20:36.000', 'system.region.area.131026', '文安县', NULL, '131026', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (239, '2020-03-29 14:20:36.000', 'system.region.area.131028', '大厂回族自治县', NULL, '131028', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (240, '2020-03-29 14:20:36.000', 'system.region.area.131081', '霸州市', NULL, '131081', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (241, '2020-03-29 14:20:36.000', 'system.region.area.131082', '三河市', NULL, '131082', 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (242, '2020-03-29 14:20:36.000', 'system.region.city.131100', '衡水市', 'area', '131100', 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (243, '2020-03-29 14:20:36.000', 'system.region.area.131102', '桃城区', NULL, '131102', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (244, '2020-03-29 14:20:36.000', 'system.region.area.131103', '冀州区', NULL, '131103', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (245, '2020-03-29 14:20:36.000', 'system.region.area.131121', '枣强县', NULL, '131121', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (246, '2020-03-29 14:20:36.000', 'system.region.area.131122', '武邑县', NULL, '131122', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (247, '2020-03-29 14:20:36.000', 'system.region.area.131123', '武强县', NULL, '131123', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (248, '2020-03-29 14:20:36.000', 'system.region.area.131124', '饶阳县', NULL, '131124', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (249, '2020-03-29 14:20:36.000', 'system.region.area.131125', '安平县', NULL, '131125', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (250, '2020-03-29 14:20:36.000', 'system.region.area.131126', '故城县', NULL, '131126', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (251, '2020-03-29 14:20:36.000', 'system.region.area.131127', '景县', NULL, '131127', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (252, '2020-03-29 14:20:36.000', 'system.region.area.131128', '阜城县', NULL, '131128', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (253, '2020-03-29 14:20:36.000', 'system.region.area.131182', '深州市', NULL, '131182', 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (254, '2020-03-29 14:20:36.000', 'system.region.province.140000', '山西省', 'city', '140000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (255, '2020-03-29 14:20:36.000', 'system.region.city.140100', '太原市', 'area', '140100', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (256, '2020-03-29 14:20:36.000', 'system.region.area.140105', '小店区', NULL, '140105', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (257, '2020-03-29 14:20:36.000', 'system.region.area.140106', '迎泽区', NULL, '140106', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (258, '2020-03-29 14:20:36.000', 'system.region.area.140107', '杏花岭区', NULL, '140107', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (259, '2020-03-29 14:20:36.000', 'system.region.area.140108', '尖草坪区', NULL, '140108', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (260, '2020-03-29 14:20:36.000', 'system.region.area.140109', '万柏林区', NULL, '140109', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (261, '2020-03-29 14:20:36.000', 'system.region.area.140110', '晋源区', NULL, '140110', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (262, '2020-03-29 14:20:36.000', 'system.region.area.140121', '清徐县', NULL, '140121', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (263, '2020-03-29 14:20:36.000', 'system.region.area.140122', '阳曲县', NULL, '140122', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (264, '2020-03-29 14:20:36.000', 'system.region.area.140123', '娄烦县', NULL, '140123', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (265, '2020-03-29 14:20:36.000', 'system.region.area.140181', '古交市', NULL, '140181', 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (266, '2020-03-29 14:20:36.000', 'system.region.city.140200', '大同市', 'area', '140200', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (267, '2020-03-29 14:20:36.000', 'system.region.area.140212', '新荣区', NULL, '140212', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (268, '2020-03-29 14:20:36.000', 'system.region.area.140213', '平城区', NULL, '140213', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (269, '2020-03-29 14:20:36.000', 'system.region.area.140214', '云冈区', NULL, '140214', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (270, '2020-03-29 14:20:36.000', 'system.region.area.140215', '云州区', NULL, '140215', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (271, '2020-03-29 14:20:36.000', 'system.region.area.140221', '阳高县', NULL, '140221', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (272, '2020-03-29 14:20:36.000', 'system.region.area.140222', '天镇县', NULL, '140222', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (273, '2020-03-29 14:20:36.000', 'system.region.area.140223', '广灵县', NULL, '140223', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (274, '2020-03-29 14:20:36.000', 'system.region.area.140224', '灵丘县', NULL, '140224', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (275, '2020-03-29 14:20:36.000', 'system.region.area.140225', '浑源县', NULL, '140225', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (276, '2020-03-29 14:20:36.000', 'system.region.area.140226', '左云县', NULL, '140226', 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (277, '2020-03-29 14:20:36.000', 'system.region.city.140300', '阳泉市', 'area', '140300', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (278, '2020-03-29 14:20:36.000', 'system.region.area.140302', '城区', NULL, '140302', 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (279, '2020-03-29 14:20:36.000', 'system.region.area.140303', '矿区', NULL, '140303', 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (280, '2020-03-29 14:20:36.000', 'system.region.area.140311', '郊区', NULL, '140311', 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (281, '2020-03-29 14:20:36.000', 'system.region.area.140321', '平定县', NULL, '140321', 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (282, '2020-03-29 14:20:36.000', 'system.region.area.140322', '盂县', NULL, '140322', 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (283, '2020-03-29 14:20:36.000', 'system.region.city.140400', '长治市', 'area', '140400', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (284, '2020-03-29 14:20:36.000', 'system.region.area.140403', '潞州区', NULL, '140403', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (285, '2020-03-29 14:20:36.000', 'system.region.area.140404', '上党区', NULL, '140404', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (286, '2020-03-29 14:20:36.000', 'system.region.area.140405', '屯留区', NULL, '140405', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (287, '2020-03-29 14:20:36.000', 'system.region.area.140406', '潞城区', NULL, '140406', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (288, '2020-03-29 14:20:36.000', 'system.region.area.140423', '襄垣县', NULL, '140423', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (289, '2020-03-29 14:20:36.000', 'system.region.area.140425', '平顺县', NULL, '140425', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (290, '2020-03-29 14:20:36.000', 'system.region.area.140426', '黎城县', NULL, '140426', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (291, '2020-03-29 14:20:36.000', 'system.region.area.140427', '壶关县', NULL, '140427', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (292, '2020-03-29 14:20:36.000', 'system.region.area.140428', '长子县', NULL, '140428', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (293, '2020-03-29 14:20:36.000', 'system.region.area.140429', '武乡县', NULL, '140429', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (294, '2020-03-29 14:20:36.000', 'system.region.area.140430', '沁县', NULL, '140430', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (295, '2020-03-29 14:20:36.000', 'system.region.area.140431', '沁源县', NULL, '140431', 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (296, '2020-03-29 14:20:36.000', 'system.region.city.140500', '晋城市', 'area', '140500', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (297, '2020-03-29 14:20:36.000', 'system.region.area.140502', '城区', NULL, '140502', 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (298, '2020-03-29 14:20:36.000', 'system.region.area.140521', '沁水县', NULL, '140521', 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (299, '2020-03-29 14:20:36.000', 'system.region.area.140522', '阳城县', NULL, '140522', 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (300, '2020-03-29 14:20:36.000', 'system.region.area.140524', '陵川县', NULL, '140524', 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (301, '2020-03-29 14:20:36.000', 'system.region.area.140525', '泽州县', NULL, '140525', 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (302, '2020-03-29 14:20:36.000', 'system.region.area.140581', '高平市', NULL, '140581', 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (303, '2020-03-29 14:20:36.000', 'system.region.city.140600', '朔州市', 'area', '140600', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (304, '2020-03-29 14:20:36.000', 'system.region.area.140602', '朔城区', NULL, '140602', 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (305, '2020-03-29 14:20:36.000', 'system.region.area.140603', '平鲁区', NULL, '140603', 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (306, '2020-03-29 14:20:36.000', 'system.region.area.140621', '山阴县', NULL, '140621', 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (307, '2020-03-29 14:20:36.000', 'system.region.area.140622', '应县', NULL, '140622', 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (308, '2020-03-29 14:20:36.000', 'system.region.area.140623', '右玉县', NULL, '140623', 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (309, '2020-03-29 14:20:36.000', 'system.region.area.140681', '怀仁市', NULL, '140681', 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (310, '2020-03-29 14:20:36.000', 'system.region.city.140700', '晋中市', 'area', '140700', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (311, '2020-03-29 14:20:36.000', 'system.region.area.140702', '榆次区', NULL, '140702', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (312, '2020-03-29 14:20:36.000', 'system.region.area.140703', '太谷区', NULL, '140703', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (313, '2020-03-29 14:20:36.000', 'system.region.area.140721', '榆社县', NULL, '140721', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (314, '2020-03-29 14:20:36.000', 'system.region.area.140722', '左权县', NULL, '140722', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (315, '2020-03-29 14:20:36.000', 'system.region.area.140723', '和顺县', NULL, '140723', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (316, '2020-03-29 14:20:36.000', 'system.region.area.140724', '昔阳县', NULL, '140724', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (317, '2020-03-29 14:20:36.000', 'system.region.area.140725', '寿阳县', NULL, '140725', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (318, '2020-03-29 14:20:36.000', 'system.region.area.140727', '祁县', NULL, '140727', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (319, '2020-03-29 14:20:36.000', 'system.region.area.140728', '平遥县', NULL, '140728', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (320, '2020-03-29 14:20:36.000', 'system.region.area.140729', '灵石县', NULL, '140729', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (321, '2020-03-29 14:20:36.000', 'system.region.area.140781', '介休市', NULL, '140781', 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (322, '2020-03-29 14:20:36.000', 'system.region.city.140800', '运城市', 'area', '140800', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (323, '2020-03-29 14:20:36.000', 'system.region.area.140802', '盐湖区', NULL, '140802', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (324, '2020-03-29 14:20:36.000', 'system.region.area.140821', '临猗县', NULL, '140821', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (325, '2020-03-29 14:20:36.000', 'system.region.area.140822', '万荣县', NULL, '140822', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (326, '2020-03-29 14:20:36.000', 'system.region.area.140823', '闻喜县', NULL, '140823', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (327, '2020-03-29 14:20:36.000', 'system.region.area.140824', '稷山县', NULL, '140824', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (328, '2020-03-29 14:20:36.000', 'system.region.area.140825', '新绛县', NULL, '140825', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (329, '2020-03-29 14:20:36.000', 'system.region.area.140826', '绛县', NULL, '140826', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (330, '2020-03-29 14:20:36.000', 'system.region.area.140827', '垣曲县', NULL, '140827', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (331, '2020-03-29 14:20:36.000', 'system.region.area.140828', '夏县', NULL, '140828', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (332, '2020-03-29 14:20:36.000', 'system.region.area.140829', '平陆县', NULL, '140829', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (333, '2020-03-29 14:20:36.000', 'system.region.area.140830', '芮城县', NULL, '140830', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (334, '2020-03-29 14:20:36.000', 'system.region.area.140881', '永济市', NULL, '140881', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (335, '2020-03-29 14:20:36.000', 'system.region.area.140882', '河津市', NULL, '140882', 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (336, '2020-03-29 14:20:36.000', 'system.region.city.140900', '忻州市', 'area', '140900', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (337, '2020-03-29 14:20:36.000', 'system.region.area.140902', '忻府区', NULL, '140902', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (338, '2020-03-29 14:20:36.000', 'system.region.area.140921', '定襄县', NULL, '140921', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (339, '2020-03-29 14:20:36.000', 'system.region.area.140922', '五台县', NULL, '140922', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (340, '2020-03-29 14:20:36.000', 'system.region.area.140923', '代县', NULL, '140923', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (341, '2020-03-29 14:20:36.000', 'system.region.area.140924', '繁峙县', NULL, '140924', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (342, '2020-03-29 14:20:36.000', 'system.region.area.140925', '宁武县', NULL, '140925', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (343, '2020-03-29 14:20:36.000', 'system.region.area.140926', '静乐县', NULL, '140926', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (344, '2020-03-29 14:20:36.000', 'system.region.area.140927', '神池县', NULL, '140927', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (345, '2020-03-29 14:20:36.000', 'system.region.area.140928', '五寨县', NULL, '140928', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (346, '2020-03-29 14:20:36.000', 'system.region.area.140929', '岢岚县', NULL, '140929', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (347, '2020-03-29 14:20:36.000', 'system.region.area.140930', '河曲县', NULL, '140930', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (348, '2020-03-29 14:20:36.000', 'system.region.area.140931', '保德县', NULL, '140931', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (349, '2020-03-29 14:20:36.000', 'system.region.area.140932', '偏关县', NULL, '140932', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (350, '2020-03-29 14:20:36.000', 'system.region.area.140981', '原平市', NULL, '140981', 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (351, '2020-03-29 14:20:36.000', 'system.region.city.141000', '临汾市', 'area', '141000', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (352, '2020-03-29 14:20:36.000', 'system.region.area.141002', '尧都区', NULL, '141002', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (353, '2020-03-29 14:20:36.000', 'system.region.area.141021', '曲沃县', NULL, '141021', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (354, '2020-03-29 14:20:36.000', 'system.region.area.141022', '翼城县', NULL, '141022', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (355, '2020-03-29 14:20:36.000', 'system.region.area.141023', '襄汾县', NULL, '141023', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (356, '2020-03-29 14:20:36.000', 'system.region.area.141024', '洪洞县', NULL, '141024', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (357, '2020-03-29 14:20:36.000', 'system.region.area.141025', '古县', NULL, '141025', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (358, '2020-03-29 14:20:36.000', 'system.region.area.141026', '安泽县', NULL, '141026', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (359, '2020-03-29 14:20:36.000', 'system.region.area.141027', '浮山县', NULL, '141027', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (360, '2020-03-29 14:20:36.000', 'system.region.area.141028', '吉县', NULL, '141028', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (361, '2020-03-29 14:20:36.000', 'system.region.area.141029', '乡宁县', NULL, '141029', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (362, '2020-03-29 14:20:36.000', 'system.region.area.141030', '大宁县', NULL, '141030', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (363, '2020-03-29 14:20:36.000', 'system.region.area.141031', '隰县', NULL, '141031', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (364, '2020-03-29 14:20:36.000', 'system.region.area.141032', '永和县', NULL, '141032', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (365, '2020-03-29 14:20:36.000', 'system.region.area.141033', '蒲县', NULL, '141033', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (366, '2020-03-29 14:20:36.000', 'system.region.area.141034', '汾西县', NULL, '141034', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (367, '2020-03-29 14:20:36.000', 'system.region.area.141081', '侯马市', NULL, '141081', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (368, '2020-03-29 14:20:36.000', 'system.region.area.141082', '霍州市', NULL, '141082', 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (369, '2020-03-29 14:20:36.000', 'system.region.city.141100', '吕梁市', 'area', '141100', 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (370, '2020-03-29 14:20:36.000', 'system.region.area.141102', '离石区', NULL, '141102', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (371, '2020-03-29 14:20:36.000', 'system.region.area.141121', '文水县', NULL, '141121', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (372, '2020-03-29 14:20:36.000', 'system.region.area.141122', '交城县', NULL, '141122', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (373, '2020-03-29 14:20:36.000', 'system.region.area.141123', '兴县', NULL, '141123', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (374, '2020-03-29 14:20:36.000', 'system.region.area.141124', '临县', NULL, '141124', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (375, '2020-03-29 14:20:36.000', 'system.region.area.141125', '柳林县', NULL, '141125', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (376, '2020-03-29 14:20:36.000', 'system.region.area.141126', '石楼县', NULL, '141126', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (377, '2020-03-29 14:20:36.000', 'system.region.area.141127', '岚县', NULL, '141127', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (378, '2020-03-29 14:20:36.000', 'system.region.area.141128', '方山县', NULL, '141128', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (379, '2020-03-29 14:20:36.000', 'system.region.area.141129', '中阳县', NULL, '141129', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (380, '2020-03-29 14:20:36.000', 'system.region.area.141130', '交口县', NULL, '141130', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (381, '2020-03-29 14:20:36.000', 'system.region.area.141181', '孝义市', NULL, '141181', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (382, '2020-03-29 14:20:36.000', 'system.region.area.141182', '汾阳市', NULL, '141182', 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (383, '2020-03-29 14:20:36.000', 'system.region.province.150000', '内蒙古自治区', 'city', '150000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (384, '2020-03-29 14:20:36.000', 'system.region.city.150100', '呼和浩特市', 'area', '150100', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (385, '2020-03-29 14:20:36.000', 'system.region.area.150102', '新城区', NULL, '150102', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (386, '2020-03-29 14:20:36.000', 'system.region.area.150103', '回民区', NULL, '150103', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (387, '2020-03-29 14:20:36.000', 'system.region.area.150104', '玉泉区', NULL, '150104', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (388, '2020-03-29 14:20:36.000', 'system.region.area.150105', '赛罕区', NULL, '150105', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (389, '2020-03-29 14:20:36.000', 'system.region.area.150121', '土默特左旗', NULL, '150121', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (390, '2020-03-29 14:20:36.000', 'system.region.area.150122', '托克托县', NULL, '150122', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (391, '2020-03-29 14:20:36.000', 'system.region.area.150123', '和林格尔县', NULL, '150123', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (392, '2020-03-29 14:20:36.000', 'system.region.area.150124', '清水河县', NULL, '150124', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (393, '2020-03-29 14:20:36.000', 'system.region.area.150125', '武川县', NULL, '150125', 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (394, '2020-03-29 14:20:36.000', 'system.region.city.150200', '包头市', 'area', '150200', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (395, '2020-03-29 14:20:36.000', 'system.region.area.150202', '东河区', NULL, '150202', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (396, '2020-03-29 14:20:36.000', 'system.region.area.150203', '昆都仑区', NULL, '150203', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (397, '2020-03-29 14:20:36.000', 'system.region.area.150204', '青山区', NULL, '150204', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (398, '2020-03-29 14:20:36.000', 'system.region.area.150205', '石拐区', NULL, '150205', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (399, '2020-03-29 14:20:36.000', 'system.region.area.150206', '白云鄂博矿区', NULL, '150206', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (400, '2020-03-29 14:20:36.000', 'system.region.area.150207', '九原区', NULL, '150207', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (401, '2020-03-29 14:20:36.000', 'system.region.area.150221', '土默特右旗', NULL, '150221', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (402, '2020-03-29 14:20:36.000', 'system.region.area.150222', '固阳县', NULL, '150222', 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (403, '2020-03-29 14:20:36.000', 'system.region.area.150223', '达尔罕茂明安联合旗', NULL, '150223', 1, 16, 394, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (404, '2020-03-29 14:20:36.000', 'system.region.city.150300', '乌海市', 'area', '150300', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (405, '2020-03-29 14:20:36.000', 'system.region.area.150302', '海勃湾区', NULL, '150302', 1, 16, 404, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (406, '2020-03-29 14:20:36.000', 'system.region.area.150303', '海南区', NULL, '150303', 1, 16, 404, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (407, '2020-03-29 14:20:36.000', 'system.region.area.150304', '乌达区', NULL, '150304', 1, 16, 404, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (408, '2020-03-29 14:20:36.000', 'system.region.city.150400', '赤峰市', 'area', '150400', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (409, '2020-03-29 14:20:36.000', 'system.region.area.150402', '红山区', NULL, '150402', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (410, '2020-03-29 14:20:36.000', 'system.region.area.150403', '元宝山区', NULL, '150403', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (411, '2020-03-29 14:20:36.000', 'system.region.area.150404', '松山区', NULL, '150404', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (412, '2020-03-29 14:20:36.000', 'system.region.area.150421', '阿鲁科尔沁旗', NULL, '150421', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (413, '2020-03-29 14:20:36.000', 'system.region.area.150422', '巴林左旗', NULL, '150422', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (414, '2020-03-29 14:20:36.000', 'system.region.area.150423', '巴林右旗', NULL, '150423', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (415, '2020-03-29 14:20:36.000', 'system.region.area.150424', '林西县', NULL, '150424', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (416, '2020-03-29 14:20:36.000', 'system.region.area.150425', '克什克腾旗', NULL, '150425', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (417, '2020-03-29 14:20:36.000', 'system.region.area.150426', '翁牛特旗', NULL, '150426', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (418, '2020-03-29 14:20:36.000', 'system.region.area.150428', '喀喇沁旗', NULL, '150428', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (419, '2020-03-29 14:20:36.000', 'system.region.area.150429', '宁城县', NULL, '150429', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (420, '2020-03-29 14:20:36.000', 'system.region.area.150430', '敖汉旗', NULL, '150430', 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (421, '2020-03-29 14:20:36.000', 'system.region.city.150500', '通辽市', 'area', '150500', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (422, '2020-03-29 14:20:36.000', 'system.region.area.150502', '科尔沁区', NULL, '150502', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (423, '2020-03-29 14:20:36.000', 'system.region.area.150521', '科尔沁左翼中旗', NULL, '150521', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (424, '2020-03-29 14:20:36.000', 'system.region.area.150522', '科尔沁左翼后旗', NULL, '150522', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (425, '2020-03-29 14:20:36.000', 'system.region.area.150523', '开鲁县', NULL, '150523', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (426, '2020-03-29 14:20:36.000', 'system.region.area.150524', '库伦旗', NULL, '150524', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (427, '2020-03-29 14:20:36.000', 'system.region.area.150525', '奈曼旗', NULL, '150525', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (428, '2020-03-29 14:20:36.000', 'system.region.area.150526', '扎鲁特旗', NULL, '150526', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (429, '2020-03-29 14:20:36.000', 'system.region.area.150581', '霍林郭勒市', NULL, '150581', 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (430, '2020-03-29 14:20:36.000', 'system.region.city.150600', '鄂尔多斯市', 'area', '150600', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (431, '2020-03-29 14:20:36.000', 'system.region.area.150602', '东胜区', NULL, '150602', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (432, '2020-03-29 14:20:36.000', 'system.region.area.150603', '康巴什区', NULL, '150603', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (433, '2020-03-29 14:20:36.000', 'system.region.area.150621', '达拉特旗', NULL, '150621', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (434, '2020-03-29 14:20:36.000', 'system.region.area.150622', '准格尔旗', NULL, '150622', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (435, '2020-03-29 14:20:36.000', 'system.region.area.150623', '鄂托克前旗', NULL, '150623', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (436, '2020-03-29 14:20:36.000', 'system.region.area.150624', '鄂托克旗', NULL, '150624', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (437, '2020-03-29 14:20:36.000', 'system.region.area.150625', '杭锦旗', NULL, '150625', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (438, '2020-03-29 14:20:36.000', 'system.region.area.150626', '乌审旗', NULL, '150626', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (439, '2020-03-29 14:20:36.000', 'system.region.area.150627', '伊金霍洛旗', NULL, '150627', 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (440, '2020-03-29 14:20:36.000', 'system.region.city.150700', '呼伦贝尔市', 'area', '150700', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (441, '2020-03-29 14:20:36.000', 'system.region.area.150702', '海拉尔区', NULL, '150702', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (442, '2020-03-29 14:20:36.000', 'system.region.area.150703', '扎赉诺尔区', NULL, '150703', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (443, '2020-03-29 14:20:36.000', 'system.region.area.150721', '阿荣旗', NULL, '150721', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (444, '2020-03-29 14:20:36.000', 'system.region.area.150722', '莫力达瓦达斡尔族自治旗', NULL, '150722', 1, 16, 440, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (445, '2020-03-29 14:20:36.000', 'system.region.area.150723', '鄂伦春自治旗', NULL, '150723', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (446, '2020-03-29 14:20:36.000', 'system.region.area.150724', '鄂温克族自治旗', NULL, '150724', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (447, '2020-03-29 14:20:36.000', 'system.region.area.150725', '陈巴尔虎旗', NULL, '150725', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (448, '2020-03-29 14:20:36.000', 'system.region.area.150726', '新巴尔虎左旗', NULL, '150726', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (449, '2020-03-29 14:20:36.000', 'system.region.area.150727', '新巴尔虎右旗', NULL, '150727', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (450, '2020-03-29 14:20:36.000', 'system.region.area.150781', '满洲里市', NULL, '150781', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (451, '2020-03-29 14:20:36.000', 'system.region.area.150782', '牙克石市', NULL, '150782', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (452, '2020-03-29 14:20:36.000', 'system.region.area.150783', '扎兰屯市', NULL, '150783', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (453, '2020-03-29 14:20:36.000', 'system.region.area.150784', '额尔古纳市', NULL, '150784', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (454, '2020-03-29 14:20:36.000', 'system.region.area.150785', '根河市', NULL, '150785', 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (455, '2020-03-29 14:20:36.000', 'system.region.city.150800', '巴彦淖尔市', 'area', '150800', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (456, '2020-03-29 14:20:36.000', 'system.region.area.150802', '临河区', NULL, '150802', 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (457, '2020-03-29 14:20:36.000', 'system.region.area.150821', '五原县', NULL, '150821', 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (458, '2020-03-29 14:20:36.000', 'system.region.area.150822', '磴口县', NULL, '150822', 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (459, '2020-03-29 14:20:36.000', 'system.region.area.150823', '乌拉特前旗', NULL, '150823', 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (460, '2020-03-29 14:20:36.000', 'system.region.area.150824', '乌拉特中旗', NULL, '150824', 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (461, '2020-03-29 14:20:36.000', 'system.region.area.150825', '乌拉特后旗', NULL, '150825', 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (462, '2020-03-29 14:20:36.000', 'system.region.area.150826', '杭锦后旗', NULL, '150826', 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (463, '2020-03-29 14:20:36.000', 'system.region.city.150900', '乌兰察布市', 'area', '150900', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (464, '2020-03-29 14:20:36.000', 'system.region.area.150902', '集宁区', NULL, '150902', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (465, '2020-03-29 14:20:36.000', 'system.region.area.150921', '卓资县', NULL, '150921', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (466, '2020-03-29 14:20:36.000', 'system.region.area.150922', '化德县', NULL, '150922', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (467, '2020-03-29 14:20:36.000', 'system.region.area.150923', '商都县', NULL, '150923', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (468, '2020-03-29 14:20:36.000', 'system.region.area.150924', '兴和县', NULL, '150924', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (469, '2020-03-29 14:20:36.000', 'system.region.area.150925', '凉城县', NULL, '150925', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (470, '2020-03-29 14:20:36.000', 'system.region.area.150926', '察哈尔右翼前旗', NULL, '150926', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (471, '2020-03-29 14:20:36.000', 'system.region.area.150927', '察哈尔右翼中旗', NULL, '150927', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (472, '2020-03-29 14:20:36.000', 'system.region.area.150928', '察哈尔右翼后旗', NULL, '150928', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (473, '2020-03-29 14:20:36.000', 'system.region.area.150929', '四子王旗', NULL, '150929', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (474, '2020-03-29 14:20:36.000', 'system.region.area.150981', '丰镇市', NULL, '150981', 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (475, '2020-03-29 14:20:36.000', 'system.region.city.152200', '兴安盟', 'area', '152200', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (476, '2020-03-29 14:20:36.000', 'system.region.area.152201', '乌兰浩特市', NULL, '152201', 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (477, '2020-03-29 14:20:36.000', 'system.region.area.152202', '阿尔山市', NULL, '152202', 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (478, '2020-03-29 14:20:36.000', 'system.region.area.152221', '科尔沁右翼前旗', NULL, '152221', 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (479, '2020-03-29 14:20:36.000', 'system.region.area.152222', '科尔沁右翼中旗', NULL, '152222', 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (480, '2020-03-29 14:20:36.000', 'system.region.area.152223', '扎赉特旗', NULL, '152223', 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (481, '2020-03-29 14:20:36.000', 'system.region.area.152224', '突泉县', NULL, '152224', 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (482, '2020-03-29 14:20:36.000', 'system.region.city.152500', '锡林郭勒盟', 'area', '152500', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (483, '2020-03-29 14:20:36.000', 'system.region.area.152501', '二连浩特市', NULL, '152501', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (484, '2020-03-29 14:20:36.000', 'system.region.area.152502', '锡林浩特市', NULL, '152502', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (485, '2020-03-29 14:20:36.000', 'system.region.area.152522', '阿巴嘎旗', NULL, '152522', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (486, '2020-03-29 14:20:36.000', 'system.region.area.152523', '苏尼特左旗', NULL, '152523', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (487, '2020-03-29 14:20:36.000', 'system.region.area.152524', '苏尼特右旗', NULL, '152524', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (488, '2020-03-29 14:20:36.000', 'system.region.area.152525', '东乌珠穆沁旗', NULL, '152525', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (489, '2020-03-29 14:20:36.000', 'system.region.area.152526', '西乌珠穆沁旗', NULL, '152526', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (490, '2020-03-29 14:20:36.000', 'system.region.area.152527', '太仆寺旗', NULL, '152527', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (491, '2020-03-29 14:20:36.000', 'system.region.area.152528', '镶黄旗', NULL, '152528', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (492, '2020-03-29 14:20:36.000', 'system.region.area.152529', '正镶白旗', NULL, '152529', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (493, '2020-03-29 14:20:36.000', 'system.region.area.152530', '正蓝旗', NULL, '152530', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (494, '2020-03-29 14:20:36.000', 'system.region.area.152531', '多伦县', NULL, '152531', 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (495, '2020-03-29 14:20:36.000', 'system.region.city.152900', '阿拉善盟', 'area', '152900', 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (496, '2020-03-29 14:20:36.000', 'system.region.area.152921', '阿拉善左旗', NULL, '152921', 1, 16, 495, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (497, '2020-03-29 14:20:36.000', 'system.region.area.152922', '阿拉善右旗', NULL, '152922', 1, 16, 495, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (498, '2020-03-29 14:20:36.000', 'system.region.area.152923', '额济纳旗', NULL, '152923', 1, 16, 495, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (499, '2020-03-29 14:20:36.000', 'system.region.province.210000', '辽宁省', 'city', '210000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (500, '2020-03-29 14:20:36.000', 'system.region.city.210100', '沈阳市', 'area', '210100', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (501, '2020-03-29 14:20:36.000', 'system.region.area.210102', '和平区', NULL, '210102', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (502, '2020-03-29 14:20:36.000', 'system.region.area.210103', '沈河区', NULL, '210103', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (503, '2020-03-29 14:20:36.000', 'system.region.area.210104', '大东区', NULL, '210104', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (504, '2020-03-29 14:20:36.000', 'system.region.area.210105', '皇姑区', NULL, '210105', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (505, '2020-03-29 14:20:36.000', 'system.region.area.210106', '铁西区', NULL, '210106', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (506, '2020-03-29 14:20:36.000', 'system.region.area.210111', '苏家屯区', NULL, '210111', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (507, '2020-03-29 14:20:36.000', 'system.region.area.210112', '浑南区', NULL, '210112', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (508, '2020-03-29 14:20:36.000', 'system.region.area.210113', '沈北新区', NULL, '210113', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (509, '2020-03-29 14:20:36.000', 'system.region.area.210114', '于洪区', NULL, '210114', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (510, '2020-03-29 14:20:36.000', 'system.region.area.210115', '辽中区', NULL, '210115', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (511, '2020-03-29 14:20:36.000', 'system.region.area.210123', '康平县', NULL, '210123', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (512, '2020-03-29 14:20:36.000', 'system.region.area.210124', '法库县', NULL, '210124', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (513, '2020-03-29 14:20:36.000', 'system.region.area.210181', '新民市', NULL, '210181', 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (514, '2020-03-29 14:20:36.000', 'system.region.city.210200', '大连市', 'area', '210200', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (515, '2020-03-29 14:20:36.000', 'system.region.area.210202', '中山区', NULL, '210202', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (516, '2020-03-29 14:20:36.000', 'system.region.area.210203', '西岗区', NULL, '210203', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (517, '2020-03-29 14:20:36.000', 'system.region.area.210204', '沙河口区', NULL, '210204', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (518, '2020-03-29 14:20:36.000', 'system.region.area.210211', '甘井子区', NULL, '210211', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (519, '2020-03-29 14:20:36.000', 'system.region.area.210212', '旅顺口区', NULL, '210212', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (520, '2020-03-29 14:20:36.000', 'system.region.area.210213', '金州区', NULL, '210213', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (521, '2020-03-29 14:20:36.000', 'system.region.area.210214', '普兰店区', NULL, '210214', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (522, '2020-03-29 14:20:36.000', 'system.region.area.210224', '长海县', NULL, '210224', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (523, '2020-03-29 14:20:36.000', 'system.region.area.210281', '瓦房店市', NULL, '210281', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (524, '2020-03-29 14:20:36.000', 'system.region.area.210283', '庄河市', NULL, '210283', 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (525, '2020-03-29 14:20:36.000', 'system.region.city.210300', '鞍山市', 'area', '210300', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (526, '2020-03-29 14:20:36.000', 'system.region.area.210302', '铁东区', NULL, '210302', 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (527, '2020-03-29 14:20:36.000', 'system.region.area.210303', '铁西区', NULL, '210303', 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (528, '2020-03-29 14:20:36.000', 'system.region.area.210304', '立山区', NULL, '210304', 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (529, '2020-03-29 14:20:36.000', 'system.region.area.210311', '千山区', NULL, '210311', 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (530, '2020-03-29 14:20:36.000', 'system.region.area.210321', '台安县', NULL, '210321', 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (531, '2020-03-29 14:20:36.000', 'system.region.area.210323', '岫岩满族自治县', NULL, '210323', 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (532, '2020-03-29 14:20:36.000', 'system.region.area.210381', '海城市', NULL, '210381', 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (533, '2020-03-29 14:20:36.000', 'system.region.city.210400', '抚顺市', 'area', '210400', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (534, '2020-03-29 14:20:36.000', 'system.region.area.210402', '新抚区', NULL, '210402', 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (535, '2020-03-29 14:20:36.000', 'system.region.area.210403', '东洲区', NULL, '210403', 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (536, '2020-03-29 14:20:36.000', 'system.region.area.210404', '望花区', NULL, '210404', 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (537, '2020-03-29 14:20:36.000', 'system.region.area.210411', '顺城区', NULL, '210411', 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (538, '2020-03-29 14:20:36.000', 'system.region.area.210421', '抚顺县', NULL, '210421', 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (539, '2020-03-29 14:20:36.000', 'system.region.area.210422', '新宾满族自治县', NULL, '210422', 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (540, '2020-03-29 14:20:36.000', 'system.region.area.210423', '清原满族自治县', NULL, '210423', 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (541, '2020-03-29 14:20:36.000', 'system.region.city.210500', '本溪市', 'area', '210500', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (542, '2020-03-29 14:20:36.000', 'system.region.area.210502', '平山区', NULL, '210502', 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (543, '2020-03-29 14:20:36.000', 'system.region.area.210503', '溪湖区', NULL, '210503', 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (544, '2020-03-29 14:20:36.000', 'system.region.area.210504', '明山区', NULL, '210504', 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (545, '2020-03-29 14:20:36.000', 'system.region.area.210505', '南芬区', NULL, '210505', 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (546, '2020-03-29 14:20:36.000', 'system.region.area.210521', '本溪满族自治县', NULL, '210521', 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (547, '2020-03-29 14:20:36.000', 'system.region.area.210522', '桓仁满族自治县', NULL, '210522', 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (548, '2020-03-29 14:20:36.000', 'system.region.city.210600', '丹东市', 'area', '210600', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (549, '2020-03-29 14:20:36.000', 'system.region.area.210602', '元宝区', NULL, '210602', 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (550, '2020-03-29 14:20:36.000', 'system.region.area.210603', '振兴区', NULL, '210603', 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (551, '2020-03-29 14:20:36.000', 'system.region.area.210604', '振安区', NULL, '210604', 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (552, '2020-03-29 14:20:36.000', 'system.region.area.210624', '宽甸满族自治县', NULL, '210624', 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (553, '2020-03-29 14:20:36.000', 'system.region.area.210681', '东港市', NULL, '210681', 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (554, '2020-03-29 14:20:36.000', 'system.region.area.210682', '凤城市', NULL, '210682', 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (555, '2020-03-29 14:20:36.000', 'system.region.city.210700', '锦州市', 'area', '210700', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (556, '2020-03-29 14:20:36.000', 'system.region.area.210702', '古塔区', NULL, '210702', 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (557, '2020-03-29 14:20:36.000', 'system.region.area.210703', '凌河区', NULL, '210703', 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (558, '2020-03-29 14:20:36.000', 'system.region.area.210711', '太和区', NULL, '210711', 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (559, '2020-03-29 14:20:36.000', 'system.region.area.210726', '黑山县', NULL, '210726', 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (560, '2020-03-29 14:20:36.000', 'system.region.area.210727', '义县', NULL, '210727', 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (561, '2020-03-29 14:20:36.000', 'system.region.area.210781', '凌海市', NULL, '210781', 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (562, '2020-03-29 14:20:36.000', 'system.region.area.210782', '北镇市', NULL, '210782', 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (563, '2020-03-29 14:20:36.000', 'system.region.city.210800', '营口市', 'area', '210800', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (564, '2020-03-29 14:20:36.000', 'system.region.area.210802', '站前区', NULL, '210802', 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (565, '2020-03-29 14:20:36.000', 'system.region.area.210803', '西市区', NULL, '210803', 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (566, '2020-03-29 14:20:36.000', 'system.region.area.210804', '鲅鱼圈区', NULL, '210804', 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (567, '2020-03-29 14:20:36.000', 'system.region.area.210811', '老边区', NULL, '210811', 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (568, '2020-03-29 14:20:36.000', 'system.region.area.210881', '盖州市', NULL, '210881', 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (569, '2020-03-29 14:20:36.000', 'system.region.area.210882', '大石桥市', NULL, '210882', 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (570, '2020-03-29 14:20:36.000', 'system.region.city.210900', '阜新市', 'area', '210900', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (571, '2020-03-29 14:20:36.000', 'system.region.area.210902', '海州区', NULL, '210902', 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (572, '2020-03-29 14:20:36.000', 'system.region.area.210903', '新邱区', NULL, '210903', 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (573, '2020-03-29 14:20:36.000', 'system.region.area.210904', '太平区', NULL, '210904', 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (574, '2020-03-29 14:20:36.000', 'system.region.area.210905', '清河门区', NULL, '210905', 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (575, '2020-03-29 14:20:36.000', 'system.region.area.210911', '细河区', NULL, '210911', 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (576, '2020-03-29 14:20:36.000', 'system.region.area.210921', '阜新蒙古族自治县', NULL, '210921', 1, 16, 570, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (577, '2020-03-29 14:20:36.000', 'system.region.area.210922', '彰武县', NULL, '210922', 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (578, '2020-03-29 14:20:36.000', 'system.region.city.211000', '辽阳市', 'area', '211000', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (579, '2020-03-29 14:20:36.000', 'system.region.area.211002', '白塔区', NULL, '211002', 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (580, '2020-03-29 14:20:36.000', 'system.region.area.211003', '文圣区', NULL, '211003', 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (581, '2020-03-29 14:20:36.000', 'system.region.area.211004', '宏伟区', NULL, '211004', 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (582, '2020-03-29 14:20:36.000', 'system.region.area.211005', '弓长岭区', NULL, '211005', 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (583, '2020-03-29 14:20:36.000', 'system.region.area.211011', '太子河区', NULL, '211011', 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (584, '2020-03-29 14:20:36.000', 'system.region.area.211021', '辽阳县', NULL, '211021', 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (585, '2020-03-29 14:20:36.000', 'system.region.area.211081', '灯塔市', NULL, '211081', 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (586, '2020-03-29 14:20:36.000', 'system.region.city.211100', '盘锦市', 'area', '211100', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (587, '2020-03-29 14:20:36.000', 'system.region.area.211102', '双台子区', NULL, '211102', 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (588, '2020-03-29 14:20:36.000', 'system.region.area.211103', '兴隆台区', NULL, '211103', 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (589, '2020-03-29 14:20:36.000', 'system.region.area.211104', '大洼区', NULL, '211104', 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (590, '2020-03-29 14:20:36.000', 'system.region.area.211122', '盘山县', NULL, '211122', 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (591, '2020-03-29 14:20:36.000', 'system.region.city.211200', '铁岭市', 'area', '211200', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (592, '2020-03-29 14:20:36.000', 'system.region.area.211202', '银州区', NULL, '211202', 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (593, '2020-03-29 14:20:36.000', 'system.region.area.211204', '清河区', NULL, '211204', 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (594, '2020-03-29 14:20:36.000', 'system.region.area.211221', '铁岭县', NULL, '211221', 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (595, '2020-03-29 14:20:36.000', 'system.region.area.211223', '西丰县', NULL, '211223', 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (596, '2020-03-29 14:20:36.000', 'system.region.area.211224', '昌图县', NULL, '211224', 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (597, '2020-03-29 14:20:36.000', 'system.region.area.211281', '调兵山市', NULL, '211281', 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (598, '2020-03-29 14:20:36.000', 'system.region.area.211282', '开原市', NULL, '211282', 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (599, '2020-03-29 14:20:36.000', 'system.region.city.211300', '朝阳市', 'area', '211300', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (600, '2020-03-29 14:20:36.000', 'system.region.area.211302', '双塔区', NULL, '211302', 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (601, '2020-03-29 14:20:36.000', 'system.region.area.211303', '龙城区', NULL, '211303', 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (602, '2020-03-29 14:20:36.000', 'system.region.area.211321', '朝阳县', NULL, '211321', 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (603, '2020-03-29 14:20:36.000', 'system.region.area.211322', '建平县', NULL, '211322', 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (604, '2020-03-29 14:20:36.000', 'system.region.area.211324', '喀喇沁左翼蒙古族自治县', NULL, '211324', 1, 16, 599, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (605, '2020-03-29 14:20:36.000', 'system.region.area.211381', '北票市', NULL, '211381', 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (606, '2020-03-29 14:20:36.000', 'system.region.area.211382', '凌源市', NULL, '211382', 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (607, '2020-03-29 14:20:36.000', 'system.region.city.211400', '葫芦岛市', 'area', '211400', 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (608, '2020-03-29 14:20:36.000', 'system.region.area.211402', '连山区', NULL, '211402', 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (609, '2020-03-29 14:20:36.000', 'system.region.area.211403', '龙港区', NULL, '211403', 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (610, '2020-03-29 14:20:36.000', 'system.region.area.211404', '南票区', NULL, '211404', 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (611, '2020-03-29 14:20:36.000', 'system.region.area.211421', '绥中县', NULL, '211421', 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (612, '2020-03-29 14:20:36.000', 'system.region.area.211422', '建昌县', NULL, '211422', 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (613, '2020-03-29 14:20:36.000', 'system.region.area.211481', '兴城市', NULL, '211481', 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (614, '2020-03-29 14:20:36.000', 'system.region.province.220000', '吉林省', 'city', '220000', 1, 14, 607, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (615, '2020-03-29 14:20:36.000', 'system.region.city.220100', '长春市', 'area', '220100', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (616, '2020-03-29 14:20:36.000', 'system.region.area.220102', '南关区', NULL, '220102', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (617, '2020-03-29 14:20:36.000', 'system.region.area.220103', '宽城区', NULL, '220103', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (618, '2020-03-29 14:20:36.000', 'system.region.area.220104', '朝阳区', NULL, '220104', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (619, '2020-03-29 14:20:36.000', 'system.region.area.220105', '二道区', NULL, '220105', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (620, '2020-03-29 14:20:36.000', 'system.region.area.220106', '绿园区', NULL, '220106', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (621, '2020-03-29 14:20:36.000', 'system.region.area.220112', '双阳区', NULL, '220112', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (622, '2020-03-29 14:20:36.000', 'system.region.area.220113', '九台区', NULL, '220113', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (623, '2020-03-29 14:20:36.000', 'system.region.area.220122', '农安县', NULL, '220122', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (624, '2020-03-29 14:20:36.000', 'system.region.area.220182', '榆树市', NULL, '220182', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (625, '2020-03-29 14:20:36.000', 'system.region.area.220183', '德惠市', NULL, '220183', 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (626, '2020-03-29 14:20:36.000', 'system.region.city.220200', '吉林市', 'area', '220200', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (627, '2020-03-29 14:20:36.000', 'system.region.area.220202', '昌邑区', NULL, '220202', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (628, '2020-03-29 14:20:36.000', 'system.region.area.220203', '龙潭区', NULL, '220203', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (629, '2020-03-29 14:20:36.000', 'system.region.area.220204', '船营区', NULL, '220204', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (630, '2020-03-29 14:20:36.000', 'system.region.area.220211', '丰满区', NULL, '220211', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (631, '2020-03-29 14:20:36.000', 'system.region.area.220221', '永吉县', NULL, '220221', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (632, '2020-03-29 14:20:36.000', 'system.region.area.220281', '蛟河市', NULL, '220281', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (633, '2020-03-29 14:20:36.000', 'system.region.area.220282', '桦甸市', NULL, '220282', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (634, '2020-03-29 14:20:36.000', 'system.region.area.220283', '舒兰市', NULL, '220283', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (635, '2020-03-29 14:20:36.000', 'system.region.area.220284', '磐石市', NULL, '220284', 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (636, '2020-03-29 14:20:36.000', 'system.region.city.220300', '四平市', 'area', '220300', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (637, '2020-03-29 14:20:36.000', 'system.region.area.220302', '铁西区', NULL, '220302', 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (638, '2020-03-29 14:20:36.000', 'system.region.area.220303', '铁东区', NULL, '220303', 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (639, '2020-03-29 14:20:36.000', 'system.region.area.220322', '梨树县', NULL, '220322', 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (640, '2020-03-29 14:20:36.000', 'system.region.area.220323', '伊通满族自治县', NULL, '220323', 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (641, '2020-03-29 14:20:36.000', 'system.region.area.220381', '公主岭市', NULL, '220381', 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (642, '2020-03-29 14:20:36.000', 'system.region.area.220382', '双辽市', NULL, '220382', 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (643, '2020-03-29 14:20:36.000', 'system.region.city.220400', '辽源市', 'area', '220400', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (644, '2020-03-29 14:20:36.000', 'system.region.area.220402', '龙山区', NULL, '220402', 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (645, '2020-03-29 14:20:36.000', 'system.region.area.220403', '西安区', NULL, '220403', 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (646, '2020-03-29 14:20:36.000', 'system.region.area.220421', '东丰县', NULL, '220421', 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (647, '2020-03-29 14:20:36.000', 'system.region.area.220422', '东辽县', NULL, '220422', 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (648, '2020-03-29 14:20:36.000', 'system.region.city.220500', '通化市', 'area', '220500', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (649, '2020-03-29 14:20:36.000', 'system.region.area.220502', '东昌区', NULL, '220502', 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (650, '2020-03-29 14:20:36.000', 'system.region.area.220503', '二道江区', NULL, '220503', 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (651, '2020-03-29 14:20:36.000', 'system.region.area.220521', '通化县', NULL, '220521', 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (652, '2020-03-29 14:20:36.000', 'system.region.area.220523', '辉南县', NULL, '220523', 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (653, '2020-03-29 14:20:36.000', 'system.region.area.220524', '柳河县', NULL, '220524', 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (654, '2020-03-29 14:20:36.000', 'system.region.area.220581', '梅河口市', NULL, '220581', 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (655, '2020-03-29 14:20:36.000', 'system.region.area.220582', '集安市', NULL, '220582', 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (656, '2020-03-29 14:20:36.000', 'system.region.city.220600', '白山市', 'area', '220600', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (657, '2020-03-29 14:20:36.000', 'system.region.area.220602', '浑江区', NULL, '220602', 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (658, '2020-03-29 14:20:36.000', 'system.region.area.220605', '江源区', NULL, '220605', 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (659, '2020-03-29 14:20:36.000', 'system.region.area.220621', '抚松县', NULL, '220621', 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (660, '2020-03-29 14:20:36.000', 'system.region.area.220622', '靖宇县', NULL, '220622', 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (661, '2020-03-29 14:20:36.000', 'system.region.area.220623', '长白朝鲜族自治县', NULL, '220623', 1, 16, 656, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (662, '2020-03-29 14:20:36.000', 'system.region.area.220681', '临江市', NULL, '220681', 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (663, '2020-03-29 14:20:36.000', 'system.region.city.220700', '松原市', 'area', '220700', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (664, '2020-03-29 14:20:36.000', 'system.region.area.220702', '宁江区', NULL, '220702', 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (665, '2020-03-29 14:20:36.000', 'system.region.area.220721', '前郭尔罗斯蒙古族自治县', NULL, '220721', 1, 16, 663, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (666, '2020-03-29 14:20:36.000', 'system.region.area.220722', '长岭县', NULL, '220722', 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (667, '2020-03-29 14:20:36.000', 'system.region.area.220723', '乾安县', NULL, '220723', 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (668, '2020-03-29 14:20:36.000', 'system.region.area.220781', '扶余市', NULL, '220781', 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (669, '2020-03-29 14:20:36.000', 'system.region.city.220800', '白城市', 'area', '220800', 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (670, '2020-03-29 14:20:36.000', 'system.region.area.220802', '洮北区', NULL, '220802', 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (671, '2020-03-29 14:20:36.000', 'system.region.area.220821', '镇赉县', NULL, '220821', 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (672, '2020-03-29 14:20:36.000', 'system.region.area.220822', '通榆县', NULL, '220822', 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (673, '2020-03-29 14:20:36.000', 'system.region.area.220881', '洮南市', NULL, '220881', 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (674, '2020-03-29 14:20:36.000', 'system.region.area.220882', '大安市', NULL, '220882', 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (675, '2020-03-29 14:20:36.000', 'system.region.city.222400', '延边朝鲜族自治州', 'area', '222400', 1, 15, 614, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (676, '2020-03-29 14:20:36.000', 'system.region.area.222401', '延吉市', NULL, '222401', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (677, '2020-03-29 14:20:36.000', 'system.region.area.222402', '图们市', NULL, '222402', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (678, '2020-03-29 14:20:36.000', 'system.region.area.222403', '敦化市', NULL, '222403', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (679, '2020-03-29 14:20:36.000', 'system.region.area.222404', '珲春市', NULL, '222404', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (680, '2020-03-29 14:20:36.000', 'system.region.area.222405', '龙井市', NULL, '222405', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (681, '2020-03-29 14:20:36.000', 'system.region.area.222406', '和龙市', NULL, '222406', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (682, '2020-03-29 14:20:36.000', 'system.region.area.222424', '汪清县', NULL, '222424', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (683, '2020-03-29 14:20:36.000', 'system.region.area.222426', '安图县', NULL, '222426', 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (684, '2020-03-29 14:20:36.000', 'system.region.province.230000', '黑龙江省', 'city', '230000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (685, '2020-03-29 14:20:36.000', 'system.region.city.230100', '哈尔滨市', 'area', '230100', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (686, '2020-03-29 14:20:36.000', 'system.region.area.230102', '道里区', NULL, '230102', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (687, '2020-03-29 14:20:36.000', 'system.region.area.230103', '南岗区', NULL, '230103', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (688, '2020-03-29 14:20:36.000', 'system.region.area.230104', '道外区', NULL, '230104', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (689, '2020-03-29 14:20:36.000', 'system.region.area.230108', '平房区', NULL, '230108', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (690, '2020-03-29 14:20:36.000', 'system.region.area.230109', '松北区', NULL, '230109', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (691, '2020-03-29 14:20:36.000', 'system.region.area.230110', '香坊区', NULL, '230110', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (692, '2020-03-29 14:20:36.000', 'system.region.area.230111', '呼兰区', NULL, '230111', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (693, '2020-03-29 14:20:36.000', 'system.region.area.230112', '阿城区', NULL, '230112', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (694, '2020-03-29 14:20:36.000', 'system.region.area.230113', '双城区', NULL, '230113', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (695, '2020-03-29 14:20:36.000', 'system.region.area.230123', '依兰县', NULL, '230123', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (696, '2020-03-29 14:20:36.000', 'system.region.area.230124', '方正县', NULL, '230124', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (697, '2020-03-29 14:20:36.000', 'system.region.area.230125', '宾县', NULL, '230125', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (698, '2020-03-29 14:20:36.000', 'system.region.area.230126', '巴彦县', NULL, '230126', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (699, '2020-03-29 14:20:36.000', 'system.region.area.230127', '木兰县', NULL, '230127', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (700, '2020-03-29 14:20:36.000', 'system.region.area.230128', '通河县', NULL, '230128', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (701, '2020-03-29 14:20:36.000', 'system.region.area.230129', '延寿县', NULL, '230129', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (702, '2020-03-29 14:20:36.000', 'system.region.area.230183', '尚志市', NULL, '230183', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (703, '2020-03-29 14:20:36.000', 'system.region.area.230184', '五常市', NULL, '230184', 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (704, '2020-03-29 14:20:36.000', 'system.region.city.230200', '齐齐哈尔市', 'area', '230200', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (705, '2020-03-29 14:20:36.000', 'system.region.area.230202', '龙沙区', NULL, '230202', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (706, '2020-03-29 14:20:36.000', 'system.region.area.230203', '建华区', NULL, '230203', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (707, '2020-03-29 14:20:36.000', 'system.region.area.230204', '铁锋区', NULL, '230204', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (708, '2020-03-29 14:20:36.000', 'system.region.area.230205', '昂昂溪区', NULL, '230205', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (709, '2020-03-29 14:20:36.000', 'system.region.area.230206', '富拉尔基区', NULL, '230206', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (710, '2020-03-29 14:20:36.000', 'system.region.area.230207', '碾子山区', NULL, '230207', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (711, '2020-03-29 14:20:36.000', 'system.region.area.230208', '梅里斯达斡尔族区', NULL, '230208', 1, 16, 704, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (712, '2020-03-29 14:20:36.000', 'system.region.area.230221', '龙江县', NULL, '230221', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (713, '2020-03-29 14:20:36.000', 'system.region.area.230223', '依安县', NULL, '230223', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (714, '2020-03-29 14:20:36.000', 'system.region.area.230224', '泰来县', NULL, '230224', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (715, '2020-03-29 14:20:36.000', 'system.region.area.230225', '甘南县', NULL, '230225', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (716, '2020-03-29 14:20:36.000', 'system.region.area.230227', '富裕县', NULL, '230227', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (717, '2020-03-29 14:20:36.000', 'system.region.area.230229', '克山县', NULL, '230229', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (718, '2020-03-29 14:20:36.000', 'system.region.area.230230', '克东县', NULL, '230230', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (719, '2020-03-29 14:20:36.000', 'system.region.area.230231', '拜泉县', NULL, '230231', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (720, '2020-03-29 14:20:36.000', 'system.region.area.230281', '讷河市', NULL, '230281', 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (721, '2020-03-29 14:20:36.000', 'system.region.city.230300', '鸡西市', 'area', '230300', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (722, '2020-03-29 14:20:36.000', 'system.region.area.230302', '鸡冠区', NULL, '230302', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (723, '2020-03-29 14:20:36.000', 'system.region.area.230303', '恒山区', NULL, '230303', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (724, '2020-03-29 14:20:36.000', 'system.region.area.230304', '滴道区', NULL, '230304', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (725, '2020-03-29 14:20:36.000', 'system.region.area.230305', '梨树区', NULL, '230305', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (726, '2020-03-29 14:20:36.000', 'system.region.area.230306', '城子河区', NULL, '230306', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (727, '2020-03-29 14:20:36.000', 'system.region.area.230307', '麻山区', NULL, '230307', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (728, '2020-03-29 14:20:36.000', 'system.region.area.230321', '鸡东县', NULL, '230321', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (729, '2020-03-29 14:20:36.000', 'system.region.area.230381', '虎林市', NULL, '230381', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (730, '2020-03-29 14:20:36.000', 'system.region.area.230382', '密山市', NULL, '230382', 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (731, '2020-03-29 14:20:36.000', 'system.region.city.230400', '鹤岗市', 'area', '230400', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (732, '2020-03-29 14:20:36.000', 'system.region.area.230402', '向阳区', NULL, '230402', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (733, '2020-03-29 14:20:36.000', 'system.region.area.230403', '工农区', NULL, '230403', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (734, '2020-03-29 14:20:36.000', 'system.region.area.230404', '南山区', NULL, '230404', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (735, '2020-03-29 14:20:36.000', 'system.region.area.230405', '兴安区', NULL, '230405', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (736, '2020-03-29 14:20:36.000', 'system.region.area.230406', '东山区', NULL, '230406', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (737, '2020-03-29 14:20:36.000', 'system.region.area.230407', '兴山区', NULL, '230407', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (738, '2020-03-29 14:20:36.000', 'system.region.area.230421', '萝北县', NULL, '230421', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (739, '2020-03-29 14:20:36.000', 'system.region.area.230422', '绥滨县', NULL, '230422', 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (740, '2020-03-29 14:20:36.000', 'system.region.city.230500', '双鸭山市', 'area', '230500', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (741, '2020-03-29 14:20:36.000', 'system.region.area.230502', '尖山区', NULL, '230502', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (742, '2020-03-29 14:20:36.000', 'system.region.area.230503', '岭东区', NULL, '230503', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (743, '2020-03-29 14:20:36.000', 'system.region.area.230505', '四方台区', NULL, '230505', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (744, '2020-03-29 14:20:36.000', 'system.region.area.230506', '宝山区', NULL, '230506', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (745, '2020-03-29 14:20:36.000', 'system.region.area.230521', '集贤县', NULL, '230521', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (746, '2020-03-29 14:20:36.000', 'system.region.area.230522', '友谊县', NULL, '230522', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (747, '2020-03-29 14:20:36.000', 'system.region.area.230523', '宝清县', NULL, '230523', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (748, '2020-03-29 14:20:36.000', 'system.region.area.230524', '饶河县', NULL, '230524', 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (749, '2020-03-29 14:20:36.000', 'system.region.city.230600', '大庆市', 'area', '230600', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (750, '2020-03-29 14:20:36.000', 'system.region.area.230602', '萨尔图区', NULL, '230602', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (751, '2020-03-29 14:20:36.000', 'system.region.area.230603', '龙凤区', NULL, '230603', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (752, '2020-03-29 14:20:36.000', 'system.region.area.230604', '让胡路区', NULL, '230604', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (753, '2020-03-29 14:20:36.000', 'system.region.area.230605', '红岗区', NULL, '230605', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (754, '2020-03-29 14:20:36.000', 'system.region.area.230606', '大同区', NULL, '230606', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (755, '2020-03-29 14:20:36.000', 'system.region.area.230621', '肇州县', NULL, '230621', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (756, '2020-03-29 14:20:36.000', 'system.region.area.230622', '肇源县', NULL, '230622', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (757, '2020-03-29 14:20:36.000', 'system.region.area.230623', '林甸县', NULL, '230623', 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (758, '2020-03-29 14:20:36.000', 'system.region.area.230624', '杜尔伯特蒙古族自治县', NULL, '230624', 1, 16, 749, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (759, '2020-03-29 14:20:36.000', 'system.region.city.230700', '伊春市', 'area', '230700', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (760, '2020-03-29 14:20:36.000', 'system.region.area.230717', '伊美区', NULL, '230717', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (761, '2020-03-29 14:20:36.000', 'system.region.area.230718', '乌翠区', NULL, '230718', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (762, '2020-03-29 14:20:36.000', 'system.region.area.230719', '友好区', NULL, '230719', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (763, '2020-03-29 14:20:36.000', 'system.region.area.230722', '嘉荫县', NULL, '230722', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (764, '2020-03-29 14:20:36.000', 'system.region.area.230723', '汤旺县', NULL, '230723', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (765, '2020-03-29 14:20:36.000', 'system.region.area.230724', '丰林县', NULL, '230724', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (766, '2020-03-29 14:20:36.000', 'system.region.area.230725', '大箐山县', NULL, '230725', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (767, '2020-03-29 14:20:36.000', 'system.region.area.230726', '南岔县', NULL, '230726', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (768, '2020-03-29 14:20:36.000', 'system.region.area.230751', '金林区', NULL, '230751', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (769, '2020-03-29 14:20:36.000', 'system.region.area.230781', '铁力市', NULL, '230781', 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (770, '2020-03-29 14:20:36.000', 'system.region.city.230800', '佳木斯市', 'area', '230800', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (771, '2020-03-29 14:20:36.000', 'system.region.area.230803', '向阳区', NULL, '230803', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (772, '2020-03-29 14:20:36.000', 'system.region.area.230804', '前进区', NULL, '230804', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (773, '2020-03-29 14:20:36.000', 'system.region.area.230805', '东风区', NULL, '230805', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (774, '2020-03-29 14:20:36.000', 'system.region.area.230811', '郊区', NULL, '230811', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (775, '2020-03-29 14:20:36.000', 'system.region.area.230822', '桦南县', NULL, '230822', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (776, '2020-03-29 14:20:36.000', 'system.region.area.230826', '桦川县', NULL, '230826', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (777, '2020-03-29 14:20:36.000', 'system.region.area.230828', '汤原县', NULL, '230828', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (778, '2020-03-29 14:20:36.000', 'system.region.area.230881', '同江市', NULL, '230881', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (779, '2020-03-29 14:20:36.000', 'system.region.area.230882', '富锦市', NULL, '230882', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (780, '2020-03-29 14:20:36.000', 'system.region.area.230883', '抚远市', NULL, '230883', 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (781, '2020-03-29 14:20:36.000', 'system.region.city.230900', '七台河市', 'area', '230900', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (782, '2020-03-29 14:20:36.000', 'system.region.area.230902', '新兴区', NULL, '230902', 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (783, '2020-03-29 14:20:36.000', 'system.region.area.230903', '桃山区', NULL, '230903', 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (784, '2020-03-29 14:20:36.000', 'system.region.area.230904', '茄子河区', NULL, '230904', 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (785, '2020-03-29 14:20:36.000', 'system.region.area.230921', '勃利县', NULL, '230921', 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (786, '2020-03-29 14:20:36.000', 'system.region.city.231000', '牡丹江市', 'area', '231000', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (787, '2020-03-29 14:20:36.000', 'system.region.area.231002', '东安区', NULL, '231002', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (788, '2020-03-29 14:20:36.000', 'system.region.area.231003', '阳明区', NULL, '231003', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (789, '2020-03-29 14:20:36.000', 'system.region.area.231004', '爱民区', NULL, '231004', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (790, '2020-03-29 14:20:36.000', 'system.region.area.231005', '西安区', NULL, '231005', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (791, '2020-03-29 14:20:36.000', 'system.region.area.231025', '林口县', NULL, '231025', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (792, '2020-03-29 14:20:36.000', 'system.region.area.231081', '绥芬河市', NULL, '231081', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (793, '2020-03-29 14:20:36.000', 'system.region.area.231083', '海林市', NULL, '231083', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (794, '2020-03-29 14:20:36.000', 'system.region.area.231084', '宁安市', NULL, '231084', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (795, '2020-03-29 14:20:36.000', 'system.region.area.231085', '穆棱市', NULL, '231085', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (796, '2020-03-29 14:20:36.000', 'system.region.area.231086', '东宁市', NULL, '231086', 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (797, '2020-03-29 14:20:36.000', 'system.region.city.231100', '黑河市', 'area', '231100', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (798, '2020-03-29 14:20:36.000', 'system.region.area.231102', '爱辉区', NULL, '231102', 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (799, '2020-03-29 14:20:36.000', 'system.region.area.231123', '逊克县', NULL, '231123', 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (800, '2020-03-29 14:20:36.000', 'system.region.area.231124', '孙吴县', NULL, '231124', 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (801, '2020-03-29 14:20:36.000', 'system.region.area.231181', '北安市', NULL, '231181', 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (802, '2020-03-29 14:20:36.000', 'system.region.area.231182', '五大连池市', NULL, '231182', 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (803, '2020-03-29 14:20:36.000', 'system.region.area.231183', '嫩江市', NULL, '231183', 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (804, '2020-03-29 14:20:36.000', 'system.region.city.231200', '绥化市', 'area', '231200', 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (805, '2020-03-29 14:20:36.000', 'system.region.area.231202', '北林区', NULL, '231202', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (806, '2020-03-29 14:20:36.000', 'system.region.area.231221', '望奎县', NULL, '231221', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (807, '2020-03-29 14:20:36.000', 'system.region.area.231222', '兰西县', NULL, '231222', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (808, '2020-03-29 14:20:36.000', 'system.region.area.231223', '青冈县', NULL, '231223', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (809, '2020-03-29 14:20:36.000', 'system.region.area.231224', '庆安县', NULL, '231224', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (810, '2020-03-29 14:20:36.000', 'system.region.area.231225', '明水县', NULL, '231225', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (811, '2020-03-29 14:20:36.000', 'system.region.area.231226', '绥棱县', NULL, '231226', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (812, '2020-03-29 14:20:36.000', 'system.region.area.231281', '安达市', NULL, '231281', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (813, '2020-03-29 14:20:36.000', 'system.region.area.231282', '肇东市', NULL, '231282', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (814, '2020-03-29 14:20:36.000', 'system.region.area.231283', '海伦市', NULL, '231283', 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (815, '2020-03-29 14:20:36.000', 'system.region.city.232700', '大兴安岭地区', 'area', '232700', 1, 15, 684, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (816, '2020-03-29 14:20:36.000', 'system.region.area.232701', '漠河市', NULL, '232701', 1, 16, 815, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (817, '2020-03-29 14:20:36.000', 'system.region.area.232721', '呼玛县', NULL, '232721', 1, 16, 815, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (818, '2020-03-29 14:20:36.000', 'system.region.area.232722', '塔河县', NULL, '232722', 1, 16, 815, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (819, '2020-03-29 14:20:36.000', 'system.region.province.310000', '上海市', 'area', '310000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (820, '2020-03-29 14:20:36.000', 'system.region.area.310101', '黄浦区', NULL, '310101', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (821, '2020-03-29 14:20:36.000', 'system.region.area.310104', '徐汇区', NULL, '310104', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (822, '2020-03-29 14:20:36.000', 'system.region.area.310105', '长宁区', NULL, '310105', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (823, '2020-03-29 14:20:36.000', 'system.region.area.310106', '静安区', NULL, '310106', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (824, '2020-03-29 14:20:36.000', 'system.region.area.310107', '普陀区', NULL, '310107', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (825, '2020-03-29 14:20:36.000', 'system.region.area.310109', '虹口区', NULL, '310109', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (826, '2020-03-29 14:20:36.000', 'system.region.area.310110', '杨浦区', NULL, '310110', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (827, '2020-03-29 14:20:36.000', 'system.region.area.310112', '闵行区', NULL, '310112', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (828, '2020-03-29 14:20:36.000', 'system.region.area.310113', '宝山区', NULL, '310113', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (829, '2020-03-29 14:20:36.000', 'system.region.area.310114', '嘉定区', NULL, '310114', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (830, '2020-03-29 14:20:36.000', 'system.region.area.310115', '浦东新区', NULL, '310115', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (831, '2020-03-29 14:20:36.000', 'system.region.area.310116', '金山区', NULL, '310116', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (832, '2020-03-29 14:20:36.000', 'system.region.area.310117', '松江区', NULL, '310117', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (833, '2020-03-29 14:20:36.000', 'system.region.area.310118', '青浦区', NULL, '310118', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (834, '2020-03-29 14:20:36.000', 'system.region.area.310120', '奉贤区', NULL, '310120', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (835, '2020-03-29 14:20:36.000', 'system.region.area.310151', '崇明区', NULL, '310151', 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (836, '2020-03-29 14:20:36.000', 'system.region.province.320000', '江苏省', 'city', '320000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (837, '2020-03-29 14:20:36.000', 'system.region.city.320100', '南京市', 'area', '320100', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (838, '2020-03-29 14:20:36.000', 'system.region.area.320102', '玄武区', NULL, '320102', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (839, '2020-03-29 14:20:36.000', 'system.region.area.320104', '秦淮区', NULL, '320104', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (840, '2020-03-29 14:20:36.000', 'system.region.area.320105', '建邺区', NULL, '320105', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (841, '2020-03-29 14:20:36.000', 'system.region.area.320106', '鼓楼区', NULL, '320106', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (842, '2020-03-29 14:20:36.000', 'system.region.area.320111', '浦口区', NULL, '320111', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (843, '2020-03-29 14:20:36.000', 'system.region.area.320113', '栖霞区', NULL, '320113', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (844, '2020-03-29 14:20:36.000', 'system.region.area.320114', '雨花台区', NULL, '320114', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (845, '2020-03-29 14:20:36.000', 'system.region.area.320115', '江宁区', NULL, '320115', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (846, '2020-03-29 14:20:36.000', 'system.region.area.320116', '六合区', NULL, '320116', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (847, '2020-03-29 14:20:36.000', 'system.region.area.320117', '溧水区', NULL, '320117', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (848, '2020-03-29 14:20:36.000', 'system.region.area.320118', '高淳区', NULL, '320118', 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (849, '2020-03-29 14:20:36.000', 'system.region.city.320200', '无锡市', 'area', '320200', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (850, '2020-03-29 14:20:36.000', 'system.region.area.320205', '锡山区', NULL, '320205', 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (851, '2020-03-29 14:20:36.000', 'system.region.area.320206', '惠山区', NULL, '320206', 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (852, '2020-03-29 14:20:36.000', 'system.region.area.320211', '滨湖区', NULL, '320211', 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (853, '2020-03-29 14:20:36.000', 'system.region.area.320213', '梁溪区', NULL, '320213', 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (854, '2020-03-29 14:20:36.000', 'system.region.area.320214', '新吴区', NULL, '320214', 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (855, '2020-03-29 14:20:36.000', 'system.region.area.320281', '江阴市', NULL, '320281', 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (856, '2020-03-29 14:20:36.000', 'system.region.area.320282', '宜兴市', NULL, '320282', 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (857, '2020-03-29 14:20:36.000', 'system.region.city.320300', '徐州市', 'area', '320300', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (858, '2020-03-29 14:20:36.000', 'system.region.area.320302', '鼓楼区', NULL, '320302', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (859, '2020-03-29 14:20:36.000', 'system.region.area.320303', '云龙区', NULL, '320303', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (860, '2020-03-29 14:20:36.000', 'system.region.area.320305', '贾汪区', NULL, '320305', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (861, '2020-03-29 14:20:36.000', 'system.region.area.320311', '泉山区', NULL, '320311', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (862, '2020-03-29 14:20:36.000', 'system.region.area.320312', '铜山区', NULL, '320312', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (863, '2020-03-29 14:20:36.000', 'system.region.area.320321', '丰县', NULL, '320321', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (864, '2020-03-29 14:20:36.000', 'system.region.area.320322', '沛县', NULL, '320322', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (865, '2020-03-29 14:20:36.000', 'system.region.area.320324', '睢宁县', NULL, '320324', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (866, '2020-03-29 14:20:36.000', 'system.region.area.320381', '新沂市', NULL, '320381', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (867, '2020-03-29 14:20:36.000', 'system.region.area.320382', '邳州市', NULL, '320382', 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (868, '2020-03-29 14:20:36.000', 'system.region.city.320400', '常州市', 'area', '320400', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (869, '2020-03-29 14:20:36.000', 'system.region.area.320402', '天宁区', NULL, '320402', 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (870, '2020-03-29 14:20:36.000', 'system.region.area.320404', '钟楼区', NULL, '320404', 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (871, '2020-03-29 14:20:36.000', 'system.region.area.320411', '新北区', NULL, '320411', 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (872, '2020-03-29 14:20:36.000', 'system.region.area.320412', '武进区', NULL, '320412', 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (873, '2020-03-29 14:20:36.000', 'system.region.area.320413', '金坛区', NULL, '320413', 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (874, '2020-03-29 14:20:36.000', 'system.region.area.320481', '溧阳市', NULL, '320481', 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (875, '2020-03-29 14:20:36.000', 'system.region.city.320500', '苏州市', 'area', '320500', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (876, '2020-03-29 14:20:36.000', 'system.region.area.320505', '虎丘区', NULL, '320505', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (877, '2020-03-29 14:20:36.000', 'system.region.area.320506', '吴中区', NULL, '320506', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (878, '2020-03-29 14:20:36.000', 'system.region.area.320507', '相城区', NULL, '320507', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (879, '2020-03-29 14:20:36.000', 'system.region.area.320508', '姑苏区', NULL, '320508', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (880, '2020-03-29 14:20:36.000', 'system.region.area.320509', '吴江区', NULL, '320509', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (881, '2020-03-29 14:20:36.000', 'system.region.area.320581', '常熟市', NULL, '320581', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (882, '2020-03-29 14:20:36.000', 'system.region.area.320582', '张家港市', NULL, '320582', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (883, '2020-03-29 14:20:36.000', 'system.region.area.320583', '昆山市', NULL, '320583', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (884, '2020-03-29 14:20:36.000', 'system.region.area.320585', '太仓市', NULL, '320585', 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (885, '2020-03-29 14:20:36.000', 'system.region.city.320600', '南通市', 'area', '320600', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (886, '2020-03-29 14:20:36.000', 'system.region.area.320602', '崇川区', NULL, '320602', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (887, '2020-03-29 14:20:36.000', 'system.region.area.320611', '港闸区', NULL, '320611', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (888, '2020-03-29 14:20:36.000', 'system.region.area.320612', '通州区', NULL, '320612', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (889, '2020-03-29 14:20:36.000', 'system.region.area.320623', '如东县', NULL, '320623', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (890, '2020-03-29 14:20:36.000', 'system.region.area.320681', '启东市', NULL, '320681', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (891, '2020-03-29 14:20:36.000', 'system.region.area.320682', '如皋市', NULL, '320682', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (892, '2020-03-29 14:20:36.000', 'system.region.area.320684', '海门市', NULL, '320684', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (893, '2020-03-29 14:20:36.000', 'system.region.area.320685', '海安市', NULL, '320685', 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (894, '2020-03-29 14:20:36.000', 'system.region.city.320700', '连云港市', 'area', '320700', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (895, '2020-03-29 14:20:36.000', 'system.region.area.320703', '连云区', NULL, '320703', 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (896, '2020-03-29 14:20:36.000', 'system.region.area.320706', '海州区', NULL, '320706', 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (897, '2020-03-29 14:20:36.000', 'system.region.area.320707', '赣榆区', NULL, '320707', 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (898, '2020-03-29 14:20:36.000', 'system.region.area.320722', '东海县', NULL, '320722', 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (899, '2020-03-29 14:20:36.000', 'system.region.area.320723', '灌云县', NULL, '320723', 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (900, '2020-03-29 14:20:36.000', 'system.region.area.320724', '灌南县', NULL, '320724', 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (901, '2020-03-29 14:20:36.000', 'system.region.city.320800', '淮安市', 'area', '320800', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (902, '2020-03-29 14:20:36.000', 'system.region.area.320803', '淮安区', NULL, '320803', 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (903, '2020-03-29 14:20:36.000', 'system.region.area.320804', '淮阴区', NULL, '320804', 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (904, '2020-03-29 14:20:36.000', 'system.region.area.320812', '清江浦区', NULL, '320812', 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (905, '2020-03-29 14:20:36.000', 'system.region.area.320813', '洪泽区', NULL, '320813', 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (906, '2020-03-29 14:20:36.000', 'system.region.area.320826', '涟水县', NULL, '320826', 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (907, '2020-03-29 14:20:36.000', 'system.region.area.320830', '盱眙县', NULL, '320830', 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (908, '2020-03-29 14:20:36.000', 'system.region.area.320831', '金湖县', NULL, '320831', 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (909, '2020-03-29 14:20:36.000', 'system.region.city.320900', '盐城市', 'area', '320900', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (910, '2020-03-29 14:20:36.000', 'system.region.area.320902', '亭湖区', NULL, '320902', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (911, '2020-03-29 14:20:36.000', 'system.region.area.320903', '盐都区', NULL, '320903', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (912, '2020-03-29 14:20:36.000', 'system.region.area.320904', '大丰区', NULL, '320904', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (913, '2020-03-29 14:20:36.000', 'system.region.area.320921', '响水县', NULL, '320921', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (914, '2020-03-29 14:20:36.000', 'system.region.area.320922', '滨海县', NULL, '320922', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (915, '2020-03-29 14:20:36.000', 'system.region.area.320923', '阜宁县', NULL, '320923', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (916, '2020-03-29 14:20:36.000', 'system.region.area.320924', '射阳县', NULL, '320924', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (917, '2020-03-29 14:20:36.000', 'system.region.area.320925', '建湖县', NULL, '320925', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (918, '2020-03-29 14:20:36.000', 'system.region.area.320981', '东台市', NULL, '320981', 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (919, '2020-03-29 14:20:36.000', 'system.region.city.321000', '扬州市', 'area', '321000', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (920, '2020-03-29 14:20:36.000', 'system.region.area.321002', '广陵区', NULL, '321002', 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (921, '2020-03-29 14:20:36.000', 'system.region.area.321003', '邗江区', NULL, '321003', 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (922, '2020-03-29 14:20:36.000', 'system.region.area.321012', '江都区', NULL, '321012', 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (923, '2020-03-29 14:20:36.000', 'system.region.area.321023', '宝应县', NULL, '321023', 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (924, '2020-03-29 14:20:36.000', 'system.region.area.321081', '仪征市', NULL, '321081', 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (925, '2020-03-29 14:20:36.000', 'system.region.area.321084', '高邮市', NULL, '321084', 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (926, '2020-03-29 14:20:36.000', 'system.region.city.321100', '镇江市', 'area', '321100', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (927, '2020-03-29 14:20:36.000', 'system.region.area.321102', '京口区', NULL, '321102', 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (928, '2020-03-29 14:20:36.000', 'system.region.area.321111', '润州区', NULL, '321111', 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (929, '2020-03-29 14:20:36.000', 'system.region.area.321112', '丹徒区', NULL, '321112', 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (930, '2020-03-29 14:20:36.000', 'system.region.area.321181', '丹阳市', NULL, '321181', 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (931, '2020-03-29 14:20:36.000', 'system.region.area.321182', '扬中市', NULL, '321182', 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (932, '2020-03-29 14:20:36.000', 'system.region.area.321183', '句容市', NULL, '321183', 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (933, '2020-03-29 14:20:36.000', 'system.region.city.321200', '泰州市', 'area', '321200', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (934, '2020-03-29 14:20:36.000', 'system.region.area.321202', '海陵区', NULL, '321202', 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (935, '2020-03-29 14:20:36.000', 'system.region.area.321203', '高港区', NULL, '321203', 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (936, '2020-03-29 14:20:36.000', 'system.region.area.321204', '姜堰区', NULL, '321204', 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (937, '2020-03-29 14:20:36.000', 'system.region.area.321281', '兴化市', NULL, '321281', 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (938, '2020-03-29 14:20:36.000', 'system.region.area.321282', '靖江市', NULL, '321282', 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (939, '2020-03-29 14:20:36.000', 'system.region.area.321283', '泰兴市', NULL, '321283', 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (940, '2020-03-29 14:20:36.000', 'system.region.city.321300', '宿迁市', 'area', '321300', 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (941, '2020-03-29 14:20:36.000', 'system.region.area.321302', '宿城区', NULL, '321302', 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (942, '2020-03-29 14:20:36.000', 'system.region.area.321311', '宿豫区', NULL, '321311', 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (943, '2020-03-29 14:20:36.000', 'system.region.area.321322', '沭阳县', NULL, '321322', 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (944, '2020-03-29 14:20:36.000', 'system.region.area.321323', '泗阳县', NULL, '321323', 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (945, '2020-03-29 14:20:36.000', 'system.region.area.321324', '泗洪县', NULL, '321324', 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (946, '2020-03-29 14:20:36.000', 'system.region.province.330000', '浙江省', 'city', '330000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (947, '2020-03-29 14:20:36.000', 'system.region.city.330100', '杭州市', 'area', '330100', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (948, '2020-03-29 14:20:36.000', 'system.region.area.330102', '上城区', NULL, '330102', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (949, '2020-03-29 14:20:36.000', 'system.region.area.330103', '下城区', NULL, '330103', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (950, '2020-03-29 14:20:36.000', 'system.region.area.330104', '江干区', NULL, '330104', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (951, '2020-03-29 14:20:36.000', 'system.region.area.330105', '拱墅区', NULL, '330105', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (952, '2020-03-29 14:20:36.000', 'system.region.area.330106', '西湖区', NULL, '330106', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (953, '2020-03-29 14:20:36.000', 'system.region.area.330108', '滨江区', NULL, '330108', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (954, '2020-03-29 14:20:36.000', 'system.region.area.330109', '萧山区', NULL, '330109', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (955, '2020-03-29 14:20:36.000', 'system.region.area.330110', '余杭区', NULL, '330110', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (956, '2020-03-29 14:20:36.000', 'system.region.area.330111', '富阳区', NULL, '330111', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (957, '2020-03-29 14:20:36.000', 'system.region.area.330112', '临安区', NULL, '330112', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (958, '2020-03-29 14:20:36.000', 'system.region.area.330122', '桐庐县', NULL, '330122', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (959, '2020-03-29 14:20:36.000', 'system.region.area.330127', '淳安县', NULL, '330127', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (960, '2020-03-29 14:20:36.000', 'system.region.area.330182', '建德市', NULL, '330182', 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (961, '2020-03-29 14:20:36.000', 'system.region.city.330200', '宁波市', 'area', '330200', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (962, '2020-03-29 14:20:36.000', 'system.region.area.330203', '海曙区', NULL, '330203', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (963, '2020-03-29 14:20:36.000', 'system.region.area.330205', '江北区', NULL, '330205', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (964, '2020-03-29 14:20:36.000', 'system.region.area.330206', '北仑区', NULL, '330206', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (965, '2020-03-29 14:20:36.000', 'system.region.area.330211', '镇海区', NULL, '330211', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (966, '2020-03-29 14:20:36.000', 'system.region.area.330212', '鄞州区', NULL, '330212', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (967, '2020-03-29 14:20:36.000', 'system.region.area.330213', '奉化区', NULL, '330213', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (968, '2020-03-29 14:20:36.000', 'system.region.area.330225', '象山县', NULL, '330225', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (969, '2020-03-29 14:20:36.000', 'system.region.area.330226', '宁海县', NULL, '330226', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (970, '2020-03-29 14:20:36.000', 'system.region.area.330281', '余姚市', NULL, '330281', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (971, '2020-03-29 14:20:36.000', 'system.region.area.330282', '慈溪市', NULL, '330282', 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (972, '2020-03-29 14:20:36.000', 'system.region.city.330300', '温州市', 'area', '330300', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (973, '2020-03-29 14:20:36.000', 'system.region.area.330302', '鹿城区', NULL, '330302', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (974, '2020-03-29 14:20:36.000', 'system.region.area.330303', '龙湾区', NULL, '330303', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (975, '2020-03-29 14:20:36.000', 'system.region.area.330304', '瓯海区', NULL, '330304', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (976, '2020-03-29 14:20:36.000', 'system.region.area.330305', '洞头区', NULL, '330305', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (977, '2020-03-29 14:20:36.000', 'system.region.area.330324', '永嘉县', NULL, '330324', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (978, '2020-03-29 14:20:36.000', 'system.region.area.330326', '平阳县', NULL, '330326', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (979, '2020-03-29 14:20:36.000', 'system.region.area.330327', '苍南县', NULL, '330327', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (980, '2020-03-29 14:20:36.000', 'system.region.area.330328', '文成县', NULL, '330328', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (981, '2020-03-29 14:20:36.000', 'system.region.area.330329', '泰顺县', NULL, '330329', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (982, '2020-03-29 14:20:36.000', 'system.region.area.330381', '瑞安市', NULL, '330381', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (983, '2020-03-29 14:20:36.000', 'system.region.area.330382', '乐清市', NULL, '330382', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (984, '2020-03-29 14:20:36.000', 'system.region.area.330383', '龙港市', NULL, '330383', 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (985, '2020-03-29 14:20:36.000', 'system.region.city.330400', '嘉兴市', 'area', '330400', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (986, '2020-03-29 14:20:36.000', 'system.region.area.330402', '南湖区', NULL, '330402', 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (987, '2020-03-29 14:20:36.000', 'system.region.area.330411', '秀洲区', NULL, '330411', 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (988, '2020-03-29 14:20:36.000', 'system.region.area.330421', '嘉善县', NULL, '330421', 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (989, '2020-03-29 14:20:36.000', 'system.region.area.330424', '海盐县', NULL, '330424', 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (990, '2020-03-29 14:20:36.000', 'system.region.area.330481', '海宁市', NULL, '330481', 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (991, '2020-03-29 14:20:36.000', 'system.region.area.330482', '平湖市', NULL, '330482', 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (992, '2020-03-29 14:20:36.000', 'system.region.area.330483', '桐乡市', NULL, '330483', 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (993, '2020-03-29 14:20:36.000', 'system.region.city.330500', '湖州市', 'area', '330500', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (994, '2020-03-29 14:20:36.000', 'system.region.area.330502', '吴兴区', NULL, '330502', 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (995, '2020-03-29 14:20:36.000', 'system.region.area.330503', '南浔区', NULL, '330503', 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (996, '2020-03-29 14:20:36.000', 'system.region.area.330521', '德清县', NULL, '330521', 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (997, '2020-03-29 14:20:36.000', 'system.region.area.330522', '长兴县', NULL, '330522', 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (998, '2020-03-29 14:20:36.000', 'system.region.area.330523', '安吉县', NULL, '330523', 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (999, '2020-03-29 14:20:36.000', 'system.region.city.330600', '绍兴市', 'area', '330600', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1000, '2020-03-29 14:20:36.000', 'system.region.area.330602', '越城区', NULL, '330602', 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1001, '2020-03-29 14:20:36.000', 'system.region.area.330603', '柯桥区', NULL, '330603', 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1002, '2020-03-29 14:20:36.000', 'system.region.area.330604', '上虞区', NULL, '330604', 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1003, '2020-03-29 14:20:36.000', 'system.region.area.330624', '新昌县', NULL, '330624', 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1004, '2020-03-29 14:20:36.000', 'system.region.area.330681', '诸暨市', NULL, '330681', 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1005, '2020-03-29 14:20:36.000', 'system.region.area.330683', '嵊州市', NULL, '330683', 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1006, '2020-03-29 14:20:36.000', 'system.region.city.330700', '金华市', 'area', '330700', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1007, '2020-03-29 14:20:36.000', 'system.region.area.330702', '婺城区', NULL, '330702', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1008, '2020-03-29 14:20:36.000', 'system.region.area.330703', '金东区', NULL, '330703', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1009, '2020-03-29 14:20:36.000', 'system.region.area.330723', '武义县', NULL, '330723', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1010, '2020-03-29 14:20:36.000', 'system.region.area.330726', '浦江县', NULL, '330726', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1011, '2020-03-29 14:20:36.000', 'system.region.area.330727', '磐安县', NULL, '330727', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1012, '2020-03-29 14:20:36.000', 'system.region.area.330781', '兰溪市', NULL, '330781', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1013, '2020-03-29 14:20:36.000', 'system.region.area.330782', '义乌市', NULL, '330782', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1014, '2020-03-29 14:20:36.000', 'system.region.area.330783', '东阳市', NULL, '330783', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1015, '2020-03-29 14:20:36.000', 'system.region.area.330784', '永康市', NULL, '330784', 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1016, '2020-03-29 14:20:36.000', 'system.region.city.330800', '衢州市', 'area', '330800', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1017, '2020-03-29 14:20:36.000', 'system.region.area.330802', '柯城区', NULL, '330802', 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1018, '2020-03-29 14:20:36.000', 'system.region.area.330803', '衢江区', NULL, '330803', 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1019, '2020-03-29 14:20:36.000', 'system.region.area.330822', '常山县', NULL, '330822', 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1020, '2020-03-29 14:20:36.000', 'system.region.area.330824', '开化县', NULL, '330824', 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1021, '2020-03-29 14:20:36.000', 'system.region.area.330825', '龙游县', NULL, '330825', 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1022, '2020-03-29 14:20:36.000', 'system.region.area.330881', '江山市', NULL, '330881', 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1023, '2020-03-29 14:20:36.000', 'system.region.city.330900', '舟山市', 'area', '330900', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1024, '2020-03-29 14:20:36.000', 'system.region.area.330902', '定海区', NULL, '330902', 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1025, '2020-03-29 14:20:36.000', 'system.region.area.330903', '普陀区', NULL, '330903', 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1026, '2020-03-29 14:20:36.000', 'system.region.area.330921', '岱山县', NULL, '330921', 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1027, '2020-03-29 14:20:36.000', 'system.region.area.330922', '嵊泗县', NULL, '330922', 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1028, '2020-03-29 14:20:36.000', 'system.region.city.331000', '台州市', 'area', '331000', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1029, '2020-03-29 14:20:36.000', 'system.region.area.331002', '椒江区', NULL, '331002', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1030, '2020-03-29 14:20:36.000', 'system.region.area.331003', '黄岩区', NULL, '331003', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1031, '2020-03-29 14:20:36.000', 'system.region.area.331004', '路桥区', NULL, '331004', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1032, '2020-03-29 14:20:36.000', 'system.region.area.331022', '三门县', NULL, '331022', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1033, '2020-03-29 14:20:36.000', 'system.region.area.331023', '天台县', NULL, '331023', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1034, '2020-03-29 14:20:36.000', 'system.region.area.331024', '仙居县', NULL, '331024', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1035, '2020-03-29 14:20:36.000', 'system.region.area.331081', '温岭市', NULL, '331081', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1036, '2020-03-29 14:20:36.000', 'system.region.area.331082', '临海市', NULL, '331082', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1037, '2020-03-29 14:20:36.000', 'system.region.area.331083', '玉环市', NULL, '331083', 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1038, '2020-03-29 14:20:36.000', 'system.region.city.331100', '丽水市', 'area', '331100', 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1039, '2020-03-29 14:20:36.000', 'system.region.area.331102', '莲都区', NULL, '331102', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1040, '2020-03-29 14:20:36.000', 'system.region.area.331121', '青田县', NULL, '331121', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1041, '2020-03-29 14:20:36.000', 'system.region.area.331122', '缙云县', NULL, '331122', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1042, '2020-03-29 14:20:36.000', 'system.region.area.331123', '遂昌县', NULL, '331123', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1043, '2020-03-29 14:20:36.000', 'system.region.area.331124', '松阳县', NULL, '331124', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1044, '2020-03-29 14:20:36.000', 'system.region.area.331125', '云和县', NULL, '331125', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1045, '2020-03-29 14:20:36.000', 'system.region.area.331126', '庆元县', NULL, '331126', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1046, '2020-03-29 14:20:36.000', 'system.region.area.331127', '景宁畲族自治县', NULL, '331127', 1, 16, 1038, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1047, '2020-03-29 14:20:36.000', 'system.region.area.331181', '龙泉市', NULL, '331181', 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1048, '2020-03-29 14:20:36.000', 'system.region.province.340000', '安徽省', 'city', '340000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1049, '2020-03-29 14:20:36.000', 'system.region.city.340100', '合肥市', 'area', '340100', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1050, '2020-03-29 14:20:36.000', 'system.region.area.340102', '瑶海区', NULL, '340102', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1051, '2020-03-29 14:20:36.000', 'system.region.area.340103', '庐阳区', NULL, '340103', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1052, '2020-03-29 14:20:36.000', 'system.region.area.340104', '蜀山区', NULL, '340104', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1053, '2020-03-29 14:20:36.000', 'system.region.area.340111', '包河区', NULL, '340111', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1054, '2020-03-29 14:20:36.000', 'system.region.area.340121', '长丰县', NULL, '340121', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1055, '2020-03-29 14:20:36.000', 'system.region.area.340122', '肥东县', NULL, '340122', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1056, '2020-03-29 14:20:36.000', 'system.region.area.340123', '肥西县', NULL, '340123', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1057, '2020-03-29 14:20:36.000', 'system.region.area.340124', '庐江县', NULL, '340124', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1058, '2020-03-29 14:20:36.000', 'system.region.area.340181', '巢湖市', NULL, '340181', 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1059, '2020-03-29 14:20:36.000', 'system.region.city.340200', '芜湖市', 'area', '340200', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1060, '2020-03-29 14:20:36.000', 'system.region.area.340202', '镜湖区', NULL, '340202', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1061, '2020-03-29 14:20:36.000', 'system.region.area.340203', '弋江区', NULL, '340203', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1062, '2020-03-29 14:20:36.000', 'system.region.area.340207', '鸠江区', NULL, '340207', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1063, '2020-03-29 14:20:36.000', 'system.region.area.340208', '三山区', NULL, '340208', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1064, '2020-03-29 14:20:36.000', 'system.region.area.340221', '芜湖县', NULL, '340221', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1065, '2020-03-29 14:20:36.000', 'system.region.area.340222', '繁昌县', NULL, '340222', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1066, '2020-03-29 14:20:36.000', 'system.region.area.340223', '南陵县', NULL, '340223', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1067, '2020-03-29 14:20:36.000', 'system.region.area.340281', '无为市', NULL, '340281', 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1068, '2020-03-29 14:20:36.000', 'system.region.city.340300', '蚌埠市', 'area', '340300', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1069, '2020-03-29 14:20:36.000', 'system.region.area.340302', '龙子湖区', NULL, '340302', 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1070, '2020-03-29 14:20:36.000', 'system.region.area.340303', '蚌山区', NULL, '340303', 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1071, '2020-03-29 14:20:36.000', 'system.region.area.340304', '禹会区', NULL, '340304', 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1072, '2020-03-29 14:20:36.000', 'system.region.area.340311', '淮上区', NULL, '340311', 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1073, '2020-03-29 14:20:36.000', 'system.region.area.340321', '怀远县', NULL, '340321', 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1074, '2020-03-29 14:20:36.000', 'system.region.area.340322', '五河县', NULL, '340322', 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1075, '2020-03-29 14:20:36.000', 'system.region.area.340323', '固镇县', NULL, '340323', 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1076, '2020-03-29 14:20:36.000', 'system.region.city.340400', '淮南市', 'area', '340400', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1077, '2020-03-29 14:20:36.000', 'system.region.area.340402', '大通区', NULL, '340402', 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1078, '2020-03-29 14:20:36.000', 'system.region.area.340403', '田家庵区', NULL, '340403', 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1079, '2020-03-29 14:20:36.000', 'system.region.area.340404', '谢家集区', NULL, '340404', 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1080, '2020-03-29 14:20:36.000', 'system.region.area.340405', '八公山区', NULL, '340405', 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1081, '2020-03-29 14:20:36.000', 'system.region.area.340406', '潘集区', NULL, '340406', 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1082, '2020-03-29 14:20:36.000', 'system.region.area.340421', '凤台县', NULL, '340421', 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1083, '2020-03-29 14:20:36.000', 'system.region.area.340422', '寿县', NULL, '340422', 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1084, '2020-03-29 14:20:36.000', 'system.region.city.340500', '马鞍山市', 'area', '340500', 1, 15, 1048, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1085, '2020-03-29 14:20:36.000', 'system.region.area.340503', '花山区', NULL, '340503', 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1086, '2020-03-29 14:20:36.000', 'system.region.area.340504', '雨山区', NULL, '340504', 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1087, '2020-03-29 14:20:36.000', 'system.region.area.340506', '博望区', NULL, '340506', 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1088, '2020-03-29 14:20:36.000', 'system.region.area.340521', '当涂县', NULL, '340521', 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1089, '2020-03-29 14:20:36.000', 'system.region.area.340522', '含山县', NULL, '340522', 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1090, '2020-03-29 14:20:36.000', 'system.region.area.340523', '和县', NULL, '340523', 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1091, '2020-03-29 14:20:36.000', 'system.region.city.340600', '淮北市', 'area', '340600', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1092, '2020-03-29 14:20:36.000', 'system.region.area.340602', '杜集区', NULL, '340602', 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1093, '2020-03-29 14:20:36.000', 'system.region.area.340603', '相山区', NULL, '340603', 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1094, '2020-03-29 14:20:36.000', 'system.region.area.340604', '烈山区', NULL, '340604', 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1095, '2020-03-29 14:20:36.000', 'system.region.area.340621', '濉溪县', NULL, '340621', 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1096, '2020-03-29 14:20:36.000', 'system.region.city.340700', '铜陵市', 'area', '340700', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1097, '2020-03-29 14:20:36.000', 'system.region.area.340705', '铜官区', NULL, '340705', 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1098, '2020-03-29 14:20:36.000', 'system.region.area.340706', '义安区', NULL, '340706', 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1099, '2020-03-29 14:20:36.000', 'system.region.area.340711', '郊区', NULL, '340711', 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1100, '2020-03-29 14:20:36.000', 'system.region.area.340722', '枞阳县', NULL, '340722', 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1101, '2020-03-29 14:20:36.000', 'system.region.city.340800', '安庆市', 'area', '340800', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1102, '2020-03-29 14:20:36.000', 'system.region.area.340802', '迎江区', NULL, '340802', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1103, '2020-03-29 14:20:36.000', 'system.region.area.340803', '大观区', NULL, '340803', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1104, '2020-03-29 14:20:36.000', 'system.region.area.340811', '宜秀区', NULL, '340811', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1105, '2020-03-29 14:20:36.000', 'system.region.area.340822', '怀宁县', NULL, '340822', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1106, '2020-03-29 14:20:36.000', 'system.region.area.340825', '太湖县', NULL, '340825', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1107, '2020-03-29 14:20:36.000', 'system.region.area.340826', '宿松县', NULL, '340826', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1108, '2020-03-29 14:20:36.000', 'system.region.area.340827', '望江县', NULL, '340827', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1109, '2020-03-29 14:20:36.000', 'system.region.area.340828', '岳西县', NULL, '340828', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1110, '2020-03-29 14:20:36.000', 'system.region.area.340881', '桐城市', NULL, '340881', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1111, '2020-03-29 14:20:36.000', 'system.region.area.340882', '潜山市', NULL, '340882', 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1112, '2020-03-29 14:20:36.000', 'system.region.city.341000', '黄山市', 'area', '341000', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1113, '2020-03-29 14:20:36.000', 'system.region.area.341002', '屯溪区', NULL, '341002', 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1114, '2020-03-29 14:20:36.000', 'system.region.area.341003', '黄山区', NULL, '341003', 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1115, '2020-03-29 14:20:36.000', 'system.region.area.341004', '徽州区', NULL, '341004', 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1116, '2020-03-29 14:20:36.000', 'system.region.area.341021', '歙县', NULL, '341021', 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1117, '2020-03-29 14:20:36.000', 'system.region.area.341022', '休宁县', NULL, '341022', 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1118, '2020-03-29 14:20:36.000', 'system.region.area.341023', '黟县', NULL, '341023', 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1119, '2020-03-29 14:20:36.000', 'system.region.area.341024', '祁门县', NULL, '341024', 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1120, '2020-03-29 14:20:36.000', 'system.region.city.341100', '滁州市', 'area', '341100', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1121, '2020-03-29 14:20:36.000', 'system.region.area.341102', '琅琊区', NULL, '341102', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1122, '2020-03-29 14:20:36.000', 'system.region.area.341103', '南谯区', NULL, '341103', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1123, '2020-03-29 14:20:36.000', 'system.region.area.341122', '来安县', NULL, '341122', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1124, '2020-03-29 14:20:36.000', 'system.region.area.341124', '全椒县', NULL, '341124', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1125, '2020-03-29 14:20:36.000', 'system.region.area.341125', '定远县', NULL, '341125', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1126, '2020-03-29 14:20:36.000', 'system.region.area.341126', '凤阳县', NULL, '341126', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1127, '2020-03-29 14:20:36.000', 'system.region.area.341181', '天长市', NULL, '341181', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1128, '2020-03-29 14:20:36.000', 'system.region.area.341182', '明光市', NULL, '341182', 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1129, '2020-03-29 14:20:36.000', 'system.region.city.341200', '阜阳市', 'area', '341200', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1130, '2020-03-29 14:20:36.000', 'system.region.area.341202', '颍州区', NULL, '341202', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1131, '2020-03-29 14:20:36.000', 'system.region.area.341203', '颍东区', NULL, '341203', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1132, '2020-03-29 14:20:36.000', 'system.region.area.341204', '颍泉区', NULL, '341204', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1133, '2020-03-29 14:20:36.000', 'system.region.area.341221', '临泉县', NULL, '341221', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1134, '2020-03-29 14:20:36.000', 'system.region.area.341222', '太和县', NULL, '341222', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1135, '2020-03-29 14:20:36.000', 'system.region.area.341225', '阜南县', NULL, '341225', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1136, '2020-03-29 14:20:36.000', 'system.region.area.341226', '颍上县', NULL, '341226', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1137, '2020-03-29 14:20:36.000', 'system.region.area.341282', '界首市', NULL, '341282', 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1138, '2020-03-29 14:20:36.000', 'system.region.city.341300', '宿州市', 'area', '341300', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1139, '2020-03-29 14:20:36.000', 'system.region.area.341302', '埇桥区', NULL, '341302', 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1140, '2020-03-29 14:20:36.000', 'system.region.area.341321', '砀山县', NULL, '341321', 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1141, '2020-03-29 14:20:36.000', 'system.region.area.341322', '萧县', NULL, '341322', 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1142, '2020-03-29 14:20:36.000', 'system.region.area.341323', '灵璧县', NULL, '341323', 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1143, '2020-03-29 14:20:36.000', 'system.region.area.341324', '泗县', NULL, '341324', 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1144, '2020-03-29 14:20:36.000', 'system.region.city.341500', '六安市', 'area', '341500', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1145, '2020-03-29 14:20:36.000', 'system.region.area.341502', '金安区', NULL, '341502', 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1146, '2020-03-29 14:20:36.000', 'system.region.area.341503', '裕安区', NULL, '341503', 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1147, '2020-03-29 14:20:36.000', 'system.region.area.341504', '叶集区', NULL, '341504', 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1148, '2020-03-29 14:20:36.000', 'system.region.area.341522', '霍邱县', NULL, '341522', 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1149, '2020-03-29 14:20:36.000', 'system.region.area.341523', '舒城县', NULL, '341523', 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1150, '2020-03-29 14:20:36.000', 'system.region.area.341524', '金寨县', NULL, '341524', 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1151, '2020-03-29 14:20:36.000', 'system.region.area.341525', '霍山县', NULL, '341525', 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1152, '2020-03-29 14:20:36.000', 'system.region.city.341600', '亳州市', 'area', '341600', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1153, '2020-03-29 14:20:36.000', 'system.region.area.341602', '谯城区', NULL, '341602', 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1154, '2020-03-29 14:20:36.000', 'system.region.area.341621', '涡阳县', NULL, '341621', 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1155, '2020-03-29 14:20:36.000', 'system.region.area.341622', '蒙城县', NULL, '341622', 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1156, '2020-03-29 14:20:36.000', 'system.region.area.341623', '利辛县', NULL, '341623', 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1157, '2020-03-29 14:20:36.000', 'system.region.city.341700', '池州市', 'area', '341700', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1158, '2020-03-29 14:20:36.000', 'system.region.area.341702', '贵池区', NULL, '341702', 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1159, '2020-03-29 14:20:36.000', 'system.region.area.341721', '东至县', NULL, '341721', 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1160, '2020-03-29 14:20:36.000', 'system.region.area.341722', '石台县', NULL, '341722', 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1161, '2020-03-29 14:20:36.000', 'system.region.area.341723', '青阳县', NULL, '341723', 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1162, '2020-03-29 14:20:36.000', 'system.region.city.341800', '宣城市', 'area', '341800', 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1163, '2020-03-29 14:20:36.000', 'system.region.area.341802', '宣州区', NULL, '341802', 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1164, '2020-03-29 14:20:36.000', 'system.region.area.341821', '郎溪县', NULL, '341821', 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1165, '2020-03-29 14:20:36.000', 'system.region.area.341823', '泾县', NULL, '341823', 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1166, '2020-03-29 14:20:36.000', 'system.region.area.341824', '绩溪县', NULL, '341824', 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1167, '2020-03-29 14:20:36.000', 'system.region.area.341825', '旌德县', NULL, '341825', 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1168, '2020-03-29 14:20:36.000', 'system.region.area.341881', '宁国市', NULL, '341881', 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1169, '2020-03-29 14:20:36.000', 'system.region.area.341882', '广德市', NULL, '341882', 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1170, '2020-03-29 14:20:36.000', 'system.region.province.350000', '福建省', 'city', '350000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1171, '2020-03-29 14:20:36.000', 'system.region.city.350100', '福州市', 'area', '350100', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1172, '2020-03-29 14:20:36.000', 'system.region.area.350102', '鼓楼区', NULL, '350102', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1173, '2020-03-29 14:20:36.000', 'system.region.area.350103', '台江区', NULL, '350103', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1174, '2020-03-29 14:20:36.000', 'system.region.area.350104', '仓山区', NULL, '350104', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1175, '2020-03-29 14:20:36.000', 'system.region.area.350105', '马尾区', NULL, '350105', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1176, '2020-03-29 14:20:36.000', 'system.region.area.350111', '晋安区', NULL, '350111', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1177, '2020-03-29 14:20:36.000', 'system.region.area.350112', '长乐区', NULL, '350112', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1178, '2020-03-29 14:20:36.000', 'system.region.area.350121', '闽侯县', NULL, '350121', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1179, '2020-03-29 14:20:36.000', 'system.region.area.350122', '连江县', NULL, '350122', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1180, '2020-03-29 14:20:36.000', 'system.region.area.350123', '罗源县', NULL, '350123', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1181, '2020-03-29 14:20:36.000', 'system.region.area.350124', '闽清县', NULL, '350124', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1182, '2020-03-29 14:20:36.000', 'system.region.area.350125', '永泰县', NULL, '350125', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1183, '2020-03-29 14:20:36.000', 'system.region.area.350128', '平潭县', NULL, '350128', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1184, '2020-03-29 14:20:36.000', 'system.region.area.350181', '福清市', NULL, '350181', 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1185, '2020-03-29 14:20:36.000', 'system.region.city.350200', '厦门市', 'area', '350200', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1186, '2020-03-29 14:20:36.000', 'system.region.area.350203', '思明区', NULL, '350203', 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1187, '2020-03-29 14:20:36.000', 'system.region.area.350205', '海沧区', NULL, '350205', 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1188, '2020-03-29 14:20:36.000', 'system.region.area.350206', '湖里区', NULL, '350206', 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1189, '2020-03-29 14:20:36.000', 'system.region.area.350211', '集美区', NULL, '350211', 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1190, '2020-03-29 14:20:36.000', 'system.region.area.350212', '同安区', NULL, '350212', 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1191, '2020-03-29 14:20:36.000', 'system.region.area.350213', '翔安区', NULL, '350213', 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1192, '2020-03-29 14:20:36.000', 'system.region.city.350300', '莆田市', 'area', '350300', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1193, '2020-03-29 14:20:36.000', 'system.region.area.350302', '城厢区', NULL, '350302', 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1194, '2020-03-29 14:20:36.000', 'system.region.area.350303', '涵江区', NULL, '350303', 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1195, '2020-03-29 14:20:36.000', 'system.region.area.350304', '荔城区', NULL, '350304', 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1196, '2020-03-29 14:20:36.000', 'system.region.area.350305', '秀屿区', NULL, '350305', 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1197, '2020-03-29 14:20:36.000', 'system.region.area.350322', '仙游县', NULL, '350322', 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1198, '2020-03-29 14:20:36.000', 'system.region.city.350400', '三明市', 'area', '350400', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1199, '2020-03-29 14:20:36.000', 'system.region.area.350402', '梅列区', NULL, '350402', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1200, '2020-03-29 14:20:36.000', 'system.region.area.350403', '三元区', NULL, '350403', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1201, '2020-03-29 14:20:36.000', 'system.region.area.350421', '明溪县', NULL, '350421', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1202, '2020-03-29 14:20:36.000', 'system.region.area.350423', '清流县', NULL, '350423', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1203, '2020-03-29 14:20:36.000', 'system.region.area.350424', '宁化县', NULL, '350424', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1204, '2020-03-29 14:20:36.000', 'system.region.area.350425', '大田县', NULL, '350425', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1205, '2020-03-29 14:20:36.000', 'system.region.area.350426', '尤溪县', NULL, '350426', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1206, '2020-03-29 14:20:36.000', 'system.region.area.350427', '沙县', NULL, '350427', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1207, '2020-03-29 14:20:36.000', 'system.region.area.350428', '将乐县', NULL, '350428', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1208, '2020-03-29 14:20:36.000', 'system.region.area.350429', '泰宁县', NULL, '350429', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1209, '2020-03-29 14:20:36.000', 'system.region.area.350430', '建宁县', NULL, '350430', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1210, '2020-03-29 14:20:36.000', 'system.region.area.350481', '永安市', NULL, '350481', 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1211, '2020-03-29 14:20:36.000', 'system.region.city.350500', '泉州市', 'area', '350500', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1212, '2020-03-29 14:20:36.000', 'system.region.area.350502', '鲤城区', NULL, '350502', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1213, '2020-03-29 14:20:36.000', 'system.region.area.350503', '丰泽区', NULL, '350503', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1214, '2020-03-29 14:20:36.000', 'system.region.area.350504', '洛江区', NULL, '350504', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1215, '2020-03-29 14:20:36.000', 'system.region.area.350505', '泉港区', NULL, '350505', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1216, '2020-03-29 14:20:36.000', 'system.region.area.350521', '惠安县', NULL, '350521', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1217, '2020-03-29 14:20:36.000', 'system.region.area.350524', '安溪县', NULL, '350524', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1218, '2020-03-29 14:20:36.000', 'system.region.area.350525', '永春县', NULL, '350525', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1219, '2020-03-29 14:20:36.000', 'system.region.area.350526', '德化县', NULL, '350526', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1220, '2020-03-29 14:20:36.000', 'system.region.area.350527', '金门县', NULL, '350527', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1221, '2020-03-29 14:20:36.000', 'system.region.area.350581', '石狮市', NULL, '350581', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1222, '2020-03-29 14:20:36.000', 'system.region.area.350582', '晋江市', NULL, '350582', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1223, '2020-03-29 14:20:36.000', 'system.region.area.350583', '南安市', NULL, '350583', 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1224, '2020-03-29 14:20:36.000', 'system.region.city.350600', '漳州市', 'area', '350600', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1225, '2020-03-29 14:20:36.000', 'system.region.area.350602', '芗城区', NULL, '350602', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1226, '2020-03-29 14:20:36.000', 'system.region.area.350603', '龙文区', NULL, '350603', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1227, '2020-03-29 14:20:36.000', 'system.region.area.350622', '云霄县', NULL, '350622', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1228, '2020-03-29 14:20:36.000', 'system.region.area.350623', '漳浦县', NULL, '350623', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1229, '2020-03-29 14:20:36.000', 'system.region.area.350624', '诏安县', NULL, '350624', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1230, '2020-03-29 14:20:36.000', 'system.region.area.350625', '长泰县', NULL, '350625', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1231, '2020-03-29 14:20:36.000', 'system.region.area.350626', '东山县', NULL, '350626', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1232, '2020-03-29 14:20:36.000', 'system.region.area.350627', '南靖县', NULL, '350627', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1233, '2020-03-29 14:20:36.000', 'system.region.area.350628', '平和县', NULL, '350628', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1234, '2020-03-29 14:20:36.000', 'system.region.area.350629', '华安县', NULL, '350629', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1235, '2020-03-29 14:20:36.000', 'system.region.area.350681', '龙海市', NULL, '350681', 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1236, '2020-03-29 14:20:36.000', 'system.region.city.350700', '南平市', 'area', '350700', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1237, '2020-03-29 14:20:36.000', 'system.region.area.350702', '延平区', NULL, '350702', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1238, '2020-03-29 14:20:36.000', 'system.region.area.350703', '建阳区', NULL, '350703', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1239, '2020-03-29 14:20:36.000', 'system.region.area.350721', '顺昌县', NULL, '350721', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1240, '2020-03-29 14:20:36.000', 'system.region.area.350722', '浦城县', NULL, '350722', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1241, '2020-03-29 14:20:36.000', 'system.region.area.350723', '光泽县', NULL, '350723', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1242, '2020-03-29 14:20:36.000', 'system.region.area.350724', '松溪县', NULL, '350724', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1243, '2020-03-29 14:20:36.000', 'system.region.area.350725', '政和县', NULL, '350725', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1244, '2020-03-29 14:20:36.000', 'system.region.area.350781', '邵武市', NULL, '350781', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1245, '2020-03-29 14:20:36.000', 'system.region.area.350782', '武夷山市', NULL, '350782', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1246, '2020-03-29 14:20:36.000', 'system.region.area.350783', '建瓯市', NULL, '350783', 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1247, '2020-03-29 14:20:36.000', 'system.region.city.350800', '龙岩市', 'area', '350800', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1248, '2020-03-29 14:20:36.000', 'system.region.area.350802', '新罗区', NULL, '350802', 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1249, '2020-03-29 14:20:36.000', 'system.region.area.350803', '永定区', NULL, '350803', 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1250, '2020-03-29 14:20:36.000', 'system.region.area.350821', '长汀县', NULL, '350821', 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1251, '2020-03-29 14:20:36.000', 'system.region.area.350823', '上杭县', NULL, '350823', 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1252, '2020-03-29 14:20:36.000', 'system.region.area.350824', '武平县', NULL, '350824', 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1253, '2020-03-29 14:20:36.000', 'system.region.area.350825', '连城县', NULL, '350825', 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1254, '2020-03-29 14:20:36.000', 'system.region.area.350881', '漳平市', NULL, '350881', 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1255, '2020-03-29 14:20:36.000', 'system.region.city.350900', '宁德市', 'area', '350900', 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1256, '2020-03-29 14:20:36.000', 'system.region.area.350902', '蕉城区', NULL, '350902', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1257, '2020-03-29 14:20:36.000', 'system.region.area.350921', '霞浦县', NULL, '350921', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1258, '2020-03-29 14:20:36.000', 'system.region.area.350922', '古田县', NULL, '350922', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1259, '2020-03-29 14:20:36.000', 'system.region.area.350923', '屏南县', NULL, '350923', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1260, '2020-03-29 14:20:36.000', 'system.region.area.350924', '寿宁县', NULL, '350924', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1261, '2020-03-29 14:20:36.000', 'system.region.area.350925', '周宁县', NULL, '350925', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1262, '2020-03-29 14:20:36.000', 'system.region.area.350926', '柘荣县', NULL, '350926', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1263, '2020-03-29 14:20:36.000', 'system.region.area.350981', '福安市', NULL, '350981', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1264, '2020-03-29 14:20:36.000', 'system.region.area.350982', '福鼎市', NULL, '350982', 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1265, '2020-03-29 14:20:36.000', 'system.region.province.360000', '江西省', 'city', '360000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1266, '2020-03-29 14:20:36.000', 'system.region.city.360100', '南昌市', 'area', '360100', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1267, '2020-03-29 14:20:36.000', 'system.region.area.360102', '东湖区', NULL, '360102', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1268, '2020-03-29 14:20:36.000', 'system.region.area.360103', '西湖区', NULL, '360103', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1269, '2020-03-29 14:20:36.000', 'system.region.area.360104', '青云谱区', NULL, '360104', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1270, '2020-03-29 14:20:36.000', 'system.region.area.360111', '青山湖区', NULL, '360111', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1271, '2020-03-29 14:20:36.000', 'system.region.area.360112', '新建区', NULL, '360112', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1272, '2020-03-29 14:20:36.000', 'system.region.area.360113', '红谷滩区', NULL, '360113', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1273, '2020-03-29 14:20:36.000', 'system.region.area.360121', '南昌县', NULL, '360121', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1274, '2020-03-29 14:20:36.000', 'system.region.area.360123', '安义县', NULL, '360123', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1275, '2020-03-29 14:20:36.000', 'system.region.area.360124', '进贤县', NULL, '360124', 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1276, '2020-03-29 14:20:36.000', 'system.region.city.360200', '景德镇市', 'area', '360200', 1, 15, 1265, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1277, '2020-03-29 14:20:36.000', 'system.region.area.360202', '昌江区', NULL, '360202', 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1278, '2020-03-29 14:20:36.000', 'system.region.area.360203', '珠山区', NULL, '360203', 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1279, '2020-03-29 14:20:36.000', 'system.region.area.360222', '浮梁县', NULL, '360222', 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1280, '2020-03-29 14:20:36.000', 'system.region.area.360281', '乐平市', NULL, '360281', 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1281, '2020-03-29 14:20:36.000', 'system.region.city.360300', '萍乡市', 'area', '360300', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1282, '2020-03-29 14:20:36.000', 'system.region.area.360302', '安源区', NULL, '360302', 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1283, '2020-03-29 14:20:36.000', 'system.region.area.360313', '湘东区', NULL, '360313', 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1284, '2020-03-29 14:20:36.000', 'system.region.area.360321', '莲花县', NULL, '360321', 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1285, '2020-03-29 14:20:36.000', 'system.region.area.360322', '上栗县', NULL, '360322', 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1286, '2020-03-29 14:20:36.000', 'system.region.area.360323', '芦溪县', NULL, '360323', 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1287, '2020-03-29 14:20:36.000', 'system.region.city.360400', '九江市', 'area', '360400', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1288, '2020-03-29 14:20:36.000', 'system.region.area.360402', '濂溪区', NULL, '360402', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1289, '2020-03-29 14:20:36.000', 'system.region.area.360403', '浔阳区', NULL, '360403', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1290, '2020-03-29 14:20:36.000', 'system.region.area.360404', '柴桑区', NULL, '360404', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1291, '2020-03-29 14:20:36.000', 'system.region.area.360423', '武宁县', NULL, '360423', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1292, '2020-03-29 14:20:36.000', 'system.region.area.360424', '修水县', NULL, '360424', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1293, '2020-03-29 14:20:36.000', 'system.region.area.360425', '永修县', NULL, '360425', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1294, '2020-03-29 14:20:36.000', 'system.region.area.360426', '德安县', NULL, '360426', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1295, '2020-03-29 14:20:36.000', 'system.region.area.360428', '都昌县', NULL, '360428', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1296, '2020-03-29 14:20:36.000', 'system.region.area.360429', '湖口县', NULL, '360429', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1297, '2020-03-29 14:20:36.000', 'system.region.area.360430', '彭泽县', NULL, '360430', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1298, '2020-03-29 14:20:36.000', 'system.region.area.360481', '瑞昌市', NULL, '360481', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1299, '2020-03-29 14:20:36.000', 'system.region.area.360482', '共青城市', NULL, '360482', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1300, '2020-03-29 14:20:36.000', 'system.region.area.360483', '庐山市', NULL, '360483', 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1301, '2020-03-29 14:20:36.000', 'system.region.city.360500', '新余市', 'area', '360500', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1302, '2020-03-29 14:20:36.000', 'system.region.area.360502', '渝水区', NULL, '360502', 1, 16, 1301, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1303, '2020-03-29 14:20:36.000', 'system.region.area.360521', '分宜县', NULL, '360521', 1, 16, 1301, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1304, '2020-03-29 14:20:36.000', 'system.region.city.360600', '鹰潭市', 'area', '360600', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1305, '2020-03-29 14:20:36.000', 'system.region.area.360602', '月湖区', NULL, '360602', 1, 16, 1304, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1306, '2020-03-29 14:20:36.000', 'system.region.area.360603', '余江区', NULL, '360603', 1, 16, 1304, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1307, '2020-03-29 14:20:36.000', 'system.region.area.360681', '贵溪市', NULL, '360681', 1, 16, 1304, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1308, '2020-03-29 14:20:36.000', 'system.region.city.360700', '赣州市', 'area', '360700', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1309, '2020-03-29 14:20:36.000', 'system.region.area.360702', '章贡区', NULL, '360702', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1310, '2020-03-29 14:20:36.000', 'system.region.area.360703', '南康区', NULL, '360703', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1311, '2020-03-29 14:20:36.000', 'system.region.area.360704', '赣县区', NULL, '360704', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1312, '2020-03-29 14:20:36.000', 'system.region.area.360722', '信丰县', NULL, '360722', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1313, '2020-03-29 14:20:36.000', 'system.region.area.360723', '大余县', NULL, '360723', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1314, '2020-03-29 14:20:36.000', 'system.region.area.360724', '上犹县', NULL, '360724', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1315, '2020-03-29 14:20:36.000', 'system.region.area.360725', '崇义县', NULL, '360725', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1316, '2020-03-29 14:20:36.000', 'system.region.area.360726', '安远县', NULL, '360726', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1317, '2020-03-29 14:20:36.000', 'system.region.area.360727', '龙南县', NULL, '360727', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1318, '2020-03-29 14:20:36.000', 'system.region.area.360728', '定南县', NULL, '360728', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1319, '2020-03-29 14:20:36.000', 'system.region.area.360729', '全南县', NULL, '360729', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1320, '2020-03-29 14:20:36.000', 'system.region.area.360730', '宁都县', NULL, '360730', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1321, '2020-03-29 14:20:36.000', 'system.region.area.360731', '于都县', NULL, '360731', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1322, '2020-03-29 14:20:36.000', 'system.region.area.360732', '兴国县', NULL, '360732', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1323, '2020-03-29 14:20:36.000', 'system.region.area.360733', '会昌县', NULL, '360733', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1324, '2020-03-29 14:20:36.000', 'system.region.area.360734', '寻乌县', NULL, '360734', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1325, '2020-03-29 14:20:36.000', 'system.region.area.360735', '石城县', NULL, '360735', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1326, '2020-03-29 14:20:36.000', 'system.region.area.360781', '瑞金市', NULL, '360781', 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1327, '2020-03-29 14:20:36.000', 'system.region.city.360800', '吉安市', 'area', '360800', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1328, '2020-03-29 14:20:36.000', 'system.region.area.360802', '吉州区', NULL, '360802', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1329, '2020-03-29 14:20:36.000', 'system.region.area.360803', '青原区', NULL, '360803', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1330, '2020-03-29 14:20:36.000', 'system.region.area.360821', '吉安县', NULL, '360821', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1331, '2020-03-29 14:20:36.000', 'system.region.area.360822', '吉水县', NULL, '360822', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1332, '2020-03-29 14:20:36.000', 'system.region.area.360823', '峡江县', NULL, '360823', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1333, '2020-03-29 14:20:36.000', 'system.region.area.360824', '新干县', NULL, '360824', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1334, '2020-03-29 14:20:36.000', 'system.region.area.360825', '永丰县', NULL, '360825', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1335, '2020-03-29 14:20:36.000', 'system.region.area.360826', '泰和县', NULL, '360826', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1336, '2020-03-29 14:20:36.000', 'system.region.area.360827', '遂川县', NULL, '360827', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1337, '2020-03-29 14:20:36.000', 'system.region.area.360828', '万安县', NULL, '360828', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1338, '2020-03-29 14:20:36.000', 'system.region.area.360829', '安福县', NULL, '360829', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1339, '2020-03-29 14:20:36.000', 'system.region.area.360830', '永新县', NULL, '360830', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1340, '2020-03-29 14:20:36.000', 'system.region.area.360881', '井冈山市', NULL, '360881', 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1341, '2020-03-29 14:20:36.000', 'system.region.city.360900', '宜春市', 'area', '360900', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1342, '2020-03-29 14:20:36.000', 'system.region.area.360902', '袁州区', NULL, '360902', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1343, '2020-03-29 14:20:36.000', 'system.region.area.360921', '奉新县', NULL, '360921', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1344, '2020-03-29 14:20:36.000', 'system.region.area.360922', '万载县', NULL, '360922', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1345, '2020-03-29 14:20:36.000', 'system.region.area.360923', '上高县', NULL, '360923', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1346, '2020-03-29 14:20:36.000', 'system.region.area.360924', '宜丰县', NULL, '360924', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1347, '2020-03-29 14:20:36.000', 'system.region.area.360925', '靖安县', NULL, '360925', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1348, '2020-03-29 14:20:36.000', 'system.region.area.360926', '铜鼓县', NULL, '360926', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1349, '2020-03-29 14:20:36.000', 'system.region.area.360981', '丰城市', NULL, '360981', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1350, '2020-03-29 14:20:36.000', 'system.region.area.360982', '樟树市', NULL, '360982', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1351, '2020-03-29 14:20:36.000', 'system.region.area.360983', '高安市', NULL, '360983', 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1352, '2020-03-29 14:20:36.000', 'system.region.city.361000', '抚州市', 'area', '361000', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1353, '2020-03-29 14:20:36.000', 'system.region.area.361002', '临川区', NULL, '361002', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1354, '2020-03-29 14:20:36.000', 'system.region.area.361003', '东乡区', NULL, '361003', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1355, '2020-03-29 14:20:36.000', 'system.region.area.361021', '南城县', NULL, '361021', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1356, '2020-03-29 14:20:36.000', 'system.region.area.361022', '黎川县', NULL, '361022', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1357, '2020-03-29 14:20:36.000', 'system.region.area.361023', '南丰县', NULL, '361023', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1358, '2020-03-29 14:20:36.000', 'system.region.area.361024', '崇仁县', NULL, '361024', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1359, '2020-03-29 14:20:36.000', 'system.region.area.361025', '乐安县', NULL, '361025', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1360, '2020-03-29 14:20:36.000', 'system.region.area.361026', '宜黄县', NULL, '361026', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1361, '2020-03-29 14:20:36.000', 'system.region.area.361027', '金溪县', NULL, '361027', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1362, '2020-03-29 14:20:36.000', 'system.region.area.361028', '资溪县', NULL, '361028', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1363, '2020-03-29 14:20:36.000', 'system.region.area.361030', '广昌县', NULL, '361030', 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1364, '2020-03-29 14:20:36.000', 'system.region.city.361100', '上饶市', 'area', '361100', 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1365, '2020-03-29 14:20:36.000', 'system.region.area.361102', '信州区', NULL, '361102', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1366, '2020-03-29 14:20:36.000', 'system.region.area.361103', '广丰区', NULL, '361103', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1367, '2020-03-29 14:20:36.000', 'system.region.area.361104', '广信区', NULL, '361104', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1368, '2020-03-29 14:20:36.000', 'system.region.area.361123', '玉山县', NULL, '361123', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1369, '2020-03-29 14:20:36.000', 'system.region.area.361124', '铅山县', NULL, '361124', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1370, '2020-03-29 14:20:36.000', 'system.region.area.361125', '横峰县', NULL, '361125', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1371, '2020-03-29 14:20:36.000', 'system.region.area.361126', '弋阳县', NULL, '361126', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1372, '2020-03-29 14:20:36.000', 'system.region.area.361127', '余干县', NULL, '361127', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1373, '2020-03-29 14:20:36.000', 'system.region.area.361128', '鄱阳县', NULL, '361128', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1374, '2020-03-29 14:20:36.000', 'system.region.area.361129', '万年县', NULL, '361129', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1375, '2020-03-29 14:20:36.000', 'system.region.area.361130', '婺源县', NULL, '361130', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1376, '2020-03-29 14:20:36.000', 'system.region.area.361181', '德兴市', NULL, '361181', 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1377, '2020-03-29 14:20:36.000', 'system.region.province.370000', '山东省', 'city', '370000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1378, '2020-03-29 14:20:36.000', 'system.region.city.370100', '济南市', 'area', '370100', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1379, '2020-03-29 14:20:36.000', 'system.region.area.370102', '历下区', NULL, '370102', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1380, '2020-03-29 14:20:36.000', 'system.region.area.370103', '市中区', NULL, '370103', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1381, '2020-03-29 14:20:36.000', 'system.region.area.370104', '槐荫区', NULL, '370104', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1382, '2020-03-29 14:20:36.000', 'system.region.area.370105', '天桥区', NULL, '370105', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1383, '2020-03-29 14:20:36.000', 'system.region.area.370112', '历城区', NULL, '370112', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1384, '2020-03-29 14:20:36.000', 'system.region.area.370113', '长清区', NULL, '370113', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1385, '2020-03-29 14:20:36.000', 'system.region.area.370114', '章丘区', NULL, '370114', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1386, '2020-03-29 14:20:36.000', 'system.region.area.370115', '济阳区', NULL, '370115', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1387, '2020-03-29 14:20:36.000', 'system.region.area.370116', '莱芜区', NULL, '370116', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1388, '2020-03-29 14:20:36.000', 'system.region.area.370117', '钢城区', NULL, '370117', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1389, '2020-03-29 14:20:36.000', 'system.region.area.370124', '平阴县', NULL, '370124', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1390, '2020-03-29 14:20:36.000', 'system.region.area.370126', '商河县', NULL, '370126', 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1391, '2020-03-29 14:20:36.000', 'system.region.city.370200', '青岛市', 'area', '370200', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1392, '2020-03-29 14:20:36.000', 'system.region.area.370202', '市南区', NULL, '370202', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1393, '2020-03-29 14:20:36.000', 'system.region.area.370203', '市北区', NULL, '370203', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1394, '2020-03-29 14:20:36.000', 'system.region.area.370211', '黄岛区', NULL, '370211', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1395, '2020-03-29 14:20:36.000', 'system.region.area.370212', '崂山区', NULL, '370212', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1396, '2020-03-29 14:20:36.000', 'system.region.area.370213', '李沧区', NULL, '370213', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1397, '2020-03-29 14:20:36.000', 'system.region.area.370214', '城阳区', NULL, '370214', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1398, '2020-03-29 14:20:36.000', 'system.region.area.370215', '即墨区', NULL, '370215', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1399, '2020-03-29 14:20:36.000', 'system.region.area.370281', '胶州市', NULL, '370281', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1400, '2020-03-29 14:20:36.000', 'system.region.area.370283', '平度市', NULL, '370283', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1401, '2020-03-29 14:20:36.000', 'system.region.area.370285', '莱西市', NULL, '370285', 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1402, '2020-03-29 14:20:36.000', 'system.region.city.370300', '淄博市', 'area', '370300', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1403, '2020-03-29 14:20:36.000', 'system.region.area.370302', '淄川区', NULL, '370302', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1404, '2020-03-29 14:20:36.000', 'system.region.area.370303', '张店区', NULL, '370303', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1405, '2020-03-29 14:20:36.000', 'system.region.area.370304', '博山区', NULL, '370304', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1406, '2020-03-29 14:20:36.000', 'system.region.area.370305', '临淄区', NULL, '370305', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1407, '2020-03-29 14:20:36.000', 'system.region.area.370306', '周村区', NULL, '370306', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1408, '2020-03-29 14:20:36.000', 'system.region.area.370321', '桓台县', NULL, '370321', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1409, '2020-03-29 14:20:36.000', 'system.region.area.370322', '高青县', NULL, '370322', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1410, '2020-03-29 14:20:36.000', 'system.region.area.370323', '沂源县', NULL, '370323', 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1411, '2020-03-29 14:20:36.000', 'system.region.city.370400', '枣庄市', 'area', '370400', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1412, '2020-03-29 14:20:36.000', 'system.region.area.370402', '市中区', NULL, '370402', 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1413, '2020-03-29 14:20:36.000', 'system.region.area.370403', '薛城区', NULL, '370403', 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1414, '2020-03-29 14:20:36.000', 'system.region.area.370404', '峄城区', NULL, '370404', 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1415, '2020-03-29 14:20:36.000', 'system.region.area.370405', '台儿庄区', NULL, '370405', 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1416, '2020-03-29 14:20:36.000', 'system.region.area.370406', '山亭区', NULL, '370406', 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1417, '2020-03-29 14:20:36.000', 'system.region.area.370481', '滕州市', NULL, '370481', 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1418, '2020-03-29 14:20:36.000', 'system.region.city.370500', '东营市', 'area', '370500', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1419, '2020-03-29 14:20:36.000', 'system.region.area.370502', '东营区', NULL, '370502', 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1420, '2020-03-29 14:20:36.000', 'system.region.area.370503', '河口区', NULL, '370503', 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1421, '2020-03-29 14:20:36.000', 'system.region.area.370505', '垦利区', NULL, '370505', 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1422, '2020-03-29 14:20:36.000', 'system.region.area.370522', '利津县', NULL, '370522', 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1423, '2020-03-29 14:20:36.000', 'system.region.area.370523', '广饶县', NULL, '370523', 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1424, '2020-03-29 14:20:36.000', 'system.region.city.370600', '烟台市', 'area', '370600', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1425, '2020-03-29 14:20:36.000', 'system.region.area.370602', '芝罘区', NULL, '370602', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1426, '2020-03-29 14:20:36.000', 'system.region.area.370611', '福山区', NULL, '370611', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1427, '2020-03-29 14:20:36.000', 'system.region.area.370612', '牟平区', NULL, '370612', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1428, '2020-03-29 14:20:36.000', 'system.region.area.370613', '莱山区', NULL, '370613', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1429, '2020-03-29 14:20:36.000', 'system.region.area.370634', '长岛县', NULL, '370634', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1430, '2020-03-29 14:20:36.000', 'system.region.area.370681', '龙口市', NULL, '370681', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1431, '2020-03-29 14:20:36.000', 'system.region.area.370682', '莱阳市', NULL, '370682', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1432, '2020-03-29 14:20:36.000', 'system.region.area.370683', '莱州市', NULL, '370683', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1433, '2020-03-29 14:20:36.000', 'system.region.area.370684', '蓬莱市', NULL, '370684', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1434, '2020-03-29 14:20:36.000', 'system.region.area.370685', '招远市', NULL, '370685', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1435, '2020-03-29 14:20:36.000', 'system.region.area.370686', '栖霞市', NULL, '370686', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1436, '2020-03-29 14:20:36.000', 'system.region.area.370687', '海阳市', NULL, '370687', 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1437, '2020-03-29 14:20:36.000', 'system.region.city.370700', '潍坊市', 'area', '370700', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1438, '2020-03-29 14:20:36.000', 'system.region.area.370702', '潍城区', NULL, '370702', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1439, '2020-03-29 14:20:36.000', 'system.region.area.370703', '寒亭区', NULL, '370703', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1440, '2020-03-29 14:20:36.000', 'system.region.area.370704', '坊子区', NULL, '370704', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1441, '2020-03-29 14:20:36.000', 'system.region.area.370705', '奎文区', NULL, '370705', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1442, '2020-03-29 14:20:36.000', 'system.region.area.370724', '临朐县', NULL, '370724', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1443, '2020-03-29 14:20:36.000', 'system.region.area.370725', '昌乐县', NULL, '370725', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1444, '2020-03-29 14:20:36.000', 'system.region.area.370781', '青州市', NULL, '370781', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1445, '2020-03-29 14:20:36.000', 'system.region.area.370782', '诸城市', NULL, '370782', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1446, '2020-03-29 14:20:36.000', 'system.region.area.370783', '寿光市', NULL, '370783', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1447, '2020-03-29 14:20:36.000', 'system.region.area.370784', '安丘市', NULL, '370784', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1448, '2020-03-29 14:20:36.000', 'system.region.area.370785', '高密市', NULL, '370785', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1449, '2020-03-29 14:20:36.000', 'system.region.area.370786', '昌邑市', NULL, '370786', 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1450, '2020-03-29 14:20:36.000', 'system.region.city.370800', '济宁市', 'area', '370800', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1451, '2020-03-29 14:20:36.000', 'system.region.area.370811', '任城区', NULL, '370811', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1452, '2020-03-29 14:20:36.000', 'system.region.area.370812', '兖州区', NULL, '370812', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1453, '2020-03-29 14:20:36.000', 'system.region.area.370826', '微山县', NULL, '370826', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1454, '2020-03-29 14:20:36.000', 'system.region.area.370827', '鱼台县', NULL, '370827', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1455, '2020-03-29 14:20:36.000', 'system.region.area.370828', '金乡县', NULL, '370828', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1456, '2020-03-29 14:20:36.000', 'system.region.area.370829', '嘉祥县', NULL, '370829', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1457, '2020-03-29 14:20:36.000', 'system.region.area.370830', '汶上县', NULL, '370830', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1458, '2020-03-29 14:20:36.000', 'system.region.area.370831', '泗水县', NULL, '370831', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1459, '2020-03-29 14:20:36.000', 'system.region.area.370832', '梁山县', NULL, '370832', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1460, '2020-03-29 14:20:36.000', 'system.region.area.370881', '曲阜市', NULL, '370881', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1461, '2020-03-29 14:20:36.000', 'system.region.area.370883', '邹城市', NULL, '370883', 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1462, '2020-03-29 14:20:36.000', 'system.region.city.370900', '泰安市', 'area', '370900', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1463, '2020-03-29 14:20:36.000', 'system.region.area.370902', '泰山区', NULL, '370902', 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1464, '2020-03-29 14:20:36.000', 'system.region.area.370911', '岱岳区', NULL, '370911', 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1465, '2020-03-29 14:20:36.000', 'system.region.area.370921', '宁阳县', NULL, '370921', 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1466, '2020-03-29 14:20:36.000', 'system.region.area.370923', '东平县', NULL, '370923', 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1467, '2020-03-29 14:20:36.000', 'system.region.area.370982', '新泰市', NULL, '370982', 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1468, '2020-03-29 14:20:36.000', 'system.region.area.370983', '肥城市', NULL, '370983', 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1469, '2020-03-29 14:20:36.000', 'system.region.city.371000', '威海市', 'area', '371000', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1470, '2020-03-29 14:20:36.000', 'system.region.area.371002', '环翠区', NULL, '371002', 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1471, '2020-03-29 14:20:36.000', 'system.region.area.371003', '文登区', NULL, '371003', 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1472, '2020-03-29 14:20:36.000', 'system.region.area.371082', '荣成市', NULL, '371082', 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1473, '2020-03-29 14:20:36.000', 'system.region.area.371083', '乳山市', NULL, '371083', 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1474, '2020-03-29 14:20:36.000', 'system.region.city.371100', '日照市', 'area', '371100', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1475, '2020-03-29 14:20:36.000', 'system.region.area.371102', '东港区', NULL, '371102', 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1476, '2020-03-29 14:20:36.000', 'system.region.area.371103', '岚山区', NULL, '371103', 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1477, '2020-03-29 14:20:36.000', 'system.region.area.371121', '五莲县', NULL, '371121', 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1478, '2020-03-29 14:20:36.000', 'system.region.area.371122', '莒县', NULL, '371122', 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1479, '2020-03-29 14:20:36.000', 'system.region.city.371300', '临沂市', 'area', '371300', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1480, '2020-03-29 14:20:36.000', 'system.region.area.371302', '兰山区', NULL, '371302', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1481, '2020-03-29 14:20:36.000', 'system.region.area.371311', '罗庄区', NULL, '371311', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1482, '2020-03-29 14:20:36.000', 'system.region.area.371312', '河东区', NULL, '371312', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1483, '2020-03-29 14:20:36.000', 'system.region.area.371321', '沂南县', NULL, '371321', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1484, '2020-03-29 14:20:36.000', 'system.region.area.371322', '郯城县', NULL, '371322', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1485, '2020-03-29 14:20:36.000', 'system.region.area.371323', '沂水县', NULL, '371323', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1486, '2020-03-29 14:20:36.000', 'system.region.area.371324', '兰陵县', NULL, '371324', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1487, '2020-03-29 14:20:36.000', 'system.region.area.371325', '费县', NULL, '371325', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1488, '2020-03-29 14:20:36.000', 'system.region.area.371326', '平邑县', NULL, '371326', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1489, '2020-03-29 14:20:36.000', 'system.region.area.371327', '莒南县', NULL, '371327', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1490, '2020-03-29 14:20:36.000', 'system.region.area.371328', '蒙阴县', NULL, '371328', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1491, '2020-03-29 14:20:36.000', 'system.region.area.371329', '临沭县', NULL, '371329', 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1492, '2020-03-29 14:20:36.000', 'system.region.city.371400', '德州市', 'area', '371400', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1493, '2020-03-29 14:20:36.000', 'system.region.area.371402', '德城区', NULL, '371402', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1494, '2020-03-29 14:20:36.000', 'system.region.area.371403', '陵城区', NULL, '371403', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1495, '2020-03-29 14:20:36.000', 'system.region.area.371422', '宁津县', NULL, '371422', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1496, '2020-03-29 14:20:36.000', 'system.region.area.371423', '庆云县', NULL, '371423', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1497, '2020-03-29 14:20:36.000', 'system.region.area.371424', '临邑县', NULL, '371424', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1498, '2020-03-29 14:20:36.000', 'system.region.area.371425', '齐河县', NULL, '371425', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1499, '2020-03-29 14:20:36.000', 'system.region.area.371426', '平原县', NULL, '371426', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1500, '2020-03-29 14:20:36.000', 'system.region.area.371427', '夏津县', NULL, '371427', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1501, '2020-03-29 14:20:36.000', 'system.region.area.371428', '武城县', NULL, '371428', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1502, '2020-03-29 14:20:36.000', 'system.region.area.371481', '乐陵市', NULL, '371481', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1503, '2020-03-29 14:20:36.000', 'system.region.area.371482', '禹城市', NULL, '371482', 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1504, '2020-03-29 14:20:36.000', 'system.region.city.371500', '聊城市', 'area', '371500', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1505, '2020-03-29 14:20:36.000', 'system.region.area.371502', '东昌府区', NULL, '371502', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1506, '2020-03-29 14:20:36.000', 'system.region.area.371503', '茌平区', NULL, '371503', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1507, '2020-03-29 14:20:36.000', 'system.region.area.371521', '阳谷县', NULL, '371521', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1508, '2020-03-29 14:20:36.000', 'system.region.area.371522', '莘县', NULL, '371522', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1509, '2020-03-29 14:20:36.000', 'system.region.area.371524', '东阿县', NULL, '371524', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1510, '2020-03-29 14:20:36.000', 'system.region.area.371525', '冠县', NULL, '371525', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1511, '2020-03-29 14:20:36.000', 'system.region.area.371526', '高唐县', NULL, '371526', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1512, '2020-03-29 14:20:36.000', 'system.region.area.371581', '临清市', NULL, '371581', 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1513, '2020-03-29 14:20:36.000', 'system.region.city.371600', '滨州市', 'area', '371600', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1514, '2020-03-29 14:20:36.000', 'system.region.area.371602', '滨城区', NULL, '371602', 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1515, '2020-03-29 14:20:36.000', 'system.region.area.371603', '沾化区', NULL, '371603', 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1516, '2020-03-29 14:20:36.000', 'system.region.area.371621', '惠民县', NULL, '371621', 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1517, '2020-03-29 14:20:36.000', 'system.region.area.371622', '阳信县', NULL, '371622', 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1518, '2020-03-29 14:20:36.000', 'system.region.area.371623', '无棣县', NULL, '371623', 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1519, '2020-03-29 14:20:36.000', 'system.region.area.371625', '博兴县', NULL, '371625', 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1520, '2020-03-29 14:20:36.000', 'system.region.area.371681', '邹平市', NULL, '371681', 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1521, '2020-03-29 14:20:36.000', 'system.region.city.371700', '菏泽市', 'area', '371700', 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1522, '2020-03-29 14:20:36.000', 'system.region.area.371702', '牡丹区', NULL, '371702', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1523, '2020-03-29 14:20:36.000', 'system.region.area.371703', '定陶区', NULL, '371703', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1524, '2020-03-29 14:20:36.000', 'system.region.area.371721', '曹县', NULL, '371721', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1525, '2020-03-29 14:20:36.000', 'system.region.area.371722', '单县', NULL, '371722', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1526, '2020-03-29 14:20:36.000', 'system.region.area.371723', '成武县', NULL, '371723', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1527, '2020-03-29 14:20:36.000', 'system.region.area.371724', '巨野县', NULL, '371724', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1528, '2020-03-29 14:20:36.000', 'system.region.area.371725', '郓城县', NULL, '371725', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1529, '2020-03-29 14:20:36.000', 'system.region.area.371726', '鄄城县', NULL, '371726', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1530, '2020-03-29 14:20:36.000', 'system.region.area.371728', '东明县', NULL, '371728', 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1531, '2020-03-29 14:20:36.000', 'system.region.province.410000', '河南省', 'city', '410000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1532, '2020-03-29 14:20:36.000', 'system.region.city.410100', '郑州市', 'area', '410100', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1533, '2020-03-29 14:20:36.000', 'system.region.area.410102', '中原区', NULL, '410102', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1534, '2020-03-29 14:20:36.000', 'system.region.area.410103', '二七区', NULL, '410103', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1535, '2020-03-29 14:20:36.000', 'system.region.area.410104', '管城回族区', NULL, '410104', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1536, '2020-03-29 14:20:36.000', 'system.region.area.410105', '金水区', NULL, '410105', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1537, '2020-03-29 14:20:36.000', 'system.region.area.410106', '上街区', NULL, '410106', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1538, '2020-03-29 14:20:36.000', 'system.region.area.410108', '惠济区', NULL, '410108', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1539, '2020-03-29 14:20:36.000', 'system.region.area.410122', '中牟县', NULL, '410122', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1540, '2020-03-29 14:20:36.000', 'system.region.area.410181', '巩义市', NULL, '410181', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1541, '2020-03-29 14:20:36.000', 'system.region.area.410182', '荥阳市', NULL, '410182', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1542, '2020-03-29 14:20:36.000', 'system.region.area.410183', '新密市', NULL, '410183', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1543, '2020-03-29 14:20:36.000', 'system.region.area.410184', '新郑市', NULL, '410184', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1544, '2020-03-29 14:20:36.000', 'system.region.area.410185', '登封市', NULL, '410185', 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1545, '2020-03-29 14:20:36.000', 'system.region.city.410200', '开封市', 'area', '410200', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1546, '2020-03-29 14:20:36.000', 'system.region.area.410202', '龙亭区', NULL, '410202', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1547, '2020-03-29 14:20:36.000', 'system.region.area.410203', '顺河回族区', NULL, '410203', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1548, '2020-03-29 14:20:36.000', 'system.region.area.410204', '鼓楼区', NULL, '410204', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1549, '2020-03-29 14:20:36.000', 'system.region.area.410205', '禹王台区', NULL, '410205', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1550, '2020-03-29 14:20:36.000', 'system.region.area.410212', '祥符区', NULL, '410212', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1551, '2020-03-29 14:20:36.000', 'system.region.area.410221', '杞县', NULL, '410221', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1552, '2020-03-29 14:20:36.000', 'system.region.area.410222', '通许县', NULL, '410222', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1553, '2020-03-29 14:20:36.000', 'system.region.area.410223', '尉氏县', NULL, '410223', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1554, '2020-03-29 14:20:36.000', 'system.region.area.410225', '兰考县', NULL, '410225', 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1555, '2020-03-29 14:20:36.000', 'system.region.city.410300', '洛阳市', 'area', '410300', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1556, '2020-03-29 14:20:36.000', 'system.region.area.410302', '老城区', NULL, '410302', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1557, '2020-03-29 14:20:36.000', 'system.region.area.410303', '西工区', NULL, '410303', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1558, '2020-03-29 14:20:36.000', 'system.region.area.410304', '瀍河回族区', NULL, '410304', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1559, '2020-03-29 14:20:36.000', 'system.region.area.410305', '涧西区', NULL, '410305', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1560, '2020-03-29 14:20:36.000', 'system.region.area.410306', '吉利区', NULL, '410306', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1561, '2020-03-29 14:20:36.000', 'system.region.area.410311', '洛龙区', NULL, '410311', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1562, '2020-03-29 14:20:36.000', 'system.region.area.410322', '孟津县', NULL, '410322', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1563, '2020-03-29 14:20:36.000', 'system.region.area.410323', '新安县', NULL, '410323', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1564, '2020-03-29 14:20:36.000', 'system.region.area.410324', '栾川县', NULL, '410324', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1565, '2020-03-29 14:20:36.000', 'system.region.area.410325', '嵩县', NULL, '410325', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1566, '2020-03-29 14:20:36.000', 'system.region.area.410326', '汝阳县', NULL, '410326', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1567, '2020-03-29 14:20:36.000', 'system.region.area.410327', '宜阳县', NULL, '410327', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1568, '2020-03-29 14:20:36.000', 'system.region.area.410328', '洛宁县', NULL, '410328', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1569, '2020-03-29 14:20:36.000', 'system.region.area.410329', '伊川县', NULL, '410329', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1570, '2020-03-29 14:20:36.000', 'system.region.area.410381', '偃师市', NULL, '410381', 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1571, '2020-03-29 14:20:36.000', 'system.region.city.410400', '平顶山市', 'area', '410400', 1, 15, 1531, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1572, '2020-03-29 14:20:36.000', 'system.region.area.410402', '新华区', NULL, '410402', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1573, '2020-03-29 14:20:36.000', 'system.region.area.410403', '卫东区', NULL, '410403', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1574, '2020-03-29 14:20:36.000', 'system.region.area.410404', '石龙区', NULL, '410404', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1575, '2020-03-29 14:20:36.000', 'system.region.area.410411', '湛河区', NULL, '410411', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1576, '2020-03-29 14:20:36.000', 'system.region.area.410421', '宝丰县', NULL, '410421', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1577, '2020-03-29 14:20:36.000', 'system.region.area.410422', '叶县', NULL, '410422', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1578, '2020-03-29 14:20:36.000', 'system.region.area.410423', '鲁山县', NULL, '410423', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1579, '2020-03-29 14:20:36.000', 'system.region.area.410425', '郏县', NULL, '410425', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1580, '2020-03-29 14:20:36.000', 'system.region.area.410481', '舞钢市', NULL, '410481', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1581, '2020-03-29 14:20:36.000', 'system.region.area.410482', '汝州市', NULL, '410482', 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1582, '2020-03-29 14:20:36.000', 'system.region.city.410500', '安阳市', 'area', '410500', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1583, '2020-03-29 14:20:36.000', 'system.region.area.410502', '文峰区', NULL, '410502', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1584, '2020-03-29 14:20:36.000', 'system.region.area.410503', '北关区', NULL, '410503', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1585, '2020-03-29 14:20:36.000', 'system.region.area.410505', '殷都区', NULL, '410505', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1586, '2020-03-29 14:20:36.000', 'system.region.area.410506', '龙安区', NULL, '410506', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1587, '2020-03-29 14:20:36.000', 'system.region.area.410522', '安阳县', NULL, '410522', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1588, '2020-03-29 14:20:36.000', 'system.region.area.410523', '汤阴县', NULL, '410523', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1589, '2020-03-29 14:20:36.000', 'system.region.area.410526', '滑县', NULL, '410526', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1590, '2020-03-29 14:20:36.000', 'system.region.area.410527', '内黄县', NULL, '410527', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1591, '2020-03-29 14:20:36.000', 'system.region.area.410581', '林州市', NULL, '410581', 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1592, '2020-03-29 14:20:36.000', 'system.region.city.410600', '鹤壁市', 'area', '410600', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1593, '2020-03-29 14:20:36.000', 'system.region.area.410602', '鹤山区', NULL, '410602', 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1594, '2020-03-29 14:20:36.000', 'system.region.area.410603', '山城区', NULL, '410603', 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1595, '2020-03-29 14:20:36.000', 'system.region.area.410611', '淇滨区', NULL, '410611', 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1596, '2020-03-29 14:20:36.000', 'system.region.area.410621', '浚县', NULL, '410621', 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1597, '2020-03-29 14:20:36.000', 'system.region.area.410622', '淇县', NULL, '410622', 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1598, '2020-03-29 14:20:36.000', 'system.region.city.410700', '新乡市', 'area', '410700', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1599, '2020-03-29 14:20:36.000', 'system.region.area.410702', '红旗区', NULL, '410702', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1600, '2020-03-29 14:20:36.000', 'system.region.area.410703', '卫滨区', NULL, '410703', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1601, '2020-03-29 14:20:36.000', 'system.region.area.410704', '凤泉区', NULL, '410704', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1602, '2020-03-29 14:20:36.000', 'system.region.area.410711', '牧野区', NULL, '410711', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1603, '2020-03-29 14:20:36.000', 'system.region.area.410721', '新乡县', NULL, '410721', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1604, '2020-03-29 14:20:36.000', 'system.region.area.410724', '获嘉县', NULL, '410724', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1605, '2020-03-29 14:20:36.000', 'system.region.area.410725', '原阳县', NULL, '410725', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1606, '2020-03-29 14:20:36.000', 'system.region.area.410726', '延津县', NULL, '410726', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1607, '2020-03-29 14:20:36.000', 'system.region.area.410727', '封丘县', NULL, '410727', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1608, '2020-03-29 14:20:36.000', 'system.region.area.410781', '卫辉市', NULL, '410781', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1609, '2020-03-29 14:20:36.000', 'system.region.area.410782', '辉县市', NULL, '410782', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1610, '2020-03-29 14:20:36.000', 'system.region.area.410783', '长垣市', NULL, '410783', 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1611, '2020-03-29 14:20:36.000', 'system.region.city.410800', '焦作市', 'area', '410800', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1612, '2020-03-29 14:20:36.000', 'system.region.area.410802', '解放区', NULL, '410802', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1613, '2020-03-29 14:20:36.000', 'system.region.area.410803', '中站区', NULL, '410803', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1614, '2020-03-29 14:20:36.000', 'system.region.area.410804', '马村区', NULL, '410804', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1615, '2020-03-29 14:20:36.000', 'system.region.area.410811', '山阳区', NULL, '410811', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1616, '2020-03-29 14:20:36.000', 'system.region.area.410821', '修武县', NULL, '410821', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1617, '2020-03-29 14:20:36.000', 'system.region.area.410822', '博爱县', NULL, '410822', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1618, '2020-03-29 14:20:36.000', 'system.region.area.410823', '武陟县', NULL, '410823', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1619, '2020-03-29 14:20:36.000', 'system.region.area.410825', '温县', NULL, '410825', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1620, '2020-03-29 14:20:36.000', 'system.region.area.410882', '沁阳市', NULL, '410882', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1621, '2020-03-29 14:20:36.000', 'system.region.area.410883', '孟州市', NULL, '410883', 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1622, '2020-03-29 14:20:36.000', 'system.region.city.410900', '濮阳市', 'area', '410900', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1623, '2020-03-29 14:20:36.000', 'system.region.area.410902', '华龙区', NULL, '410902', 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1624, '2020-03-29 14:20:36.000', 'system.region.area.410922', '清丰县', NULL, '410922', 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1625, '2020-03-29 14:20:36.000', 'system.region.area.410923', '南乐县', NULL, '410923', 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1626, '2020-03-29 14:20:36.000', 'system.region.area.410926', '范县', NULL, '410926', 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1627, '2020-03-29 14:20:36.000', 'system.region.area.410927', '台前县', NULL, '410927', 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1628, '2020-03-29 14:20:36.000', 'system.region.area.410928', '濮阳县', NULL, '410928', 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1629, '2020-03-29 14:20:36.000', 'system.region.city.411000', '许昌市', 'area', '411000', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1630, '2020-03-29 14:20:36.000', 'system.region.area.411002', '魏都区', NULL, '411002', 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1631, '2020-03-29 14:20:36.000', 'system.region.area.411003', '建安区', NULL, '411003', 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1632, '2020-03-29 14:20:36.000', 'system.region.area.411024', '鄢陵县', NULL, '411024', 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1633, '2020-03-29 14:20:36.000', 'system.region.area.411025', '襄城县', NULL, '411025', 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1634, '2020-03-29 14:20:36.000', 'system.region.area.411081', '禹州市', NULL, '411081', 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1635, '2020-03-29 14:20:36.000', 'system.region.area.411082', '长葛市', NULL, '411082', 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1636, '2020-03-29 14:20:36.000', 'system.region.city.411100', '漯河市', 'area', '411100', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1637, '2020-03-29 14:20:36.000', 'system.region.area.411102', '源汇区', NULL, '411102', 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1638, '2020-03-29 14:20:36.000', 'system.region.area.411103', '郾城区', NULL, '411103', 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1639, '2020-03-29 14:20:36.000', 'system.region.area.411104', '召陵区', NULL, '411104', 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1640, '2020-03-29 14:20:36.000', 'system.region.area.411121', '舞阳县', NULL, '411121', 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1641, '2020-03-29 14:20:36.000', 'system.region.area.411122', '临颍县', NULL, '411122', 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1642, '2020-03-29 14:20:36.000', 'system.region.city.411200', '三门峡市', 'area', '411200', 1, 15, 1531, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1643, '2020-03-29 14:20:36.000', 'system.region.area.411202', '湖滨区', NULL, '411202', 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1644, '2020-03-29 14:20:36.000', 'system.region.area.411203', '陕州区', NULL, '411203', 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1645, '2020-03-29 14:20:36.000', 'system.region.area.411221', '渑池县', NULL, '411221', 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1646, '2020-03-29 14:20:36.000', 'system.region.area.411224', '卢氏县', NULL, '411224', 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1647, '2020-03-29 14:20:36.000', 'system.region.area.411281', '义马市', NULL, '411281', 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1648, '2020-03-29 14:20:36.000', 'system.region.area.411282', '灵宝市', NULL, '411282', 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1649, '2020-03-29 14:20:36.000', 'system.region.city.411300', '南阳市', 'area', '411300', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1650, '2020-03-29 14:20:36.000', 'system.region.area.411302', '宛城区', NULL, '411302', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1651, '2020-03-29 14:20:36.000', 'system.region.area.411303', '卧龙区', NULL, '411303', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1652, '2020-03-29 14:20:36.000', 'system.region.area.411321', '南召县', NULL, '411321', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1653, '2020-03-29 14:20:36.000', 'system.region.area.411322', '方城县', NULL, '411322', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1654, '2020-03-29 14:20:36.000', 'system.region.area.411323', '西峡县', NULL, '411323', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1655, '2020-03-29 14:20:36.000', 'system.region.area.411324', '镇平县', NULL, '411324', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1656, '2020-03-29 14:20:36.000', 'system.region.area.411325', '内乡县', NULL, '411325', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1657, '2020-03-29 14:20:36.000', 'system.region.area.411326', '淅川县', NULL, '411326', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1658, '2020-03-29 14:20:36.000', 'system.region.area.411327', '社旗县', NULL, '411327', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1659, '2020-03-29 14:20:36.000', 'system.region.area.411328', '唐河县', NULL, '411328', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1660, '2020-03-29 14:20:36.000', 'system.region.area.411329', '新野县', NULL, '411329', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1661, '2020-03-29 14:20:36.000', 'system.region.area.411330', '桐柏县', NULL, '411330', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1662, '2020-03-29 14:20:36.000', 'system.region.area.411381', '邓州市', NULL, '411381', 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1663, '2020-03-29 14:20:36.000', 'system.region.city.411400', '商丘市', 'area', '411400', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1664, '2020-03-29 14:20:36.000', 'system.region.area.411402', '梁园区', NULL, '411402', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1665, '2020-03-29 14:20:36.000', 'system.region.area.411403', '睢阳区', NULL, '411403', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1666, '2020-03-29 14:20:36.000', 'system.region.area.411421', '民权县', NULL, '411421', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1667, '2020-03-29 14:20:36.000', 'system.region.area.411422', '睢县', NULL, '411422', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1668, '2020-03-29 14:20:36.000', 'system.region.area.411423', '宁陵县', NULL, '411423', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1669, '2020-03-29 14:20:36.000', 'system.region.area.411424', '柘城县', NULL, '411424', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1670, '2020-03-29 14:20:36.000', 'system.region.area.411425', '虞城县', NULL, '411425', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1671, '2020-03-29 14:20:36.000', 'system.region.area.411426', '夏邑县', NULL, '411426', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1672, '2020-03-29 14:20:36.000', 'system.region.area.411481', '永城市', NULL, '411481', 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1673, '2020-03-29 14:20:36.000', 'system.region.city.411500', '信阳市', 'area', '411500', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1674, '2020-03-29 14:20:36.000', 'system.region.area.411502', '浉河区', NULL, '411502', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1675, '2020-03-29 14:20:36.000', 'system.region.area.411503', '平桥区', NULL, '411503', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1676, '2020-03-29 14:20:36.000', 'system.region.area.411521', '罗山县', NULL, '411521', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1677, '2020-03-29 14:20:36.000', 'system.region.area.411522', '光山县', NULL, '411522', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1678, '2020-03-29 14:20:36.000', 'system.region.area.411523', '新县', NULL, '411523', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1679, '2020-03-29 14:20:36.000', 'system.region.area.411524', '商城县', NULL, '411524', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1680, '2020-03-29 14:20:36.000', 'system.region.area.411525', '固始县', NULL, '411525', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1681, '2020-03-29 14:20:36.000', 'system.region.area.411526', '潢川县', NULL, '411526', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1682, '2020-03-29 14:20:36.000', 'system.region.area.411527', '淮滨县', NULL, '411527', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1683, '2020-03-29 14:20:36.000', 'system.region.area.411528', '息县', NULL, '411528', 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1684, '2020-03-29 14:20:36.000', 'system.region.city.411600', '周口市', 'area', '411600', 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1685, '2020-03-29 14:20:36.000', 'system.region.area.411602', '川汇区', NULL, '411602', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1686, '2020-03-29 14:20:36.000', 'system.region.area.411603', '淮阳区', NULL, '411603', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1687, '2020-03-29 14:20:36.000', 'system.region.area.411621', '扶沟县', NULL, '411621', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1688, '2020-03-29 14:20:36.000', 'system.region.area.411622', '西华县', NULL, '411622', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1689, '2020-03-29 14:20:36.000', 'system.region.area.411623', '商水县', NULL, '411623', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1690, '2020-03-29 14:20:36.000', 'system.region.area.411624', '沈丘县', NULL, '411624', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1691, '2020-03-29 14:20:36.000', 'system.region.area.411625', '郸城县', NULL, '411625', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1692, '2020-03-29 14:20:36.000', 'system.region.area.411627', '太康县', NULL, '411627', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1693, '2020-03-29 14:20:36.000', 'system.region.area.411628', '鹿邑县', NULL, '411628', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1694, '2020-03-29 14:20:36.000', 'system.region.area.411681', '项城市', NULL, '411681', 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1695, '2020-03-29 14:20:36.000', 'system.region.city.411700', '驻马店市', 'area', '411700', 1, 15, 1531, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1696, '2020-03-29 14:20:36.000', 'system.region.area.411702', '驿城区', NULL, '411702', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1697, '2020-03-29 14:20:36.000', 'system.region.area.411721', '西平县', NULL, '411721', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1698, '2020-03-29 14:20:36.000', 'system.region.area.411722', '上蔡县', NULL, '411722', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1699, '2020-03-29 14:20:36.000', 'system.region.area.411723', '平舆县', NULL, '411723', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1700, '2020-03-29 14:20:36.000', 'system.region.area.411724', '正阳县', NULL, '411724', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1701, '2020-03-29 14:20:36.000', 'system.region.area.411725', '确山县', NULL, '411725', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1702, '2020-03-29 14:20:36.000', 'system.region.area.411726', '泌阳县', NULL, '411726', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1703, '2020-03-29 14:20:36.000', 'system.region.area.411727', '汝南县', NULL, '411727', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1704, '2020-03-29 14:20:36.000', 'system.region.area.411728', '遂平县', NULL, '411728', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1705, '2020-03-29 14:20:36.000', 'system.region.area.411729', '新蔡县', NULL, '411729', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1706, '2020-03-29 14:20:36.000', 'system.region.area.419001', '济源市', NULL, '419001', 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1707, '2020-03-29 14:20:36.000', 'system.region.province.420000', '湖北省', 'city', '420000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1708, '2020-03-29 14:20:36.000', 'system.region.city.420100', '武汉市', 'area', '420100', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1709, '2020-03-29 14:20:36.000', 'system.region.area.420102', '江岸区', NULL, '420102', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1710, '2020-03-29 14:20:36.000', 'system.region.area.420103', '江汉区', NULL, '420103', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1711, '2020-03-29 14:20:36.000', 'system.region.area.420104', '硚口区', NULL, '420104', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1712, '2020-03-29 14:20:36.000', 'system.region.area.420105', '汉阳区', NULL, '420105', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1713, '2020-03-29 14:20:36.000', 'system.region.area.420106', '武昌区', NULL, '420106', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1714, '2020-03-29 14:20:36.000', 'system.region.area.420107', '青山区', NULL, '420107', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1715, '2020-03-29 14:20:36.000', 'system.region.area.420111', '洪山区', NULL, '420111', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1716, '2020-03-29 14:20:36.000', 'system.region.area.420112', '东西湖区', NULL, '420112', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1717, '2020-03-29 14:20:36.000', 'system.region.area.420113', '汉南区', NULL, '420113', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1718, '2020-03-29 14:20:36.000', 'system.region.area.420114', '蔡甸区', NULL, '420114', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1719, '2020-03-29 14:20:36.000', 'system.region.area.420115', '江夏区', NULL, '420115', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1720, '2020-03-29 14:20:36.000', 'system.region.area.420116', '黄陂区', NULL, '420116', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1721, '2020-03-29 14:20:36.000', 'system.region.area.420117', '新洲区', NULL, '420117', 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1722, '2020-03-29 14:20:36.000', 'system.region.city.420200', '黄石市', 'area', '420200', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1723, '2020-03-29 14:20:36.000', 'system.region.area.420202', '黄石港区', NULL, '420202', 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1724, '2020-03-29 14:20:36.000', 'system.region.area.420203', '西塞山区', NULL, '420203', 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1725, '2020-03-29 14:20:36.000', 'system.region.area.420204', '下陆区', NULL, '420204', 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1726, '2020-03-29 14:20:36.000', 'system.region.area.420205', '铁山区', NULL, '420205', 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1727, '2020-03-29 14:20:36.000', 'system.region.area.420222', '阳新县', NULL, '420222', 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1728, '2020-03-29 14:20:36.000', 'system.region.area.420281', '大冶市', NULL, '420281', 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1729, '2020-03-29 14:20:36.000', 'system.region.city.420300', '十堰市', 'area', '420300', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1730, '2020-03-29 14:20:36.000', 'system.region.area.420302', '茅箭区', NULL, '420302', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1731, '2020-03-29 14:20:36.000', 'system.region.area.420303', '张湾区', NULL, '420303', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1732, '2020-03-29 14:20:36.000', 'system.region.area.420304', '郧阳区', NULL, '420304', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1733, '2020-03-29 14:20:36.000', 'system.region.area.420322', '郧西县', NULL, '420322', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1734, '2020-03-29 14:20:36.000', 'system.region.area.420323', '竹山县', NULL, '420323', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1735, '2020-03-29 14:20:36.000', 'system.region.area.420324', '竹溪县', NULL, '420324', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1736, '2020-03-29 14:20:36.000', 'system.region.area.420325', '房县', NULL, '420325', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1737, '2020-03-29 14:20:36.000', 'system.region.area.420381', '丹江口市', NULL, '420381', 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1738, '2020-03-29 14:20:36.000', 'system.region.city.420500', '宜昌市', 'area', '420500', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1739, '2020-03-29 14:20:36.000', 'system.region.area.420502', '西陵区', NULL, '420502', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1740, '2020-03-29 14:20:36.000', 'system.region.area.420503', '伍家岗区', NULL, '420503', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1741, '2020-03-29 14:20:36.000', 'system.region.area.420504', '点军区', NULL, '420504', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1742, '2020-03-29 14:20:36.000', 'system.region.area.420505', '猇亭区', NULL, '420505', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1743, '2020-03-29 14:20:36.000', 'system.region.area.420506', '夷陵区', NULL, '420506', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1744, '2020-03-29 14:20:36.000', 'system.region.area.420525', '远安县', NULL, '420525', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1745, '2020-03-29 14:20:36.000', 'system.region.area.420526', '兴山县', NULL, '420526', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1746, '2020-03-29 14:20:36.000', 'system.region.area.420527', '秭归县', NULL, '420527', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1747, '2020-03-29 14:20:36.000', 'system.region.area.420528', '长阳土家族自治县', NULL, '420528', 1, 16, 1738, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1748, '2020-03-29 14:20:36.000', 'system.region.area.420529', '五峰土家族自治县', NULL, '420529', 1, 16, 1738, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1749, '2020-03-29 14:20:36.000', 'system.region.area.420581', '宜都市', NULL, '420581', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1750, '2020-03-29 14:20:36.000', 'system.region.area.420582', '当阳市', NULL, '420582', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1751, '2020-03-29 14:20:36.000', 'system.region.area.420583', '枝江市', NULL, '420583', 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1752, '2020-03-29 14:20:36.000', 'system.region.city.420600', '襄阳市', 'area', '420600', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1753, '2020-03-29 14:20:36.000', 'system.region.area.420602', '襄城区', NULL, '420602', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1754, '2020-03-29 14:20:36.000', 'system.region.area.420606', '樊城区', NULL, '420606', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1755, '2020-03-29 14:20:36.000', 'system.region.area.420607', '襄州区', NULL, '420607', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1756, '2020-03-29 14:20:36.000', 'system.region.area.420624', '南漳县', NULL, '420624', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1757, '2020-03-29 14:20:36.000', 'system.region.area.420625', '谷城县', NULL, '420625', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1758, '2020-03-29 14:20:36.000', 'system.region.area.420626', '保康县', NULL, '420626', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1759, '2020-03-29 14:20:36.000', 'system.region.area.420682', '老河口市', NULL, '420682', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1760, '2020-03-29 14:20:36.000', 'system.region.area.420683', '枣阳市', NULL, '420683', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1761, '2020-03-29 14:20:36.000', 'system.region.area.420684', '宜城市', NULL, '420684', 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1762, '2020-03-29 14:20:36.000', 'system.region.city.420700', '鄂州市', 'area', '420700', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1763, '2020-03-29 14:20:36.000', 'system.region.area.420702', '梁子湖区', NULL, '420702', 1, 16, 1762, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1764, '2020-03-29 14:20:36.000', 'system.region.area.420703', '华容区', NULL, '420703', 1, 16, 1762, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1765, '2020-03-29 14:20:36.000', 'system.region.area.420704', '鄂城区', NULL, '420704', 1, 16, 1762, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1766, '2020-03-29 14:20:36.000', 'system.region.city.420800', '荆门市', 'area', '420800', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1767, '2020-03-29 14:20:36.000', 'system.region.area.420802', '东宝区', NULL, '420802', 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1768, '2020-03-29 14:20:36.000', 'system.region.area.420804', '掇刀区', NULL, '420804', 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1769, '2020-03-29 14:20:36.000', 'system.region.area.420822', '沙洋县', NULL, '420822', 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1770, '2020-03-29 14:20:36.000', 'system.region.area.420881', '钟祥市', NULL, '420881', 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1771, '2020-03-29 14:20:36.000', 'system.region.area.420882', '京山市', NULL, '420882', 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1772, '2020-03-29 14:20:36.000', 'system.region.city.420900', '孝感市', 'area', '420900', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1773, '2020-03-29 14:20:36.000', 'system.region.area.420902', '孝南区', NULL, '420902', 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1774, '2020-03-29 14:20:36.000', 'system.region.area.420921', '孝昌县', NULL, '420921', 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1775, '2020-03-29 14:20:36.000', 'system.region.area.420922', '大悟县', NULL, '420922', 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1776, '2020-03-29 14:20:36.000', 'system.region.area.420923', '云梦县', NULL, '420923', 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1777, '2020-03-29 14:20:36.000', 'system.region.area.420981', '应城市', NULL, '420981', 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1778, '2020-03-29 14:20:36.000', 'system.region.area.420982', '安陆市', NULL, '420982', 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1779, '2020-03-29 14:20:36.000', 'system.region.area.420984', '汉川市', NULL, '420984', 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1780, '2020-03-29 14:20:36.000', 'system.region.city.421000', '荆州市', 'area', '421000', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1781, '2020-03-29 14:20:36.000', 'system.region.area.421002', '沙市区', NULL, '421002', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1782, '2020-03-29 14:20:36.000', 'system.region.area.421003', '荆州区', NULL, '421003', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1783, '2020-03-29 14:20:36.000', 'system.region.area.421022', '公安县', NULL, '421022', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1784, '2020-03-29 14:20:36.000', 'system.region.area.421023', '监利县', NULL, '421023', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1785, '2020-03-29 14:20:36.000', 'system.region.area.421024', '江陵县', NULL, '421024', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1786, '2020-03-29 14:20:36.000', 'system.region.area.421081', '石首市', NULL, '421081', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1787, '2020-03-29 14:20:36.000', 'system.region.area.421083', '洪湖市', NULL, '421083', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1788, '2020-03-29 14:20:36.000', 'system.region.area.421087', '松滋市', NULL, '421087', 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1789, '2020-03-29 14:20:36.000', 'system.region.city.421100', '黄冈市', 'area', '421100', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1790, '2020-03-29 14:20:36.000', 'system.region.area.421102', '黄州区', NULL, '421102', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1791, '2020-03-29 14:20:36.000', 'system.region.area.421121', '团风县', NULL, '421121', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1792, '2020-03-29 14:20:36.000', 'system.region.area.421122', '红安县', NULL, '421122', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1793, '2020-03-29 14:20:36.000', 'system.region.area.421123', '罗田县', NULL, '421123', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1794, '2020-03-29 14:20:36.000', 'system.region.area.421124', '英山县', NULL, '421124', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1795, '2020-03-29 14:20:36.000', 'system.region.area.421125', '浠水县', NULL, '421125', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1796, '2020-03-29 14:20:36.000', 'system.region.area.421126', '蕲春县', NULL, '421126', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1797, '2020-03-29 14:20:36.000', 'system.region.area.421127', '黄梅县', NULL, '421127', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1798, '2020-03-29 14:20:36.000', 'system.region.area.421181', '麻城市', NULL, '421181', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1799, '2020-03-29 14:20:36.000', 'system.region.area.421182', '武穴市', NULL, '421182', 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1800, '2020-03-29 14:20:36.000', 'system.region.city.421200', '咸宁市', 'area', '421200', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1801, '2020-03-29 14:20:36.000', 'system.region.area.421202', '咸安区', NULL, '421202', 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1802, '2020-03-29 14:20:36.000', 'system.region.area.421221', '嘉鱼县', NULL, '421221', 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1803, '2020-03-29 14:20:36.000', 'system.region.area.421222', '通城县', NULL, '421222', 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1804, '2020-03-29 14:20:36.000', 'system.region.area.421223', '崇阳县', NULL, '421223', 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1805, '2020-03-29 14:20:36.000', 'system.region.area.421224', '通山县', NULL, '421224', 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1806, '2020-03-29 14:20:36.000', 'system.region.area.421281', '赤壁市', NULL, '421281', 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1807, '2020-03-29 14:20:36.000', 'system.region.city.421300', '随州市', 'area', '421300', 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1808, '2020-03-29 14:20:36.000', 'system.region.area.421303', '曾都区', NULL, '421303', 1, 16, 1807, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1809, '2020-03-29 14:20:36.000', 'system.region.area.421321', '随县', NULL, '421321', 1, 16, 1807, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1810, '2020-03-29 14:20:36.000', 'system.region.area.421381', '广水市', NULL, '421381', 1, 16, 1807, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1811, '2020-03-29 14:20:36.000', 'system.region.city.422800', '恩施土家族苗族自治州', 'area', '422800', 1, 15, 1707, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1812, '2020-03-29 14:20:36.000', 'system.region.area.422801', '恩施市', NULL, '422801', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1813, '2020-03-29 14:20:36.000', 'system.region.area.422802', '利川市', NULL, '422802', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1814, '2020-03-29 14:20:36.000', 'system.region.area.422822', '建始县', NULL, '422822', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1815, '2020-03-29 14:20:36.000', 'system.region.area.422823', '巴东县', NULL, '422823', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1816, '2020-03-29 14:20:36.000', 'system.region.area.422825', '宣恩县', NULL, '422825', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1817, '2020-03-29 14:20:36.000', 'system.region.area.422826', '咸丰县', NULL, '422826', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1818, '2020-03-29 14:20:36.000', 'system.region.area.422827', '来凤县', NULL, '422827', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1819, '2020-03-29 14:20:36.000', 'system.region.area.422828', '鹤峰县', NULL, '422828', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1820, '2020-03-29 14:20:36.000', 'system.region.area.429004', '仙桃市', NULL, '429004', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1821, '2020-03-29 14:20:36.000', 'system.region.area.429005', '潜江市', NULL, '429005', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1822, '2020-03-29 14:20:36.000', 'system.region.area.429006', '天门市', NULL, '429006', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1823, '2020-03-29 14:20:36.000', 'system.region.area.429021', '神农架林区', NULL, '429021', 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1824, '2020-03-29 14:20:36.000', 'system.region.province.430000', '湖南省', 'city', '430000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1825, '2020-03-29 14:20:36.000', 'system.region.city.430100', '长沙市', 'area', '430100', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1826, '2020-03-29 14:20:36.000', 'system.region.area.430102', '芙蓉区', NULL, '430102', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1827, '2020-03-29 14:20:36.000', 'system.region.area.430103', '天心区', NULL, '430103', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1828, '2020-03-29 14:20:36.000', 'system.region.area.430104', '岳麓区', NULL, '430104', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1829, '2020-03-29 14:20:36.000', 'system.region.area.430105', '开福区', NULL, '430105', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1830, '2020-03-29 14:20:36.000', 'system.region.area.430111', '雨花区', NULL, '430111', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1831, '2020-03-29 14:20:36.000', 'system.region.area.430112', '望城区', NULL, '430112', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1832, '2020-03-29 14:20:36.000', 'system.region.area.430121', '长沙县', NULL, '430121', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1833, '2020-03-29 14:20:36.000', 'system.region.area.430181', '浏阳市', NULL, '430181', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1834, '2020-03-29 14:20:36.000', 'system.region.area.430182', '宁乡市', NULL, '430182', 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1835, '2020-03-29 14:20:36.000', 'system.region.city.430200', '株洲市', 'area', '430200', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1836, '2020-03-29 14:20:36.000', 'system.region.area.430202', '荷塘区', NULL, '430202', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1837, '2020-03-29 14:20:36.000', 'system.region.area.430203', '芦淞区', NULL, '430203', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1838, '2020-03-29 14:20:36.000', 'system.region.area.430204', '石峰区', NULL, '430204', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1839, '2020-03-29 14:20:36.000', 'system.region.area.430211', '天元区', NULL, '430211', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1840, '2020-03-29 14:20:36.000', 'system.region.area.430212', '渌口区', NULL, '430212', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1841, '2020-03-29 14:20:36.000', 'system.region.area.430223', '攸县', NULL, '430223', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1842, '2020-03-29 14:20:36.000', 'system.region.area.430224', '茶陵县', NULL, '430224', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1843, '2020-03-29 14:20:36.000', 'system.region.area.430225', '炎陵县', NULL, '430225', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1844, '2020-03-29 14:20:36.000', 'system.region.area.430281', '醴陵市', NULL, '430281', 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1845, '2020-03-29 14:20:36.000', 'system.region.city.430300', '湘潭市', 'area', '430300', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1846, '2020-03-29 14:20:36.000', 'system.region.area.430302', '雨湖区', NULL, '430302', 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1847, '2020-03-29 14:20:36.000', 'system.region.area.430304', '岳塘区', NULL, '430304', 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1848, '2020-03-29 14:20:36.000', 'system.region.area.430321', '湘潭县', NULL, '430321', 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1849, '2020-03-29 14:20:36.000', 'system.region.area.430381', '湘乡市', NULL, '430381', 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1850, '2020-03-29 14:20:36.000', 'system.region.area.430382', '韶山市', NULL, '430382', 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1851, '2020-03-29 14:20:36.000', 'system.region.city.430400', '衡阳市', 'area', '430400', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1852, '2020-03-29 14:20:36.000', 'system.region.area.430405', '珠晖区', NULL, '430405', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1853, '2020-03-29 14:20:36.000', 'system.region.area.430406', '雁峰区', NULL, '430406', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1854, '2020-03-29 14:20:36.000', 'system.region.area.430407', '石鼓区', NULL, '430407', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1855, '2020-03-29 14:20:36.000', 'system.region.area.430408', '蒸湘区', NULL, '430408', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1856, '2020-03-29 14:20:36.000', 'system.region.area.430412', '南岳区', NULL, '430412', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1857, '2020-03-29 14:20:36.000', 'system.region.area.430421', '衡阳县', NULL, '430421', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1858, '2020-03-29 14:20:36.000', 'system.region.area.430422', '衡南县', NULL, '430422', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1859, '2020-03-29 14:20:36.000', 'system.region.area.430423', '衡山县', NULL, '430423', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1860, '2020-03-29 14:20:36.000', 'system.region.area.430424', '衡东县', NULL, '430424', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1861, '2020-03-29 14:20:36.000', 'system.region.area.430426', '祁东县', NULL, '430426', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1862, '2020-03-29 14:20:36.000', 'system.region.area.430481', '耒阳市', NULL, '430481', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1863, '2020-03-29 14:20:36.000', 'system.region.area.430482', '常宁市', NULL, '430482', 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1864, '2020-03-29 14:20:36.000', 'system.region.city.430500', '邵阳市', 'area', '430500', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1865, '2020-03-29 14:20:36.000', 'system.region.area.430502', '双清区', NULL, '430502', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1866, '2020-03-29 14:20:36.000', 'system.region.area.430503', '大祥区', NULL, '430503', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1867, '2020-03-29 14:20:36.000', 'system.region.area.430511', '北塔区', NULL, '430511', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1868, '2020-03-29 14:20:36.000', 'system.region.area.430522', '新邵县', NULL, '430522', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1869, '2020-03-29 14:20:36.000', 'system.region.area.430523', '邵阳县', NULL, '430523', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1870, '2020-03-29 14:20:36.000', 'system.region.area.430524', '隆回县', NULL, '430524', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1871, '2020-03-29 14:20:36.000', 'system.region.area.430525', '洞口县', NULL, '430525', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1872, '2020-03-29 14:20:36.000', 'system.region.area.430527', '绥宁县', NULL, '430527', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1873, '2020-03-29 14:20:36.000', 'system.region.area.430528', '新宁县', NULL, '430528', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1874, '2020-03-29 14:20:36.000', 'system.region.area.430529', '城步苗族自治县', NULL, '430529', 1, 16, 1864, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1875, '2020-03-29 14:20:36.000', 'system.region.area.430581', '武冈市', NULL, '430581', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1876, '2020-03-29 14:20:36.000', 'system.region.area.430582', '邵东市', NULL, '430582', 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1877, '2020-03-29 14:20:36.000', 'system.region.city.430600', '岳阳市', 'area', '430600', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1878, '2020-03-29 14:20:36.000', 'system.region.area.430602', '岳阳楼区', NULL, '430602', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1879, '2020-03-29 14:20:36.000', 'system.region.area.430603', '云溪区', NULL, '430603', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1880, '2020-03-29 14:20:36.000', 'system.region.area.430611', '君山区', NULL, '430611', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1881, '2020-03-29 14:20:36.000', 'system.region.area.430621', '岳阳县', NULL, '430621', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1882, '2020-03-29 14:20:36.000', 'system.region.area.430623', '华容县', NULL, '430623', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1883, '2020-03-29 14:20:36.000', 'system.region.area.430624', '湘阴县', NULL, '430624', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1884, '2020-03-29 14:20:36.000', 'system.region.area.430626', '平江县', NULL, '430626', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1885, '2020-03-29 14:20:36.000', 'system.region.area.430681', '汨罗市', NULL, '430681', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1886, '2020-03-29 14:20:36.000', 'system.region.area.430682', '临湘市', NULL, '430682', 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1887, '2020-03-29 14:20:36.000', 'system.region.city.430700', '常德市', 'area', '430700', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1888, '2020-03-29 14:20:36.000', 'system.region.area.430702', '武陵区', NULL, '430702', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1889, '2020-03-29 14:20:36.000', 'system.region.area.430703', '鼎城区', NULL, '430703', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1890, '2020-03-29 14:20:36.000', 'system.region.area.430721', '安乡县', NULL, '430721', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1891, '2020-03-29 14:20:36.000', 'system.region.area.430722', '汉寿县', NULL, '430722', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1892, '2020-03-29 14:20:36.000', 'system.region.area.430723', '澧县', NULL, '430723', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1893, '2020-03-29 14:20:36.000', 'system.region.area.430724', '临澧县', NULL, '430724', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1894, '2020-03-29 14:20:36.000', 'system.region.area.430725', '桃源县', NULL, '430725', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1895, '2020-03-29 14:20:36.000', 'system.region.area.430726', '石门县', NULL, '430726', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1896, '2020-03-29 14:20:36.000', 'system.region.area.430781', '津市市', NULL, '430781', 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1897, '2020-03-29 14:20:36.000', 'system.region.city.430800', '张家界市', 'area', '430800', 1, 15, 1824, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1898, '2020-03-29 14:20:36.000', 'system.region.area.430802', '永定区', NULL, '430802', 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1899, '2020-03-29 14:20:36.000', 'system.region.area.430811', '武陵源区', NULL, '430811', 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1900, '2020-03-29 14:20:36.000', 'system.region.area.430821', '慈利县', NULL, '430821', 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1901, '2020-03-29 14:20:36.000', 'system.region.area.430822', '桑植县', NULL, '430822', 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1902, '2020-03-29 14:20:36.000', 'system.region.city.430900', '益阳市', 'area', '430900', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1903, '2020-03-29 14:20:36.000', 'system.region.area.430902', '资阳区', NULL, '430902', 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1904, '2020-03-29 14:20:36.000', 'system.region.area.430903', '赫山区', NULL, '430903', 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1905, '2020-03-29 14:20:36.000', 'system.region.area.430921', '南县', NULL, '430921', 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1906, '2020-03-29 14:20:36.000', 'system.region.area.430922', '桃江县', NULL, '430922', 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1907, '2020-03-29 14:20:36.000', 'system.region.area.430923', '安化县', NULL, '430923', 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1908, '2020-03-29 14:20:36.000', 'system.region.area.430981', '沅江市', NULL, '430981', 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1909, '2020-03-29 14:20:36.000', 'system.region.city.431000', '郴州市', 'area', '431000', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1910, '2020-03-29 14:20:36.000', 'system.region.area.431002', '北湖区', NULL, '431002', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1911, '2020-03-29 14:20:36.000', 'system.region.area.431003', '苏仙区', NULL, '431003', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1912, '2020-03-29 14:20:36.000', 'system.region.area.431021', '桂阳县', NULL, '431021', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1913, '2020-03-29 14:20:36.000', 'system.region.area.431022', '宜章县', NULL, '431022', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1914, '2020-03-29 14:20:36.000', 'system.region.area.431023', '永兴县', NULL, '431023', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1915, '2020-03-29 14:20:36.000', 'system.region.area.431024', '嘉禾县', NULL, '431024', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1916, '2020-03-29 14:20:36.000', 'system.region.area.431025', '临武县', NULL, '431025', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1917, '2020-03-29 14:20:36.000', 'system.region.area.431026', '汝城县', NULL, '431026', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1918, '2020-03-29 14:20:36.000', 'system.region.area.431027', '桂东县', NULL, '431027', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1919, '2020-03-29 14:20:36.000', 'system.region.area.431028', '安仁县', NULL, '431028', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1920, '2020-03-29 14:20:36.000', 'system.region.area.431081', '资兴市', NULL, '431081', 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1921, '2020-03-29 14:20:36.000', 'system.region.city.431100', '永州市', 'area', '431100', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1922, '2020-03-29 14:20:36.000', 'system.region.area.431102', '零陵区', NULL, '431102', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1923, '2020-03-29 14:20:36.000', 'system.region.area.431103', '冷水滩区', NULL, '431103', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1924, '2020-03-29 14:20:36.000', 'system.region.area.431121', '祁阳县', NULL, '431121', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1925, '2020-03-29 14:20:36.000', 'system.region.area.431122', '东安县', NULL, '431122', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1926, '2020-03-29 14:20:36.000', 'system.region.area.431123', '双牌县', NULL, '431123', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1927, '2020-03-29 14:20:36.000', 'system.region.area.431124', '道县', NULL, '431124', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1928, '2020-03-29 14:20:36.000', 'system.region.area.431125', '江永县', NULL, '431125', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1929, '2020-03-29 14:20:36.000', 'system.region.area.431126', '宁远县', NULL, '431126', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1930, '2020-03-29 14:20:36.000', 'system.region.area.431127', '蓝山县', NULL, '431127', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1931, '2020-03-29 14:20:36.000', 'system.region.area.431128', '新田县', NULL, '431128', 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1932, '2020-03-29 14:20:36.000', 'system.region.area.431129', '江华瑶族自治县', NULL, '431129', 1, 16, 1921, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1933, '2020-03-29 14:20:36.000', 'system.region.city.431200', '怀化市', 'area', '431200', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1934, '2020-03-29 14:20:36.000', 'system.region.area.431202', '鹤城区', NULL, '431202', 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1935, '2020-03-29 14:20:36.000', 'system.region.area.431221', '中方县', NULL, '431221', 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1936, '2020-03-29 14:20:36.000', 'system.region.area.431222', '沅陵县', NULL, '431222', 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1937, '2020-03-29 14:20:36.000', 'system.region.area.431223', '辰溪县', NULL, '431223', 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1938, '2020-03-29 14:20:36.000', 'system.region.area.431224', '溆浦县', NULL, '431224', 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1939, '2020-03-29 14:20:36.000', 'system.region.area.431225', '会同县', NULL, '431225', 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1940, '2020-03-29 14:20:36.000', 'system.region.area.431226', '麻阳苗族自治县', NULL, '431226', 1, 16, 1933, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1941, '2020-03-29 14:20:36.000', 'system.region.area.431227', '新晃侗族自治县', NULL, '431227', 1, 16, 1933, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1942, '2020-03-29 14:20:36.000', 'system.region.area.431228', '芷江侗族自治县', NULL, '431228', 1, 16, 1933, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1943, '2020-03-29 14:20:36.000', 'system.region.area.431229', '靖州苗族侗族自治县', NULL, '431229', 1, 16, 1933, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1944, '2020-03-29 14:20:36.000', 'system.region.area.431230', '通道侗族自治县', NULL, '431230', 1, 16, 1933, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1945, '2020-03-29 14:20:36.000', 'system.region.area.431281', '洪江市', NULL, '431281', 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1946, '2020-03-29 14:20:36.000', 'system.region.city.431300', '娄底市', 'area', '431300', 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1947, '2020-03-29 14:20:36.000', 'system.region.area.431302', '娄星区', NULL, '431302', 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1948, '2020-03-29 14:20:36.000', 'system.region.area.431321', '双峰县', NULL, '431321', 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1949, '2020-03-29 14:20:36.000', 'system.region.area.431322', '新化县', NULL, '431322', 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1950, '2020-03-29 14:20:36.000', 'system.region.area.431381', '冷水江市', NULL, '431381', 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1951, '2020-03-29 14:20:36.000', 'system.region.area.431382', '涟源市', NULL, '431382', 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1952, '2020-03-29 14:20:36.000', 'system.region.city.433100', '湘西土家族苗族自治州', 'area', '433100', 1, 15, 1824, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1953, '2020-03-29 14:20:36.000', 'system.region.area.433101', '吉首市', NULL, '433101', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1954, '2020-03-29 14:20:36.000', 'system.region.area.433122', '泸溪县', NULL, '433122', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1955, '2020-03-29 14:20:36.000', 'system.region.area.433123', '凤凰县', NULL, '433123', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1956, '2020-03-29 14:20:36.000', 'system.region.area.433124', '花垣县', NULL, '433124', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1957, '2020-03-29 14:20:36.000', 'system.region.area.433125', '保靖县', NULL, '433125', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1958, '2020-03-29 14:20:36.000', 'system.region.area.433126', '古丈县', NULL, '433126', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1959, '2020-03-29 14:20:36.000', 'system.region.area.433127', '永顺县', NULL, '433127', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1960, '2020-03-29 14:20:36.000', 'system.region.area.433130', '龙山县', NULL, '433130', 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1961, '2020-03-29 14:20:36.000', 'system.region.province.440000', '广东省', 'city', '440000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1962, '2020-03-29 14:20:36.000', 'system.region.city.440100', '广州市', 'area', '440100', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1963, '2020-03-29 14:20:36.000', 'system.region.area.440103', '荔湾区', NULL, '440103', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1964, '2020-03-29 14:20:36.000', 'system.region.area.440104', '越秀区', NULL, '440104', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1965, '2020-03-29 14:20:36.000', 'system.region.area.440105', '海珠区', NULL, '440105', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1966, '2020-03-29 14:20:36.000', 'system.region.area.440106', '天河区', NULL, '440106', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1967, '2020-03-29 14:20:36.000', 'system.region.area.440111', '白云区', NULL, '440111', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1968, '2020-03-29 14:20:36.000', 'system.region.area.440112', '黄埔区', NULL, '440112', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1969, '2020-03-29 14:20:36.000', 'system.region.area.440113', '番禺区', NULL, '440113', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1970, '2020-03-29 14:20:36.000', 'system.region.area.440114', '花都区', NULL, '440114', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1971, '2020-03-29 14:20:36.000', 'system.region.area.440115', '南沙区', NULL, '440115', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1972, '2020-03-29 14:20:36.000', 'system.region.area.440117', '从化区', NULL, '440117', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1973, '2020-03-29 14:20:36.000', 'system.region.area.440118', '增城区', NULL, '440118', 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1974, '2020-03-29 14:20:36.000', 'system.region.city.440200', '韶关市', 'area', '440200', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1975, '2020-03-29 14:20:36.000', 'system.region.area.440203', '武江区', NULL, '440203', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1976, '2020-03-29 14:20:36.000', 'system.region.area.440204', '浈江区', NULL, '440204', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1977, '2020-03-29 14:20:36.000', 'system.region.area.440205', '曲江区', NULL, '440205', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1978, '2020-03-29 14:20:36.000', 'system.region.area.440222', '始兴县', NULL, '440222', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1979, '2020-03-29 14:20:36.000', 'system.region.area.440224', '仁化县', NULL, '440224', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1980, '2020-03-29 14:20:36.000', 'system.region.area.440229', '翁源县', NULL, '440229', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1981, '2020-03-29 14:20:36.000', 'system.region.area.440232', '乳源瑶族自治县', NULL, '440232', 1, 16, 1974, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1982, '2020-03-29 14:20:36.000', 'system.region.area.440233', '新丰县', NULL, '440233', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1983, '2020-03-29 14:20:36.000', 'system.region.area.440281', '乐昌市', NULL, '440281', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1984, '2020-03-29 14:20:36.000', 'system.region.area.440282', '南雄市', NULL, '440282', 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1985, '2020-03-29 14:20:36.000', 'system.region.city.440300', '深圳市', 'area', '440300', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1986, '2020-03-29 14:20:36.000', 'system.region.area.440303', '罗湖区', NULL, '440303', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1987, '2020-03-29 14:20:36.000', 'system.region.area.440304', '福田区', NULL, '440304', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1988, '2020-03-29 14:20:36.000', 'system.region.area.440305', '南山区', NULL, '440305', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1989, '2020-03-29 14:20:36.000', 'system.region.area.440306', '宝安区', NULL, '440306', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1990, '2020-03-29 14:20:36.000', 'system.region.area.440307', '龙岗区', NULL, '440307', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1991, '2020-03-29 14:20:36.000', 'system.region.area.440308', '盐田区', NULL, '440308', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1992, '2020-03-29 14:20:36.000', 'system.region.area.440309', '龙华区', NULL, '440309', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1993, '2020-03-29 14:20:36.000', 'system.region.area.440310', '坪山区', NULL, '440310', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1994, '2020-03-29 14:20:36.000', 'system.region.area.440311', '光明区', NULL, '440311', 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1995, '2020-03-29 14:20:36.000', 'system.region.city.440400', '珠海市', 'area', '440400', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1996, '2020-03-29 14:20:36.000', 'system.region.area.440402', '香洲区', NULL, '440402', 1, 16, 1995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1997, '2020-03-29 14:20:36.000', 'system.region.area.440403', '斗门区', NULL, '440403', 1, 16, 1995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1998, '2020-03-29 14:20:36.000', 'system.region.area.440404', '金湾区', NULL, '440404', 1, 16, 1995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (1999, '2020-03-29 14:20:36.000', 'system.region.city.440500', '汕头市', 'area', '440500', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2000, '2020-03-29 14:20:36.000', 'system.region.area.440507', '龙湖区', NULL, '440507', 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2001, '2020-03-29 14:20:36.000', 'system.region.area.440511', '金平区', NULL, '440511', 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2002, '2020-03-29 14:20:36.000', 'system.region.area.440512', '濠江区', NULL, '440512', 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2003, '2020-03-29 14:20:36.000', 'system.region.area.440513', '潮阳区', NULL, '440513', 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2004, '2020-03-29 14:20:36.000', 'system.region.area.440514', '潮南区', NULL, '440514', 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2005, '2020-03-29 14:20:36.000', 'system.region.area.440515', '澄海区', NULL, '440515', 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2006, '2020-03-29 14:20:36.000', 'system.region.area.440523', '南澳县', NULL, '440523', 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2007, '2020-03-29 14:20:36.000', 'system.region.city.440600', '佛山市', 'area', '440600', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2008, '2020-03-29 14:20:36.000', 'system.region.area.440604', '禅城区', NULL, '440604', 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2009, '2020-03-29 14:20:36.000', 'system.region.area.440605', '南海区', NULL, '440605', 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2010, '2020-03-29 14:20:36.000', 'system.region.area.440606', '顺德区', NULL, '440606', 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2011, '2020-03-29 14:20:36.000', 'system.region.area.440607', '三水区', NULL, '440607', 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2012, '2020-03-29 14:20:36.000', 'system.region.area.440608', '高明区', NULL, '440608', 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2013, '2020-03-29 14:20:36.000', 'system.region.city.440700', '江门市', 'area', '440700', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2014, '2020-03-29 14:20:36.000', 'system.region.area.440703', '蓬江区', NULL, '440703', 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2015, '2020-03-29 14:20:36.000', 'system.region.area.440704', '江海区', NULL, '440704', 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2016, '2020-03-29 14:20:36.000', 'system.region.area.440705', '新会区', NULL, '440705', 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2017, '2020-03-29 14:20:36.000', 'system.region.area.440781', '台山市', NULL, '440781', 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2018, '2020-03-29 14:20:36.000', 'system.region.area.440783', '开平市', NULL, '440783', 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2019, '2020-03-29 14:20:36.000', 'system.region.area.440784', '鹤山市', NULL, '440784', 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2020, '2020-03-29 14:20:36.000', 'system.region.area.440785', '恩平市', NULL, '440785', 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2021, '2020-03-29 14:20:36.000', 'system.region.city.440800', '湛江市', 'area', '440800', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2022, '2020-03-29 14:20:36.000', 'system.region.area.440802', '赤坎区', NULL, '440802', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2023, '2020-03-29 14:20:36.000', 'system.region.area.440803', '霞山区', NULL, '440803', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2024, '2020-03-29 14:20:36.000', 'system.region.area.440804', '坡头区', NULL, '440804', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2025, '2020-03-29 14:20:36.000', 'system.region.area.440811', '麻章区', NULL, '440811', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2026, '2020-03-29 14:20:36.000', 'system.region.area.440823', '遂溪县', NULL, '440823', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2027, '2020-03-29 14:20:36.000', 'system.region.area.440825', '徐闻县', NULL, '440825', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2028, '2020-03-29 14:20:36.000', 'system.region.area.440881', '廉江市', NULL, '440881', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2029, '2020-03-29 14:20:36.000', 'system.region.area.440882', '雷州市', NULL, '440882', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2030, '2020-03-29 14:20:36.000', 'system.region.area.440883', '吴川市', NULL, '440883', 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2031, '2020-03-29 14:20:36.000', 'system.region.city.440900', '茂名市', 'area', '440900', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2032, '2020-03-29 14:20:36.000', 'system.region.area.440902', '茂南区', NULL, '440902', 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2033, '2020-03-29 14:20:36.000', 'system.region.area.440904', '电白区', NULL, '440904', 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2034, '2020-03-29 14:20:36.000', 'system.region.area.440981', '高州市', NULL, '440981', 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2035, '2020-03-29 14:20:36.000', 'system.region.area.440982', '化州市', NULL, '440982', 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2036, '2020-03-29 14:20:36.000', 'system.region.area.440983', '信宜市', NULL, '440983', 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2037, '2020-03-29 14:20:36.000', 'system.region.city.441200', '肇庆市', 'area', '441200', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2038, '2020-03-29 14:20:36.000', 'system.region.area.441202', '端州区', NULL, '441202', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2039, '2020-03-29 14:20:36.000', 'system.region.area.441203', '鼎湖区', NULL, '441203', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2040, '2020-03-29 14:20:36.000', 'system.region.area.441204', '高要区', NULL, '441204', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2041, '2020-03-29 14:20:36.000', 'system.region.area.441223', '广宁县', NULL, '441223', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2042, '2020-03-29 14:20:36.000', 'system.region.area.441224', '怀集县', NULL, '441224', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2043, '2020-03-29 14:20:36.000', 'system.region.area.441225', '封开县', NULL, '441225', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2044, '2020-03-29 14:20:36.000', 'system.region.area.441226', '德庆县', NULL, '441226', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2045, '2020-03-29 14:20:36.000', 'system.region.area.441284', '四会市', NULL, '441284', 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2046, '2020-03-29 14:20:36.000', 'system.region.city.441300', '惠州市', 'area', '441300', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2047, '2020-03-29 14:20:36.000', 'system.region.area.441302', '惠城区', NULL, '441302', 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2048, '2020-03-29 14:20:36.000', 'system.region.area.441303', '惠阳区', NULL, '441303', 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2049, '2020-03-29 14:20:36.000', 'system.region.area.441322', '博罗县', NULL, '441322', 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2050, '2020-03-29 14:20:36.000', 'system.region.area.441323', '惠东县', NULL, '441323', 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2051, '2020-03-29 14:20:36.000', 'system.region.area.441324', '龙门县', NULL, '441324', 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2052, '2020-03-29 14:20:36.000', 'system.region.city.441400', '梅州市', 'area', '441400', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2053, '2020-03-29 14:20:36.000', 'system.region.area.441402', '梅江区', NULL, '441402', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2054, '2020-03-29 14:20:36.000', 'system.region.area.441403', '梅县区', NULL, '441403', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2055, '2020-03-29 14:20:36.000', 'system.region.area.441422', '大埔县', NULL, '441422', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2056, '2020-03-29 14:20:36.000', 'system.region.area.441423', '丰顺县', NULL, '441423', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2057, '2020-03-29 14:20:36.000', 'system.region.area.441424', '五华县', NULL, '441424', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2058, '2020-03-29 14:20:36.000', 'system.region.area.441426', '平远县', NULL, '441426', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2059, '2020-03-29 14:20:36.000', 'system.region.area.441427', '蕉岭县', NULL, '441427', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2060, '2020-03-29 14:20:36.000', 'system.region.area.441481', '兴宁市', NULL, '441481', 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2061, '2020-03-29 14:20:36.000', 'system.region.city.441500', '汕尾市', 'area', '441500', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2062, '2020-03-29 14:20:36.000', 'system.region.area.441502', '城区', NULL, '441502', 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2063, '2020-03-29 14:20:36.000', 'system.region.area.441521', '海丰县', NULL, '441521', 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2064, '2020-03-29 14:20:36.000', 'system.region.area.441523', '陆河县', NULL, '441523', 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2065, '2020-03-29 14:20:36.000', 'system.region.area.441581', '陆丰市', NULL, '441581', 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2066, '2020-03-29 14:20:36.000', 'system.region.city.441600', '河源市', 'area', '441600', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2067, '2020-03-29 14:20:36.000', 'system.region.area.441602', '源城区', NULL, '441602', 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2068, '2020-03-29 14:20:36.000', 'system.region.area.441621', '紫金县', NULL, '441621', 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2069, '2020-03-29 14:20:36.000', 'system.region.area.441622', '龙川县', NULL, '441622', 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2070, '2020-03-29 14:20:36.000', 'system.region.area.441623', '连平县', NULL, '441623', 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2071, '2020-03-29 14:20:36.000', 'system.region.area.441624', '和平县', NULL, '441624', 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2072, '2020-03-29 14:20:36.000', 'system.region.area.441625', '东源县', NULL, '441625', 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2073, '2020-03-29 14:20:36.000', 'system.region.city.441700', '阳江市', 'area', '441700', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2074, '2020-03-29 14:20:36.000', 'system.region.area.441702', '江城区', NULL, '441702', 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2075, '2020-03-29 14:20:36.000', 'system.region.area.441704', '阳东区', NULL, '441704', 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2076, '2020-03-29 14:20:36.000', 'system.region.area.441721', '阳西县', NULL, '441721', 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2077, '2020-03-29 14:20:36.000', 'system.region.area.441781', '阳春市', NULL, '441781', 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2078, '2020-03-29 14:20:36.000', 'system.region.city.441800', '清远市', 'area', '441800', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2079, '2020-03-29 14:20:36.000', 'system.region.area.441802', '清城区', NULL, '441802', 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2080, '2020-03-29 14:20:36.000', 'system.region.area.441803', '清新区', NULL, '441803', 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2081, '2020-03-29 14:20:36.000', 'system.region.area.441821', '佛冈县', NULL, '441821', 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2082, '2020-03-29 14:20:36.000', 'system.region.area.441823', '阳山县', NULL, '441823', 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2083, '2020-03-29 14:20:36.000', 'system.region.area.441825', '连山壮族瑶族自治县', NULL, '441825', 1, 16, 2078, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2084, '2020-03-29 14:20:36.000', 'system.region.area.441826', '连南瑶族自治县', NULL, '441826', 1, 16, 2078, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2085, '2020-03-29 14:20:36.000', 'system.region.area.441881', '英德市', NULL, '441881', 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2086, '2020-03-29 14:20:36.000', 'system.region.area.441882', '连州市', NULL, '441882', 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2087, '2020-03-29 14:20:36.000', 'system.region.city.441900', '东莞市', 'area', '441900', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2088, '2020-03-29 14:20:36.000', 'system.region.city.442000', '中山市', 'area', '442000', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2089, '2020-03-29 14:20:36.000', 'system.region.city.445100', '潮州市', 'area', '445100', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2090, '2020-03-29 14:20:36.000', 'system.region.area.445102', '湘桥区', NULL, '445102', 1, 16, 2089, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2091, '2020-03-29 14:20:36.000', 'system.region.area.445103', '潮安区', NULL, '445103', 1, 16, 2089, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2092, '2020-03-29 14:20:36.000', 'system.region.area.445122', '饶平县', NULL, '445122', 1, 16, 2089, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2093, '2020-03-29 14:20:36.000', 'system.region.city.445200', '揭阳市', 'area', '445200', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2094, '2020-03-29 14:20:36.000', 'system.region.area.445202', '榕城区', NULL, '445202', 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2095, '2020-03-29 14:20:36.000', 'system.region.area.445203', '揭东区', NULL, '445203', 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2096, '2020-03-29 14:20:36.000', 'system.region.area.445222', '揭西县', NULL, '445222', 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2097, '2020-03-29 14:20:36.000', 'system.region.area.445224', '惠来县', NULL, '445224', 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2098, '2020-03-29 14:20:36.000', 'system.region.area.445281', '普宁市', NULL, '445281', 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2099, '2020-03-29 14:20:36.000', 'system.region.city.445300', '云浮市', 'area', '445300', 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2100, '2020-03-29 14:20:36.000', 'system.region.area.445302', '云城区', NULL, '445302', 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2101, '2020-03-29 14:20:36.000', 'system.region.area.445303', '云安区', NULL, '445303', 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2102, '2020-03-29 14:20:36.000', 'system.region.area.445321', '新兴县', NULL, '445321', 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2103, '2020-03-29 14:20:36.000', 'system.region.area.445322', '郁南县', NULL, '445322', 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2104, '2020-03-29 14:20:36.000', 'system.region.area.445381', '罗定市', NULL, '445381', 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2105, '2020-03-29 14:20:36.000', 'system.region.province.450000', '广西壮族自治区', 'city', '450000', 1, 14, NULL,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2106, '2020-03-29 14:20:36.000', 'system.region.city.450100', '南宁市', 'area', '450100', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2107, '2020-03-29 14:20:36.000', 'system.region.area.450102', '兴宁区', NULL, '450102', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2108, '2020-03-29 14:20:36.000', 'system.region.area.450103', '青秀区', NULL, '450103', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2109, '2020-03-29 14:20:36.000', 'system.region.area.450105', '江南区', NULL, '450105', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2110, '2020-03-29 14:20:36.000', 'system.region.area.450107', '西乡塘区', NULL, '450107', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2111, '2020-03-29 14:20:36.000', 'system.region.area.450108', '良庆区', NULL, '450108', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2112, '2020-03-29 14:20:36.000', 'system.region.area.450109', '邕宁区', NULL, '450109', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2113, '2020-03-29 14:20:36.000', 'system.region.area.450110', '武鸣区', NULL, '450110', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2114, '2020-03-29 14:20:36.000', 'system.region.area.450123', '隆安县', NULL, '450123', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2115, '2020-03-29 14:20:36.000', 'system.region.area.450124', '马山县', NULL, '450124', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2116, '2020-03-29 14:20:36.000', 'system.region.area.450125', '上林县', NULL, '450125', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2117, '2020-03-29 14:20:36.000', 'system.region.area.450126', '宾阳县', NULL, '450126', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2118, '2020-03-29 14:20:36.000', 'system.region.area.450127', '横县', NULL, '450127', 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2119, '2020-03-29 14:20:36.000', 'system.region.city.450200', '柳州市', 'area', '450200', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2120, '2020-03-29 14:20:36.000', 'system.region.area.450202', '城中区', NULL, '450202', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2121, '2020-03-29 14:20:36.000', 'system.region.area.450203', '鱼峰区', NULL, '450203', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2122, '2020-03-29 14:20:36.000', 'system.region.area.450204', '柳南区', NULL, '450204', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2123, '2020-03-29 14:20:36.000', 'system.region.area.450205', '柳北区', NULL, '450205', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2124, '2020-03-29 14:20:36.000', 'system.region.area.450206', '柳江区', NULL, '450206', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2125, '2020-03-29 14:20:36.000', 'system.region.area.450222', '柳城县', NULL, '450222', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2126, '2020-03-29 14:20:36.000', 'system.region.area.450223', '鹿寨县', NULL, '450223', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2127, '2020-03-29 14:20:36.000', 'system.region.area.450224', '融安县', NULL, '450224', 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2128, '2020-03-29 14:20:36.000', 'system.region.area.450225', '融水苗族自治县', NULL, '450225', 1, 16, 2119, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2129, '2020-03-29 14:20:36.000', 'system.region.area.450226', '三江侗族自治县', NULL, '450226', 1, 16, 2119, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2130, '2020-03-29 14:20:36.000', 'system.region.city.450300', '桂林市', 'area', '450300', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2131, '2020-03-29 14:20:36.000', 'system.region.area.450302', '秀峰区', NULL, '450302', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2132, '2020-03-29 14:20:36.000', 'system.region.area.450303', '叠彩区', NULL, '450303', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2133, '2020-03-29 14:20:36.000', 'system.region.area.450304', '象山区', NULL, '450304', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2134, '2020-03-29 14:20:36.000', 'system.region.area.450305', '七星区', NULL, '450305', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2135, '2020-03-29 14:20:36.000', 'system.region.area.450311', '雁山区', NULL, '450311', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2136, '2020-03-29 14:20:36.000', 'system.region.area.450312', '临桂区', NULL, '450312', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2137, '2020-03-29 14:20:36.000', 'system.region.area.450321', '阳朔县', NULL, '450321', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2138, '2020-03-29 14:20:36.000', 'system.region.area.450323', '灵川县', NULL, '450323', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2139, '2020-03-29 14:20:36.000', 'system.region.area.450324', '全州县', NULL, '450324', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2140, '2020-03-29 14:20:36.000', 'system.region.area.450325', '兴安县', NULL, '450325', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2141, '2020-03-29 14:20:36.000', 'system.region.area.450326', '永福县', NULL, '450326', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2142, '2020-03-29 14:20:36.000', 'system.region.area.450327', '灌阳县', NULL, '450327', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2143, '2020-03-29 14:20:36.000', 'system.region.area.450328', '龙胜各族自治县', NULL, '450328', 1, 16, 2130, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2144, '2020-03-29 14:20:36.000', 'system.region.area.450329', '资源县', NULL, '450329', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2145, '2020-03-29 14:20:36.000', 'system.region.area.450330', '平乐县', NULL, '450330', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2146, '2020-03-29 14:20:36.000', 'system.region.area.450381', '荔浦市', NULL, '450381', 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2147, '2020-03-29 14:20:36.000', 'system.region.area.450332', '恭城瑶族自治县', NULL, '450332', 1, 16, 2130, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2148, '2020-03-29 14:20:36.000', 'system.region.city.450400', '梧州市', 'area', '450400', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2149, '2020-03-29 14:20:36.000', 'system.region.area.450403', '万秀区', NULL, '450403', 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2150, '2020-03-29 14:20:36.000', 'system.region.area.450405', '长洲区', NULL, '450405', 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2151, '2020-03-29 14:20:36.000', 'system.region.area.450406', '龙圩区', NULL, '450406', 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2152, '2020-03-29 14:20:36.000', 'system.region.area.450421', '苍梧县', NULL, '450421', 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2153, '2020-03-29 14:20:36.000', 'system.region.area.450422', '藤县', NULL, '450422', 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2154, '2020-03-29 14:20:36.000', 'system.region.area.450423', '蒙山县', NULL, '450423', 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2155, '2020-03-29 14:20:36.000', 'system.region.area.450481', '岑溪市', NULL, '450481', 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2156, '2020-03-29 14:20:36.000', 'system.region.city.450500', '北海市', 'area', '450500', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2157, '2020-03-29 14:20:36.000', 'system.region.area.450502', '海城区', NULL, '450502', 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2158, '2020-03-29 14:20:36.000', 'system.region.area.450503', '银海区', NULL, '450503', 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2159, '2020-03-29 14:20:36.000', 'system.region.area.450512', '铁山港区', NULL, '450512', 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2160, '2020-03-29 14:20:36.000', 'system.region.area.450521', '合浦县', NULL, '450521', 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2161, '2020-03-29 14:20:36.000', 'system.region.city.450600', '防城港市', 'area', '450600', 1, 15, 2105, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2162, '2020-03-29 14:20:36.000', 'system.region.area.450602', '港口区', NULL, '450602', 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2163, '2020-03-29 14:20:36.000', 'system.region.area.450603', '防城区', NULL, '450603', 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2164, '2020-03-29 14:20:36.000', 'system.region.area.450621', '上思县', NULL, '450621', 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2165, '2020-03-29 14:20:36.000', 'system.region.area.450681', '东兴市', NULL, '450681', 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2166, '2020-03-29 14:20:36.000', 'system.region.city.450700', '钦州市', 'area', '450700', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2167, '2020-03-29 14:20:36.000', 'system.region.area.450702', '钦南区', NULL, '450702', 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2168, '2020-03-29 14:20:36.000', 'system.region.area.450703', '钦北区', NULL, '450703', 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2169, '2020-03-29 14:20:36.000', 'system.region.area.450721', '灵山县', NULL, '450721', 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2170, '2020-03-29 14:20:36.000', 'system.region.area.450722', '浦北县', NULL, '450722', 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2171, '2020-03-29 14:20:36.000', 'system.region.city.450800', '贵港市', 'area', '450800', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2172, '2020-03-29 14:20:36.000', 'system.region.area.450802', '港北区', NULL, '450802', 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2173, '2020-03-29 14:20:36.000', 'system.region.area.450803', '港南区', NULL, '450803', 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2174, '2020-03-29 14:20:36.000', 'system.region.area.450804', '覃塘区', NULL, '450804', 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2175, '2020-03-29 14:20:36.000', 'system.region.area.450821', '平南县', NULL, '450821', 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2176, '2020-03-29 14:20:36.000', 'system.region.area.450881', '桂平市', NULL, '450881', 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2177, '2020-03-29 14:20:36.000', 'system.region.city.450900', '玉林市', 'area', '450900', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2178, '2020-03-29 14:20:36.000', 'system.region.area.450902', '玉州区', NULL, '450902', 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2179, '2020-03-29 14:20:36.000', 'system.region.area.450903', '福绵区', NULL, '450903', 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2180, '2020-03-29 14:20:36.000', 'system.region.area.450921', '容县', NULL, '450921', 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2181, '2020-03-29 14:20:36.000', 'system.region.area.450922', '陆川县', NULL, '450922', 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2182, '2020-03-29 14:20:36.000', 'system.region.area.450923', '博白县', NULL, '450923', 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2183, '2020-03-29 14:20:36.000', 'system.region.area.450924', '兴业县', NULL, '450924', 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2184, '2020-03-29 14:20:36.000', 'system.region.area.450981', '北流市', NULL, '450981', 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2185, '2020-03-29 14:20:36.000', 'system.region.city.451000', '百色市', 'area', '451000', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2186, '2020-03-29 14:20:36.000', 'system.region.area.451002', '右江区', NULL, '451002', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2187, '2020-03-29 14:20:36.000', 'system.region.area.451003', '田阳区', NULL, '451003', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2188, '2020-03-29 14:20:36.000', 'system.region.area.451022', '田东县', NULL, '451022', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2189, '2020-03-29 14:20:36.000', 'system.region.area.451024', '德保县', NULL, '451024', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2190, '2020-03-29 14:20:36.000', 'system.region.area.451026', '那坡县', NULL, '451026', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2191, '2020-03-29 14:20:36.000', 'system.region.area.451027', '凌云县', NULL, '451027', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2192, '2020-03-29 14:20:36.000', 'system.region.area.451028', '乐业县', NULL, '451028', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2193, '2020-03-29 14:20:36.000', 'system.region.area.451029', '田林县', NULL, '451029', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2194, '2020-03-29 14:20:36.000', 'system.region.area.451030', '西林县', NULL, '451030', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2195, '2020-03-29 14:20:36.000', 'system.region.area.451031', '隆林各族自治县', NULL, '451031', 1, 16, 2185, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2196, '2020-03-29 14:20:36.000', 'system.region.area.451081', '靖西市', NULL, '451081', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2197, '2020-03-29 14:20:36.000', 'system.region.area.451082', '平果市', NULL, '451082', 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2198, '2020-03-29 14:20:36.000', 'system.region.city.451100', '贺州市', 'area', '451100', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2199, '2020-03-29 14:20:36.000', 'system.region.area.451102', '八步区', NULL, '451102', 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2200, '2020-03-29 14:20:36.000', 'system.region.area.451103', '平桂区', NULL, '451103', 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2201, '2020-03-29 14:20:36.000', 'system.region.area.451121', '昭平县', NULL, '451121', 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2202, '2020-03-29 14:20:36.000', 'system.region.area.451122', '钟山县', NULL, '451122', 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2203, '2020-03-29 14:20:36.000', 'system.region.area.451123', '富川瑶族自治县', NULL, '451123', 1, 16, 2198, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2204, '2020-03-29 14:20:36.000', 'system.region.city.451200', '河池市', 'area', '451200', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2205, '2020-03-29 14:20:36.000', 'system.region.area.451202', '金城江区', NULL, '451202', 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2206, '2020-03-29 14:20:36.000', 'system.region.area.451203', '宜州区', NULL, '451203', 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2207, '2020-03-29 14:20:36.000', 'system.region.area.451221', '南丹县', NULL, '451221', 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2208, '2020-03-29 14:20:36.000', 'system.region.area.451222', '天峨县', NULL, '451222', 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2209, '2020-03-29 14:20:36.000', 'system.region.area.451223', '凤山县', NULL, '451223', 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2210, '2020-03-29 14:20:36.000', 'system.region.area.451224', '东兰县', NULL, '451224', 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2211, '2020-03-29 14:20:36.000', 'system.region.area.451225', '罗城仫佬族自治县', NULL, '451225', 1, 16, 2204, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2212, '2020-03-29 14:20:36.000', 'system.region.area.451226', '环江毛南族自治县', NULL, '451226', 1, 16, 2204, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2213, '2020-03-29 14:20:36.000', 'system.region.area.451227', '巴马瑶族自治县', NULL, '451227', 1, 16, 2204, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2214, '2020-03-29 14:20:36.000', 'system.region.area.451228', '都安瑶族自治县', NULL, '451228', 1, 16, 2204, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2215, '2020-03-29 14:20:36.000', 'system.region.area.451229', '大化瑶族自治县', NULL, '451229', 1, 16, 2204, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2216, '2020-03-29 14:20:36.000', 'system.region.city.451300', '来宾市', 'area', '451300', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2217, '2020-03-29 14:20:36.000', 'system.region.area.451302', '兴宾区', NULL, '451302', 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2218, '2020-03-29 14:20:36.000', 'system.region.area.451321', '忻城县', NULL, '451321', 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2219, '2020-03-29 14:20:36.000', 'system.region.area.451322', '象州县', NULL, '451322', 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2220, '2020-03-29 14:20:36.000', 'system.region.area.451323', '武宣县', NULL, '451323', 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2221, '2020-03-29 14:20:36.000', 'system.region.area.451324', '金秀瑶族自治县', NULL, '451324', 1, 16, 2216, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2222, '2020-03-29 14:20:36.000', 'system.region.area.451381', '合山市', NULL, '451381', 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2223, '2020-03-29 14:20:36.000', 'system.region.city.451400', '崇左市', 'area', '451400', 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2224, '2020-03-29 14:20:36.000', 'system.region.area.451402', '江州区', NULL, '451402', 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2225, '2020-03-29 14:20:36.000', 'system.region.area.451421', '扶绥县', NULL, '451421', 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2226, '2020-03-29 14:20:36.000', 'system.region.area.451422', '宁明县', NULL, '451422', 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2227, '2020-03-29 14:20:36.000', 'system.region.area.451423', '龙州县', NULL, '451423', 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2228, '2020-03-29 14:20:36.000', 'system.region.area.451424', '大新县', NULL, '451424', 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2229, '2020-03-29 14:20:36.000', 'system.region.area.451425', '天等县', NULL, '451425', 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2230, '2020-03-29 14:20:36.000', 'system.region.area.451481', '凭祥市', NULL, '451481', 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2231, '2020-03-29 14:20:36.000', 'system.region.province.460000', '海南省', 'city', '460000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2232, '2020-03-29 14:20:36.000', 'system.region.city.460100', '海口市', 'area', '460100', 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2233, '2020-03-29 14:20:36.000', 'system.region.area.460105', '秀英区', NULL, '460105', 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2234, '2020-03-29 14:20:36.000', 'system.region.area.460106', '龙华区', NULL, '460106', 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2235, '2020-03-29 14:20:36.000', 'system.region.area.460107', '琼山区', NULL, '460107', 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2236, '2020-03-29 14:20:36.000', 'system.region.area.460108', '美兰区', NULL, '460108', 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2237, '2020-03-29 14:20:36.000', 'system.region.city.460200', '三亚市', 'area', '460200', 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2238, '2020-03-29 14:20:36.000', 'system.region.area.460202', '海棠区', NULL, '460202', 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2239, '2020-03-29 14:20:36.000', 'system.region.area.460203', '吉阳区', NULL, '460203', 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2240, '2020-03-29 14:20:36.000', 'system.region.area.460204', '天涯区', NULL, '460204', 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2241, '2020-03-29 14:20:36.000', 'system.region.area.460205', '崖州区', NULL, '460205', 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2242, '2020-03-29 14:20:36.000', 'system.region.city.460300', '三沙市', 'area', '460300', 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2243, '2020-03-29 14:20:36.000', 'system.region.city.460400', '儋州市', 'area', '460400', 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2244, '2020-03-29 14:20:36.000', 'system.region.area.469001', '五指山市', NULL, '469001', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2245, '2020-03-29 14:20:36.000', 'system.region.area.469002', '琼海市', NULL, '469002', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2246, '2020-03-29 14:20:36.000', 'system.region.area.469005', '文昌市', NULL, '469005', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2247, '2020-03-29 14:20:36.000', 'system.region.area.469006', '万宁市', NULL, '469006', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2248, '2020-03-29 14:20:36.000', 'system.region.area.469007', '东方市', NULL, '469007', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2249, '2020-03-29 14:20:36.000', 'system.region.area.469021', '定安县', NULL, '469021', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2250, '2020-03-29 14:20:36.000', 'system.region.area.469022', '屯昌县', NULL, '469022', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2251, '2020-03-29 14:20:36.000', 'system.region.area.469023', '澄迈县', NULL, '469023', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2252, '2020-03-29 14:20:36.000', 'system.region.area.469024', '临高县', NULL, '469024', 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2253, '2020-03-29 14:20:36.000', 'system.region.area.469025', '白沙黎族自治县', NULL, '469025', 1, 16, 2243, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2254, '2020-03-29 14:20:36.000', 'system.region.area.469026', '昌江黎族自治县', NULL, '469026', 1, 16, 2243, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2255, '2020-03-29 14:20:36.000', 'system.region.area.469027', '乐东黎族自治县', NULL, '469027', 1, 16, 2243, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2256, '2020-03-29 14:20:36.000', 'system.region.area.469028', '陵水黎族自治县', NULL, '469028', 1, 16, 2243, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2257, '2020-03-29 14:20:36.000', 'system.region.area.469029', '保亭黎族苗族自治县', NULL, '469029', 1, 16, 2243, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2258, '2020-03-29 14:20:36.000', 'system.region.area.469030', '琼中黎族苗族自治县', NULL, '469030', 1, 16, 2243, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2259, '2020-03-29 14:20:36.000', 'system.region.province.500000', '重庆市', 'area', '500000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2260, '2020-03-29 14:20:36.000', 'system.region.area.500101', '万州区', NULL, '500101', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2261, '2020-03-29 14:20:36.000', 'system.region.area.500102', '涪陵区', NULL, '500102', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2262, '2020-03-29 14:20:36.000', 'system.region.area.500103', '渝中区', NULL, '500103', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2263, '2020-03-29 14:20:36.000', 'system.region.area.500104', '大渡口区', NULL, '500104', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2264, '2020-03-29 14:20:36.000', 'system.region.area.500105', '江北区', NULL, '500105', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2265, '2020-03-29 14:20:36.000', 'system.region.area.500106', '沙坪坝区', NULL, '500106', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2266, '2020-03-29 14:20:36.000', 'system.region.area.500107', '九龙坡区', NULL, '500107', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2267, '2020-03-29 14:20:36.000', 'system.region.area.500108', '南岸区', NULL, '500108', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2268, '2020-03-29 14:20:36.000', 'system.region.area.500109', '北碚区', NULL, '500109', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2269, '2020-03-29 14:20:36.000', 'system.region.area.500110', '綦江区', NULL, '500110', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2270, '2020-03-29 14:20:36.000', 'system.region.area.500111', '大足区', NULL, '500111', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2271, '2020-03-29 14:20:36.000', 'system.region.area.500112', '渝北区', NULL, '500112', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2272, '2020-03-29 14:20:36.000', 'system.region.area.500113', '巴南区', NULL, '500113', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2273, '2020-03-29 14:20:36.000', 'system.region.area.500114', '黔江区', NULL, '500114', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2274, '2020-03-29 14:20:36.000', 'system.region.area.500115', '长寿区', NULL, '500115', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2275, '2020-03-29 14:20:36.000', 'system.region.area.500116', '江津区', NULL, '500116', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2276, '2020-03-29 14:20:36.000', 'system.region.area.500117', '合川区', NULL, '500117', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2277, '2020-03-29 14:20:36.000', 'system.region.area.500118', '永川区', NULL, '500118', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2278, '2020-03-29 14:20:36.000', 'system.region.area.500119', '南川区', NULL, '500119', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2279, '2020-03-29 14:20:36.000', 'system.region.area.500120', '璧山区', NULL, '500120', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2280, '2020-03-29 14:20:36.000', 'system.region.area.500151', '铜梁区', NULL, '500151', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2281, '2020-03-29 14:20:36.000', 'system.region.area.500152', '潼南区', NULL, '500152', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2282, '2020-03-29 14:20:36.000', 'system.region.area.500153', '荣昌区', NULL, '500153', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2283, '2020-03-29 14:20:36.000', 'system.region.area.500154', '开州区', NULL, '500154', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2284, '2020-03-29 14:20:36.000', 'system.region.area.500155', '梁平区', NULL, '500155', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2285, '2020-03-29 14:20:36.000', 'system.region.area.500156', '武隆区', NULL, '500156', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2286, '2020-03-29 14:20:36.000', 'system.region.area.500229', '城口县', NULL, '500229', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2287, '2020-03-29 14:20:36.000', 'system.region.area.500230', '丰都县', NULL, '500230', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2288, '2020-03-29 14:20:36.000', 'system.region.area.500231', '垫江县', NULL, '500231', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2289, '2020-03-29 14:20:36.000', 'system.region.area.500233', '忠县', NULL, '500233', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2290, '2020-03-29 14:20:36.000', 'system.region.area.500235', '云阳县', NULL, '500235', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2291, '2020-03-29 14:20:36.000', 'system.region.area.500236', '奉节县', NULL, '500236', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2292, '2020-03-29 14:20:36.000', 'system.region.area.500237', '巫山县', NULL, '500237', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2293, '2020-03-29 14:20:36.000', 'system.region.area.500238', '巫溪县', NULL, '500238', 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2294, '2020-03-29 14:20:36.000', 'system.region.area.500240', '石柱土家族自治县', NULL, '500240', 1, 16, 2259, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2295, '2020-03-29 14:20:36.000', 'system.region.area.500241', '秀山土家族苗族自治县', NULL, '500241', 1, 16, 2259, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2296, '2020-03-29 14:20:36.000', 'system.region.area.500242', '酉阳土家族苗族自治县', NULL, '500242', 1, 16, 2259, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2297, '2020-03-29 14:20:36.000', 'system.region.area.500243', '彭水苗族土家族自治县', NULL, '500243', 1, 16, 2259, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2298, '2020-03-29 14:20:36.000', 'system.region.province.510000', '四川省', 'city', '510000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2299, '2020-03-29 14:20:36.000', 'system.region.city.510100', '成都市', 'area', '510100', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2300, '2020-03-29 14:20:36.000', 'system.region.area.510104', '锦江区', NULL, '510104', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2301, '2020-03-29 14:20:36.000', 'system.region.area.510105', '青羊区', NULL, '510105', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2302, '2020-03-29 14:20:36.000', 'system.region.area.510106', '金牛区', NULL, '510106', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2303, '2020-03-29 14:20:36.000', 'system.region.area.510107', '武侯区', NULL, '510107', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2304, '2020-03-29 14:20:36.000', 'system.region.area.510108', '成华区', NULL, '510108', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2305, '2020-03-29 14:20:36.000', 'system.region.area.510112', '龙泉驿区', NULL, '510112', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2306, '2020-03-29 14:20:36.000', 'system.region.area.510113', '青白江区', NULL, '510113', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2307, '2020-03-29 14:20:36.000', 'system.region.area.510114', '新都区', NULL, '510114', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2308, '2020-03-29 14:20:36.000', 'system.region.area.510115', '温江区', NULL, '510115', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2309, '2020-03-29 14:20:36.000', 'system.region.area.510116', '双流区', NULL, '510116', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2310, '2020-03-29 14:20:36.000', 'system.region.area.510117', '郫都区', NULL, '510117', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2311, '2020-03-29 14:20:36.000', 'system.region.area.510121', '金堂县', NULL, '510121', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2312, '2020-03-29 14:20:36.000', 'system.region.area.510129', '大邑县', NULL, '510129', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2313, '2020-03-29 14:20:36.000', 'system.region.area.510131', '蒲江县', NULL, '510131', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2314, '2020-03-29 14:20:36.000', 'system.region.area.510132', '新津县', NULL, '510132', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2315, '2020-03-29 14:20:36.000', 'system.region.area.510181', '都江堰市', NULL, '510181', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2316, '2020-03-29 14:20:36.000', 'system.region.area.510182', '彭州市', NULL, '510182', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2317, '2020-03-29 14:20:36.000', 'system.region.area.510183', '邛崃市', NULL, '510183', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2318, '2020-03-29 14:20:36.000', 'system.region.area.510184', '崇州市', NULL, '510184', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2319, '2020-03-29 14:20:36.000', 'system.region.area.510185', '简阳市', NULL, '510185', 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2320, '2020-03-29 14:20:36.000', 'system.region.city.510300', '自贡市', 'area', '510300', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2321, '2020-03-29 14:20:36.000', 'system.region.area.510302', '自流井区', NULL, '510302', 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2322, '2020-03-29 14:20:36.000', 'system.region.area.510303', '贡井区', NULL, '510303', 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2323, '2020-03-29 14:20:36.000', 'system.region.area.510304', '大安区', NULL, '510304', 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2324, '2020-03-29 14:20:36.000', 'system.region.area.510311', '沿滩区', NULL, '510311', 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2325, '2020-03-29 14:20:36.000', 'system.region.area.510321', '荣县', NULL, '510321', 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2326, '2020-03-29 14:20:36.000', 'system.region.area.510322', '富顺县', NULL, '510322', 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2327, '2020-03-29 14:20:36.000', 'system.region.city.510400', '攀枝花市', 'area', '510400', 1, 15, 2298, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2328, '2020-03-29 14:20:36.000', 'system.region.area.510402', '东区', NULL, '510402', 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2329, '2020-03-29 14:20:36.000', 'system.region.area.510403', '西区', NULL, '510403', 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2330, '2020-03-29 14:20:36.000', 'system.region.area.510411', '仁和区', NULL, '510411', 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2331, '2020-03-29 14:20:36.000', 'system.region.area.510421', '米易县', NULL, '510421', 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2332, '2020-03-29 14:20:36.000', 'system.region.area.510422', '盐边县', NULL, '510422', 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2333, '2020-03-29 14:20:36.000', 'system.region.city.510500', '泸州市', 'area', '510500', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2334, '2020-03-29 14:20:36.000', 'system.region.area.510502', '江阳区', NULL, '510502', 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2335, '2020-03-29 14:20:36.000', 'system.region.area.510503', '纳溪区', NULL, '510503', 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2336, '2020-03-29 14:20:36.000', 'system.region.area.510504', '龙马潭区', NULL, '510504', 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2337, '2020-03-29 14:20:36.000', 'system.region.area.510521', '泸县', NULL, '510521', 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2338, '2020-03-29 14:20:36.000', 'system.region.area.510522', '合江县', NULL, '510522', 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2339, '2020-03-29 14:20:36.000', 'system.region.area.510524', '叙永县', NULL, '510524', 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2340, '2020-03-29 14:20:36.000', 'system.region.area.510525', '古蔺县', NULL, '510525', 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2341, '2020-03-29 14:20:36.000', 'system.region.city.510600', '德阳市', 'area', '510600', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2342, '2020-03-29 14:20:36.000', 'system.region.area.510603', '旌阳区', NULL, '510603', 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2343, '2020-03-29 14:20:36.000', 'system.region.area.510604', '罗江区', NULL, '510604', 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2344, '2020-03-29 14:20:36.000', 'system.region.area.510623', '中江县', NULL, '510623', 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2345, '2020-03-29 14:20:36.000', 'system.region.area.510681', '广汉市', NULL, '510681', 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2346, '2020-03-29 14:20:36.000', 'system.region.area.510682', '什邡市', NULL, '510682', 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2347, '2020-03-29 14:20:36.000', 'system.region.area.510683', '绵竹市', NULL, '510683', 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2348, '2020-03-29 14:20:36.000', 'system.region.city.510700', '绵阳市', 'area', '510700', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2349, '2020-03-29 14:20:36.000', 'system.region.area.510703', '涪城区', NULL, '510703', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2350, '2020-03-29 14:20:36.000', 'system.region.area.510704', '游仙区', NULL, '510704', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2351, '2020-03-29 14:20:36.000', 'system.region.area.510705', '安州区', NULL, '510705', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2352, '2020-03-29 14:20:36.000', 'system.region.area.510722', '三台县', NULL, '510722', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2353, '2020-03-29 14:20:36.000', 'system.region.area.510723', '盐亭县', NULL, '510723', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2354, '2020-03-29 14:20:36.000', 'system.region.area.510725', '梓潼县', NULL, '510725', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2355, '2020-03-29 14:20:36.000', 'system.region.area.510726', '北川羌族自治县', NULL, '510726', 1, 16, 2348, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2356, '2020-03-29 14:20:36.000', 'system.region.area.510727', '平武县', NULL, '510727', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2357, '2020-03-29 14:20:36.000', 'system.region.area.510781', '江油市', NULL, '510781', 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2358, '2020-03-29 14:20:36.000', 'system.region.city.510800', '广元市', 'area', '510800', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2359, '2020-03-29 14:20:36.000', 'system.region.area.510802', '利州区', NULL, '510802', 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2360, '2020-03-29 14:20:36.000', 'system.region.area.510811', '昭化区', NULL, '510811', 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2361, '2020-03-29 14:20:36.000', 'system.region.area.510812', '朝天区', NULL, '510812', 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2362, '2020-03-29 14:20:36.000', 'system.region.area.510821', '旺苍县', NULL, '510821', 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2363, '2020-03-29 14:20:36.000', 'system.region.area.510822', '青川县', NULL, '510822', 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2364, '2020-03-29 14:20:36.000', 'system.region.area.510823', '剑阁县', NULL, '510823', 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2365, '2020-03-29 14:20:36.000', 'system.region.area.510824', '苍溪县', NULL, '510824', 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2366, '2020-03-29 14:20:36.000', 'system.region.city.510900', '遂宁市', 'area', '510900', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2367, '2020-03-29 14:20:36.000', 'system.region.area.510903', '船山区', NULL, '510903', 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2368, '2020-03-29 14:20:36.000', 'system.region.area.510904', '安居区', NULL, '510904', 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2369, '2020-03-29 14:20:36.000', 'system.region.area.510921', '蓬溪县', NULL, '510921', 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2370, '2020-03-29 14:20:36.000', 'system.region.area.510923', '大英县', NULL, '510923', 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2371, '2020-03-29 14:20:36.000', 'system.region.area.510981', '射洪市', NULL, '510981', 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2372, '2020-03-29 14:20:36.000', 'system.region.city.511000', '内江市', 'area', '511000', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2373, '2020-03-29 14:20:36.000', 'system.region.area.511002', '市中区', NULL, '511002', 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2374, '2020-03-29 14:20:36.000', 'system.region.area.511011', '东兴区', NULL, '511011', 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2375, '2020-03-29 14:20:36.000', 'system.region.area.511024', '威远县', NULL, '511024', 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2376, '2020-03-29 14:20:36.000', 'system.region.area.511025', '资中县', NULL, '511025', 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2377, '2020-03-29 14:20:36.000', 'system.region.area.511083', '隆昌市', NULL, '511083', 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2378, '2020-03-29 14:20:36.000', 'system.region.city.511100', '乐山市', 'area', '511100', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2379, '2020-03-29 14:20:36.000', 'system.region.area.511102', '市中区', NULL, '511102', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2380, '2020-03-29 14:20:36.000', 'system.region.area.511111', '沙湾区', NULL, '511111', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2381, '2020-03-29 14:20:36.000', 'system.region.area.511112', '五通桥区', NULL, '511112', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2382, '2020-03-29 14:20:36.000', 'system.region.area.511113', '金口河区', NULL, '511113', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2383, '2020-03-29 14:20:36.000', 'system.region.area.511123', '犍为县', NULL, '511123', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2384, '2020-03-29 14:20:36.000', 'system.region.area.511124', '井研县', NULL, '511124', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2385, '2020-03-29 14:20:36.000', 'system.region.area.511126', '夹江县', NULL, '511126', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2386, '2020-03-29 14:20:36.000', 'system.region.area.511129', '沐川县', NULL, '511129', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2387, '2020-03-29 14:20:36.000', 'system.region.area.511132', '峨边彝族自治县', NULL, '511132', 1, 16, 2378, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2388, '2020-03-29 14:20:36.000', 'system.region.area.511133', '马边彝族自治县', NULL, '511133', 1, 16, 2378, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2389, '2020-03-29 14:20:36.000', 'system.region.area.511181', '峨眉山市', NULL, '511181', 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2390, '2020-03-29 14:20:36.000', 'system.region.city.511300', '南充市', 'area', '511300', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2391, '2020-03-29 14:20:36.000', 'system.region.area.511302', '顺庆区', NULL, '511302', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2392, '2020-03-29 14:20:36.000', 'system.region.area.511303', '高坪区', NULL, '511303', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2393, '2020-03-29 14:20:36.000', 'system.region.area.511304', '嘉陵区', NULL, '511304', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2394, '2020-03-29 14:20:36.000', 'system.region.area.511321', '南部县', NULL, '511321', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2395, '2020-03-29 14:20:36.000', 'system.region.area.511322', '营山县', NULL, '511322', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2396, '2020-03-29 14:20:36.000', 'system.region.area.511323', '蓬安县', NULL, '511323', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2397, '2020-03-29 14:20:36.000', 'system.region.area.511324', '仪陇县', NULL, '511324', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2398, '2020-03-29 14:20:36.000', 'system.region.area.511325', '西充县', NULL, '511325', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2399, '2020-03-29 14:20:36.000', 'system.region.area.511381', '阆中市', NULL, '511381', 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2400, '2020-03-29 14:20:36.000', 'system.region.city.511400', '眉山市', 'area', '511400', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2401, '2020-03-29 14:20:36.000', 'system.region.area.511402', '东坡区', NULL, '511402', 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2402, '2020-03-29 14:20:36.000', 'system.region.area.511403', '彭山区', NULL, '511403', 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2403, '2020-03-29 14:20:36.000', 'system.region.area.511421', '仁寿县', NULL, '511421', 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2404, '2020-03-29 14:20:36.000', 'system.region.area.511423', '洪雅县', NULL, '511423', 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2405, '2020-03-29 14:20:36.000', 'system.region.area.511424', '丹棱县', NULL, '511424', 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2406, '2020-03-29 14:20:36.000', 'system.region.area.511425', '青神县', NULL, '511425', 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2407, '2020-03-29 14:20:36.000', 'system.region.city.511500', '宜宾市', 'area', '511500', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2408, '2020-03-29 14:20:36.000', 'system.region.area.511502', '翠屏区', NULL, '511502', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2409, '2020-03-29 14:20:36.000', 'system.region.area.511503', '南溪区', NULL, '511503', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2410, '2020-03-29 14:20:36.000', 'system.region.area.511504', '叙州区', NULL, '511504', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2411, '2020-03-29 14:20:36.000', 'system.region.area.511523', '江安县', NULL, '511523', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2412, '2020-03-29 14:20:36.000', 'system.region.area.511524', '长宁县', NULL, '511524', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2413, '2020-03-29 14:20:36.000', 'system.region.area.511525', '高县', NULL, '511525', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2414, '2020-03-29 14:20:36.000', 'system.region.area.511526', '珙县', NULL, '511526', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2415, '2020-03-29 14:20:36.000', 'system.region.area.511527', '筠连县', NULL, '511527', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2416, '2020-03-29 14:20:36.000', 'system.region.area.511528', '兴文县', NULL, '511528', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2417, '2020-03-29 14:20:36.000', 'system.region.area.511529', '屏山县', NULL, '511529', 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2418, '2020-03-29 14:20:36.000', 'system.region.city.511600', '广安市', 'area', '511600', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2419, '2020-03-29 14:20:36.000', 'system.region.area.511602', '广安区', NULL, '511602', 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2420, '2020-03-29 14:20:36.000', 'system.region.area.511603', '前锋区', NULL, '511603', 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2421, '2020-03-29 14:20:36.000', 'system.region.area.511621', '岳池县', NULL, '511621', 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2422, '2020-03-29 14:20:36.000', 'system.region.area.511622', '武胜县', NULL, '511622', 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2423, '2020-03-29 14:20:36.000', 'system.region.area.511623', '邻水县', NULL, '511623', 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2424, '2020-03-29 14:20:36.000', 'system.region.area.511681', '华蓥市', NULL, '511681', 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2425, '2020-03-29 14:20:36.000', 'system.region.city.511700', '达州市', 'area', '511700', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2426, '2020-03-29 14:20:36.000', 'system.region.area.511702', '通川区', NULL, '511702', 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2427, '2020-03-29 14:20:36.000', 'system.region.area.511703', '达川区', NULL, '511703', 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2428, '2020-03-29 14:20:36.000', 'system.region.area.511722', '宣汉县', NULL, '511722', 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2429, '2020-03-29 14:20:36.000', 'system.region.area.511723', '开江县', NULL, '511723', 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2430, '2020-03-29 14:20:36.000', 'system.region.area.511724', '大竹县', NULL, '511724', 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2431, '2020-03-29 14:20:36.000', 'system.region.area.511725', '渠县', NULL, '511725', 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2432, '2020-03-29 14:20:36.000', 'system.region.area.511781', '万源市', NULL, '511781', 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2433, '2020-03-29 14:20:36.000', 'system.region.city.511800', '雅安市', 'area', '511800', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2434, '2020-03-29 14:20:36.000', 'system.region.area.511802', '雨城区', NULL, '511802', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2435, '2020-03-29 14:20:36.000', 'system.region.area.511803', '名山区', NULL, '511803', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2436, '2020-03-29 14:20:36.000', 'system.region.area.511822', '荥经县', NULL, '511822', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2437, '2020-03-29 14:20:36.000', 'system.region.area.511823', '汉源县', NULL, '511823', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2438, '2020-03-29 14:20:36.000', 'system.region.area.511824', '石棉县', NULL, '511824', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2439, '2020-03-29 14:20:36.000', 'system.region.area.511825', '天全县', NULL, '511825', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2440, '2020-03-29 14:20:36.000', 'system.region.area.511826', '芦山县', NULL, '511826', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2441, '2020-03-29 14:20:36.000', 'system.region.area.511827', '宝兴县', NULL, '511827', 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2442, '2020-03-29 14:20:36.000', 'system.region.city.511900', '巴中市', 'area', '511900', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2443, '2020-03-29 14:20:36.000', 'system.region.area.511902', '巴州区', NULL, '511902', 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2444, '2020-03-29 14:20:36.000', 'system.region.area.511903', '恩阳区', NULL, '511903', 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2445, '2020-03-29 14:20:36.000', 'system.region.area.511921', '通江县', NULL, '511921', 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2446, '2020-03-29 14:20:36.000', 'system.region.area.511922', '南江县', NULL, '511922', 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2447, '2020-03-29 14:20:36.000', 'system.region.area.511923', '平昌县', NULL, '511923', 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2448, '2020-03-29 14:20:36.000', 'system.region.city.512000', '资阳市', 'area', '512000', 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2449, '2020-03-29 14:20:36.000', 'system.region.area.512002', '雁江区', NULL, '512002', 1, 16, 2448, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2450, '2020-03-29 14:20:36.000', 'system.region.area.512021', '安岳县', NULL, '512021', 1, 16, 2448, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2451, '2020-03-29 14:20:36.000', 'system.region.area.512022', '乐至县', NULL, '512022', 1, 16, 2448, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2452, '2020-03-29 14:20:36.000', 'system.region.city.513200', '阿坝藏族羌族自治州', 'area', '513200', 1, 15, 2298, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2453, '2020-03-29 14:20:36.000', 'system.region.area.513201', '马尔康市', NULL, '513201', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2454, '2020-03-29 14:20:36.000', 'system.region.area.513221', '汶川县', NULL, '513221', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2455, '2020-03-29 14:20:36.000', 'system.region.area.513222', '理县', NULL, '513222', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2456, '2020-03-29 14:20:36.000', 'system.region.area.513223', '茂县', NULL, '513223', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2457, '2020-03-29 14:20:36.000', 'system.region.area.513224', '松潘县', NULL, '513224', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2458, '2020-03-29 14:20:36.000', 'system.region.area.513225', '九寨沟县', NULL, '513225', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2459, '2020-03-29 14:20:36.000', 'system.region.area.513226', '金川县', NULL, '513226', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2460, '2020-03-29 14:20:36.000', 'system.region.area.513227', '小金县', NULL, '513227', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2461, '2020-03-29 14:20:36.000', 'system.region.area.513228', '黑水县', NULL, '513228', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2462, '2020-03-29 14:20:36.000', 'system.region.area.513230', '壤塘县', NULL, '513230', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2463, '2020-03-29 14:20:36.000', 'system.region.area.513231', '阿坝县', NULL, '513231', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2464, '2020-03-29 14:20:36.000', 'system.region.area.513232', '若尔盖县', NULL, '513232', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2465, '2020-03-29 14:20:36.000', 'system.region.area.513233', '红原县', NULL, '513233', 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2466, '2020-03-29 14:20:36.000', 'system.region.city.513300', '甘孜藏族自治州', 'area', '513300', 1, 15, 2298, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2467, '2020-03-29 14:20:36.000', 'system.region.area.513301', '康定市', NULL, '513301', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2468, '2020-03-29 14:20:36.000', 'system.region.area.513322', '泸定县', NULL, '513322', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2469, '2020-03-29 14:20:36.000', 'system.region.area.513323', '丹巴县', NULL, '513323', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2470, '2020-03-29 14:20:36.000', 'system.region.area.513324', '九龙县', NULL, '513324', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2471, '2020-03-29 14:20:36.000', 'system.region.area.513325', '雅江县', NULL, '513325', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2472, '2020-03-29 14:20:36.000', 'system.region.area.513326', '道孚县', NULL, '513326', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2473, '2020-03-29 14:20:36.000', 'system.region.area.513327', '炉霍县', NULL, '513327', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2474, '2020-03-29 14:20:36.000', 'system.region.area.513328', '甘孜县', NULL, '513328', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2475, '2020-03-29 14:20:36.000', 'system.region.area.513329', '新龙县', NULL, '513329', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2476, '2020-03-29 14:20:36.000', 'system.region.area.513330', '德格县', NULL, '513330', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2477, '2020-03-29 14:20:36.000', 'system.region.area.513331', '白玉县', NULL, '513331', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2478, '2020-03-29 14:20:36.000', 'system.region.area.513332', '石渠县', NULL, '513332', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2479, '2020-03-29 14:20:36.000', 'system.region.area.513333', '色达县', NULL, '513333', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2480, '2020-03-29 14:20:36.000', 'system.region.area.513334', '理塘县', NULL, '513334', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2481, '2020-03-29 14:20:36.000', 'system.region.area.513335', '巴塘县', NULL, '513335', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2482, '2020-03-29 14:20:36.000', 'system.region.area.513336', '乡城县', NULL, '513336', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2483, '2020-03-29 14:20:36.000', 'system.region.area.513337', '稻城县', NULL, '513337', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2484, '2020-03-29 14:20:36.000', 'system.region.area.513338', '得荣县', NULL, '513338', 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2485, '2020-03-29 14:20:36.000', 'system.region.city.513400', '凉山彝族自治州', 'area', '513400', 1, 15, 2298, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2486, '2020-03-29 14:20:36.000', 'system.region.area.513401', '西昌市', NULL, '513401', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2487, '2020-03-29 14:20:36.000', 'system.region.area.513422', '木里藏族自治县', NULL, '513422', 1, 16, 2485, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2488, '2020-03-29 14:20:36.000', 'system.region.area.513423', '盐源县', NULL, '513423', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2489, '2020-03-29 14:20:36.000', 'system.region.area.513424', '德昌县', NULL, '513424', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2490, '2020-03-29 14:20:36.000', 'system.region.area.513425', '会理县', NULL, '513425', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2491, '2020-03-29 14:20:36.000', 'system.region.area.513426', '会东县', NULL, '513426', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2492, '2020-03-29 14:20:36.000', 'system.region.area.513427', '宁南县', NULL, '513427', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2493, '2020-03-29 14:20:36.000', 'system.region.area.513428', '普格县', NULL, '513428', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2494, '2020-03-29 14:20:36.000', 'system.region.area.513429', '布拖县', NULL, '513429', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2495, '2020-03-29 14:20:36.000', 'system.region.area.513430', '金阳县', NULL, '513430', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2496, '2020-03-29 14:20:36.000', 'system.region.area.513431', '昭觉县', NULL, '513431', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2497, '2020-03-29 14:20:36.000', 'system.region.area.513432', '喜德县', NULL, '513432', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2498, '2020-03-29 14:20:36.000', 'system.region.area.513433', '冕宁县', NULL, '513433', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2499, '2020-03-29 14:20:36.000', 'system.region.area.513434', '越西县', NULL, '513434', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2500, '2020-03-29 14:20:36.000', 'system.region.area.513435', '甘洛县', NULL, '513435', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2501, '2020-03-29 14:20:36.000', 'system.region.area.513436', '美姑县', NULL, '513436', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2502, '2020-03-29 14:20:36.000', 'system.region.area.513437', '雷波县', NULL, '513437', 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2503, '2020-03-29 14:20:36.000', 'system.region.province.520000', '贵州省', 'city', '520000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2504, '2020-03-29 14:20:36.000', 'system.region.city.520100', '贵阳市', 'area', '520100', 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2505, '2020-03-29 14:20:36.000', 'system.region.area.520102', '南明区', NULL, '520102', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2506, '2020-03-29 14:20:36.000', 'system.region.area.520103', '云岩区', NULL, '520103', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2507, '2020-03-29 14:20:36.000', 'system.region.area.520111', '花溪区', NULL, '520111', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2508, '2020-03-29 14:20:36.000', 'system.region.area.520112', '乌当区', NULL, '520112', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2509, '2020-03-29 14:20:36.000', 'system.region.area.520113', '白云区', NULL, '520113', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2510, '2020-03-29 14:20:36.000', 'system.region.area.520115', '观山湖区', NULL, '520115', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2511, '2020-03-29 14:20:36.000', 'system.region.area.520121', '开阳县', NULL, '520121', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2512, '2020-03-29 14:20:36.000', 'system.region.area.520122', '息烽县', NULL, '520122', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2513, '2020-03-29 14:20:36.000', 'system.region.area.520123', '修文县', NULL, '520123', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2514, '2020-03-29 14:20:36.000', 'system.region.area.520181', '清镇市', NULL, '520181', 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2515, '2020-03-29 14:20:36.000', 'system.region.city.520200', '六盘水市', 'area', '520200', 1, 15, 2503, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2516, '2020-03-29 14:20:36.000', 'system.region.area.520201', '钟山区', NULL, '520201', 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2517, '2020-03-29 14:20:36.000', 'system.region.area.520203', '六枝特区', NULL, '520203', 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2518, '2020-03-29 14:20:36.000', 'system.region.area.520221', '水城县', NULL, '520221', 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2519, '2020-03-29 14:20:36.000', 'system.region.area.520281', '盘州市', NULL, '520281', 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2520, '2020-03-29 14:20:36.000', 'system.region.city.520300', '遵义市', 'area', '520300', 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2521, '2020-03-29 14:20:36.000', 'system.region.area.520302', '红花岗区', NULL, '520302', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2522, '2020-03-29 14:20:36.000', 'system.region.area.520303', '汇川区', NULL, '520303', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2523, '2020-03-29 14:20:36.000', 'system.region.area.520304', '播州区', NULL, '520304', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2524, '2020-03-29 14:20:36.000', 'system.region.area.520322', '桐梓县', NULL, '520322', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2525, '2020-03-29 14:20:36.000', 'system.region.area.520323', '绥阳县', NULL, '520323', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2526, '2020-03-29 14:20:36.000', 'system.region.area.520324', '正安县', NULL, '520324', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2527, '2020-03-29 14:20:36.000', 'system.region.area.520325', '道真仡佬族苗族自治县', NULL, '520325', 1, 16, 2520, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2528, '2020-03-29 14:20:36.000', 'system.region.area.520326', '务川仡佬族苗族自治县', NULL, '520326', 1, 16, 2520, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2529, '2020-03-29 14:20:36.000', 'system.region.area.520327', '凤冈县', NULL, '520327', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2530, '2020-03-29 14:20:36.000', 'system.region.area.520328', '湄潭县', NULL, '520328', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2531, '2020-03-29 14:20:36.000', 'system.region.area.520329', '余庆县', NULL, '520329', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2532, '2020-03-29 14:20:36.000', 'system.region.area.520330', '习水县', NULL, '520330', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2533, '2020-03-29 14:20:36.000', 'system.region.area.520381', '赤水市', NULL, '520381', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2534, '2020-03-29 14:20:36.000', 'system.region.area.520382', '仁怀市', NULL, '520382', 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2535, '2020-03-29 14:20:36.000', 'system.region.city.520400', '安顺市', 'area', '520400', 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2536, '2020-03-29 14:20:36.000', 'system.region.area.520402', '西秀区', NULL, '520402', 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2537, '2020-03-29 14:20:36.000', 'system.region.area.520403', '平坝区', NULL, '520403', 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2538, '2020-03-29 14:20:36.000', 'system.region.area.520422', '普定县', NULL, '520422', 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2539, '2020-03-29 14:20:36.000', 'system.region.area.520423', '镇宁布依族苗族自治县', NULL, '520423', 1, 16, 2535, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2540, '2020-03-29 14:20:36.000', 'system.region.area.520424', '关岭布依族苗族自治县', NULL, '520424', 1, 16, 2535, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2541, '2020-03-29 14:20:36.000', 'system.region.area.520425', '紫云苗族布依族自治县', NULL, '520425', 1, 16, 2535, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2542, '2020-03-29 14:20:36.000', 'system.region.city.520500', '毕节市', 'area', '520500', 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2543, '2020-03-29 14:20:36.000', 'system.region.area.520502', '七星关区', NULL, '520502', 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2544, '2020-03-29 14:20:36.000', 'system.region.area.520521', '大方县', NULL, '520521', 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2545, '2020-03-29 14:20:36.000', 'system.region.area.520522', '黔西县', NULL, '520522', 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2546, '2020-03-29 14:20:36.000', 'system.region.area.520523', '金沙县', NULL, '520523', 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2547, '2020-03-29 14:20:36.000', 'system.region.area.520524', '织金县', NULL, '520524', 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2548, '2020-03-29 14:20:36.000', 'system.region.area.520525', '纳雍县', NULL, '520525', 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2549, '2020-03-29 14:20:36.000', 'system.region.area.520526', '威宁彝族回族苗族自治县', NULL, '520526', 1, 16, 2542, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2550, '2020-03-29 14:20:36.000', 'system.region.area.520527', '赫章县', NULL, '520527', 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2551, '2020-03-29 14:20:36.000', 'system.region.city.520600', '铜仁市', 'area', '520600', 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2552, '2020-03-29 14:20:36.000', 'system.region.area.520602', '碧江区', NULL, '520602', 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2553, '2020-03-29 14:20:36.000', 'system.region.area.520603', '万山区', NULL, '520603', 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2554, '2020-03-29 14:20:36.000', 'system.region.area.520621', '江口县', NULL, '520621', 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2555, '2020-03-29 14:20:36.000', 'system.region.area.520622', '玉屏侗族自治县', NULL, '520622', 1, 16, 2551, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2556, '2020-03-29 14:20:36.000', 'system.region.area.520623', '石阡县', NULL, '520623', 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2557, '2020-03-29 14:20:36.000', 'system.region.area.520624', '思南县', NULL, '520624', 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2558, '2020-03-29 14:20:36.000', 'system.region.area.520625', '印江土家族苗族自治县', NULL, '520625', 1, 16, 2551, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2559, '2020-03-29 14:20:36.000', 'system.region.area.520626', '德江县', NULL, '520626', 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2560, '2020-03-29 14:20:36.000', 'system.region.area.520627', '沿河土家族自治县', NULL, '520627', 1, 16, 2551, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2561, '2020-03-29 14:20:36.000', 'system.region.area.520628', '松桃苗族自治县', NULL, '520628', 1, 16, 2551, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2562, '2020-03-29 14:20:36.000', 'system.region.city.522300', '黔西南布依族苗族自治州', 'area', '522300', 1, 15, 2503,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2563, '2020-03-29 14:20:36.000', 'system.region.area.522301', '兴义市', NULL, '522301', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2564, '2020-03-29 14:20:36.000', 'system.region.area.522302', '兴仁市', NULL, '522302', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2565, '2020-03-29 14:20:36.000', 'system.region.area.522323', '普安县', NULL, '522323', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2566, '2020-03-29 14:20:36.000', 'system.region.area.522324', '晴隆县', NULL, '522324', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2567, '2020-03-29 14:20:36.000', 'system.region.area.522325', '贞丰县', NULL, '522325', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2568, '2020-03-29 14:20:36.000', 'system.region.area.522326', '望谟县', NULL, '522326', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2569, '2020-03-29 14:20:36.000', 'system.region.area.522327', '册亨县', NULL, '522327', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2570, '2020-03-29 14:20:36.000', 'system.region.area.522328', '安龙县', NULL, '522328', 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2571, '2020-03-29 14:20:36.000', 'system.region.city.522600', '黔东南苗族侗族自治州', 'area', '522600', 1, 15, 2503, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2572, '2020-03-29 14:20:36.000', 'system.region.area.522601', '凯里市', NULL, '522601', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2573, '2020-03-29 14:20:36.000', 'system.region.area.522622', '黄平县', NULL, '522622', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2574, '2020-03-29 14:20:36.000', 'system.region.area.522623', '施秉县', NULL, '522623', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2575, '2020-03-29 14:20:36.000', 'system.region.area.522624', '三穗县', NULL, '522624', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2576, '2020-03-29 14:20:36.000', 'system.region.area.522625', '镇远县', NULL, '522625', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2577, '2020-03-29 14:20:36.000', 'system.region.area.522626', '岑巩县', NULL, '522626', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2578, '2020-03-29 14:20:36.000', 'system.region.area.522627', '天柱县', NULL, '522627', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2579, '2020-03-29 14:20:36.000', 'system.region.area.522628', '锦屏县', NULL, '522628', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2580, '2020-03-29 14:20:36.000', 'system.region.area.522629', '剑河县', NULL, '522629', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2581, '2020-03-29 14:20:36.000', 'system.region.area.522630', '台江县', NULL, '522630', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2582, '2020-03-29 14:20:36.000', 'system.region.area.522631', '黎平县', NULL, '522631', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2583, '2020-03-29 14:20:36.000', 'system.region.area.522632', '榕江县', NULL, '522632', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2584, '2020-03-29 14:20:36.000', 'system.region.area.522633', '从江县', NULL, '522633', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2585, '2020-03-29 14:20:36.000', 'system.region.area.522634', '雷山县', NULL, '522634', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2586, '2020-03-29 14:20:36.000', 'system.region.area.522635', '麻江县', NULL, '522635', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2587, '2020-03-29 14:20:36.000', 'system.region.area.522636', '丹寨县', NULL, '522636', 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2588, '2020-03-29 14:20:36.000', 'system.region.city.522700', '黔南布依族苗族自治州', 'area', '522700', 1, 15, 2503, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2589, '2020-03-29 14:20:36.000', 'system.region.area.522701', '都匀市', NULL, '522701', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2590, '2020-03-29 14:20:36.000', 'system.region.area.522702', '福泉市', NULL, '522702', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2591, '2020-03-29 14:20:36.000', 'system.region.area.522722', '荔波县', NULL, '522722', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2592, '2020-03-29 14:20:36.000', 'system.region.area.522723', '贵定县', NULL, '522723', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2593, '2020-03-29 14:20:36.000', 'system.region.area.522725', '瓮安县', NULL, '522725', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2594, '2020-03-29 14:20:36.000', 'system.region.area.522726', '独山县', NULL, '522726', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2595, '2020-03-29 14:20:36.000', 'system.region.area.522727', '平塘县', NULL, '522727', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2596, '2020-03-29 14:20:36.000', 'system.region.area.522728', '罗甸县', NULL, '522728', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2597, '2020-03-29 14:20:36.000', 'system.region.area.522729', '长顺县', NULL, '522729', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2598, '2020-03-29 14:20:36.000', 'system.region.area.522730', '龙里县', NULL, '522730', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2599, '2020-03-29 14:20:36.000', 'system.region.area.522731', '惠水县', NULL, '522731', 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2600, '2020-03-29 14:20:36.000', 'system.region.area.522732', '三都水族自治县', NULL, '522732', 1, 16, 2588, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2601, '2020-03-29 14:20:36.000', 'system.region.province.530000', '云南省', 'city', '530000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2602, '2020-03-29 14:20:36.000', 'system.region.city.530100', '昆明市', 'area', '530100', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2603, '2020-03-29 14:20:36.000', 'system.region.area.530102', '五华区', NULL, '530102', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2604, '2020-03-29 14:20:36.000', 'system.region.area.530103', '盘龙区', NULL, '530103', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2605, '2020-03-29 14:20:36.000', 'system.region.area.530111', '官渡区', NULL, '530111', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2606, '2020-03-29 14:20:36.000', 'system.region.area.530112', '西山区', NULL, '530112', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2607, '2020-03-29 14:20:36.000', 'system.region.area.530113', '东川区', NULL, '530113', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2608, '2020-03-29 14:20:36.000', 'system.region.area.530114', '呈贡区', NULL, '530114', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2609, '2020-03-29 14:20:36.000', 'system.region.area.530115', '晋宁区', NULL, '530115', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2610, '2020-03-29 14:20:36.000', 'system.region.area.530124', '富民县', NULL, '530124', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2611, '2020-03-29 14:20:36.000', 'system.region.area.530125', '宜良县', NULL, '530125', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2612, '2020-03-29 14:20:36.000', 'system.region.area.530126', '石林彝族自治县', NULL, '530126', 1, 16, 2602, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2613, '2020-03-29 14:20:36.000', 'system.region.area.530127', '嵩明县', NULL, '530127', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2614, '2020-03-29 14:20:36.000', 'system.region.area.530128', '禄劝彝族苗族自治县', NULL, '530128', 1, 16, 2602, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2615, '2020-03-29 14:20:36.000', 'system.region.area.530129', '寻甸回族彝族自治县', NULL, '530129', 1, 16, 2602, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2616, '2020-03-29 14:20:36.000', 'system.region.area.530181', '安宁市', NULL, '530181', 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2617, '2020-03-29 14:20:36.000', 'system.region.city.530300', '曲靖市', 'area', '530300', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2618, '2020-03-29 14:20:36.000', 'system.region.area.530302', '麒麟区', NULL, '530302', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2619, '2020-03-29 14:20:36.000', 'system.region.area.530303', '沾益区', NULL, '530303', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2620, '2020-03-29 14:20:36.000', 'system.region.area.530304', '马龙区', NULL, '530304', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2621, '2020-03-29 14:20:36.000', 'system.region.area.530322', '陆良县', NULL, '530322', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2622, '2020-03-29 14:20:36.000', 'system.region.area.530323', '师宗县', NULL, '530323', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2623, '2020-03-29 14:20:36.000', 'system.region.area.530324', '罗平县', NULL, '530324', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2624, '2020-03-29 14:20:36.000', 'system.region.area.530325', '富源县', NULL, '530325', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2625, '2020-03-29 14:20:36.000', 'system.region.area.530326', '会泽县', NULL, '530326', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2626, '2020-03-29 14:20:36.000', 'system.region.area.530381', '宣威市', NULL, '530381', 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2627, '2020-03-29 14:20:36.000', 'system.region.city.530400', '玉溪市', 'area', '530400', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2628, '2020-03-29 14:20:36.000', 'system.region.area.530402', '红塔区', NULL, '530402', 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2629, '2020-03-29 14:20:36.000', 'system.region.area.530403', '江川区', NULL, '530403', 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2630, '2020-03-29 14:20:36.000', 'system.region.area.530423', '通海县', NULL, '530423', 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2631, '2020-03-29 14:20:36.000', 'system.region.area.530424', '华宁县', NULL, '530424', 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2632, '2020-03-29 14:20:36.000', 'system.region.area.530425', '易门县', NULL, '530425', 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2633, '2020-03-29 14:20:36.000', 'system.region.area.530426', '峨山彝族自治县', NULL, '530426', 1, 16, 2627, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2634, '2020-03-29 14:20:36.000', 'system.region.area.530427', '新平彝族傣族自治县', NULL, '530427', 1, 16, 2627, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2635, '2020-03-29 14:20:36.000', 'system.region.area.530428', '元江哈尼族彝族傣族自治县', NULL, '530428', 1, 16, 2627, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2636, '2020-03-29 14:20:36.000', 'system.region.area.530481', '澄江市', NULL, '530481', 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2637, '2020-03-29 14:20:36.000', 'system.region.city.530500', '保山市', 'area', '530500', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2638, '2020-03-29 14:20:36.000', 'system.region.area.530502', '隆阳区', NULL, '530502', 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2639, '2020-03-29 14:20:36.000', 'system.region.area.530521', '施甸县', NULL, '530521', 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2640, '2020-03-29 14:20:36.000', 'system.region.area.530523', '龙陵县', NULL, '530523', 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2641, '2020-03-29 14:20:36.000', 'system.region.area.530524', '昌宁县', NULL, '530524', 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2642, '2020-03-29 14:20:36.000', 'system.region.area.530581', '腾冲市', NULL, '530581', 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2643, '2020-03-29 14:20:36.000', 'system.region.city.530600', '昭通市', 'area', '530600', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2644, '2020-03-29 14:20:36.000', 'system.region.area.530602', '昭阳区', NULL, '530602', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2645, '2020-03-29 14:20:36.000', 'system.region.area.530621', '鲁甸县', NULL, '530621', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2646, '2020-03-29 14:20:36.000', 'system.region.area.530622', '巧家县', NULL, '530622', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2647, '2020-03-29 14:20:36.000', 'system.region.area.530623', '盐津县', NULL, '530623', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2648, '2020-03-29 14:20:36.000', 'system.region.area.530624', '大关县', NULL, '530624', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2649, '2020-03-29 14:20:36.000', 'system.region.area.530625', '永善县', NULL, '530625', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2650, '2020-03-29 14:20:36.000', 'system.region.area.530626', '绥江县', NULL, '530626', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2651, '2020-03-29 14:20:36.000', 'system.region.area.530627', '镇雄县', NULL, '530627', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2652, '2020-03-29 14:20:36.000', 'system.region.area.530628', '彝良县', NULL, '530628', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2653, '2020-03-29 14:20:36.000', 'system.region.area.530629', '威信县', NULL, '530629', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2654, '2020-03-29 14:20:36.000', 'system.region.area.530681', '水富市', NULL, '530681', 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2655, '2020-03-29 14:20:36.000', 'system.region.city.530700', '丽江市', 'area', '530700', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2656, '2020-03-29 14:20:36.000', 'system.region.area.530702', '古城区', NULL, '530702', 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2657, '2020-03-29 14:20:36.000', 'system.region.area.530721', '玉龙纳西族自治县', NULL, '530721', 1, 16, 2655, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2658, '2020-03-29 14:20:36.000', 'system.region.area.530722', '永胜县', NULL, '530722', 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2659, '2020-03-29 14:20:36.000', 'system.region.area.530723', '华坪县', NULL, '530723', 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2660, '2020-03-29 14:20:36.000', 'system.region.area.530724', '宁蒗彝族自治县', NULL, '530724', 1, 16, 2655, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2661, '2020-03-29 14:20:36.000', 'system.region.city.530800', '普洱市', 'area', '530800', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2662, '2020-03-29 14:20:36.000', 'system.region.area.530802', '思茅区', NULL, '530802', 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2663, '2020-03-29 14:20:36.000', 'system.region.area.530821', '宁洱哈尼族彝族自治县', NULL, '530821', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2664, '2020-03-29 14:20:36.000', 'system.region.area.530822', '墨江哈尼族自治县', NULL, '530822', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2665, '2020-03-29 14:20:36.000', 'system.region.area.530823', '景东彝族自治县', NULL, '530823', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2666, '2020-03-29 14:20:36.000', 'system.region.area.530824', '景谷傣族彝族自治县', NULL, '530824', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2667, '2020-03-29 14:20:36.000', 'system.region.area.530825', '镇沅彝族哈尼族拉祜族自治县', NULL, '530825', 1, 16, 2661,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2668, '2020-03-29 14:20:36.000', 'system.region.area.530826', '江城哈尼族彝族自治县', NULL, '530826', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2669, '2020-03-29 14:20:36.000', 'system.region.area.530827', '孟连傣族拉祜族佤族自治县', NULL, '530827', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2670, '2020-03-29 14:20:36.000', 'system.region.area.530828', '澜沧拉祜族自治县', NULL, '530828', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2671, '2020-03-29 14:20:36.000', 'system.region.area.530829', '西盟佤族自治县', NULL, '530829', 1, 16, 2661, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2672, '2020-03-29 14:20:36.000', 'system.region.city.530900', '临沧市', 'area', '530900', 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2673, '2020-03-29 14:20:36.000', 'system.region.area.530902', '临翔区', NULL, '530902', 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2674, '2020-03-29 14:20:36.000', 'system.region.area.530921', '凤庆县', NULL, '530921', 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2675, '2020-03-29 14:20:36.000', 'system.region.area.530922', '云县', NULL, '530922', 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2676, '2020-03-29 14:20:36.000', 'system.region.area.530923', '永德县', NULL, '530923', 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2677, '2020-03-29 14:20:36.000', 'system.region.area.530924', '镇康县', NULL, '530924', 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2678, '2020-03-29 14:20:36.000', 'system.region.area.530925', '双江拉祜族佤族布朗族傣族自治县', NULL, '530925', 1, 16, 2672,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2679, '2020-03-29 14:20:36.000', 'system.region.area.530926', '耿马傣族佤族自治县', NULL, '530926', 1, 16, 2672, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2680, '2020-03-29 14:20:36.000', 'system.region.area.530927', '沧源佤族自治县', NULL, '530927', 1, 16, 2672, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2681, '2020-03-29 14:20:36.000', 'system.region.city.532300', '楚雄彝族自治州', 'area', '532300', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2682, '2020-03-29 14:20:36.000', 'system.region.area.532301', '楚雄市', NULL, '532301', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2683, '2020-03-29 14:20:36.000', 'system.region.area.532322', '双柏县', NULL, '532322', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2684, '2020-03-29 14:20:36.000', 'system.region.area.532323', '牟定县', NULL, '532323', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2685, '2020-03-29 14:20:36.000', 'system.region.area.532324', '南华县', NULL, '532324', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2686, '2020-03-29 14:20:36.000', 'system.region.area.532325', '姚安县', NULL, '532325', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2687, '2020-03-29 14:20:36.000', 'system.region.area.532326', '大姚县', NULL, '532326', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2688, '2020-03-29 14:20:36.000', 'system.region.area.532327', '永仁县', NULL, '532327', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2689, '2020-03-29 14:20:36.000', 'system.region.area.532328', '元谋县', NULL, '532328', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2690, '2020-03-29 14:20:36.000', 'system.region.area.532329', '武定县', NULL, '532329', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2691, '2020-03-29 14:20:36.000', 'system.region.area.532331', '禄丰县', NULL, '532331', 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2692, '2020-03-29 14:20:36.000', 'system.region.city.532500', '红河哈尼族彝族自治州', 'area', '532500', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2693, '2020-03-29 14:20:36.000', 'system.region.area.532501', '个旧市', NULL, '532501', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2694, '2020-03-29 14:20:36.000', 'system.region.area.532502', '开远市', NULL, '532502', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2695, '2020-03-29 14:20:36.000', 'system.region.area.532503', '蒙自市', NULL, '532503', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2696, '2020-03-29 14:20:36.000', 'system.region.area.532504', '弥勒市', NULL, '532504', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2697, '2020-03-29 14:20:36.000', 'system.region.area.532523', '屏边苗族自治县', NULL, '532523', 1, 16, 2692, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2698, '2020-03-29 14:20:36.000', 'system.region.area.532524', '建水县', NULL, '532524', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2699, '2020-03-29 14:20:36.000', 'system.region.area.532525', '石屏县', NULL, '532525', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2700, '2020-03-29 14:20:36.000', 'system.region.area.532527', '泸西县', NULL, '532527', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2701, '2020-03-29 14:20:36.000', 'system.region.area.532528', '元阳县', NULL, '532528', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2702, '2020-03-29 14:20:36.000', 'system.region.area.532529', '红河县', NULL, '532529', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2703, '2020-03-29 14:20:36.000', 'system.region.area.532530', '金平苗族瑶族傣族自治县', NULL, '532530', 1, 16, 2692, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2704, '2020-03-29 14:20:36.000', 'system.region.area.532531', '绿春县', NULL, '532531', 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2705, '2020-03-29 14:20:36.000', 'system.region.area.532532', '河口瑶族自治县', NULL, '532532', 1, 16, 2692, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2706, '2020-03-29 14:20:36.000', 'system.region.city.532600', '文山壮族苗族自治州', 'area', '532600', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2707, '2020-03-29 14:20:36.000', 'system.region.area.532601', '文山市', NULL, '532601', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2708, '2020-03-29 14:20:36.000', 'system.region.area.532622', '砚山县', NULL, '532622', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2709, '2020-03-29 14:20:36.000', 'system.region.area.532623', '西畴县', NULL, '532623', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2710, '2020-03-29 14:20:36.000', 'system.region.area.532624', '麻栗坡县', NULL, '532624', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2711, '2020-03-29 14:20:36.000', 'system.region.area.532625', '马关县', NULL, '532625', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2712, '2020-03-29 14:20:36.000', 'system.region.area.532626', '丘北县', NULL, '532626', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2713, '2020-03-29 14:20:36.000', 'system.region.area.532627', '广南县', NULL, '532627', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2714, '2020-03-29 14:20:36.000', 'system.region.area.532628', '富宁县', NULL, '532628', 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2715, '2020-03-29 14:20:36.000', 'system.region.city.532800', '西双版纳傣族自治州', 'area', '532800', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2716, '2020-03-29 14:20:36.000', 'system.region.area.532801', '景洪市', NULL, '532801', 1, 16, 2715, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2717, '2020-03-29 14:20:36.000', 'system.region.area.532822', '勐海县', NULL, '532822', 1, 16, 2715, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2718, '2020-03-29 14:20:36.000', 'system.region.area.532823', '勐腊县', NULL, '532823', 1, 16, 2715, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2719, '2020-03-29 14:20:36.000', 'system.region.city.532900', '大理白族自治州', 'area', '532900', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2720, '2020-03-29 14:20:36.000', 'system.region.area.532901', '大理市', NULL, '532901', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2721, '2020-03-29 14:20:36.000', 'system.region.area.532922', '漾濞彝族自治县', NULL, '532922', 1, 16, 2719, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2722, '2020-03-29 14:20:36.000', 'system.region.area.532923', '祥云县', NULL, '532923', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2723, '2020-03-29 14:20:36.000', 'system.region.area.532924', '宾川县', NULL, '532924', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2724, '2020-03-29 14:20:36.000', 'system.region.area.532925', '弥渡县', NULL, '532925', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2725, '2020-03-29 14:20:36.000', 'system.region.area.532926', '南涧彝族自治县', NULL, '532926', 1, 16, 2719, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2726, '2020-03-29 14:20:36.000', 'system.region.area.532927', '巍山彝族回族自治县', NULL, '532927', 1, 16, 2719, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2727, '2020-03-29 14:20:36.000', 'system.region.area.532928', '永平县', NULL, '532928', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2728, '2020-03-29 14:20:36.000', 'system.region.area.532929', '云龙县', NULL, '532929', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2729, '2020-03-29 14:20:36.000', 'system.region.area.532930', '洱源县', NULL, '532930', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2730, '2020-03-29 14:20:36.000', 'system.region.area.532931', '剑川县', NULL, '532931', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2731, '2020-03-29 14:20:36.000', 'system.region.area.532932', '鹤庆县', NULL, '532932', 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2732, '2020-03-29 14:20:36.000', 'system.region.city.533100', '德宏傣族景颇族自治州', 'area', '533100', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2733, '2020-03-29 14:20:36.000', 'system.region.area.533102', '瑞丽市', NULL, '533102', 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2734, '2020-03-29 14:20:36.000', 'system.region.area.533103', '芒市', NULL, '533103', 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2735, '2020-03-29 14:20:36.000', 'system.region.area.533122', '梁河县', NULL, '533122', 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2736, '2020-03-29 14:20:36.000', 'system.region.area.533123', '盈江县', NULL, '533123', 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2737, '2020-03-29 14:20:36.000', 'system.region.area.533124', '陇川县', NULL, '533124', 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2738, '2020-03-29 14:20:36.000', 'system.region.city.533300', '怒江傈僳族自治州', 'area', '533300', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2739, '2020-03-29 14:20:36.000', 'system.region.area.533301', '泸水市', NULL, '533301', 1, 16, 2738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2740, '2020-03-29 14:20:36.000', 'system.region.area.533323', '福贡县', NULL, '533323', 1, 16, 2738, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2741, '2020-03-29 14:20:36.000', 'system.region.area.533324', '贡山独龙族怒族自治县', NULL, '533324', 1, 16, 2738, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2742, '2020-03-29 14:20:36.000', 'system.region.area.533325', '兰坪白族普米族自治县', NULL, '533325', 1, 16, 2738, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2743, '2020-03-29 14:20:36.000', 'system.region.city.533400', '迪庆藏族自治州', 'area', '533400', 1, 15, 2601, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2744, '2020-03-29 14:20:36.000', 'system.region.area.533401', '香格里拉市', NULL, '533401', 1, 16, 2743, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2745, '2020-03-29 14:20:36.000', 'system.region.area.533422', '德钦县', NULL, '533422', 1, 16, 2743, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2746, '2020-03-29 14:20:36.000', 'system.region.area.533423', '维西傈僳族自治县', NULL, '533423', 1, 16, 2743, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2747, '2020-03-29 14:20:36.000', 'system.region.province.540000', '西藏自治区', 'city', '540000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2748, '2020-03-29 14:20:36.000', 'system.region.city.540100', '拉萨市', 'area', '540100', 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2749, '2020-03-29 14:20:36.000', 'system.region.area.540102', '城关区', NULL, '540102', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2750, '2020-03-29 14:20:36.000', 'system.region.area.540103', '堆龙德庆区', NULL, '540103', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2751, '2020-03-29 14:20:36.000', 'system.region.area.540104', '达孜区', NULL, '540104', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2752, '2020-03-29 14:20:36.000', 'system.region.area.540121', '林周县', NULL, '540121', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2753, '2020-03-29 14:20:36.000', 'system.region.area.540122', '当雄县', NULL, '540122', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2754, '2020-03-29 14:20:36.000', 'system.region.area.540123', '尼木县', NULL, '540123', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2755, '2020-03-29 14:20:36.000', 'system.region.area.540124', '曲水县', NULL, '540124', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2756, '2020-03-29 14:20:36.000', 'system.region.area.540127', '墨竹工卡县', NULL, '540127', 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2757, '2020-03-29 14:20:36.000', 'system.region.city.540200', '日喀则市', 'area', '540200', 1, 15, 2747, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2758, '2020-03-29 14:20:36.000', 'system.region.area.540202', '桑珠孜区', NULL, '540202', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2759, '2020-03-29 14:20:36.000', 'system.region.area.540221', '南木林县', NULL, '540221', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2760, '2020-03-29 14:20:36.000', 'system.region.area.540222', '江孜县', NULL, '540222', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2761, '2020-03-29 14:20:36.000', 'system.region.area.540223', '定日县', NULL, '540223', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2762, '2020-03-29 14:20:36.000', 'system.region.area.540224', '萨迦县', NULL, '540224', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2763, '2020-03-29 14:20:36.000', 'system.region.area.540225', '拉孜县', NULL, '540225', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2764, '2020-03-29 14:20:36.000', 'system.region.area.540226', '昂仁县', NULL, '540226', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2765, '2020-03-29 14:20:36.000', 'system.region.area.540227', '谢通门县', NULL, '540227', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2766, '2020-03-29 14:20:36.000', 'system.region.area.540228', '白朗县', NULL, '540228', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2767, '2020-03-29 14:20:36.000', 'system.region.area.540229', '仁布县', NULL, '540229', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2768, '2020-03-29 14:20:36.000', 'system.region.area.540230', '康马县', NULL, '540230', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2769, '2020-03-29 14:20:36.000', 'system.region.area.540231', '定结县', NULL, '540231', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2770, '2020-03-29 14:20:36.000', 'system.region.area.540232', '仲巴县', NULL, '540232', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2771, '2020-03-29 14:20:36.000', 'system.region.area.540233', '亚东县', NULL, '540233', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2772, '2020-03-29 14:20:36.000', 'system.region.area.540234', '吉隆县', NULL, '540234', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2773, '2020-03-29 14:20:36.000', 'system.region.area.540235', '聂拉木县', NULL, '540235', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2774, '2020-03-29 14:20:36.000', 'system.region.area.540236', '萨嘎县', NULL, '540236', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2775, '2020-03-29 14:20:36.000', 'system.region.area.540237', '岗巴县', NULL, '540237', 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2776, '2020-03-29 14:20:36.000', 'system.region.city.540300', '昌都市', 'area', '540300', 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2777, '2020-03-29 14:20:36.000', 'system.region.area.540302', '卡若区', NULL, '540302', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2778, '2020-03-29 14:20:36.000', 'system.region.area.540321', '江达县', NULL, '540321', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2779, '2020-03-29 14:20:36.000', 'system.region.area.540322', '贡觉县', NULL, '540322', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2780, '2020-03-29 14:20:36.000', 'system.region.area.540323', '类乌齐县', NULL, '540323', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2781, '2020-03-29 14:20:36.000', 'system.region.area.540324', '丁青县', NULL, '540324', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2782, '2020-03-29 14:20:36.000', 'system.region.area.540325', '察雅县', NULL, '540325', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2783, '2020-03-29 14:20:36.000', 'system.region.area.540326', '八宿县', NULL, '540326', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2784, '2020-03-29 14:20:36.000', 'system.region.area.540327', '左贡县', NULL, '540327', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2785, '2020-03-29 14:20:36.000', 'system.region.area.540328', '芒康县', NULL, '540328', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2786, '2020-03-29 14:20:36.000', 'system.region.area.540329', '洛隆县', NULL, '540329', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2787, '2020-03-29 14:20:36.000', 'system.region.area.540330', '边坝县', NULL, '540330', 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2788, '2020-03-29 14:20:36.000', 'system.region.city.540400', '林芝市', 'area', '540400', 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2789, '2020-03-29 14:20:36.000', 'system.region.area.540402', '巴宜区', NULL, '540402', 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2790, '2020-03-29 14:20:36.000', 'system.region.area.540421', '工布江达县', NULL, '540421', 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2791, '2020-03-29 14:20:36.000', 'system.region.area.540422', '米林县', NULL, '540422', 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2792, '2020-03-29 14:20:36.000', 'system.region.area.540423', '墨脱县', NULL, '540423', 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2793, '2020-03-29 14:20:36.000', 'system.region.area.540424', '波密县', NULL, '540424', 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2794, '2020-03-29 14:20:36.000', 'system.region.area.540425', '察隅县', NULL, '540425', 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2795, '2020-03-29 14:20:36.000', 'system.region.area.540426', '朗县', NULL, '540426', 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2796, '2020-03-29 14:20:36.000', 'system.region.city.540500', '山南市', 'area', '540500', 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2797, '2020-03-29 14:20:36.000', 'system.region.area.540502', '乃东区', NULL, '540502', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2798, '2020-03-29 14:20:36.000', 'system.region.area.540521', '扎囊县', NULL, '540521', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2799, '2020-03-29 14:20:36.000', 'system.region.area.540522', '贡嘎县', NULL, '540522', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2800, '2020-03-29 14:20:36.000', 'system.region.area.540523', '桑日县', NULL, '540523', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2801, '2020-03-29 14:20:36.000', 'system.region.area.540524', '琼结县', NULL, '540524', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2802, '2020-03-29 14:20:36.000', 'system.region.area.540525', '曲松县', NULL, '540525', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2803, '2020-03-29 14:20:36.000', 'system.region.area.540526', '措美县', NULL, '540526', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2804, '2020-03-29 14:20:36.000', 'system.region.area.540527', '洛扎县', NULL, '540527', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2805, '2020-03-29 14:20:36.000', 'system.region.area.540528', '加查县', NULL, '540528', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2806, '2020-03-29 14:20:36.000', 'system.region.area.540529', '隆子县', NULL, '540529', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2807, '2020-03-29 14:20:36.000', 'system.region.area.540530', '错那县', NULL, '540530', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2808, '2020-03-29 14:20:36.000', 'system.region.area.540531', '浪卡子县', NULL, '540531', 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2809, '2020-03-29 14:20:36.000', 'system.region.city.540600', '那曲市', 'area', '540600', 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2810, '2020-03-29 14:20:36.000', 'system.region.area.540602', '色尼区', NULL, '540602', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2811, '2020-03-29 14:20:36.000', 'system.region.area.540621', '嘉黎县', NULL, '540621', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2812, '2020-03-29 14:20:36.000', 'system.region.area.540622', '比如县', NULL, '540622', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2813, '2020-03-29 14:20:36.000', 'system.region.area.540623', '聂荣县', NULL, '540623', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2814, '2020-03-29 14:20:36.000', 'system.region.area.540624', '安多县', NULL, '540624', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2815, '2020-03-29 14:20:36.000', 'system.region.area.540625', '申扎县', NULL, '540625', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2816, '2020-03-29 14:20:36.000', 'system.region.area.540626', '索县', NULL, '540626', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2817, '2020-03-29 14:20:36.000', 'system.region.area.540627', '班戈县', NULL, '540627', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2818, '2020-03-29 14:20:36.000', 'system.region.area.540628', '巴青县', NULL, '540628', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2819, '2020-03-29 14:20:36.000', 'system.region.area.540629', '尼玛县', NULL, '540629', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2820, '2020-03-29 14:20:36.000', 'system.region.area.540630', '双湖县', NULL, '540630', 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2821, '2020-03-29 14:20:36.000', 'system.region.city.542500', '阿里地区', 'area', '542500', 1, 15, 2747, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2822, '2020-03-29 14:20:36.000', 'system.region.area.542521', '普兰县', NULL, '542521', 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2823, '2020-03-29 14:20:36.000', 'system.region.area.542522', '札达县', NULL, '542522', 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2824, '2020-03-29 14:20:36.000', 'system.region.area.542523', '噶尔县', NULL, '542523', 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2825, '2020-03-29 14:20:36.000', 'system.region.area.542524', '日土县', NULL, '542524', 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2826, '2020-03-29 14:20:36.000', 'system.region.area.542525', '革吉县', NULL, '542525', 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2827, '2020-03-29 14:20:36.000', 'system.region.area.542526', '改则县', NULL, '542526', 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2828, '2020-03-29 14:20:36.000', 'system.region.area.542527', '措勤县', NULL, '542527', 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2829, '2020-03-29 14:20:36.000', 'system.region.province.610000', '陕西省', 'city', '610000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2830, '2020-03-29 14:20:36.000', 'system.region.city.610100', '西安市', 'area', '610100', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2831, '2020-03-29 14:20:36.000', 'system.region.area.610102', '新城区', NULL, '610102', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2832, '2020-03-29 14:20:36.000', 'system.region.area.610103', '碑林区', NULL, '610103', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2833, '2020-03-29 14:20:36.000', 'system.region.area.610104', '莲湖区', NULL, '610104', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2834, '2020-03-29 14:20:36.000', 'system.region.area.610111', '灞桥区', NULL, '610111', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2835, '2020-03-29 14:20:36.000', 'system.region.area.610112', '未央区', NULL, '610112', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2836, '2020-03-29 14:20:36.000', 'system.region.area.610113', '雁塔区', NULL, '610113', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2837, '2020-03-29 14:20:36.000', 'system.region.area.610114', '阎良区', NULL, '610114', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2838, '2020-03-29 14:20:36.000', 'system.region.area.610115', '临潼区', NULL, '610115', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2839, '2020-03-29 14:20:36.000', 'system.region.area.610116', '长安区', NULL, '610116', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2840, '2020-03-29 14:20:36.000', 'system.region.area.610117', '高陵区', NULL, '610117', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2841, '2020-03-29 14:20:36.000', 'system.region.area.610118', '鄠邑区', NULL, '610118', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2842, '2020-03-29 14:20:36.000', 'system.region.area.610122', '蓝田县', NULL, '610122', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2843, '2020-03-29 14:20:36.000', 'system.region.area.610124', '周至县', NULL, '610124', 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2844, '2020-03-29 14:20:36.000', 'system.region.city.610200', '铜川市', 'area', '610200', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2845, '2020-03-29 14:20:36.000', 'system.region.area.610202', '王益区', NULL, '610202', 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2846, '2020-03-29 14:20:36.000', 'system.region.area.610203', '印台区', NULL, '610203', 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2847, '2020-03-29 14:20:36.000', 'system.region.area.610204', '耀州区', NULL, '610204', 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2848, '2020-03-29 14:20:36.000', 'system.region.area.610222', '宜君县', NULL, '610222', 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2849, '2020-03-29 14:20:36.000', 'system.region.city.610300', '宝鸡市', 'area', '610300', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2850, '2020-03-29 14:20:36.000', 'system.region.area.610302', '渭滨区', NULL, '610302', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2851, '2020-03-29 14:20:36.000', 'system.region.area.610303', '金台区', NULL, '610303', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2852, '2020-03-29 14:20:36.000', 'system.region.area.610304', '陈仓区', NULL, '610304', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2853, '2020-03-29 14:20:36.000', 'system.region.area.610322', '凤翔县', NULL, '610322', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2854, '2020-03-29 14:20:36.000', 'system.region.area.610323', '岐山县', NULL, '610323', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2855, '2020-03-29 14:20:36.000', 'system.region.area.610324', '扶风县', NULL, '610324', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2856, '2020-03-29 14:20:36.000', 'system.region.area.610326', '眉县', NULL, '610326', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2857, '2020-03-29 14:20:36.000', 'system.region.area.610327', '陇县', NULL, '610327', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2858, '2020-03-29 14:20:36.000', 'system.region.area.610328', '千阳县', NULL, '610328', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2859, '2020-03-29 14:20:36.000', 'system.region.area.610329', '麟游县', NULL, '610329', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2860, '2020-03-29 14:20:36.000', 'system.region.area.610330', '凤县', NULL, '610330', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2861, '2020-03-29 14:20:36.000', 'system.region.area.610331', '太白县', NULL, '610331', 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2862, '2020-03-29 14:20:36.000', 'system.region.city.610400', '咸阳市', 'area', '610400', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2863, '2020-03-29 14:20:36.000', 'system.region.area.610402', '秦都区', NULL, '610402', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2864, '2020-03-29 14:20:36.000', 'system.region.area.610403', '杨陵区', NULL, '610403', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2865, '2020-03-29 14:20:36.000', 'system.region.area.610404', '渭城区', NULL, '610404', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2866, '2020-03-29 14:20:36.000', 'system.region.area.610422', '三原县', NULL, '610422', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2867, '2020-03-29 14:20:36.000', 'system.region.area.610423', '泾阳县', NULL, '610423', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2868, '2020-03-29 14:20:36.000', 'system.region.area.610424', '乾县', NULL, '610424', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2869, '2020-03-29 14:20:36.000', 'system.region.area.610425', '礼泉县', NULL, '610425', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2870, '2020-03-29 14:20:36.000', 'system.region.area.610426', '永寿县', NULL, '610426', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2871, '2020-03-29 14:20:36.000', 'system.region.area.610428', '长武县', NULL, '610428', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2872, '2020-03-29 14:20:36.000', 'system.region.area.610429', '旬邑县', NULL, '610429', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2873, '2020-03-29 14:20:36.000', 'system.region.area.610430', '淳化县', NULL, '610430', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2874, '2020-03-29 14:20:36.000', 'system.region.area.610431', '武功县', NULL, '610431', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2875, '2020-03-29 14:20:36.000', 'system.region.area.610481', '兴平市', NULL, '610481', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2876, '2020-03-29 14:20:36.000', 'system.region.area.610482', '彬州市', NULL, '610482', 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2877, '2020-03-29 14:20:36.000', 'system.region.city.610500', '渭南市', 'area', '610500', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2878, '2020-03-29 14:20:36.000', 'system.region.area.610502', '临渭区', NULL, '610502', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2879, '2020-03-29 14:20:36.000', 'system.region.area.610503', '华州区', NULL, '610503', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2880, '2020-03-29 14:20:36.000', 'system.region.area.610522', '潼关县', NULL, '610522', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2881, '2020-03-29 14:20:36.000', 'system.region.area.610523', '大荔县', NULL, '610523', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2882, '2020-03-29 14:20:36.000', 'system.region.area.610524', '合阳县', NULL, '610524', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2883, '2020-03-29 14:20:36.000', 'system.region.area.610525', '澄城县', NULL, '610525', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2884, '2020-03-29 14:20:36.000', 'system.region.area.610526', '蒲城县', NULL, '610526', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2885, '2020-03-29 14:20:36.000', 'system.region.area.610527', '白水县', NULL, '610527', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2886, '2020-03-29 14:20:36.000', 'system.region.area.610528', '富平县', NULL, '610528', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2887, '2020-03-29 14:20:36.000', 'system.region.area.610581', '韩城市', NULL, '610581', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2888, '2020-03-29 14:20:36.000', 'system.region.area.610582', '华阴市', NULL, '610582', 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2889, '2020-03-29 14:20:36.000', 'system.region.city.610600', '延安市', 'area', '610600', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2890, '2020-03-29 14:20:36.000', 'system.region.area.610602', '宝塔区', NULL, '610602', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2891, '2020-03-29 14:20:36.000', 'system.region.area.610603', '安塞区', NULL, '610603', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2892, '2020-03-29 14:20:36.000', 'system.region.area.610621', '延长县', NULL, '610621', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2893, '2020-03-29 14:20:36.000', 'system.region.area.610622', '延川县', NULL, '610622', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2894, '2020-03-29 14:20:36.000', 'system.region.area.610625', '志丹县', NULL, '610625', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2895, '2020-03-29 14:20:36.000', 'system.region.area.610626', '吴起县', NULL, '610626', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2896, '2020-03-29 14:20:36.000', 'system.region.area.610627', '甘泉县', NULL, '610627', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2897, '2020-03-29 14:20:36.000', 'system.region.area.610628', '富县', NULL, '610628', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2898, '2020-03-29 14:20:36.000', 'system.region.area.610629', '洛川县', NULL, '610629', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2899, '2020-03-29 14:20:36.000', 'system.region.area.610630', '宜川县', NULL, '610630', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2900, '2020-03-29 14:20:36.000', 'system.region.area.610631', '黄龙县', NULL, '610631', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2901, '2020-03-29 14:20:36.000', 'system.region.area.610632', '黄陵县', NULL, '610632', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2902, '2020-03-29 14:20:36.000', 'system.region.area.610681', '子长市', NULL, '610681', 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2903, '2020-03-29 14:20:36.000', 'system.region.city.610700', '汉中市', 'area', '610700', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2904, '2020-03-29 14:20:36.000', 'system.region.area.610702', '汉台区', NULL, '610702', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2905, '2020-03-29 14:20:36.000', 'system.region.area.610703', '南郑区', NULL, '610703', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2906, '2020-03-29 14:20:36.000', 'system.region.area.610722', '城固县', NULL, '610722', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2907, '2020-03-29 14:20:36.000', 'system.region.area.610723', '洋县', NULL, '610723', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2908, '2020-03-29 14:20:36.000', 'system.region.area.610724', '西乡县', NULL, '610724', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2909, '2020-03-29 14:20:36.000', 'system.region.area.610725', '勉县', NULL, '610725', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2910, '2020-03-29 14:20:36.000', 'system.region.area.610726', '宁强县', NULL, '610726', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2911, '2020-03-29 14:20:36.000', 'system.region.area.610727', '略阳县', NULL, '610727', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2912, '2020-03-29 14:20:36.000', 'system.region.area.610728', '镇巴县', NULL, '610728', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2913, '2020-03-29 14:20:36.000', 'system.region.area.610729', '留坝县', NULL, '610729', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2914, '2020-03-29 14:20:36.000', 'system.region.area.610730', '佛坪县', NULL, '610730', 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2915, '2020-03-29 14:20:36.000', 'system.region.city.610800', '榆林市', 'area', '610800', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2916, '2020-03-29 14:20:36.000', 'system.region.area.610802', '榆阳区', NULL, '610802', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2917, '2020-03-29 14:20:36.000', 'system.region.area.610803', '横山区', NULL, '610803', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2918, '2020-03-29 14:20:36.000', 'system.region.area.610822', '府谷县', NULL, '610822', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2919, '2020-03-29 14:20:36.000', 'system.region.area.610824', '靖边县', NULL, '610824', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2920, '2020-03-29 14:20:36.000', 'system.region.area.610825', '定边县', NULL, '610825', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2921, '2020-03-29 14:20:36.000', 'system.region.area.610826', '绥德县', NULL, '610826', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2922, '2020-03-29 14:20:36.000', 'system.region.area.610827', '米脂县', NULL, '610827', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2923, '2020-03-29 14:20:36.000', 'system.region.area.610828', '佳县', NULL, '610828', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2924, '2020-03-29 14:20:36.000', 'system.region.area.610829', '吴堡县', NULL, '610829', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2925, '2020-03-29 14:20:36.000', 'system.region.area.610830', '清涧县', NULL, '610830', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2926, '2020-03-29 14:20:36.000', 'system.region.area.610831', '子洲县', NULL, '610831', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2927, '2020-03-29 14:20:36.000', 'system.region.area.610881', '神木市', NULL, '610881', 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2928, '2020-03-29 14:20:36.000', 'system.region.city.610900', '安康市', 'area', '610900', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2929, '2020-03-29 14:20:36.000', 'system.region.area.610902', '汉滨区', NULL, '610902', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2930, '2020-03-29 14:20:36.000', 'system.region.area.610921', '汉阴县', NULL, '610921', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2931, '2020-03-29 14:20:36.000', 'system.region.area.610922', '石泉县', NULL, '610922', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2932, '2020-03-29 14:20:36.000', 'system.region.area.610923', '宁陕县', NULL, '610923', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2933, '2020-03-29 14:20:36.000', 'system.region.area.610924', '紫阳县', NULL, '610924', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2934, '2020-03-29 14:20:36.000', 'system.region.area.610925', '岚皋县', NULL, '610925', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2935, '2020-03-29 14:20:36.000', 'system.region.area.610926', '平利县', NULL, '610926', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2936, '2020-03-29 14:20:36.000', 'system.region.area.610927', '镇坪县', NULL, '610927', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2937, '2020-03-29 14:20:36.000', 'system.region.area.610928', '旬阳县', NULL, '610928', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2938, '2020-03-29 14:20:36.000', 'system.region.area.610929', '白河县', NULL, '610929', 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2939, '2020-03-29 14:20:36.000', 'system.region.city.611000', '商洛市', 'area', '611000', 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2940, '2020-03-29 14:20:36.000', 'system.region.area.611002', '商州区', NULL, '611002', 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2941, '2020-03-29 14:20:36.000', 'system.region.area.611021', '洛南县', NULL, '611021', 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2942, '2020-03-29 14:20:36.000', 'system.region.area.611022', '丹凤县', NULL, '611022', 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2943, '2020-03-29 14:20:36.000', 'system.region.area.611023', '商南县', NULL, '611023', 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2944, '2020-03-29 14:20:36.000', 'system.region.area.611024', '山阳县', NULL, '611024', 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2945, '2020-03-29 14:20:36.000', 'system.region.area.611025', '镇安县', NULL, '611025', 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2946, '2020-03-29 14:20:36.000', 'system.region.area.611026', '柞水县', NULL, '611026', 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2947, '2020-03-29 14:20:36.000', 'system.region.province.620000', '甘肃省', 'city', '620000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2948, '2020-03-29 14:20:36.000', 'system.region.city.620100', '兰州市', 'area', '620100', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2949, '2020-03-29 14:20:36.000', 'system.region.area.620102', '城关区', NULL, '620102', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2950, '2020-03-29 14:20:36.000', 'system.region.area.620103', '七里河区', NULL, '620103', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2951, '2020-03-29 14:20:36.000', 'system.region.area.620104', '西固区', NULL, '620104', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2952, '2020-03-29 14:20:36.000', 'system.region.area.620105', '安宁区', NULL, '620105', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2953, '2020-03-29 14:20:36.000', 'system.region.area.620111', '红古区', NULL, '620111', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2954, '2020-03-29 14:20:36.000', 'system.region.area.620121', '永登县', NULL, '620121', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2955, '2020-03-29 14:20:36.000', 'system.region.area.620122', '皋兰县', NULL, '620122', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2956, '2020-03-29 14:20:36.000', 'system.region.area.620123', '榆中县', NULL, '620123', 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2957, '2020-03-29 14:20:36.000', 'system.region.city.620200', '嘉峪关市', 'area', '620200', 1, 15, 2947, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2958, '2020-03-29 14:20:36.000', 'system.region.city.620300', '金昌市', 'area', '620300', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2959, '2020-03-29 14:20:36.000', 'system.region.area.620302', '金川区', NULL, '620302', 1, 16, 2958, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2960, '2020-03-29 14:20:36.000', 'system.region.area.620321', '永昌县', NULL, '620321', 1, 16, 2958, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2961, '2020-03-29 14:20:36.000', 'system.region.city.620400', '白银市', 'area', '620400', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2962, '2020-03-29 14:20:36.000', 'system.region.area.620402', '白银区', NULL, '620402', 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2963, '2020-03-29 14:20:36.000', 'system.region.area.620403', '平川区', NULL, '620403', 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2964, '2020-03-29 14:20:36.000', 'system.region.area.620421', '靖远县', NULL, '620421', 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2965, '2020-03-29 14:20:36.000', 'system.region.area.620422', '会宁县', NULL, '620422', 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2966, '2020-03-29 14:20:36.000', 'system.region.area.620423', '景泰县', NULL, '620423', 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2967, '2020-03-29 14:20:36.000', 'system.region.city.620500', '天水市', 'area', '620500', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2968, '2020-03-29 14:20:36.000', 'system.region.area.620502', '秦州区', NULL, '620502', 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2969, '2020-03-29 14:20:36.000', 'system.region.area.620503', '麦积区', NULL, '620503', 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2970, '2020-03-29 14:20:36.000', 'system.region.area.620521', '清水县', NULL, '620521', 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2971, '2020-03-29 14:20:36.000', 'system.region.area.620522', '秦安县', NULL, '620522', 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2972, '2020-03-29 14:20:36.000', 'system.region.area.620523', '甘谷县', NULL, '620523', 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2973, '2020-03-29 14:20:36.000', 'system.region.area.620524', '武山县', NULL, '620524', 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2974, '2020-03-29 14:20:36.000', 'system.region.area.620525', '张家川回族自治县', NULL, '620525', 1, 16, 2967, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2975, '2020-03-29 14:20:36.000', 'system.region.city.620600', '武威市', 'area', '620600', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2976, '2020-03-29 14:20:36.000', 'system.region.area.620602', '凉州区', NULL, '620602', 1, 16, 2975, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2977, '2020-03-29 14:20:36.000', 'system.region.area.620621', '民勤县', NULL, '620621', 1, 16, 2975, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2978, '2020-03-29 14:20:36.000', 'system.region.area.620622', '古浪县', NULL, '620622', 1, 16, 2975, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2979, '2020-03-29 14:20:36.000', 'system.region.area.620623', '天祝藏族自治县', NULL, '620623', 1, 16, 2975, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2980, '2020-03-29 14:20:36.000', 'system.region.city.620700', '张掖市', 'area', '620700', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2981, '2020-03-29 14:20:36.000', 'system.region.area.620702', '甘州区', NULL, '620702', 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2982, '2020-03-29 14:20:36.000', 'system.region.area.620721', '肃南裕固族自治县', NULL, '620721', 1, 16, 2980, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2983, '2020-03-29 14:20:36.000', 'system.region.area.620722', '民乐县', NULL, '620722', 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2984, '2020-03-29 14:20:36.000', 'system.region.area.620723', '临泽县', NULL, '620723', 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2985, '2020-03-29 14:20:36.000', 'system.region.area.620724', '高台县', NULL, '620724', 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2986, '2020-03-29 14:20:36.000', 'system.region.area.620725', '山丹县', NULL, '620725', 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2987, '2020-03-29 14:20:36.000', 'system.region.city.620800', '平凉市', 'area', '620800', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2988, '2020-03-29 14:20:36.000', 'system.region.area.620802', '崆峒区', NULL, '620802', 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2989, '2020-03-29 14:20:36.000', 'system.region.area.620821', '泾川县', NULL, '620821', 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2990, '2020-03-29 14:20:36.000', 'system.region.area.620822', '灵台县', NULL, '620822', 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2991, '2020-03-29 14:20:36.000', 'system.region.area.620823', '崇信县', NULL, '620823', 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2992, '2020-03-29 14:20:36.000', 'system.region.area.620825', '庄浪县', NULL, '620825', 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2993, '2020-03-29 14:20:36.000', 'system.region.area.620826', '静宁县', NULL, '620826', 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2994, '2020-03-29 14:20:36.000', 'system.region.area.620881', '华亭市', NULL, '620881', 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2995, '2020-03-29 14:20:36.000', 'system.region.city.620900', '酒泉市', 'area', '620900', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2996, '2020-03-29 14:20:36.000', 'system.region.area.620902', '肃州区', NULL, '620902', 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2997, '2020-03-29 14:20:36.000', 'system.region.area.620921', '金塔县', NULL, '620921', 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2998, '2020-03-29 14:20:36.000', 'system.region.area.620922', '瓜州县', NULL, '620922', 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (2999, '2020-03-29 14:20:36.000', 'system.region.area.620923', '肃北蒙古族自治县', NULL, '620923', 1, 16, 2995, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3000, '2020-03-29 14:20:36.000', 'system.region.area.620924', '阿克塞哈萨克族自治县', NULL, '620924', 1, 16, 2995, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3001, '2020-03-29 14:20:36.000', 'system.region.area.620981', '玉门市', NULL, '620981', 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3002, '2020-03-29 14:20:36.000', 'system.region.area.620982', '敦煌市', NULL, '620982', 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3003, '2020-03-29 14:20:36.000', 'system.region.city.621000', '庆阳市', 'area', '621000', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3004, '2020-03-29 14:20:36.000', 'system.region.area.621002', '西峰区', NULL, '621002', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3005, '2020-03-29 14:20:36.000', 'system.region.area.621021', '庆城县', NULL, '621021', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3006, '2020-03-29 14:20:36.000', 'system.region.area.621022', '环县', NULL, '621022', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3007, '2020-03-29 14:20:36.000', 'system.region.area.621023', '华池县', NULL, '621023', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3008, '2020-03-29 14:20:36.000', 'system.region.area.621024', '合水县', NULL, '621024', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3009, '2020-03-29 14:20:36.000', 'system.region.area.621025', '正宁县', NULL, '621025', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3010, '2020-03-29 14:20:36.000', 'system.region.area.621026', '宁县', NULL, '621026', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3011, '2020-03-29 14:20:36.000', 'system.region.area.621027', '镇原县', NULL, '621027', 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3012, '2020-03-29 14:20:36.000', 'system.region.city.621100', '定西市', 'area', '621100', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3013, '2020-03-29 14:20:36.000', 'system.region.area.621102', '安定区', NULL, '621102', 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3014, '2020-03-29 14:20:36.000', 'system.region.area.621121', '通渭县', NULL, '621121', 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3015, '2020-03-29 14:20:36.000', 'system.region.area.621122', '陇西县', NULL, '621122', 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3016, '2020-03-29 14:20:36.000', 'system.region.area.621123', '渭源县', NULL, '621123', 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3017, '2020-03-29 14:20:36.000', 'system.region.area.621124', '临洮县', NULL, '621124', 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3018, '2020-03-29 14:20:36.000', 'system.region.area.621125', '漳县', NULL, '621125', 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3019, '2020-03-29 14:20:36.000', 'system.region.area.621126', '岷县', NULL, '621126', 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3020, '2020-03-29 14:20:36.000', 'system.region.city.621200', '陇南市', 'area', '621200', 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3021, '2020-03-29 14:20:36.000', 'system.region.area.621202', '武都区', NULL, '621202', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3022, '2020-03-29 14:20:36.000', 'system.region.area.621221', '成县', NULL, '621221', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3023, '2020-03-29 14:20:36.000', 'system.region.area.621222', '文县', NULL, '621222', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3024, '2020-03-29 14:20:36.000', 'system.region.area.621223', '宕昌县', NULL, '621223', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3025, '2020-03-29 14:20:36.000', 'system.region.area.621224', '康县', NULL, '621224', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3026, '2020-03-29 14:20:36.000', 'system.region.area.621225', '西和县', NULL, '621225', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3027, '2020-03-29 14:20:36.000', 'system.region.area.621226', '礼县', NULL, '621226', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3028, '2020-03-29 14:20:36.000', 'system.region.area.621227', '徽县', NULL, '621227', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3029, '2020-03-29 14:20:36.000', 'system.region.area.621228', '两当县', NULL, '621228', 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3030, '2020-03-29 14:20:36.000', 'system.region.city.622900', '临夏回族自治州', 'area', '622900', 1, 15, 2947, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3031, '2020-03-29 14:20:36.000', 'system.region.area.622901', '临夏市', NULL, '622901', 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3032, '2020-03-29 14:20:36.000', 'system.region.area.622921', '临夏县', NULL, '622921', 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3033, '2020-03-29 14:20:36.000', 'system.region.area.622922', '康乐县', NULL, '622922', 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3034, '2020-03-29 14:20:36.000', 'system.region.area.622923', '永靖县', NULL, '622923', 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3035, '2020-03-29 14:20:36.000', 'system.region.area.622924', '广河县', NULL, '622924', 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3036, '2020-03-29 14:20:36.000', 'system.region.area.622925', '和政县', NULL, '622925', 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3037, '2020-03-29 14:20:36.000', 'system.region.area.622926', '东乡族自治县', NULL, '622926', 1, 16, 3030, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3038, '2020-03-29 14:20:36.000', 'system.region.area.622927', '积石山保安族东乡族撒拉族自治县', NULL, '622927', 1, 16, 3030,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3039, '2020-03-29 14:20:36.000', 'system.region.city.623000', '甘南藏族自治州', 'area', '623000', 1, 15, 2947, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3040, '2020-03-29 14:20:36.000', 'system.region.area.623001', '合作市', NULL, '623001', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3041, '2020-03-29 14:20:36.000', 'system.region.area.623021', '临潭县', NULL, '623021', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3042, '2020-03-29 14:20:36.000', 'system.region.area.623022', '卓尼县', NULL, '623022', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3043, '2020-03-29 14:20:36.000', 'system.region.area.623023', '舟曲县', NULL, '623023', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3044, '2020-03-29 14:20:36.000', 'system.region.area.623024', '迭部县', NULL, '623024', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3045, '2020-03-29 14:20:36.000', 'system.region.area.623025', '玛曲县', NULL, '623025', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3046, '2020-03-29 14:20:36.000', 'system.region.area.623026', '碌曲县', NULL, '623026', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3047, '2020-03-29 14:20:36.000', 'system.region.area.623027', '夏河县', NULL, '623027', 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3048, '2020-03-29 14:20:36.000', 'system.region.province.630000', '青海省', 'city', '630000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3049, '2020-03-29 14:20:36.000', 'system.region.city.630100', '西宁市', 'area', '630100', 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3050, '2020-03-29 14:20:36.000', 'system.region.area.630102', '城东区', NULL, '630102', 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3051, '2020-03-29 14:20:36.000', 'system.region.area.630103', '城中区', NULL, '630103', 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3052, '2020-03-29 14:20:36.000', 'system.region.area.630104', '城西区', NULL, '630104', 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3053, '2020-03-29 14:20:36.000', 'system.region.area.630105', '城北区', NULL, '630105', 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3054, '2020-03-29 14:20:36.000', 'system.region.area.630106', '湟中区', NULL, '630106', 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3055, '2020-03-29 14:20:36.000', 'system.region.area.630121', '大通回族土族自治县', NULL, '630121', 1, 16, 3049, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3056, '2020-03-29 14:20:36.000', 'system.region.area.630123', '湟源县', NULL, '630123', 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3057, '2020-03-29 14:20:36.000', 'system.region.city.630200', '海东市', 'area', '630200', 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3058, '2020-03-29 14:20:36.000', 'system.region.area.630202', '乐都区', NULL, '630202', 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3059, '2020-03-29 14:20:36.000', 'system.region.area.630203', '平安区', NULL, '630203', 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3060, '2020-03-29 14:20:36.000', 'system.region.area.630222', '民和回族土族自治县', NULL, '630222', 1, 16, 3057, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3061, '2020-03-29 14:20:36.000', 'system.region.area.630223', '互助土族自治县', NULL, '630223', 1, 16, 3057, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3062, '2020-03-29 14:20:36.000', 'system.region.area.630224', '化隆回族自治县', NULL, '630224', 1, 16, 3057, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3063, '2020-03-29 14:20:36.000', 'system.region.area.630225', '循化撒拉族自治县', NULL, '630225', 1, 16, 3057, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3064, '2020-03-29 14:20:36.000', 'system.region.city.632200', '海北藏族自治州', 'area', '632200', 1, 15, 3048, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3065, '2020-03-29 14:20:36.000', 'system.region.area.632221', '门源回族自治县', NULL, '632221', 1, 16, 3064, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3066, '2020-03-29 14:20:36.000', 'system.region.area.632222', '祁连县', NULL, '632222', 1, 16, 3064, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3067, '2020-03-29 14:20:36.000', 'system.region.area.632223', '海晏县', NULL, '632223', 1, 16, 3064, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3068, '2020-03-29 14:20:36.000', 'system.region.area.632224', '刚察县', NULL, '632224', 1, 16, 3064, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3069, '2020-03-29 14:20:36.000', 'system.region.city.632300', '黄南藏族自治州', 'area', '632300', 1, 15, 3048, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3070, '2020-03-29 14:20:36.000', 'system.region.area.632321', '同仁县', NULL, '632321', 1, 16, 3069, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3071, '2020-03-29 14:20:36.000', 'system.region.area.632322', '尖扎县', NULL, '632322', 1, 16, 3069, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3072, '2020-03-29 14:20:36.000', 'system.region.area.632323', '泽库县', NULL, '632323', 1, 16, 3069, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3073, '2020-03-29 14:20:36.000', 'system.region.area.632324', '河南蒙古族自治县', NULL, '632324', 1, 16, 3069, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3074, '2020-03-29 14:20:36.000', 'system.region.city.632500', '海南藏族自治州', 'area', '632500', 1, 15, 3048, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3075, '2020-03-29 14:20:36.000', 'system.region.area.632521', '共和县', NULL, '632521', 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3076, '2020-03-29 14:20:36.000', 'system.region.area.632522', '同德县', NULL, '632522', 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3077, '2020-03-29 14:20:36.000', 'system.region.area.632523', '贵德县', NULL, '632523', 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3078, '2020-03-29 14:20:36.000', 'system.region.area.632524', '兴海县', NULL, '632524', 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3079, '2020-03-29 14:20:36.000', 'system.region.area.632525', '贵南县', NULL, '632525', 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3080, '2020-03-29 14:20:36.000', 'system.region.city.632600', '果洛藏族自治州', 'area', '632600', 1, 15, 3048, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3081, '2020-03-29 14:20:36.000', 'system.region.area.632621', '玛沁县', NULL, '632621', 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3082, '2020-03-29 14:20:36.000', 'system.region.area.632622', '班玛县', NULL, '632622', 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3083, '2020-03-29 14:20:36.000', 'system.region.area.632623', '甘德县', NULL, '632623', 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3084, '2020-03-29 14:20:36.000', 'system.region.area.632624', '达日县', NULL, '632624', 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3085, '2020-03-29 14:20:36.000', 'system.region.area.632625', '久治县', NULL, '632625', 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3086, '2020-03-29 14:20:36.000', 'system.region.area.632626', '玛多县', NULL, '632626', 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3087, '2020-03-29 14:20:36.000', 'system.region.city.632700', '玉树藏族自治州', 'area', '632700', 1, 15, 3048, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3088, '2020-03-29 14:20:36.000', 'system.region.area.632701', '玉树市', NULL, '632701', 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3089, '2020-03-29 14:20:36.000', 'system.region.area.632722', '杂多县', NULL, '632722', 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3090, '2020-03-29 14:20:36.000', 'system.region.area.632723', '称多县', NULL, '632723', 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3091, '2020-03-29 14:20:36.000', 'system.region.area.632724', '治多县', NULL, '632724', 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3092, '2020-03-29 14:20:36.000', 'system.region.area.632725', '囊谦县', NULL, '632725', 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3093, '2020-03-29 14:20:36.000', 'system.region.area.632726', '曲麻莱县', NULL, '632726', 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3094, '2020-03-29 14:20:36.000', 'system.region.city.632800', '海西蒙古族藏族自治州', 'area', '632800', 1, 15, 3048, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3095, '2020-03-29 14:20:36.000', 'system.region.area.632801', '格尔木市', NULL, '632801', 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3096, '2020-03-29 14:20:36.000', 'system.region.area.632802', '德令哈市', NULL, '632802', 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3097, '2020-03-29 14:20:36.000', 'system.region.area.632803', '茫崖市', NULL, '632803', 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3098, '2020-03-29 14:20:36.000', 'system.region.area.632821', '乌兰县', NULL, '632821', 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3099, '2020-03-29 14:20:36.000', 'system.region.area.632822', '都兰县', NULL, '632822', 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3100, '2020-03-29 14:20:36.000', 'system.region.area.632823', '天峻县', NULL, '632823', 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3101, '2020-03-29 14:20:36.000', 'system.region.province.640000', '宁夏回族自治区', 'city', '640000', 1, 14, NULL,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3102, '2020-03-29 14:20:36.000', 'system.region.city.640100', '银川市', 'area', '640100', 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3103, '2020-03-29 14:20:36.000', 'system.region.area.640104', '兴庆区', NULL, '640104', 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3104, '2020-03-29 14:20:36.000', 'system.region.area.640105', '西夏区', NULL, '640105', 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3105, '2020-03-29 14:20:36.000', 'system.region.area.640106', '金凤区', NULL, '640106', 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3106, '2020-03-29 14:20:36.000', 'system.region.area.640121', '永宁县', NULL, '640121', 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3107, '2020-03-29 14:20:36.000', 'system.region.area.640122', '贺兰县', NULL, '640122', 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3108, '2020-03-29 14:20:36.000', 'system.region.area.640181', '灵武市', NULL, '640181', 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3109, '2020-03-29 14:20:36.000', 'system.region.city.640200', '石嘴山市', 'area', '640200', 1, 15, 3101, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3110, '2020-03-29 14:20:36.000', 'system.region.area.640202', '大武口区', NULL, '640202', 1, 16, 3109, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3111, '2020-03-29 14:20:36.000', 'system.region.area.640205', '惠农区', NULL, '640205', 1, 16, 3109, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3112, '2020-03-29 14:20:36.000', 'system.region.area.640221', '平罗县', NULL, '640221', 1, 16, 3109, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3113, '2020-03-29 14:20:36.000', 'system.region.city.640300', '吴忠市', 'area', '640300', 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3114, '2020-03-29 14:20:36.000', 'system.region.area.640302', '利通区', NULL, '640302', 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3115, '2020-03-29 14:20:36.000', 'system.region.area.640303', '红寺堡区', NULL, '640303', 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3116, '2020-03-29 14:20:36.000', 'system.region.area.640323', '盐池县', NULL, '640323', 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3117, '2020-03-29 14:20:36.000', 'system.region.area.640324', '同心县', NULL, '640324', 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3118, '2020-03-29 14:20:36.000', 'system.region.area.640381', '青铜峡市', NULL, '640381', 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3119, '2020-03-29 14:20:36.000', 'system.region.city.640400', '固原市', 'area', '640400', 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3120, '2020-03-29 14:20:36.000', 'system.region.area.640402', '原州区', NULL, '640402', 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3121, '2020-03-29 14:20:36.000', 'system.region.area.640422', '西吉县', NULL, '640422', 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3122, '2020-03-29 14:20:36.000', 'system.region.area.640423', '隆德县', NULL, '640423', 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3123, '2020-03-29 14:20:36.000', 'system.region.area.640424', '泾源县', NULL, '640424', 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3124, '2020-03-29 14:20:36.000', 'system.region.area.640425', '彭阳县', NULL, '640425', 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3125, '2020-03-29 14:20:36.000', 'system.region.city.640500', '中卫市', 'area', '640500', 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3126, '2020-03-29 14:20:36.000', 'system.region.area.640502', '沙坡头区', NULL, '640502', 1, 16, 3125, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3127, '2020-03-29 14:20:36.000', 'system.region.area.640521', '中宁县', NULL, '640521', 1, 16, 3125, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3128, '2020-03-29 14:20:36.000', 'system.region.area.640522', '海原县', NULL, '640522', 1, 16, 3125, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3129, '2020-03-29 14:20:36.000', 'system.region.province.650000', '新疆维吾尔自治区', 'city', '650000', 1, 14, NULL,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3130, '2020-03-29 14:20:36.000', 'system.region.city.650100', '乌鲁木齐市', 'area', '650100', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3131, '2020-03-29 14:20:36.000', 'system.region.area.650102', '天山区', NULL, '650102', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3132, '2020-03-29 14:20:36.000', 'system.region.area.650103', '沙依巴克区', NULL, '650103', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3133, '2020-03-29 14:20:36.000', 'system.region.area.650104', '新市区', NULL, '650104', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3134, '2020-03-29 14:20:36.000', 'system.region.area.650105', '水磨沟区', NULL, '650105', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3135, '2020-03-29 14:20:36.000', 'system.region.area.650106', '头屯河区', NULL, '650106', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3136, '2020-03-29 14:20:36.000', 'system.region.area.650107', '达坂城区', NULL, '650107', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3137, '2020-03-29 14:20:36.000', 'system.region.area.650109', '米东区', NULL, '650109', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3138, '2020-03-29 14:20:36.000', 'system.region.area.650121', '乌鲁木齐县', NULL, '650121', 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3139, '2020-03-29 14:20:36.000', 'system.region.city.650200', '克拉玛依市', 'area', '650200', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3140, '2020-03-29 14:20:36.000', 'system.region.area.650202', '独山子区', NULL, '650202', 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3141, '2020-03-29 14:20:36.000', 'system.region.area.650203', '克拉玛依区', NULL, '650203', 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3142, '2020-03-29 14:20:36.000', 'system.region.area.650204', '白碱滩区', NULL, '650204', 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3143, '2020-03-29 14:20:36.000', 'system.region.area.650205', '乌尔禾区', NULL, '650205', 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3144, '2020-03-29 14:20:36.000', 'system.region.city.650400', '吐鲁番市', 'area', '650400', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3145, '2020-03-29 14:20:36.000', 'system.region.area.650402', '高昌区', NULL, '650402', 1, 16, 3144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3146, '2020-03-29 14:20:36.000', 'system.region.area.650421', '鄯善县', NULL, '650421', 1, 16, 3144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3147, '2020-03-29 14:20:36.000', 'system.region.area.650422', '托克逊县', NULL, '650422', 1, 16, 3144, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3148, '2020-03-29 14:20:36.000', 'system.region.city.650500', '哈密市', 'area', '650500', 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3149, '2020-03-29 14:20:36.000', 'system.region.area.650502', '伊州区', NULL, '650502', 1, 16, 3148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3150, '2020-03-29 14:20:36.000', 'system.region.area.650521', '巴里坤哈萨克自治县', NULL, '650521', 1, 16, 3148, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3151, '2020-03-29 14:20:36.000', 'system.region.area.650522', '伊吾县', NULL, '650522', 1, 16, 3148, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3152, '2020-03-29 14:20:36.000', 'system.region.city.652300', '昌吉回族自治州', 'area', '652300', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3153, '2020-03-29 14:20:36.000', 'system.region.area.652301', '昌吉市', NULL, '652301', 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3154, '2020-03-29 14:20:36.000', 'system.region.area.652302', '阜康市', NULL, '652302', 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3155, '2020-03-29 14:20:36.000', 'system.region.area.652323', '呼图壁县', NULL, '652323', 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3156, '2020-03-29 14:20:36.000', 'system.region.area.652324', '玛纳斯县', NULL, '652324', 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3157, '2020-03-29 14:20:36.000', 'system.region.area.652325', '奇台县', NULL, '652325', 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3158, '2020-03-29 14:20:36.000', 'system.region.area.652327', '吉木萨尔县', NULL, '652327', 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3159, '2020-03-29 14:20:36.000', 'system.region.area.652328', '木垒哈萨克自治县', NULL, '652328', 1, 16, 3152, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3160, '2020-03-29 14:20:36.000', 'system.region.city.652700', '博尔塔拉蒙古自治州', 'area', '652700', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3161, '2020-03-29 14:20:36.000', 'system.region.area.652701', '博乐市', NULL, '652701', 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3162, '2020-03-29 14:20:36.000', 'system.region.area.652702', '阿拉山口市', NULL, '652702', 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3163, '2020-03-29 14:20:36.000', 'system.region.area.652722', '精河县', NULL, '652722', 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3164, '2020-03-29 14:20:36.000', 'system.region.area.652723', '温泉县', NULL, '652723', 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3165, '2020-03-29 14:20:36.000', 'system.region.city.652800', '巴音郭楞蒙古自治州', 'area', '652800', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3166, '2020-03-29 14:20:36.000', 'system.region.area.652801', '库尔勒市', NULL, '652801', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3167, '2020-03-29 14:20:36.000', 'system.region.area.652822', '轮台县', NULL, '652822', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3168, '2020-03-29 14:20:36.000', 'system.region.area.652823', '尉犁县', NULL, '652823', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3169, '2020-03-29 14:20:36.000', 'system.region.area.652824', '若羌县', NULL, '652824', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3170, '2020-03-29 14:20:36.000', 'system.region.area.652825', '且末县', NULL, '652825', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3171, '2020-03-29 14:20:36.000', 'system.region.area.652826', '焉耆回族自治县', NULL, '652826', 1, 16, 3165, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3172, '2020-03-29 14:20:36.000', 'system.region.area.652827', '和静县', NULL, '652827', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3173, '2020-03-29 14:20:36.000', 'system.region.area.652828', '和硕县', NULL, '652828', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3174, '2020-03-29 14:20:36.000', 'system.region.area.652829', '博湖县', NULL, '652829', 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3175, '2020-03-29 14:20:36.000', 'system.region.city.652900', '阿克苏地区', 'area', '652900', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3176, '2020-03-29 14:20:36.000', 'system.region.area.652901', '阿克苏市', NULL, '652901', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3177, '2020-03-29 14:20:36.000', 'system.region.area.652902', '库车市', NULL, '652902', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3178, '2020-03-29 14:20:36.000', 'system.region.area.652922', '温宿县', NULL, '652922', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3179, '2020-03-29 14:20:36.000', 'system.region.area.652924', '沙雅县', NULL, '652924', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3180, '2020-03-29 14:20:36.000', 'system.region.area.652925', '新和县', NULL, '652925', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3181, '2020-03-29 14:20:36.000', 'system.region.area.652926', '拜城县', NULL, '652926', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3182, '2020-03-29 14:20:36.000', 'system.region.area.652927', '乌什县', NULL, '652927', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3183, '2020-03-29 14:20:36.000', 'system.region.area.652928', '阿瓦提县', NULL, '652928', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3184, '2020-03-29 14:20:36.000', 'system.region.area.652929', '柯坪县', NULL, '652929', 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3185, '2020-03-29 14:20:36.000', 'system.region.city.653000', '克孜勒苏柯尔克孜自治州', 'area', '653000', 1, 15, 3129,
        NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3186, '2020-03-29 14:20:36.000', 'system.region.area.653001', '阿图什市', NULL, '653001', 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3187, '2020-03-29 14:20:36.000', 'system.region.area.653022', '阿克陶县', NULL, '653022', 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3188, '2020-03-29 14:20:36.000', 'system.region.area.653023', '阿合奇县', NULL, '653023', 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3189, '2020-03-29 14:20:36.000', 'system.region.area.653024', '乌恰县', NULL, '653024', 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3190, '2020-03-29 14:20:36.000', 'system.region.city.653100', '喀什地区', 'area', '653100', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3191, '2020-03-29 14:20:36.000', 'system.region.area.653101', '喀什市', NULL, '653101', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3192, '2020-03-29 14:20:36.000', 'system.region.area.653121', '疏附县', NULL, '653121', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3193, '2020-03-29 14:20:36.000', 'system.region.area.653122', '疏勒县', NULL, '653122', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3194, '2020-03-29 14:20:36.000', 'system.region.area.653123', '英吉沙县', NULL, '653123', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3195, '2020-03-29 14:20:36.000', 'system.region.area.653124', '泽普县', NULL, '653124', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3196, '2020-03-29 14:20:36.000', 'system.region.area.653125', '莎车县', NULL, '653125', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3197, '2020-03-29 14:20:36.000', 'system.region.area.653126', '叶城县', NULL, '653126', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3198, '2020-03-29 14:20:36.000', 'system.region.area.653127', '麦盖提县', NULL, '653127', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3199, '2020-03-29 14:20:36.000', 'system.region.area.653128', '岳普湖县', NULL, '653128', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3200, '2020-03-29 14:20:36.000', 'system.region.area.653129', '伽师县', NULL, '653129', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3201, '2020-03-29 14:20:36.000', 'system.region.area.653130', '巴楚县', NULL, '653130', 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3202, '2020-03-29 14:20:36.000', 'system.region.area.653131', '塔什库尔干塔吉克自治县', NULL, '653131', 1, 16, 3190, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3203, '2020-03-29 14:20:36.000', 'system.region.city.653200', '和田地区', 'area', '653200', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3204, '2020-03-29 14:20:36.000', 'system.region.area.653201', '和田市', NULL, '653201', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3205, '2020-03-29 14:20:36.000', 'system.region.area.653221', '和田县', NULL, '653221', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3206, '2020-03-29 14:20:36.000', 'system.region.area.653222', '墨玉县', NULL, '653222', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3207, '2020-03-29 14:20:36.000', 'system.region.area.653223', '皮山县', NULL, '653223', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3208, '2020-03-29 14:20:36.000', 'system.region.area.653224', '洛浦县', NULL, '653224', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3209, '2020-03-29 14:20:36.000', 'system.region.area.653225', '策勒县', NULL, '653225', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3210, '2020-03-29 14:20:36.000', 'system.region.area.653226', '于田县', NULL, '653226', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3211, '2020-03-29 14:20:36.000', 'system.region.area.653227', '民丰县', NULL, '653227', 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3212, '2020-03-29 14:20:36.000', 'system.region.city.654000', '伊犁哈萨克自治州', 'area', '654000', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3213, '2020-03-29 14:20:36.000', 'system.region.area.654002', '伊宁市', NULL, '654002', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3214, '2020-03-29 14:20:36.000', 'system.region.area.654003', '奎屯市', NULL, '654003', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3215, '2020-03-29 14:20:36.000', 'system.region.area.654004', '霍尔果斯市', NULL, '654004', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3216, '2020-03-29 14:20:36.000', 'system.region.area.654021', '伊宁县', NULL, '654021', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3217, '2020-03-29 14:20:36.000', 'system.region.area.654022', '察布查尔锡伯自治县', NULL, '654022', 1, 16, 3212, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3218, '2020-03-29 14:20:36.000', 'system.region.area.654023', '霍城县', NULL, '654023', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3219, '2020-03-29 14:20:36.000', 'system.region.area.654024', '巩留县', NULL, '654024', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3220, '2020-03-29 14:20:36.000', 'system.region.area.654025', '新源县', NULL, '654025', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3221, '2020-03-29 14:20:36.000', 'system.region.area.654026', '昭苏县', NULL, '654026', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3222, '2020-03-29 14:20:36.000', 'system.region.area.654027', '特克斯县', NULL, '654027', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3223, '2020-03-29 14:20:36.000', 'system.region.area.654028', '尼勒克县', NULL, '654028', 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3224, '2020-03-29 14:20:36.000', 'system.region.city.654200', '塔城地区', 'area', '654200', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3225, '2020-03-29 14:20:36.000', 'system.region.area.654201', '塔城市', NULL, '654201', 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3226, '2020-03-29 14:20:36.000', 'system.region.area.654202', '乌苏市', NULL, '654202', 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3227, '2020-03-29 14:20:36.000', 'system.region.area.654221', '额敏县', NULL, '654221', 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3228, '2020-03-29 14:20:36.000', 'system.region.area.654223', '沙湾县', NULL, '654223', 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3229, '2020-03-29 14:20:36.000', 'system.region.area.654224', '托里县', NULL, '654224', 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3230, '2020-03-29 14:20:36.000', 'system.region.area.654225', '裕民县', NULL, '654225', 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3231, '2020-03-29 14:20:36.000', 'system.region.area.654226', '和布克赛尔蒙古自治县', NULL, '654226', 1, 16, 3224, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3232, '2020-03-29 14:20:36.000', 'system.region.city.654300', '阿勒泰地区', 'area', '654300', 1, 15, 3129, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3233, '2020-03-29 14:20:36.000', 'system.region.area.654301', '阿勒泰市', NULL, '654301', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3234, '2020-03-29 14:20:36.000', 'system.region.area.654321', '布尔津县', NULL, '654321', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3235, '2020-03-29 14:20:36.000', 'system.region.area.654322', '富蕴县', NULL, '654322', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3236, '2020-03-29 14:20:36.000', 'system.region.area.654323', '福海县', NULL, '654323', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3237, '2020-03-29 14:20:36.000', 'system.region.area.654324', '哈巴河县', NULL, '654324', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3238, '2020-03-29 14:20:36.000', 'system.region.area.654325', '青河县', NULL, '654325', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3239, '2020-03-29 14:20:36.000', 'system.region.area.654326', '吉木乃县', NULL, '654326', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3240, '2020-03-29 14:20:36.000', 'system.region.area.659001', '石河子市', NULL, '659001', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3241, '2020-03-29 14:20:36.000', 'system.region.area.659002', '阿拉尔市', NULL, '659002', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3242, '2020-03-29 14:20:36.000', 'system.region.area.659003', '图木舒克市', NULL, '659003', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3243, '2020-03-29 14:20:36.000', 'system.region.area.659004', '五家渠市', NULL, '659004', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3244, '2020-03-29 14:20:36.000', 'system.region.area.659005', '北屯市', NULL, '659005', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3245, '2020-03-29 14:20:36.000', 'system.region.area.659006', '铁门关市', NULL, '659006', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3246, '2020-03-29 14:20:36.000', 'system.region.area.659007', '双河市', NULL, '659007', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3247, '2020-03-29 14:20:36.000', 'system.region.area.659008', '可克达拉市', NULL, '659008', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3248, '2020-03-29 14:20:36.000', 'system.region.area.659009', '昆玉市', NULL, '659009', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3249, '2020-03-29 14:20:36.000', 'system.region.area.659010', '胡杨河市', NULL, '659010', 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3250, '2020-03-29 14:20:36.000', 'system.region.province.710000', '台湾省', NULL, '710000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3251, '2020-03-29 14:20:36.000', 'system.region.province.810000', '香港特别行政区', NULL, '810000', 1, 14, NULL, NULL,
        NULL);
INSERT INTO `tb_data_dictionary`
VALUES (3252, '2020-03-29 14:20:36.000', 'system.region.province.820000', '澳门特别行政区', NULL, '820000', 1, 14, NULL, NULL,
        NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_dictionary_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_dictionary_type`;
CREATE TABLE `tb_dictionary_type`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
    `code`          varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '键名称',
    `name`          varchar(64) COLLATE utf8mb4_bin  NOT NULL COMMENT '类型名称',
    `parent_id`     int(11) DEFAULT NULL COMMENT '父字典类型,根节点为 null',
    `remark`        varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_type` (`code`) USING BTREE
) ENGINE=InnoDB COMMENT='数据字典类型表';

-- ----------------------------
-- Records of tb_dictionary_type
-- ----------------------------
BEGIN;
INSERT INTO `tb_dictionary_type`
VALUES (1, '2020-03-29 13:48:39.000', 'system', '系统配置项', NULL, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (2, '2020-03-29 13:49:01.000', 'system.crypto', '加解密', 1, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (3, '2020-03-29 14:16:09.000', 'system.crypto.access', '访问', 2, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (4, '2020-03-29 14:18:01.000', 'system.crypto.access.predicate', '条件', 3, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (5, '2020-03-29 14:18:54.000', 'system.crypto.access.type', '类型', 3, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (6, '2020-03-29 14:18:54.000', 'system.crypto.algorithm.padding-scheme', '加解密算法填充方案', 3, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (7, '2020-03-29 14:18:54.000', 'system.crypto.algorithm.mode', '加解密算法模型', 3, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (8, '2020-03-29 14:18:54.000', 'system.email', '邮箱', 1, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (9, '2020-03-29 14:18:54.000', 'system.sms', '短信', 1, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (10, '2020-03-29 14:18:54.000', 'system.notification', '通知', 1, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (11, '2020-03-29 14:18:54.000', 'system.notification.dynamic', '动态通知', 1, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (12, '2020-03-29 14:18:54.000', 'system.notification.comment', '评论通知', 1, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (13, '2020-03-29 14:18:54.000', 'system.region', '区域', 1, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (14, '2020-03-29 14:18:54.000', 'system.region.province', '省', 13, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (15, '2020-03-29 14:18:54.000', 'system.region.city', '市', 13, NULL);
INSERT INTO `tb_dictionary_type`
VALUES (16, '2020-03-29 14:18:54.000', 'system.region.area', '县', 13, NULL);
COMMIT;

SET
FOREIGN_KEY_CHECKS = 1;
