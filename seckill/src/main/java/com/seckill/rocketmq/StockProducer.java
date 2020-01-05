package com.seckill.rocketmq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class StockProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockProducer.class);

    @Value("${mq.stock.namesrv.addr}")
    private String namesrvAddr;

    @Value("${mq.stock.topic}")
    private String topic;

    private DefaultMQProducer producer;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new DefaultMQProducer("stock_producer_group");
        producer.setNamesrvAddr(namesrvAddr);
        producer.start();
    }

    public boolean asyncDecrease(Integer itemId, Integer amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("itemId", itemId);
        body.put("amount", amount);
        Message message = new Message(topic, JSON.toJSON(body).toString().getBytes(Charset.forName("UTF-8")));
        try {
            producer.send(message);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

}
