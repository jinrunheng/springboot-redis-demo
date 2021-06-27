package com.github.springbootredisdemo;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.Random;
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
    public void testTransactional() {
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

    /**
     * Redis  HyperLogLog
     */
    @Test
    public void testHyperLogLog() {
        String redisKey = "test:hll:01";
        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 0; i < 10000; i++) {
            int r = new Random().nextInt(10000);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size); // almost 10000
    }

    // 3组数据合并，再统计合并后重复数据的独立总数
    @Test
    public void testHyperLogLogUnion() {
        String redisKey01 = "test:hll:01";
        for (int i = 0; i < 100; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey01, i);
        }

        String redisKey02 = "test:hll:02";
        for (int i = 100; i < 200; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey02, i);
        }

        String redisKey03 = "test:hll:03";
        for (int i = 50; i < 150; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey03, i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey01, redisKey02, redisKey03);

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size); //almost 200
    }

    /**
     * 统计一组数据的布尔值
     */
    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";
        // record
        redisTemplate.opsForValue().setBit(redisKey, 0, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 9, true);

        // select
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 0));
        Assertions.assertFalse(redisTemplate.opsForValue().getBit(redisKey, 1));
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 4));
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 9));

        // statistics
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        Assertions.assertEquals(3l, (long) execute);
    }

    // 统计三组数据的布尔值，并对这三组数据做 or 运算
    @Test
    public void testBitMapOperation() {
        String redisKey02 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey02, 0, true);
        redisTemplate.opsForValue().setBit(redisKey02, 1, true);
        redisTemplate.opsForValue().setBit(redisKey02, 2, true);

        String redisKey03 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey03, 0, true);
        redisTemplate.opsForValue().setBit(redisKey03, 2, true);
        redisTemplate.opsForValue().setBit(redisKey03, 4, true);

        String redisKey04 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey04, 4, true);
        redisTemplate.opsForValue().setBit(redisKey04, 5, true);
        redisTemplate.opsForValue().setBit(redisKey04, 6, true);


        // redisKey02 : 1110000
        // redisKey03 : 1010100
        // redisKey04 : 0000111
        // redisKey02  OR redisKey03 OR redisKey04
        // redisKey 1110111
        // redisKey.bitCount = 6
        String redisKey = "test:bm:or";
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR
                        , redisKey.getBytes(),
                        redisKey02.getBytes(), redisKey03.getBytes(), redisKey04.getBytes());
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
        Assertions.assertEquals(6l, (long) execute);
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 0));
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 1));
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 2));
        Assertions.assertFalse(redisTemplate.opsForValue().getBit(redisKey, 3));
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 4));
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 5));
        Assertions.assertTrue(redisTemplate.opsForValue().getBit(redisKey, 6));
    }

}
