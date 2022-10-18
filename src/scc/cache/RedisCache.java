package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;

import java.util.List;

public class RedisCache {

    public static final boolean IS_ACTIVE = true;
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

    public void putUser(UserDAO user) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set("user:"+user.getNickname(), mapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the user in cache.\n"+e.getMessage());
        }
    }

    public void putAuction(AuctionDAO auction) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set("auction:"+auction.getId(), mapper.writeValueAsString(auction));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public void putBid(BidDAO bid) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set("bid:"+bid.getId(), mapper.writeValueAsString(bid));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the bid in cache.\n"+e.getMessage());
        }
    }

    public boolean existUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            return jedis.exists("user:"+nickname);
        }
    }

    public UserDAO getUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            String stringUser = jedis.get("user:"+nickname);
            return mapper.readValue(stringUser, UserDAO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the user in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<AuctionDAO> getAuctionByUser(String nickname) {
        // TODO
       return null;
    }

    public List<BidDAO> getBidsByUser(String nickname) {
        // TODO
        return null;
    }

    public void deleteUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.del("user:"+nickname);
        }
    }

}
