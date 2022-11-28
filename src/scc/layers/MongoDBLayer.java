package scc.layers;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.QuestionDAO;
import scc.dao.UserDAO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import static scc.utils.AzureProperties.COSMOSDB_DATABASE;

public class MongoDBLayer implements DatabaseLayer {

	private static final String USERS = "users";
	private static final String AUCTIONS = "auctions";
	private static final String BIDS = "bids";
	private static final String QUESTIONS = "questions";

	private static final String DB_NAME = System.getenv(COSMOSDB_DATABASE);

	private static MongoDBLayer instance;

	public static synchronized MongoDBLayer getInstance() {
		if(instance != null) {
			return instance;
		}

		// TODO - http://mongodb.github.io/mongo-java-driver/4.0/driver/getting-started/quick-start-pojo/
		// TODO - Não sei bem se esta parte é assim que se inicializa
		MongoClient client = new MongoClient();

		instance = new MongoDBLayer(client);
		return instance;
	}

	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<UserDAO> users;
	private MongoCollection<AuctionDAO> auctions;
	private MongoCollection<BidDAO> bids;
	private MongoCollection<QuestionDAO> questions;

	public MongoDBLayer(MongoClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if( db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getCollection(USERS, UserDAO.class);
		auctions =  db.getCollection(AUCTIONS, AuctionDAO.class);
		bids =  db.getCollection(BIDS, BidDAO.class);
		questions =  db.getCollection(QUESTIONS, QuestionDAO.class);
	}

	public void delUserById(String id) {
		init();

		users.deleteOne(Filters.eq("id", id));
		//PartitionKey key = new PartitionKey(id);
		//return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public void delUser(UserDAO user) {
		delUserById(user.getId());
		//return users.deleteItem(user, new CosmosItemRequestOptions());
	}

	public UserDAO putUser(UserDAO user) {
		init();

		users.insertOne(user);
		return user;
		//return users.createItem(user);
	}

	public UserDAO getUserById(String id) {
		init();

		return users.find(Filters.eq("id", id), UserDAO.class).first();
		/*CosmosPagedIterable<UserDAO> iterable = users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
		Iterator<UserDAO> iterator = iterable.iterator();
		UserDAO userDAO = null;
		if(iterator.hasNext())
			userDAO = iterator.next();
		return userDAO;*/
	}

	public UserDAO replaceUser(UserDAO user) {
		init();

		users.replaceOne(Filters.eq("id",user.getId()), user);
		return user;
		//PartitionKey key = new PartitionKey(user.getId());
		//return users.replaceItem(user, user.getId(), key, new CosmosItemRequestOptions());
	}

	public List<UserDAO> getUsers() {
		init();

		return users.find(UserDAO.class).into(new ArrayList<UserDAO>());
		//return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	// Auctions
	public AuctionDAO putAuction(AuctionDAO auction) {
		init();

		auctions.insertOne(auction);
		return auction;
		//return auctions.createItem(auction);
	}

	public AuctionDAO getAuctionById(String id) {
		init();

		return auctions.find(Filters.eq("id", id), AuctionDAO.class).first();
		/*CosmosPagedIterable<AuctionDAO> iterable = auctions.queryItems("SELECT * FROM auctions WHERE auctions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
		Iterator<AuctionDAO> iterator = iterable.iterator();
		AuctionDAO auctionDAO = null;
		if(iterator.hasNext())
			auctionDAO = iterator.next();
		return auctionDAO;*/
	}

	public void delAuctionById(String id) {
		init();

		auctions.deleteOne(Filters.eq("id", id));
		/*PartitionKey key = new PartitionKey(id);
		return auctions.deleteItem(id, key, new CosmosItemRequestOptions());*/
	}

	public AuctionDAO replaceAuction(AuctionDAO auction) {
		init();

		auctions.replaceOne(Filters.eq("id",auction.getId()), auction);
		return auction;
		/*PartitionKey key = new PartitionKey(auction.getId());
		return auctions.replaceItem(auction, auction.getId(), key, new CosmosItemRequestOptions());*/
	}

	public List<AuctionDAO> getAuctionsByUser(String nickname) {
		init();

		return auctions.find(Filters.eq("ownerNickname", nickname), AuctionDAO.class).into(new ArrayList<>());
		/*CosmosPagedIterable<AuctionDAO> iterable = auctions.queryItems("SELECT * FROM auctions WHERE CONTAINS(auctions.ownerNickname,\"" + nickname + "\")", new CosmosQueryRequestOptions(), AuctionDAO.class);
		return iterable.stream().toList();*/
	}

	public List<AuctionDAO> getAuctionsAboutToClose() {
		init();

		LocalDate now = LocalDate.now(ZoneId.systemDefault());
		LocalDateTime beginNow = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
		LocalDateTime endNow = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59);

		return auctions.find(Filters.and( Filters.gt("endtime", beginNow.toString()), Filters.lt("endtime",endNow.toString()) ), AuctionDAO.class).into(new ArrayList<>());
		/*CosmosPagedIterable<AuctionDAO> iterable = auctions.queryItems(
				"SELECT * " +
						"FROM auctions " +
						"WHERE auctions.endTime BETWEEN '"+beginNow.toString()+"' and '"+endNow.toString()+"'",
				new CosmosQueryRequestOptions(), AuctionDAO.class);

		return iterable.stream().filter(auctionDAO -> auctionDAO.getStatus().equals(AuctionStatus.OPEN)).toList();*/
	}

	// Bids
	public BidDAO putBid(BidDAO bid) {
		init();

		bids.insertOne(bid);
		return bid;
		//return bids.createItem(bid);
	}

	public List<BidDAO> getBidsByAuction(String auctionId) {
		init();

		return bids.find(Filters.eq("auctionId", auctionId), BidDAO.class).into(new ArrayList<>());
		/*CosmosPagedIterable<BidDAO> iterable = bids.queryItems("SELECT * FROM bids WHERE CONTAINS(bids.auctionId,\"" + auctionId + "\")", new CosmosQueryRequestOptions(), BidDAO.class);
		return iterable.stream().toList();*/
	}

	public List<BidDAO> getBidsByUser(String nickname) {
		init();

		return bids.find(Filters.eq("userNickname", nickname), BidDAO.class).into(new ArrayList<>());
		/*CosmosPagedIterable<BidDAO> iterable = bids.queryItems("SELECT * FROM bids WHERE CONTAINS(bids.userNickname,\"" + nickname + "\")", new CosmosQueryRequestOptions(), BidDAO.class);
		return iterable.stream().toList();*/
	}

	public BidDAO replaceBid(BidDAO bid) {
		init();

		bids.replaceOne(Filters.eq("id",bid.getId()), bid);
		return bid;
		/*PartitionKey key = new PartitionKey(bid.getId());
		return bids.replaceItem(bid, bid.getId(), key, new CosmosItemRequestOptions());*/
	}

	public QuestionDAO putQuestion(QuestionDAO question) {
		init();

		questions.insertOne(question);
		return question;
		//return questions.createItem(question);
	}

	public QuestionDAO getQuestionById(String id){
		init();

		return questions.find(Filters.eq("id", id), QuestionDAO.class).first();
		/*CosmosPagedIterable<QuestionDAO> iterable = questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
		Iterator<QuestionDAO> iterator = iterable.iterator();
		QuestionDAO questionDAO = null;
		if(iterator.hasNext())
			questionDAO = iterator.next();
		return questionDAO;*/
	}

	public List<QuestionDAO> getQuestionsByAuctionId(String auctionId){
		init();

		return questions.find(Filters.eq("auctionId", auctionId), QuestionDAO.class).into(new ArrayList<>());
		/*CosmosPagedIterable<QuestionDAO> iterable = questions.queryItems("SELECT * FROM questions WHERE CONTAINS(questions.auctionId,\"" + auctionId + "\")", new CosmosQueryRequestOptions(), QuestionDAO.class);
		return iterable.stream().toList();*/
	}

	public void close() {
		client.close();
	}
}
