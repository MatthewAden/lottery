package cn.matthew.trigger;

import cn.matthew.trigger.api.IRaffleActivityController;
import cn.matthew.trigger.api.dto.RaffleAwardListRequestDTO;
import cn.matthew.trigger.api.vo.RaffleAwardListResponseVO;
import cn.matthew.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleStrategyControllerTest {

    @Resource
    private IRaffleActivityController raffleStrategyService;

    @Test
    public void test_queryRaffleAwardList() {
        RaffleAwardListRequestDTO request = new RaffleAwardListRequestDTO();
        request.setUserId("matthew");
        request.setActivityId(100001L);
        Response<List<RaffleAwardListResponseVO>> response = raffleStrategyService.queryAwardList(request);

        log.info("请求参数：{}", JSON.toJSONString(request));
        log.info("测试结果：{}", JSON.toJSONString(response));
    }

}
