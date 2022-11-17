package scc.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.dao.AuctionDAO;
import scc.dao.UserDAO;
import static scc.utils.AzureProperties.REDIS_KEY;
import static scc.utils.AzureProperties.REDIS_URL;

public class RedisCache {

    public static final boolean IS_ACTIVE = true;
    private static final String RedisHostname = System.getenv(REDIS_URL);
    private static final String RedisKey = System.getenv(REDIS_KEY);

    private static JedisPool instance;
    private static RedisCache redisCache;

    private static final String AUCTION_KEY = "auction:";
    private static final String USER_KEY = "user:";
    private static final String SESSION_KEY = "session:";

    private static final int SESSION_EXP_TIME = 3600;

    public RedisCache() {
        getCachePool();
    }

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

    public void putUser(UserDAO user) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.set(USER_KEY+user.getId(), mapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the user in cache.\n"+e.getMessage());
        }
    }

    public void putAuction(AuctionDAO auction) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public void putSession(String sessionId, String nickname) {
        String cacheId = SESSION_KEY+sessionId;
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.set(cacheId, nickname);
            jedis.expire(cacheId, SESSION_EXP_TIME);
        } catch (Exception e) {
            System.out.println("Redis Cache: unable to put the session in cache.\n"+e.getMessage());
        }
    }

    public boolean existSession(String sessionId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            return jedis.exists(SESSION_KEY+sessionId);
        }
    }

    public UserDAO getUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            String stringUser = jedis.get(USER_KEY+nickname);
            if(stringUser ==  null) { return null; }
            return mapper.readValue(stringUser, UserDAO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the user in cache.\n"+e.getMessage());
            return null;
        }
    }


    public AuctionDAO getAuction(String id) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            String stringAuction = jedis.get(AUCTION_KEY+id);
            if(stringAuction ==  null) {
                System.out.println("Redis Cache: unable to get the auction in cache.");
                return null; }
            System.out.println("Redis Cache: auction found in cache.");
            return mapper.readValue(stringAuction, AuctionDAO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the auction in cache.\n"+e.getMessage());
            return null;
        }
    }

    public String getSession(String sessionId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            return jedis.get(SESSION_KEY+sessionId);
        }
    }

    public void deleteUser(String nickname) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.del(USER_KEY+nickname);
        }
    }

    public void deleteSession(String sessionId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.del(SESSION_KEY+sessionId);
        }
    }
}
