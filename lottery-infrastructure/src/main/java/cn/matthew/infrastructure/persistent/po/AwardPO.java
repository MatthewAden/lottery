package cn.matthew.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Data
public class AwardPO {

    /** 自增ID */
    private Long id;
    /** 抽奖奖品ID - 内部流转使用 */
    private Integer award_id;
    /** 奖品对接标识 - 每一个都是一个对应的发奖策略 */
    private String award_key;
    /** 奖品配置信息 */
    private String award_config;
    /** 奖品内容描述 */
    private String award_desc;
    /** 创建时间 */
    private Date create_time;
    /** 更新时间 */
    private Date update_time;
}
