package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.model.User;

public class RedisCache {

    private static final boolean IS_ACTIVE = true;
    private static final String RedisHostname = "scc2223cache4204.redis.cache.windows.net";
    private static final String RedisKey = "?????=";

    private static JedisPool instance;
    private static RedisCache redisCache;

    public synchronized static JedisPool getCachePool() {
        if(instance != null)
            return instance;

        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        instance = new JedisPool(poolConfig, RedisHostname, 6380, 1000, RedisKey, true);

        return instance;
    }

    public static synchronized RedisCache getInstance() {
        if(redisCache != null) {
            return redisCache;
        }
        redisCache = new RedisCache();
        return redisCache;
    }

    public void createUser(User user) {
        if(IS_ACTIVE) {
            ObjectMapper mapper = new ObjectMapper();
            try(Jedis jedis = instance.getResource()) {
                jedis.set("user:"+user.getNickname(), mapper.writeValueAsString(user));
            } catch (JsonProcessingException e) {
                System.out.println("Redis Cache: unable to put the user in cache.\n"+e.getMessage());
            }
        }
    }

    public boolean existUser(String nickname) {
        if(IS_ACTIVE) {
            ObjectMapper mapper = new ObjectMapper();
            try(Jedis jedis = instance.getResource()) {
                return jedis.exists("user:"+nickname);
            }
        }
        return false;
    }

    public User getUser(String nickname) {
        if(IS_ACTIVE) {
            ObjectMapper mapper = new ObjectMapper();
            try(Jedis jedis = instance.getResource()) {
                String stringUser = jedis.get("user:"+nickname);
                return mapper.readValue(stringUser, User.class);
            } catch (JsonProcessingException e) {
                System.out.println("Redis Cache: unable to get the user in cache.\n"+e.getMessage());
            }
        }
        return null;
    }

    public void deleteUser(String nickname) {
        if(IS_ACTIVE) {
            ObjectMapper mapper = new ObjectMapper();
            try(Jedis jedis = instance.getResource()) {
                jedis.del("user:"+nickname);
            }
        }
    }

}
