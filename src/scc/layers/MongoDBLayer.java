package scc.layers;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.QuestionDAO;
import scc.dao.UserDAO;
import scc.utils.AuctionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static scc.utils.AzureProperties.*;

public class MongoDBLayer implements DatabaseLayer {

	private static final String USERS = "users";
	private static final String AUCTIONS = "auctions";
	private static final String BIDS = "bids";
	private static final String QUESTIONS = "questions";

	private static final String DB_HOSTNAME = System.getenv(MONGODB_HOSTNAME);
	private static final String DB_NAME = System.getenv(MONGODB_DATABASE);


	private static MongoDBLayer instance;

	public static synchronized MongoDBLayer getInstance() {
		if(instance != null) {
			return instance;
		}
		MongoClient client = new MongoClient(DB_HOSTNAME, 27017);		//TODO: porta em system.env

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

		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		db = client.getDatabase(DB_NAME).withCodecRegistry(pojoCodecRegistry);
		users = db.getCollection(USERS, UserDAO.class);
		auctions =  db.getCollection(AUCTIONS, AuctionDAO.class);
		bids =  db.getCollection(BIDS, BidDAO.class);
		questions =  db.getCollection(QUESTIONS, QuestionDAO.class);
	}

	public void delUserById(String id) {
		init();

		users.deleteOne(Filters.eq("_id", id));
	}

	public UserDAO putUser(UserDAO user) {
		init();

		users.insertOne(user);
		return user;
	}

	public UserDAO getUserById(String id) {
		init();

		return users.find(Filters.eq("_id", id), UserDAO.class).first();
	}

	public UserDAO replaceUser(UserDAO user) {
		init();

		users.replaceOne(Filters.eq("_id",user.get_id()), user);
		return user;
	}

	public List<UserDAO> getUsers() {
		init();

		return users.find(UserDAO.class).into(new ArrayList<UserDAO>());
	}

	// Auctions
	public AuctionDAO putAuction(AuctionDAO auction) {
		init();

		auctions.insertOne(auction);
		return auction;
	}

	public AuctionDAO getAuctionById(String id) {
		init();

		return auctions.find(Filters.eq("_id", id), AuctionDAO.class).first();
	}

	public AuctionDAO replaceAuction(AuctionDAO auction) {
		init();

		auctions.replaceOne(Filters.eq("_id",auction.get_id()), auction);
		return auction;
	}

	public List<AuctionDAO> getAuctionsByUser(String nickname) {
		init();

		return auctions.find(Filters.eq("ownerNickname", nickname), AuctionDAO.class).into(new ArrayList<>());
	}

	public List<AuctionDAO> getAuctionsAboutToClose() {
		init();

		LocalDate now = LocalDate.now(ZoneId.systemDefault());
		LocalDateTime beginNow = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
		LocalDateTime endNow = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59);

		//TODO Filters.where?
		return auctions.find(Filters.and( Filters.gt("endTime", beginNow.toString()), Filters.lt("endTime",endNow.toString()) ), AuctionDAO.class).filter(Filters.eq("status", AuctionStatus.OPEN)).into(new ArrayList<>());
	}

	// Bids
	public BidDAO putBid(BidDAO bid) {
		init();

		bids.insertOne(bid);
		return bid;
	}

	public List<BidDAO> getBidsByAuction(String auctionId) {
		init();

		return bids.find(Filters.eq("auctionId", auctionId), BidDAO.class).into(new ArrayList<>());
	}

	public List<BidDAO> getBidsByUser(String nickname) {
		init();

		return bids.find(Filters.eq("userNickname", nickname), BidDAO.class).into(new ArrayList<>());
	}

	public BidDAO replaceBid(BidDAO bid) {
		init();

		bids.replaceOne(Filters.eq("_id",bid.get_id()), bid);
		return bid;
	}

	public QuestionDAO putQuestion(QuestionDAO question) {
		init();

		questions.insertOne(question);
		return question;
	}

	public QuestionDAO getQuestionById(String id){
		init();

		return questions.find(Filters.eq("_id", id), QuestionDAO.class).first();
	}

	public List<QuestionDAO> getQuestionsByAuctionId(String auctionId){
		init();

		return questions.find(Filters.eq("auctionId", auctionId), QuestionDAO.class).into(new ArrayList<>());
	}

	public void close() {
		client.close();
	}
}
