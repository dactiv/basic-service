package com.github.dactiv.basic.socket.server.service;

import com.github.dactiv.basic.socket.server.config.ApplicationConfig;
import com.github.dactiv.basic.socket.server.dao.RoomParticipantDao;
import com.github.dactiv.basic.socket.server.domain.enitty.RoomParticipantEntity;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 *
 * tb_room_participant 的业务逻辑
 *
 * <p>Table: tb_room_participant - 房间参与者，用于说明某个房间里存在些什么人</p>
 *
 * @see RoomParticipantEntity
 *
 * @author maurice.chen
 *
 * @since 2021-12-10 11:17:49
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoomParticipantService extends BasicService<RoomParticipantDao, RoomParticipantEntity> {

    private final RedissonClient redissonClient;

    private final ApplicationConfig config;

    public RoomParticipantService(RedissonClient redissonClient, ApplicationConfig config) {
        this.redissonClient = redissonClient;
        this.config = config;
    }

    /**
     * 根据房间 id 查找 table : tb_room_participant 实体
     *
     * @param roomId 过滤条件
     *
     * @return tb_room_participant 实体集合
     *
     * @see RoomParticipantEntity
     */
    public List<RoomParticipantEntity> findByRoomId(Integer roomId) {
        return lambdaQuery()
                .eq(RoomParticipantEntity::getRoomId, roomId)
                .list();
    }

    /**
     * 统计参与者数量
     *
     * @param roomId 房间 id
     *
     * @return 参与者数量
     */
    public Long countByRoomId(Integer roomId) {

        CacheProperties cache = config.getRoomParticipantCountCache();

        RBucket<Long> bucket = redissonClient.getBucket(cache.getName(roomId));

        Long count = bucket.get();

        if (Objects.nonNull(count)) {
            return count;
        }

        count = lambdaQuery()
            .eq(RoomParticipantEntity::getRoomId, roomId)
            .count();

        if (Objects.nonNull(cache.getExpiresTime())) {
            bucket.setAsync(count, cache.getExpiresTime().getValue(), cache.getExpiresTime().getUnit());
        } else {
            bucket.setAsync(count);
        }

        return count;
    }
}
