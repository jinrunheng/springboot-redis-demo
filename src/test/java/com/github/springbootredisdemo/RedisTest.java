package com.github.springbootredisdemo;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.Set;
import java.util.TreeSet;

@SpringBootTest
@ContextConfiguration(classes = SpringbootRedisDemoApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    private void clear() {
        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }

    @Test
    public void testStrings() {
        clear();
        String redisKey = "test:count";
        // 存数据
        redisTemplate.opsForValue().set(redisKey, 1);
        // 取数据 redisTemplate.opsForValue().get(redisKey)
        Assertions.assertEquals(redisTemplate.opsForValue().get(redisKey), 1);
        // increment
        Assertions.assertEquals(redisTemplate.opsForValue().increment(redisKey), 2);
        // decrement
        Assertions.assertEquals(redisTemplate.opsForValue().decrement(redisKey), 1);
    }

    @Test
    public void testHash() {
        clear();
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "kim");
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testList() {
        clear();
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        Assertions.assertEquals(redisTemplate.opsForList().size(redisKey), 3);
        Assertions.assertEquals(redisTemplate.opsForList().index(redisKey, 0), 103);
        Assertions.assertEquals(redisTemplate.opsForList().range(redisKey, 0, 2), Lists.newArrayList(103, 102, 101));
    }

    @Test
    public void testSet() {
        clear();
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "Kim", "Bob", "Jack", "Rose", "Mike");
        Assertions.assertEquals(redisTemplate.opsForSet().size(redisKey), 5);
        // pop an element randomly
        redisTemplate.opsForSet().pop(redisKey);
        System.out.println(redisTemplate.opsForSet().members(redisKey));
        Assertions.assertEquals(redisTemplate.opsForSet().size(redisKey), 4);
    }

    @Test
    public void testSortedSet() {
        clear();
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "Kim", 80);
        redisTemplate.opsForZSet().add(redisKey, "Bob", 60);
        redisTemplate.opsForZSet().add(redisKey, "Jack", 100);
        redisTemplate.opsForZSet().add(redisKey, "Rose", 40);
        redisTemplate.opsForZSet().add(redisKey, "Mike", 90);

        Assertions.assertEquals(redisTemplate.opsForZSet().zCard(redisKey), 5);
        // get Kim's score
        Assertions.assertEquals(redisTemplate.opsForZSet().score(redisKey, "Kim"), 80);
        // get Kim's rank by natural order from zero(0,1,2,3,4.....)
        Assertions.assertEquals(redisTemplate.opsForZSet().rank(redisKey, "Kim"), 2);
        // get kim's rank by descending order
        Assertions.assertEquals(redisTemplate.opsForZSet().reverseRank(redisKey, "Kim"), 2);
        TreeSet set = new TreeSet();
        set.add("Rose");
        set.add("Bob");
        set.add("Kim");
        Assertions.assertEquals(redisTemplate.opsForZSet().range(redisKey, 0, 2), set);
        set.clear();
        set.add("Jack");
        set.add("Mike");
        set.add("Kim");
        Assertions.assertEquals(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2), set);

    }

    /**
     * Redis supports programming transaction,puts all commands into the queue,and submits together
     */
    @Test
    public void testTransactional(){
        clear();
        Object execute = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:transactional";
                // enable transaction
                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey, "Kim");
                redisOperations.opsForSet().add(redisKey, "Bob");
                redisOperations.opsForSet().add(redisKey, "Jack");

                // submit transaction
                return redisOperations.exec();
            }
        });
    }
}
