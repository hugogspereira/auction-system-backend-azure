package scc.layers;

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
public class RedisMongoLayer {

	private final MongoDBLayer mongoDBLayer;
	private final RedisCache redisCache;

	private static RedisMongoLayer instance;

	public static synchronized RedisMongoLayer getInstance() {
		if( instance != null)
			return instance;

		instance = new RedisMongoLayer();
		return instance;
	}

	public RedisMongoLayer() {
		this.mongoDBLayer = MongoDBLayer.getInstance();
		this.redisCache = RedisCache.getInstance();
	}





	// USERS
	public User putUser(User user) {
		UserDAO userDao = new UserDAO(user);
		mongoDBLayer.putUser(userDao);
		return user;
	}

	public UserDAO getUserById(String id) {
		UserDAO userDao;
		if(RedisCache.IS_ACTIVE ) {
			userDao = redisCache.getUser(id);
			if(userDao == null) {
				userDao = mongoDBLayer.getUserById(id);
				if(userDao != null) {
					redisCache.putUser(userDao);
				}
			}
		}
		else {
			userDao = mongoDBLayer.getUserById(id);
		}
		return userDao;
	}

	public void replaceUser(UserDAO userDAO) {
		userDAO = mongoDBLayer.replaceUser(userDAO);
		if(RedisCache.IS_ACTIVE ) {
			redisCache.putUser(userDAO);
		}
	}

	public void deleteUser(String nickname) {
		mongoDBLayer.delUserById(nickname);
		if(RedisCache.IS_ACTIVE ) {
			redisCache.deleteUser(nickname);
		}
	}





	// AUCTIONS
	public Auction putAuction(Auction auction) {
		AuctionDAO auctionDAO = new AuctionDAO(auction);
		mongoDBLayer.putAuction(auctionDAO);
		return auction;
	}

	public void replaceAuction(AuctionDAO auction) {
		auction = mongoDBLayer.replaceAuction(auction);
		if(RedisCache.IS_ACTIVE) {
			redisCache.replaceAuction(auction);
		}
	}

	public AuctionDAO getAuctionById(String id) {
		AuctionDAO auctionDao;
		if(RedisCache.IS_ACTIVE ) {
			auctionDao = redisCache.getAuction(id);
			if(auctionDao == null) {
				auctionDao = mongoDBLayer.getAuctionById(id);
				if(auctionDao == null) { return null; }
				redisCache.putAuction(auctionDao);
			}
		}
		else {
			auctionDao = mongoDBLayer.getAuctionById(id);
		}
		return auctionDao;
	}



	// BIDS
	public Bid putBid(Bid bid, AuctionDAO auctionDAO) {
		BidDAO bidDAO = new BidDAO(bid);
		mongoDBLayer.putBid(bidDAO);
		AuctionDAO auction = mongoDBLayer.replaceAuction(auctionDAO);
		if(RedisCache.IS_ACTIVE) {
			redisCache.replaceAuction(auction);
		}
		return bid;
	}



	// Methods not using cache

	public void replaceBid(BidDAO bid) {
		mongoDBLayer.replaceBid(bid);
	}

	public List<AuctionDAO> getAuctionsByUser(String nickname) {
		return mongoDBLayer.getAuctionsByUser(nickname);
	}

	public List<BidDAO> getBidsByUser(String nickname) {
		return mongoDBLayer.getBidsByUser(nickname);
	}

	public List<BidDAO> getBidsByAuction(String auctionId) {
		return mongoDBLayer.getBidsByAuction(auctionId);
	}

	public QuestionDAO getQuestionById(String id) {
		return mongoDBLayer.getQuestionById(id);
	}

	public Question putQuestion(Question question) {
		QuestionDAO questionDAO = new QuestionDAO(question);
		mongoDBLayer.putQuestion(questionDAO);
		return question;
	}

	public List<QuestionDAO> getQuestionsByAuctionId(String id) {
		return mongoDBLayer.getQuestionsByAuctionId(id);
	}

	public List<AuctionDAO> getAuctionAboutToClose() {
		return mongoDBLayer.getAuctionsAboutToClose();
	}
}