package cn.matthew.domain.award.repository;


import cn.matthew.domain.award.aggregate.UserAwardRecordAggregate;

public interface IAwardRepository {

    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

}
