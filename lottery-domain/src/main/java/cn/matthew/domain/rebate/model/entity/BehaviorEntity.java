package cn.matthew.domain.rebate.model.entity;

import cn.matthew.domain.rebate.model.valobj.BehaviorTypeVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {
    private String userId;
    private BehaviorTypeVO behaviorTypeVO;
    private String outBusinessNo;
}