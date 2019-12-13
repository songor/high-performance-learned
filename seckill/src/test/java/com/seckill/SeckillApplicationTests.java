package com.seckill;

import com.seckill.dao.UserDOMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeckillApplicationTests {

    @Autowired
    UserDOMapper userDOMapper;

    @Test
    void contextLoads() {
        userDOMapper.selectByPrimaryKey(1);
    }

}
