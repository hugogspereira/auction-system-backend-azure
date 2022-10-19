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
    private static final String USER_KEY = "user:";
    private static final String USER_AUCTIONS_KEY = "user_auctions_";
    private static final String USER_BIDS_KEY = "user_bids_";
    private static final String BIDS_AUCTION_KEY = "bids_auction_";

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
            jedis.set(USER_KEY+user.getNickname(), mapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the user in cache.\n"+e.getMessage());
        }
    }

    public void putAuction(AuctionDAO auction) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(AUCTION_KEY+auction.getId(), mapper.writeValueAsString(auction));
            // TODO: Quando se remove o owner (deleteUser) faz sentido apagar a lista
            jedis.lpush(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":", mapper.writeValueAsString(auction));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the auction in cache.\n"+e.getMessage());
        }
    }

    public void putBid(BidDAO bid) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            jedis.set(BID_KEY+bid.getId(), mapper.writeValueAsString(bid));
            // TODO: Quando se remove o owner (deleteUser) faz sentido apagar a lista
            jedis.lpush(USER_BIDS_KEY+bid.getUserNickname()+":", mapper.writeValueAsString(bid));
            jedis.lpush(BIDS_AUCTION_KEY+bid.getAuctionId()+":", mapper.writeValueAsString(bid));
        } catch (JsonProcessingException e) {
            System.out.println("Redis Cache: unable to put the bid in cache.\n"+e.getMessage());
        }
    }

    public boolean existUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            return jedis.exists(USER_KEY+nickname);
        }
    }

    public UserDAO getUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            String stringUser = jedis.get(USER_KEY+nickname);
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

    public List<AuctionDAO> getAuctionsByUser(String nickname) {
        ObjectMapper mapper = new ObjectMapper();
        try(Jedis jedis = instance.getResource()) {
            List<String> listOfAuctions = jedis.lrange(USER_AUCTIONS_KEY+nickname+":", 0, -1);
            // TODO: Tentar ver algo deste genero para n estar a fazer o for
            //List<AuctionDAO> list = mapper.readValue(listOfAuctions, AuctionDAO[].class);

            //iterate over the auctions
            List<AuctionDAO> resList = new ArrayList<>(listOfAuctions.size());
            for(String auction : listOfAuctions) {
                AuctionDAO auctionDAO = mapper.readValue(auction, AuctionDAO.class);
                resList.add(auctionDAO);
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
            List<String> listOfBids = jedis.lrange(USER_BIDS_KEY+nickname+":", 0, -1);
            List<BidDAO> resList = new ArrayList<>(listOfBids.size());

            //iterate over the bids
            for(String bid : listOfBids) {
                BidDAO bidDAO = mapper.readValue(bid, BidDAO.class);
                resList.add(bidDAO);
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
            List<String> listOfBids = jedis.lrange(BIDS_AUCTION_KEY+auctionId+":", 0, -1);
            List<BidDAO> resList = new ArrayList<>(listOfBids.size());

            //iterate over the bids
            for(String bid : listOfBids) {
                BidDAO bidDAO = mapper.readValue(bid, BidDAO.class);
                resList.add(bidDAO);
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
            jedis.del(USER_KEY+nickname);
        }
    }

}
