package scc.srv;

import com.azure.cosmos.CosmosException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.dao.UserDAO;
import scc.layers.BlobStorageLayer;
import scc.layers.CosmosDBLayer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.model.User;

import java.util.Iterator;

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
        if(user == null || user.getNickname() == null || user.getPhotoId() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        else if(!blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // ?
        }
        // Create User
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
        // TODO: After a user is deleted, auctions and bids from the user appear as been performed by a default “Deleted User” user.

        if(nickname == null || password == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Get User
        UserDAO userDao = null;
        try {
            userDao = cosmosDBLayer.getUserById(nickname);
        }
        catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        /* Needed ??? Maybe not */
        if(userDao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else if(userDao.getPwd().equals(password)) {
            // Delete User
            try {
                CosmosItemResponse<Object> response2 = cosmosDBLayer.delUserById(nickname);
                UserDAO deletedUser = (UserDAO) response2.getItem();
                return new User(deletedUser.getNickname(), deletedUser.getName(), deletedUser.getPwd(), deletedUser.getPhotoId());
            }
            catch (CosmosException e) {
                throw new WebApplicationException(e.getStatusCode());
            }
        }
        else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

    }

    @PUT
    @Path("/{nickname}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@PathParam("nickname") String nickname, @QueryParam("password") String password, User user) { // void  ?
        if(nickname == null || password == null || user == null || !nickname.equals(user.getNickname())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Get User
        UserDAO userDao = null;
        try {
            userDao = cosmosDBLayer.getUserById(nickname);
        }
        catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
        /* Needed ??? Maybe not */
        if(userDao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else if(!(userDao.getPwd().equals(password))) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Delete User
        // TODO: Need to be changed, because document says that when deleting a user all the other things happen, so it is needed to distinguish that events
        // Create User
        try {
            cosmosDBLayer.delUserById(nickname);
            cosmosDBLayer.putUser(new UserDAO(user));
        }
        catch (CosmosException e) {
            throw new WebApplicationException(e.getStatusCode());
        }
    }
}
