package scc.layers;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.QuestionDAO;
import scc.dao.UserDAO;

import java.util.Iterator;
import java.util.List;

import static scc.utils.AzureProperties.*;

public class CosmosDBLayer {
	private static final String CONNECTION_URL = System.getenv(COSMOSDB_URL);
	private static final String DB_KEY = System.getenv(COSMOSDB_KEY);
	private static final String DB_NAME = System.getenv(COSMOSDB_DATABASE);
	
	private static CosmosDBLayer instance;

	public static synchronized CosmosDBLayer getInstance() {
		if( instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder()
		         .endpoint(CONNECTION_URL)
		         .key(DB_KEY)
		         //.directMode()
		         .gatewayMode()		
		         // replace by .directMode() for better performance
		         .consistencyLevel(ConsistencyLevel.SESSION)
		         .connectionSharingAcrossClientsEnabled(true)
		         .contentResponseOnWriteEnabled(true)
		         .buildClient();
		instance = new CosmosDBLayer( client);
		return instance;
		
	}
	
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users;
	private CosmosContainer auctions;
	private CosmosContainer bids;
	private CosmosContainer questions;


	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}
	
	private synchronized void init() {
		if( db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("users");
		auctions = db.getContainer("auctions");
		bids = db.getContainer("bids");
		questions = db.getContainer("questions");

	}

	public CosmosItemResponse<Object> delUserById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<Object> delUser(UserDAO user) {
		init();
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		init();
		return users.createItem(user);
	}

	public UserDAO getUserById(String id) {
		init();
		CosmosPagedIterable<UserDAO> iterable = users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
		Iterator<UserDAO> iterator = iterable.iterator();
		UserDAO userDAO = null;
		if(iterator.hasNext())
			userDAO = iterator.next();
		return userDAO;
	}

	public CosmosItemResponse<UserDAO> replaceUser(UserDAO user) {
		init();
		PartitionKey key = new PartitionKey(user.getNickname());
		return users.replaceItem(user, user.getNickname(), key, new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	// Auctions
	public CosmosItemResponse<AuctionDAO> putAuction(AuctionDAO auction) {
		init();
		return auctions.createItem(auction);
	}

	public AuctionDAO getAuctionById(String id) {
		init();
		CosmosPagedIterable<AuctionDAO> iterable = auctions.queryItems("SELECT * FROM auctions WHERE auctions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
		Iterator<AuctionDAO> iterator = iterable.iterator();
		AuctionDAO auctionDAO = null;
		if(iterator.hasNext())
			auctionDAO = iterator.next();
		return auctionDAO;
	}

	public CosmosItemResponse<Object> delAuctionById(String id) {
		init();
		PartitionKey key = new PartitionKey(id);
		return auctions.deleteItem(id, key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<AuctionDAO> replaceAuction(AuctionDAO auction) {
		init();
		PartitionKey key = new PartitionKey(auction.getId());
		return auctions.replaceItem(auction, auction.getId(), key, new CosmosItemRequestOptions());
	}

	public List<AuctionDAO> getAuctionsByUser(String nickname) {
		init();
		CosmosPagedIterable<AuctionDAO> iterable = bids.queryItems("SELECT * FROM auctions LEFT JOIN users ON auctions.ownerNickname=\"" + nickname + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);   //TODO: check query
		return iterable.stream().toList();
	}

	// Bids
	public CosmosItemResponse<BidDAO> putBid(BidDAO bid) {
		init();
		return bids.createItem(bid);
	}

	public List<BidDAO> getBidsByAuction(String auctionId) {
		init();
		CosmosPagedIterable<BidDAO> iterable = bids.queryItems("SELECT * FROM bids LEFT JOIN auctions ON bids.auctionId=\"" + auctionId + "\"", new CosmosQueryRequestOptions(), BidDAO.class);   //TODO: check query
		return iterable.stream().toList();
	}

	public List<BidDAO> getBidsByUser(String nickname) {
		init();
		CosmosPagedIterable<BidDAO> iterable = bids.queryItems("SELECT * FROM bids LEFT JOIN users ON bids.userNickname=\"" + nickname + "\"", new CosmosQueryRequestOptions(), BidDAO.class);   //TODO: check query
		return iterable.stream().toList();
	}

	public CosmosItemResponse<BidDAO> replaceBid(BidDAO bid) {
		init();
		PartitionKey key = new PartitionKey(bid.getId());
		return bids.replaceItem(bid, bid.getId(), key, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<QuestionDAO> putQuestion(QuestionDAO question) {
		init();
		return questions.createItem(question);
	}

	public QuestionDAO getQuestionById(String id){
		init();
		CosmosPagedIterable<QuestionDAO> iterable = questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
		Iterator<QuestionDAO> iterator = iterable.iterator();
		QuestionDAO questionDAO = null;
		if(iterator.hasNext())
			questionDAO = iterator.next();
		return questionDAO;
	}

	public List<QuestionDAO> getQuestionsByAuctionId(String auctionId){
		init();
		CosmosPagedIterable<QuestionDAO> iterable = questions.queryItems("SELECT * FROM questions LEFT JOIN auctions ON questions.auctionId=\"" + auctionId + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
		return iterable.stream().toList();
	}

	public void close() {
		client.close();
	}
}
