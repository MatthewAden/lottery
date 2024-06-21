package cn.matthew.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.matthew.infrastructure.persistent.po.TaskPo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface ITaskDao {

    void insert(TaskPo task);

    @DBRouter
    void updateTaskSendMessageCompleted(TaskPo task);

    @DBRouter
    void updateTaskSendMessageFail(TaskPo task);

    List<TaskPo> queryNoSendMessageTaskList();

}
