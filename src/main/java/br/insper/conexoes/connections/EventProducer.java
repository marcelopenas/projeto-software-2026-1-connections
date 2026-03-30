package br.insper.conexoes.connections;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProducer {

    private final RedisTemplate<String, Event> redisTemplate;
    private static final String QUEUE_NAME = "events-queue";

    public EventProducer(RedisTemplate<String, Event> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void send(Event event) {
        redisTemplate.opsForList().leftPush(QUEUE_NAME, event);
    }
}
