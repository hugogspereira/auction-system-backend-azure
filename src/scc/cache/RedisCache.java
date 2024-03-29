package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.dao.AuctionDAO;
import scc.dao.UserDAO;

import static scc.utils.AzureProperties.REDIS_PORT;
import static scc.utils.AzureProperties.REDIS_URL;

public class RedisCache {

    public static final boolean IS_ACTIVE = true;
    private static final String RedisHostname = System.getenv(REDIS_URL);
    private static final String RedisPort = System.getenv(REDIS_PORT);

    //private static final String RedisKey = System.getenv(REDIS_KEY);

    private static JedisPool instance;
    private static RedisCache redisCache;

    public static final String AUCTION_KEY = "auction:";

    public static final String USER_KEY = "user:";

    private static final String SESSION_KEY = "session:";

    private static final int SESSION_EXP_TIME = 3600;
    private static final int DEFAULT_EXP_TIME = 600;

    public RedisCache() {
        getCachePool();
    }

    public synchronized static JedisPool getCachePool() {
        if (instance != null)
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
        instance = new JedisPool(poolConfig, RedisHostname, Integer.parseInt(RedisPort), 1000);

        return instance;
    }

    public static synchronized RedisCache getInstance() {
        if (redisCache != null) {
            return redisCache;
        }
        redisCache = new RedisCache();
        return redisCache;
    }



    // USERS
    public void putUser(UserDAO user) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(USER_KEY+user.getId(), mapper.writeValueAsString(user));
            jedis.expire(USER_KEY+user.getId(),DEFAULT_EXP_TIME);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the user in cache.\n"+e.getMessage());
        }
    }

    public UserDAO getUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            String stringUser = jedis.get(USER_KEY+nickname);
            if(stringUser ==  null) { return null; }
            return mapper.readValue(stringUser, UserDAO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the user in cache.\n"+e.getMessage());
            return null;
        }
    }

    public void deleteUser(String nickname) {
        try(Jedis jedis = instance.getResource()) {
            jedis.del(USER_KEY+nickname);
        }
    }









    // AUCTIONS
    public void putAuction(AuctionDAO auction) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));
            jedis.expire(AUCTION_KEY+auction.getId(),DEFAULT_EXP_TIME);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public void replaceAuction(AuctionDAO auction) {
        ObjectMapper mapper;
        try(Jedis jedis = instance.getResource()) {
            mapper = new ObjectMapper();
            jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));
            jedis.expire(AUCTION_KEY+auction.getId(), DEFAULT_EXP_TIME);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public AuctionDAO getAuction(String id) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try(Jedis jedis = instance.getResource()) {
            String stringAuction = jedis.get(AUCTION_KEY+id);
            if(stringAuction ==  null) { return null; }
            return mapper.readValue(stringAuction, AuctionDAO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the auction in cache.\n"+e.getMessage());
            return null;
        }
    }





    // SESSION
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

    public String getSession(String sessionId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            return jedis.get(SESSION_KEY+sessionId);
        }
    }

    public void deleteSession(String sessionId) {
        try (Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.del(SESSION_KEY + sessionId);
        }
    }
}
