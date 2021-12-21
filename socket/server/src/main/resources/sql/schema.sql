SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_room
-- ----------------------------
DROP TABLE IF EXISTS `tb_room`;
CREATE TABLE `tb_room` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `name` varchar(32) NOT NULL COMMENT '房间名称',
  `last_message_time` datetime(3) DEFAULT NULL COMMENT '最后修改时间',
  `type` varchar(16) NOT NULL COMMENT '类型，用于区分房间的属性使用',
  `status` tinyint(4) DEFAULT '0' COMMENT '状态：0.今哟过，1.启用',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='房间信息，用于说明当前用户存在些什么房间。';

-- ----------------------------
-- Table structure for tb_room_participant
-- ----------------------------
DROP TABLE IF EXISTS `tb_room_participant`;
CREATE TABLE `tb_room_participant` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `user_id` int(20) NOT NULL COMMENT '用户 id',
  `role` smallint(6) NOT NULL COMMENT '用户角色',
  `room_id` int(20) NOT NULL COMMENT '房间 id',
  `last_send_time` datetime(3) DEFAULT NULL COMMENT '最后发送消息时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_user_id_room_id` (`user_id`,`room_id`)
) ENGINE=InnoDB COMMENT='房间参与者，用于说明某个房间里存在些什么人';

SET FOREIGN_KEY_CHECKS = 1;
