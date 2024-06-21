package cn.matthew.trigger;

import cn.matthew.trigger.api.IRaffleActivityController;
import cn.matthew.trigger.api.dto.ActivityDrawRequestDTO;
import cn.matthew.trigger.api.vo.ActivityDrawResponseVO;
import cn.matthew.types.model.Response;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleActivityControllerTest {

    @Resource
    private IRaffleActivityController raffleActivityController;

    @Test
    public void test_armory() {
        Response<Boolean> response = raffleActivityController.armory(100001L);
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

    @Test
    public void test_draw() {
        ActivityDrawRequestDTO request = new ActivityDrawRequestDTO();
        request.setActivityId(100001L);
        request.setUserId("matthew");
        Response<ActivityDrawResponseVO> response = raffleActivityController.draw(request);

        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

}
