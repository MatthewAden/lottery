package cn.matthew.domain.award.service;

import cn.matthew.domain.award.model.entity.UserAwardRecordEntity;

/**
 * @Author: matthew
 * @Description: 记录用户中奖
 **/
public interface IAwardService {
    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);
}
