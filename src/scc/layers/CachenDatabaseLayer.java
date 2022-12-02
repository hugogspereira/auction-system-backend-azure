package scc.layers;

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
public class CachenDatabaseLayer {

	//public static final boolean USE_COSMOSDB = false;	// false = use MongoDB

	private final DatabaseLayer databaseLayer;
	private final RedisCache redisCache;

	private static CachenDatabaseLayer instance;

	public static synchronized CachenDatabaseLayer getInstance() {
		if( instance != null)
			return instance;

		instance = new CachenDatabaseLayer();
		return instance;
	}

	public CachenDatabaseLayer() {
		//this.databaseLayer = USE_COSMOSDB ? CosmosDBLayer.getInstance() : MongoDBLayer.getInstance();
		this.databaseLayer = MongoDBLayer.getInstance();
		this.redisCache = RedisCache.getInstance();
	}





	// USERS
	public User putUser(User user) {
		UserDAO userDao = new UserDAO(user);
		try {
			databaseLayer.putUser(userDao);
			return user;
		}
		catch(Exception e) {
			throw new WebApplicationException(e);
		}
	}

	public UserDAO getUserById(String id) {
		UserDAO userDao;
		if(RedisCache.IS_ACTIVE ) {
			userDao = redisCache.getUser(id);
			System.out.println("cache user -> "+userDao);
			if(userDao == null) {
				userDao = databaseLayer.getUserById(id);
				if(userDao != null) {
					redisCache.putUser(userDao);
				}
			}
		}
		else {
			userDao = databaseLayer.getUserById(id);
		}
		return userDao;
	}

	public void replaceUser(UserDAO userDAO) {
		try {
			userDAO = databaseLayer.replaceUser(userDAO);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.putUser(userDAO);
			}
		}
		catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}

	public void deleteUser(String nickname) {
		try {
			databaseLayer.delUserById(nickname);
			if(RedisCache.IS_ACTIVE ) {
				redisCache.deleteUser(nickname);
			}
		}
		catch(Exception e) {
			throw new WebApplicationException(e);
		}
	}





	// AUCTIONS
	public Auction putAuction(Auction auction) {
		AuctionDAO auctionDAO = new AuctionDAO(auction);
		try {
			databaseLayer.putAuction(auctionDAO);
			return auction;
		}
		catch(Exception e) {
			throw new WebApplicationException(e);
		}
	}

	public void replaceAuction(AuctionDAO auction) {
		try {
			auction = databaseLayer.replaceAuction(auction);
			if(RedisCache.IS_ACTIVE) {
				redisCache.replaceAuction(auction);
			}
		}
		catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	public AuctionDAO getAuctionById(String id) {
		AuctionDAO auctionDao;
		if(RedisCache.IS_ACTIVE ) {
			auctionDao = redisCache.getAuction(id);
			System.out.println("cache auction -> "+auctionDao);
			if(auctionDao == null) {
				auctionDao = databaseLayer.getAuctionById(id);
				if(auctionDao == null) { return null; }
				redisCache.putAuction(auctionDao);
			}
		}
		else {
			auctionDao = databaseLayer.getAuctionById(id);
		}
		return auctionDao;
	}



	// BIDS
	public Bid putBid(Bid bid, AuctionDAO auctionDAO) {
		BidDAO bidDAO = new BidDAO(bid);
		try {
			databaseLayer.putBid(bidDAO);
			AuctionDAO auction = databaseLayer.replaceAuction(auctionDAO);
			if(RedisCache.IS_ACTIVE) {
				redisCache.replaceAuction(auction);
			}
			return bid;
		}
		catch(Exception e) {
			throw new WebApplicationException(e);
		}
	}



	// Methods not using cache

	public void replaceBid(BidDAO bid) {
		try {
			databaseLayer.replaceBid(bid);
		}
		catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	public List<AuctionDAO> getAuctionsByUser(String nickname) {
		return databaseLayer.getAuctionsByUser(nickname);
	}

	public List<BidDAO> getBidsByUser(String nickname) {
		return databaseLayer.getBidsByUser(nickname);
	}

	public List<BidDAO> getBidsByAuction(String auctionId) {
		return databaseLayer.getBidsByAuction(auctionId);
	}

	public QuestionDAO getQuestionById(String id) {
		return databaseLayer.getQuestionById(id);
	}

	public Question putQuestion(Question question) {
		QuestionDAO questionDAO = new QuestionDAO(question);
		databaseLayer.putQuestion(questionDAO);
		return question;
	}

	public List<QuestionDAO> getQuestionsByAuctionId(String id) {
		return databaseLayer.getQuestionsByAuctionId(id);
	}

	public List<AuctionDAO> getAuctionAboutToClose() {
		return databaseLayer.getAuctionsAboutToClose();
	}
}