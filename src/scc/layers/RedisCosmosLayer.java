package scc.layers;

import com.azure.cosmos.CosmosException;
import jakarta.ws.rs.WebApplicationException;
import scc.cache.RedisCache;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.QuestionDAO;
import scc.dao.UserDAO;
import scc.model.Auction;
import scc.model.Bid;
import scc.model.Question;
import scc.model.User;
import java.util.List;
import static scc.cache.RedisCache.*;

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





	// USERS
	public User putUser(User user) {
		UserDAO userDao = new UserDAO(user);
		try {
			cosmosDBLayer.putUser(userDao);
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
				if(userDao != null) {
					redisCache.putUser(userDao);
				}
			}
		}
		else {
			userDao = cosmosDBLayer.getUserById(id);
		}
		return userDao;
	}

	public void replaceUser(UserDAO userDAO) {
		try {
			userDAO = cosmosDBLayer.replaceUser(userDAO).getItem();
			if(RedisCache.IS_ACTIVE ) {
				redisCache.replaceUser(userDAO);
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





	// BIDS
	public Bid putBid(Bid bid, AuctionDAO auctionDAO) {
		BidDAO bidDAO = new BidDAO(bid);
		try {
			cosmosDBLayer.putBid(bidDAO);
			cosmosDBLayer.replaceAuction(auctionDAO);
			if(RedisCache.IS_ACTIVE) {
				redisCache.invalidateKey(USER_BIDS_KEY + bid.getUserNickname() + ":");
				redisCache.invalidateKey(BIDS_AUCTION_KEY+bid.getAuctionId()+":");
			}
			return bid;
		}
		catch(CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public void replaceBid(BidDAO bid) {
		try {
			bid = cosmosDBLayer.replaceBid(bid).getItem();
			if(RedisCache.IS_ACTIVE ) {
				redisCache.replaceBid(bid);

				redisCache.invalidateKey(USER_BIDS_KEY + bid.getUserNickname() + ":");
				redisCache.invalidateKey(BIDS_AUCTION_KEY+bid.getAuctionId()+":");
			}
		}
		catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public List<BidDAO> getBidsByUser(String nickname) {
		List<BidDAO> bidsDao;
		if(RedisCache.IS_ACTIVE ) {
			bidsDao = redisCache.getBidsByUser(nickname);
			if(bidsDao == null) {
				bidsDao = cosmosDBLayer.getBidsByUser(nickname);
				if(bidsDao == null) { return null; }
				for (BidDAO bid: bidsDao) {
					redisCache.putBid(bid);
				}
			}
		}
		else {
			bidsDao = cosmosDBLayer.getBidsByUser(nickname);
		}
		return bidsDao;
	}

	public List<BidDAO> getBidsByAuction(String auctionId) {
		List<BidDAO> bidsDao;
		if(RedisCache.IS_ACTIVE ) {
			bidsDao = redisCache.getBidsByAuction(auctionId);
			if(bidsDao == null) {
				bidsDao = cosmosDBLayer.getBidsByAuction(auctionId);
				if(bidsDao == null) { return null; }
				for (BidDAO bid: bidsDao) {
					redisCache.putBid(bid);
				}
			}
		}
		else {
			bidsDao = cosmosDBLayer.getBidsByAuction(auctionId);
		}
		return bidsDao;
	}





	// AUCTIONS
	public Auction putAuction(Auction auction) {
		AuctionDAO auctionDAO = new AuctionDAO(auction);
		try {
			cosmosDBLayer.putAuction(auctionDAO);
			if(RedisCache.IS_ACTIVE) {
				redisCache.invalidateKey(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":");
			}
			return auction;
		}
		catch(CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public void replaceAuction(AuctionDAO auction) {
		try {
			auction = cosmosDBLayer.replaceAuction(auction).getItem();
			if(RedisCache.IS_ACTIVE) {
				redisCache.replaceAuction(auction);

				redisCache.invalidateKey(USER_AUCTIONS_KEY+auction.getOwnerNickname()+":");
			}
		}
		catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public AuctionDAO getAuctionById(String id) {
		AuctionDAO auctionDao;
		if(RedisCache.IS_ACTIVE ) {
			auctionDao = redisCache.getAuction(id);
			if(auctionDao == null) {
				auctionDao = cosmosDBLayer.getAuctionById(id);
				if(auctionDao == null) { return null; }
				redisCache.putAuction(auctionDao);
			}
		}
		else {
			auctionDao = cosmosDBLayer.getAuctionById(id);
		}
		return auctionDao;
	}

	public List<AuctionDAO> getAuctionsByUser(String nickname) {
		List<AuctionDAO> auctionsDao;
		if(RedisCache.IS_ACTIVE ) {
			auctionsDao = redisCache.getAuctionsByUser(nickname);
			if(auctionsDao == null) {
				auctionsDao = cosmosDBLayer.getAuctionsByUser(nickname);
				if(auctionsDao == null) { return null; }
				for (AuctionDAO auction: auctionsDao) {
					redisCache.putAuction(auction);
				}
			}
		}
		else {
			auctionsDao = cosmosDBLayer.getAuctionsByUser(nickname);
		}
		return auctionsDao;
	}





	// Methods not using cache

	public QuestionDAO getQuestionById(String id) {
		return cosmosDBLayer.getQuestionById(id);
	}

	public Question putQuestion(Question question) {
		QuestionDAO questionDAO = new QuestionDAO(question);
		cosmosDBLayer.putQuestion(questionDAO);
		return question;
	}

	public List<QuestionDAO> getQuestionsByAuctionId(String id) {
		return cosmosDBLayer.getQuestionsByAuctionId(id);
	}

	public List<AuctionDAO> getAuctionAboutToClose() {
		return cosmosDBLayer.getAuctionsAboutToClose();
	}
}
