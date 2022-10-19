package scc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisCache {

    public static final boolean IS_ACTIVE = true;
    private static final String RedisHostname = "scc2223cache4204.redis.cache.windows.net";
    private static final String RedisKey = "?????=";

    private static JedisPool instance;
    private static RedisCache redisCache;

    private static final String AUCTION_KEY = "auction:";
    private static final String BID_KEY = "bid:";

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
            jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public void putBid(BidDAO bid) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(BID_KEY+bid.getId(), mapper.writeValueAsString(bid));
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

    public AuctionDAO getAuction(String id) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            String stringUser = jedis.get(AUCTION_KEY+id);
            return mapper.readValue(stringUser, AuctionDAO.class);
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the auction in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<AuctionDAO> getAuctionByUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            List<AuctionDAO> resList = new ArrayList<>();
            //fetch all the keys that match the pattern 'auction:'
            Set<String> keys = jedis.keys(AUCTION_KEY);
            //TODO: what happens if the user has no auctions or there are no keys in the cache?

            //iterate over the keys and fetch the auctions
            for(String key : keys) {
                String stringAuction = jedis.get(key);
                AuctionDAO auction = mapper.readValue(stringAuction, AuctionDAO.class);
                if(auction.getOwnerNickname().equals(nickname)) {
                    resList.add(auction);
                }
            }
            return resList;
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the auctions in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<BidDAO> getBidsByUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            List<BidDAO> resList = new ArrayList<>();
            //fetch all the keys that match the pattern 'bid:'
            Set<String> keys = jedis.keys(BID_KEY);
            //iterate over the keys and fetch the bids that match the user nickname
            for(String key : keys) {
                String stringBid = jedis.get(key);
                BidDAO bid = mapper.readValue(stringBid, BidDAO.class);
                if(bid.getUserNickname().equals(nickname)) {
                    resList.add(bid);
                }
            }
            return resList;
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the bids in cache.\n"+e.getMessage());
            return null;
        }
    }

    public List<BidDAO> getBidsByAuction(String auctionId) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()){
            List<BidDAO> resList = new ArrayList<>();
            //fetch all the keys that match the pattern 'bid:'
            Set<String> keys = jedis.keys(BID_KEY);
            //iterate over the keys and fetch the bids that match the auction id
            for(String key : keys) {
                String stringBid = jedis.get(key);
                BidDAO bid = mapper.readValue(stringBid, BidDAO.class);
                if(bid.getAuctionId().equals(auctionId)) {
                    resList.add(bid);
                }
            }
            return resList;
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to get the bids in cache.\n"+e.getMessage());
            return null;
        }
    }

    public void deleteUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.del("user:"+nickname);
        }
    }

}
