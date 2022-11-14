package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;
import java.util.Collection;
import java.util.List;

import static scc.utils.AzureProperties.REDIS_KEY;
import static scc.utils.AzureProperties.REDIS_URL;

public class RedisCache {

    public static final boolean IS_ACTIVE = true;
    private static final String RedisHostname = System.getenv(REDIS_URL);
    private static final String RedisKey = System.getenv(REDIS_KEY);

    private static JedisPool instance;
    private static RedisCache redisCache;

    private static final String AUCTION_KEY = "auction:";
    private static final String BID_KEY = "bid:";
    private static final String USER_KEY = "user:";
    private static final String USER_AUCTIONS_KEY = "user_auctions_";
    private static final String USER_BIDS_KEY = "user_bids_";
    private static final String BIDS_AUCTION_KEY = "bids_auction_";
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
        try(Jedis jedis = instance.getResource()) {
            jedis.set(USER_KEY+user.getId(), mapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the user in cache.\n"+e.getMessage());
        }
    }

    public void putAuction(AuctionDAO auction) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));

            jedis.hset(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":", auction.getId(), mapper.writeValueAsString(auction)); //
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public void putBid(BidDAO bid) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(BID_KEY+bid.getId(), mapper.writeValueAsString(bid));

            jedis.hset(USER_BIDS_KEY+bid.getUserNickname()+":", bid.getId(), mapper.writeValueAsString(bid)); //
            jedis.hset(BIDS_AUCTION_KEY+bid.getAuctionId()+":", bid.getId(), mapper.writeValueAsString(bid)); //
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the bid in cache.\n"+e.getMessage());
        }
    }

    public void replaceBid(BidDAO bid) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(BID_KEY+bid.getId(), mapper.writeValueAsString(bid));

            jedis.hset(USER_BIDS_KEY+bid.getUserNickname()+":", bid.getId(), mapper.writeValueAsString(bid)); //
            jedis.hset(BIDS_AUCTION_KEY+bid.getAuctionId()+":", bid.getId(), mapper.writeValueAsString(bid)); //
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the bid in cache.\n"+e.getMessage());
        }
    }

    public void putSession(String sessionId, String userId) {
        String cacheId = SESSION_KEY+sessionId;
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.set(cacheId, userId);
            jedis.expire(cacheId, SESSION_EXP_TIME);
        } catch (Exception e) {
            System.out.println("Redis Cache: unable to put the session in cache.\n"+e.getMessage());
        }
    }

    public boolean existUser(String nickname) {
        try(Jedis jedis = instance.getResource()) {
            return jedis.exists(USER_KEY+nickname);
        }
    }

    public boolean existSession(String sessionId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            return jedis.exists(SESSION_KEY+sessionId);
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

    public AuctionDAO getAuction(String id) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            String stringAuction = jedis.get(AUCTION_KEY+id);
            if(stringAuction ==  null) { return null; }
            return mapper.readValue(stringAuction, AuctionDAO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the auction in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<AuctionDAO> getAuctionsByUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            Collection<String> listOfAuctions = jedis.hgetAll(USER_AUCTIONS_KEY+nickname+":").values(); //
            if(listOfAuctions ==  null) { return null; }
            List<AuctionDAO> resList = mapper.readValue(listOfAuctions.toString(), mapper.getTypeFactory().constructCollectionType(List.class, AuctionDAO.class));
            return resList;
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the auctions in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<BidDAO> getBidsByUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            Collection<String> listOfBids = jedis.hgetAll(USER_BIDS_KEY+nickname+":").values();  //
            if(listOfBids ==  null) { return null; }
            List<BidDAO> resList = mapper.readValue(listOfBids.toString(), mapper.getTypeFactory().constructCollectionType(List.class, BidDAO.class));
            return resList;
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the bids in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<BidDAO> getBidsByAuction(String auctionId) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()){
            Collection<String> listOfBids = jedis.hgetAll(BIDS_AUCTION_KEY+auctionId+":").values(); //
            if(listOfBids ==  null) { return null; }
            List<BidDAO> resList = mapper.readValue(listOfBids.toString(), mapper.getTypeFactory().constructCollectionType(List.class, BidDAO.class));
            return resList;
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the bids in cache.\n"+e.getMessage());
            return null;
        }
    }

    public String getSession(String sessionId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            return jedis.get(SESSION_KEY+sessionId);
        }
    }

    public void deleteUser(String nickname) {
        try(Jedis jedis = instance.getResource()) {
            jedis.del(USER_KEY+nickname);
            jedis.del(USER_AUCTIONS_KEY+nickname+":");
            jedis.del(USER_BIDS_KEY+nickname+":");
        }
    }

    public void deleteSession(String sessionId) {
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.del(SESSION_KEY+sessionId);
        }
    }
}
