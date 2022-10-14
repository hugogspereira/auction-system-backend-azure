package scc.srv;

import com.azure.cosmos.CosmosException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.dao.AuctionDAO;
import scc.dao.BidDAO;
import scc.dao.UserDAO;
import scc.layers.BlobStorageLayer;
import scc.layers.CosmosDBLayer;
import scc.model.User;
import scc.utils.Hash;

import java.util.List;

import static scc.dao.AuctionDAO.DELETED_USER;

@Path(UsersResource.PATH)
public class UsersResource {

    public static final String PATH = "/user";

    private final CosmosDBLayer cosmosDBLayer;
    private final BlobStorageLayer blobStorageLayer;

    public UsersResource() {
        cosmosDBLayer = CosmosDBLayer.getInstance();
        blobStorageLayer = BlobStorageLayer.getInstance();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User createUser(User user) {
        if(user == null || user.getNickname() == null || user.getName() == null || user.getPwd() == null || user.getPhotoId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if(!blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        // Create User
        user.setPwd(Hash.of(user.getPwd()));
        try {
            cosmosDBLayer.putUser(new UserDAO(user));
            return user;
        }
        catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
    }

    @DELETE
    @Path("/{nickname}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@PathParam("nickname") String nickname, @QueryParam("password") String password) {
        if(nickname == null || password == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Get User
        UserDAO userDao = cosmosDBLayer.getUserById(nickname);
        if(userDao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        checkPwd(userDao.getPwd(), password);

        // Check if there is not an OPEN auction
        List<AuctionDAO> auctions = cosmosDBLayer.getAuctionsByUser(nickname);
        if(auctions.stream().anyMatch(auctionDAO -> auctionDAO.isOpen())) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        // Delete User
        try {
            auctions.forEach(auctionDAO -> {
                auctionDAO.setOwnerNickname(DELETED_USER);
                cosmosDBLayer.replaceAuction(auctionDAO);
            });
            List<BidDAO> bids = cosmosDBLayer.getBidsByUser(nickname);
            bids.forEach(bidDAO -> {
                bidDAO.setUserNickname(DELETED_USER);
                cosmosDBLayer.replaceBid(bidDAO);
            });
            // TODO: maybe garbage collector
            cosmosDBLayer.delUserById(nickname);
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
        UserDAO userDao = cosmosDBLayer.getUserById(nickname);
        if(userDao == null || !blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        checkPwd(userDao.getPwd(), password);
        // Update User
        userDao.setName(user.getName());
        userDao.setPwd(user.getPwd());
        userDao.setPhotoId(user.getPhotoId());
        try {
            cosmosDBLayer.replaceUser(userDao);
        }
        catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
    }

    private void checkPwd(String expectedPwd, String pwd) {
        if(!expectedPwd.equals(Hash.of(pwd))) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }
}
