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
			userDao = cosmosDBLayer.putUser(userDao).getItem();
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
			userDAO = cosmosDBLayer.replaceUser(userDAO).getItem();
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
			auctionDAO = cosmosDBLayer.putAuction(auctionDAO).getItem();
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putAuction(auctionDAO);
			}
			return auction;
		}
		catch(CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public AuctionDAO getAuctionById(String id) {
		AuctionDAO auctionDao;
		if(RedisCache.IS_ACTIVE ) {
			auctionDao = redisCache.getAuction(id);
			if(auctionDao == null) {
				auctionDao = cosmosDBLayer.getAuctionById(id);
			}
		}
		else {
			auctionDao = cosmosDBLayer.getAuctionById(id);
		}
		return auctionDao;
	}

	public void replaceAuction(AuctionDAO auction) {
		try {
			auction = cosmosDBLayer.replaceAuction(auction).getItem();
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putAuction(auction);
			}
		}
		catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public Bid putBid(Bid bid, AuctionDAO auctionDAO) {
		BidDAO bidDAO = new BidDAO(bid);
		try {
			bidDAO = cosmosDBLayer.putBid(bidDAO).getItem();
			auctionDAO = cosmosDBLayer.replaceAuction(auctionDAO).getItem();
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putBid(bidDAO);
				redisCache.putAuction(auctionDAO);
			}
			return bid;
		}
		catch(CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	public List<BidDAO> getBidsByUser(String nickname) {
		List<BidDAO> bidsDao;
		if(RedisCache.IS_ACTIVE ) {
			bidsDao = redisCache.getBidsByUser(nickname);
			if(bidsDao == null) {
				bidsDao = cosmosDBLayer.getBidsByUser(nickname);
			}
		}
		else {
			bidsDao = cosmosDBLayer.getBidsByUser(nickname);
		}
		return bidsDao;
	}

	public void replaceBid(BidDAO bid) {
		try {
			cosmosDBLayer.replaceBid(bid).getItem();
		}
		catch (CosmosException e) {
			throw new WebApplicationException(e.getStatusCode());
		}
	}

	// methods not using cache

	public List<AuctionDAO> getAuctionsByUser(String nickname) {
		return cosmosDBLayer.getAuctionsByUser(nickname);
	}

	public List<BidDAO> getBidsByAuction(String auctionId) {
		return cosmosDBLayer.getBidsByAuction(auctionId);
	}

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
