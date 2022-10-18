package scc.srv;

import com.azure.cosmos.CosmosException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;
import scc.layers.BlobStorageLayer;
import scc.layers.RedisCosmosLayer;
import scc.model.User;
import scc.utils.Hash;
import java.util.List;
import static scc.dao.AuctionDAO.DELETED_USER;

@Path(UsersResource.PATH)
public class UsersResource {

    public static final String PATH = "/user";
    //private final CosmosDBLayer cosmosDBLayer;
    private final BlobStorageLayer blobStorageLayer;
    //private final RedisCache redisCache;
    private final RedisCosmosLayer redisCosmosLayer;

    public UsersResource() {
        //cosmosDBLayer = CosmosDBLayer.getInstance();
        blobStorageLayer = BlobStorageLayer.getInstance();
        //redisCache = RedisCache.getInstance();
        redisCosmosLayer = RedisCosmosLayer.getInstance();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User createUser(User user) {
        if(user == null || user.getNickname() == null || user.getName() == null || user.getPwd() == null || user.getPhotoId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        else if(!blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        // Create User
        user.setPwd(Hash.of(user.getPwd()));
        return redisCosmosLayer.putUser(user);
    }

    @DELETE
    @Path("/{nickname}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@PathParam("nickname") String nickname, @QueryParam("password") String password) {
        if(nickname == null || password == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Get User
        UserDAO userDao = redisCosmosLayer.getUserById(nickname);
        // See if User exists
        if(userDao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        checkPwd(userDao.getPwd(), password);

        // Check if there is not an OPEN auction
        List<AuctionDAO> auctions = redisCosmosLayer.getAuctionsByUser(nickname);
        if(auctions.stream().anyMatch(auctionDAO -> auctionDAO.isOpen())) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        // Delete User
        try {
            auctions.forEach(auctionDAO -> {
                auctionDAO.setOwnerNickname(DELETED_USER);
                redisCosmosLayer.replaceAuction(auctionDAO);
            });
            List<BidDAO> bids = redisCosmosLayer.getBidsByUser(nickname);
            bids.forEach(bidDAO -> {
                bidDAO.setUserNickname(DELETED_USER);
                redisCosmosLayer.replaceBid(bidDAO);
            });
            // TODO: maybe garbage collector
            redisCosmosLayer.deleteUser(nickname);
        } catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        return userDao.toUser();
    }

    @PUT
    @Path("/{nickname}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@PathParam("nickname") String nickname, @QueryParam("password") String password, User user) {
        if(nickname == null || password == null || user == null || user.getName() == null || user.getPwd() == null || user.getPhotoId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Get User
        UserDAO userDao = redisCosmosLayer.getUserById(nickname);
        // See if User exists
        if(userDao == null || !blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        checkPwd(userDao.getPwd(), password);
        // Update User
        userDao.setName(user.getName());
        userDao.setPwd(user.getPwd());
        userDao.setPhotoId(user.getPhotoId());
        redisCosmosLayer.replaceUser(userDao);
    }

    private void checkPwd(String expectedPwd, String pwd) {
        if(!expectedPwd.equals(Hash.of(pwd))) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }
}
