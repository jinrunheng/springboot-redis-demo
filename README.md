## Springboot-redis-demo

this is a demo for Spring Boot integrates redis

### Usage
```bash
docker pull redis
```
```bash
docker run --name redis-demo -p 6379:6379 -d redis
```
```bash
docker exec -it 7b89405e00bc5e6359773910772e5ffe9fe543e15b2ff03c239cbcf359fe0815 redis-cli
```

### Basic usage of redis

how to choose db
```redis
select 0~15
```
how to flush db
```redis
flushdb
```
#### strings
```redis
127.0.0.1:6379> set test:count 1
OK
127.0.0.1:6379> get test:count
"1"
127.0.0.1:6379> incr test:count
(integer) 2
127.0.0.1:6379> decr test:count
(integer) 1
```
#### hash
```redis
127.0.0.1:6379> hset test:user id 1
(integer) 1
127.0.0.1:6379> hset test:user username zhangsan
(integer) 1
127.0.0.1:6379> hget test:user id
"1"
127.0.0.1:6379> hget test:user username
"zhangsan"
```
#### list
```redis
127.0.0.1:6379> lpush test:ids 101 102 103
(integer) 3
127.0.0.1:6379> llen test:ids
(integer) 3
127.0.0.1:6379> lrange test:ids 0 2
1) "103"
2) "102"
3) "101"
127.0.0.1:6379> lindex test:ids 0
"103"
127.0.0.1:6379> rpop test:ids
"101"
127.0.0.1:6379> lpop test:ids
"103"
```
#### set
when you use `spop` ,an element will pop **randomly** from the set

`smembers` can view all elements in the set
```redis
127.0.0.1:6379> sadd test:teachers aaa bbb ccc ddd eee
(integer) 5
127.0.0.1:6379> scard test:teachers
(integer) 5
127.0.0.1:6379> spop test:teachers
"eee"
127.0.0.1:6379> smembers test:teachers
1) "bbb"
2) "ddd"
3) "ccc"
4) "aaa"
```
#### zset
`zrank` will return the ranking of elements that stored in zset, index start form 0 
```redis
127.0.0.1:6379> zadd test:students 10 aaa 20 bbb 30 ccc 40 ddd 50 eee
(integer) 5
127.0.0.1:6379> zcard test:students
(integer) 5
127.0.0.1:6379> zscore test:students ccc
"30"
127.0.0.1:6379> zrank test:students ccc
(integer) 2
127.0.0.1:6379> zrange test:students 0 2
1) "aaa"
2) "bbb"
3) "ccc"
```
#### global scope commands
```redis
keys * // show all keys
keys test* // show all keys prefixd by test
type test:user // show the type of key
exists test:user // dose the key exist
del test:user // delete key
expire test:students 10 // expire the key survival time in seconds

```
```redis
127.0.0.1:6379> keys *
1) "test:count"
2) "test:students"
3) "test:user"
4) "test:ids"
5) "test:teachers"
127.0.0.1:6379> keys test*
1) "test:count"
2) "test:students"
3) "test:user"
4) "test:ids"
5) "test:teachers"
127.0.0.1:6379> type test:user
hash
127.0.0.1:6379> exists test:user
(integer) 1
127.0.0.1:6379> del test:user
(integer) 1
127.0.0.1:6379> del test:user
(integer) 0
127.0.0.1:6379> expire test:students 10
(integer) 1
127.0.0.1:6379> exists test:students
(integer) 0

```