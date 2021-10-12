package com.github.dactiv.basic.file.manager.entity;

import com.github.dactiv.framework.commons.id.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 用户头像历史记录实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserAvatarHistory extends IdEntity<Integer> {

    private static final long serialVersionUID = -856691872498409024L;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 桶名称
     */
    private String bucketName;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 历史头像名称
     */
    private List<String> values = new LinkedList<>();

}
