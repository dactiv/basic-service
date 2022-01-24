package com.github.dactiv.basic.authentication.domain.meta;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * ip 区域原数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class IpRegionMeta implements Serializable {

    private static final long serialVersionUID = -357706294703499044L;

    /**
     * 市字段名称
     */
    public static final String CITY_NAME = "city";
    /**
     * 省字段名称
     */
    public static final String PROVINCE_NAME = "province";
    /**
     * 区域字段名称
     */
    public static final String AREA_NAME = "area";

    /**
     * ip 地址
     */
    @NonNull
    @NotEmpty
    private String ipAddress;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区域
     */
    private String area;
}
