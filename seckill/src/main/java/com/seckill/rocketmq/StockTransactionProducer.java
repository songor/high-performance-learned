package com.seckill.rocketmq;

import com.alibaba.fastjson.JSON;
import com.seckill.dao.StockLogDOMapper;
import com.seckill.dataobject.StockLogDO;
import com.seckill.service.OrderService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class StockTransactionProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockTransactionProducer.class);

    @Value("${mq.stock.namesrv.addr}")
    private String namesrvAddr;

    @Value("${mq.stock.topic}")
    private String topic;

    private TransactionMQProducer producer;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        producer = new TransactionMQProducer("stock_transaction_producer_group");
        producer.setNamesrvAddr(namesrvAddr);
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                Map<String, Object> args = (Map<String, Object>) o;
                Integer userId = (Integer) args.get("userId");
                Integer itemId = (Integer) args.get("itemId");
                Integer amount = (Integer) args.get("amount");
                Integer promoId = (Integer) args.get("promoId");
                String stockLogId = (String) args.get("stockLogId");
                try {
                    orderService.createOrder(userId, itemId, amount, promoId, stockLogId);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            /**
             * UNKNOWN 状态定时回调
             */
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                Map<String, Object> body = JSON.parseObject(new String(messageExt.getBody()), Map.class);
                String stockLogId = (String) body.get("stockLogId");
                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if (2 == stockLogDO.getStatus()) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else if (1 == stockLogDO.getStatus()) {
                    return LocalTransactionState.UNKNOW;
                } else {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }
        });
        producer.start();
    }

    public boolean transactionAsyncDecreaseStock(Integer userId, Integer itemId, Integer amount, Integer promoId, String stockLogId) {
        Map<String, Object> body = new HashMap<>();
        body.put("itemId", itemId);
        body.put("amount", amount);
        body.put("stockLogId", stockLogId);
        Message message = new Message(topic, JSON.toJSON(body).toString().getBytes(Charset.forName("UTF-8")));

        Map<String, Object> args = new HashMap<>();
        args.put("userId", userId);
        args.put("itemId", itemId);
        args.put("amount", amount);
        args.put("promoId", promoId);
        args.put("stockLogId", stockLogId);

        TransactionSendResult result;
        try {
            // 二阶段提交
            result = producer.sendMessageInTransaction(message, args);
        } catch (MQClientException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        if (LocalTransactionState.COMMIT_MESSAGE == result.getLocalTransactionState()) {
            return true;
        }
        return false;
    }

}
