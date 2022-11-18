package scc.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;
import java.util.Collection;
import java.util.List;
import static scc.dao.AuctionDAO.DELETED_USER;
import static scc.utils.AzureProperties.REDIS_KEY;
import static scc.utils.AzureProperties.REDIS_URL;

public class RedisCache {

    public static final boolean IS_ACTIVE = true;
    private static final String RedisHostname = System.getenv(REDIS_URL);
    private static final String RedisKey = System.getenv(REDIS_KEY);

    private static JedisPool instance;
    private static RedisCache redisCache;

    public static final String AUCTION_KEY = "auction:";
    public static final String USER_AUCTIONS_KEY = "user_auctions_";    //

    public static final String BID_KEY = "bid:";
    public static final String BIDS_AUCTION_KEY = "bids_auction_";     //

    public static final String USER_KEY = "user:";
    public static final String USER_BIDS_KEY = "user_bids_";           //

    private static final String SESSION_KEY = "session:";

    private static final int SESSION_EXP_TIME = 3600;
    private static final int DEFAULT_EXP_TIME = 1800;

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

    public void invalidateKey(String key) {
        try(Jedis jedis = instance.getResource()) {
            jedis.del(key);
        }
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

    public void replaceUser(UserDAO user) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            if(jedis.exists(USER_KEY+user.getId())) {
                jedis.set(USER_KEY+user.getId(), mapper.writeValueAsString(user));
                jedis.expire(USER_KEY+user.getId(),DEFAULT_EXP_TIME);
            }
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
            jedis.del(USER_BIDS_KEY+nickname+":");
            jedis.del(USER_AUCTIONS_KEY+nickname+":");
        }
    }





    // BIDS
    public void putBid(BidDAO bid) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(BID_KEY+bid.getId(), mapper.writeValueAsString(bid));
            jedis.expire(BID_KEY+bid.getId(), DEFAULT_EXP_TIME);

            if(!bid.getUserNickname().equals(DELETED_USER)) {
                mapper = new ObjectMapper();
                if(jedis.exists(USER_BIDS_KEY + bid.getUserNickname() + ":")) {
                    jedis.lpush(USER_BIDS_KEY + bid.getUserNickname() + ":",  mapper.writeValueAsString(bid));
                    //jedis.hset(USER_BIDS_KEY + bid.getUserNickname() + ":", bid.getId(), mapper.writeValueAsString(bid));
                }
                else {
                    jedis.lpush(USER_BIDS_KEY + bid.getUserNickname() + ":",  mapper.writeValueAsString(bid));
                    //jedis.hset(USER_BIDS_KEY + bid.getUserNickname() + ":", bid.getId(), mapper.writeValueAsString(bid));
                    jedis.expire(USER_BIDS_KEY + bid.getUserNickname() + ":", DEFAULT_EXP_TIME);
                }
            }

            mapper = new ObjectMapper();
            if(jedis.exists(BIDS_AUCTION_KEY + bid.getAuctionId() + ":")) {
                jedis.lpush(BIDS_AUCTION_KEY + bid.getAuctionId() + ":",  mapper.writeValueAsString(bid));
                //jedis.hset(BIDS_AUCTION_KEY + bid.getAuctionId() + ":", bid.getId(), mapper.writeValueAsString(bid));
            }
            else {
                jedis.lpush(BIDS_AUCTION_KEY + bid.getAuctionId() + ":",  mapper.writeValueAsString(bid));
                //jedis.hset(BIDS_AUCTION_KEY + bid.getAuctionId() + ":", bid.getId(), mapper.writeValueAsString(bid));
                jedis.expire(BIDS_AUCTION_KEY + bid.getAuctionId() + ":", DEFAULT_EXP_TIME);
            }
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the bid in cache.\n"+e.getMessage());
        }
    }

    public void replaceBid(BidDAO bid) {
        ObjectMapper mapper;
        try(Jedis jedis = instance.getResource()) {
            if(jedis.exists(BID_KEY+bid.getId())) {
                mapper = new ObjectMapper();
                jedis.set(BID_KEY+bid.getId(), mapper.writeValueAsString(bid));
                jedis.expire(BID_KEY+bid.getId(), DEFAULT_EXP_TIME);
            }
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the bid in cache.\n"+e.getMessage());
        }
    }

    public List<BidDAO> getBidsByUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            /*
             * Get all fields and values of the hash stored at key
             */
            List<String> listOfBids = jedis.lrange(USER_BIDS_KEY+nickname+":", 0, -1); //jedis.hvals(USER_BIDS_KEY+nickname+":");
            if(listOfBids ==  null) { return null; }
            return mapper.readValue(listOfBids.toString(), mapper.getTypeFactory().constructCollectionType(List.class, BidDAO.class));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the bids in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<BidDAO> getBidsByAuction(String auctionId) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()){
            List<String> listOfBids = jedis.lrange(BIDS_AUCTION_KEY+auctionId+":", 0, -1); // jedis.hvals(BIDS_AUCTION_KEY+auctionId+":");
            if(listOfBids ==  null) { return null; }
            return mapper.readValue(listOfBids.toString(), mapper.getTypeFactory().constructCollectionType(List.class, BidDAO.class));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the bids in cache.\n"+e.getMessage());
            return null;
        }
    }





    // AUCTIONS
    public void putAuction(AuctionDAO auction) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));
            jedis.expire(AUCTION_KEY+auction.getId(),DEFAULT_EXP_TIME);

            if(!auction.getOwnerNickname().equals(DELETED_USER)) {
                mapper = new ObjectMapper();
                if(jedis.exists(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":")) {
                    jedis.lpush(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":",  mapper.writeValueAsString(auction));
                    //jedis.hset(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":", auction.getId(), mapper.writeValueAsString(auction));
                }
                else {
                    jedis.lpush(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":",  mapper.writeValueAsString(auction));
                    //jedis.hset(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":", auction.getId(), mapper.writeValueAsString(auction));
                    jedis.expire(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":", DEFAULT_EXP_TIME);
                }
            }
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public void replaceAuction(AuctionDAO auction) {
        ObjectMapper mapper;
        try(Jedis jedis = instance.getResource()) {
            if(jedis.exists(AUCTION_KEY+auction.getId())) {
                mapper = new ObjectMapper();
                jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));
                jedis.expire(AUCTION_KEY+auction.getId(), DEFAULT_EXP_TIME);
            }
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

    public List<AuctionDAO> getAuctionsByUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<String> listOfAuctions = jedis.lrange(USER_AUCTIONS_KEY+nickname+":", 0, -1); //jedis.hvals(USER_AUCTIONS_KEY+nickname+":");
            if(listOfAuctions ==  null) { return null; }
            return mapper.readValue(listOfAuctions.toString(), mapper.getTypeFactory().constructCollectionType(List.class, AuctionDAO.class));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the auctions in cache.\n"+e.getMessage());
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
        try(Jedis jedis = RedisCache.getCachePool().getResource()) {
            jedis.del(SESSION_KEY+sessionId);
        }
    }
}
