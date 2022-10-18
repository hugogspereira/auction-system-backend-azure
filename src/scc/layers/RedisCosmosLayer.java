package scc.layers;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;
import jakarta.ws.rs.WebApplicationException;
import scc.cache.RedisCache;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;
import scc.model.Auction;
import scc.model.User;

import java.util.List;

/**
 * Layer with cache and database
 */
public class RedisCosmosLayer {

	private final CosmosDBLayer cosmosDBLayer;
	private final RedisCache redisCache;

	private static RedisCosmosLayer instance;

	public static synchronized RedisCosmosLayer getInstance() {
		if( instance != null)
			return instance;

		instance = new RedisCosmosLayer();
		return instance;
	}

	public RedisCosmosLayer() {
		this.cosmosDBLayer = CosmosDBLayer.getInstance();
		this.redisCache = RedisCache.getInstance();
	}

	public User putUser(User user) {
		UserDAO userDao = new UserDAO(user);
		try {
			cosmosDBLayer.putUser(userDao);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putUser(userDao);
			}
			return user;
		}
		catch(CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public UserDAO getUserById(String id) {
		UserDAO userDao;
		if(RedisCache.IS_ACTIVE ) {
			userDao = redisCache.getUser(id);
			if(userDao == null) {
				userDao = cosmosDBLayer.getUserById(id);
			}
		}
		else {
			userDao = cosmosDBLayer.getUserById(id);
		}
		return userDao;
	}

	public void replaceUser(UserDAO userDAO) {
		try {
			cosmosDBLayer.replaceUser(userDAO);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putUser(userDAO);
			}
		}
		catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}

	}

	public void deleteUser(String nickname) {
		try {
			cosmosDBLayer.delUserById(nickname);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.deleteUser(nickname);
			}
		}
		catch(CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public Auction putAuction(Auction auction) {
		AuctionDAO auctionDAO = new AuctionDAO(auction);
		try {
			cosmosDBLayer.putAuction(auctionDAO);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putAuction(auctionDAO);
			}
			return auction;
		}
		catch(CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public List<AuctionDAO> getAuctionsByUser(String id) {
		List<AuctionDAO> auctionsDao;
		if(RedisCache.IS_ACTIVE ) {
			auctionsDao = redisCache.getAuctionByUser(id);
			if(auctionsDao == null) {
				auctionsDao = cosmosDBLayer.getAuctionsByUser(id);
			}
		}
		else {
			auctionsDao = cosmosDBLayer.getAuctionsByUser(id);
		}
		return auctionsDao;
	}

	public void replaceAuction(AuctionDAO auction) {
		try {
			cosmosDBLayer.replaceAuction(auction);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putAuction(auction);
			}
		}
		catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public List<BidDAO> getBidsByUser(String id) {
		List<BidDAO> bidsDao;
		if(RedisCache.IS_ACTIVE ) {
			bidsDao = redisCache.getBidsByUser(id);
			if(bidsDao == null) {
				bidsDao = cosmosDBLayer.getBidsByUser(id);
			}
		}
		else {
			bidsDao = cosmosDBLayer.getBidsByUser(id);
		}
		return bidsDao;
	}

	public void replaceBid(BidDAO bid) {
		try {
			cosmosDBLayer.replaceBid(bid);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putBid(bid);
			}
		}
		catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}


}
