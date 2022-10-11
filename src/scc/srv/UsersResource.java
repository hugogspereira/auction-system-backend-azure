package scc.srv;

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

@Path("/user")
public class UsersResource {

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
    public String createUser(User user) {  // return String ?
        if(user == null || user.getNickname() == null || user.getPhotoId() == null || blobStorageLayer.existsBlob(user.getPhotoId())) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Create User
        CosmosItemResponse<UserDAO> response = cosmosDBLayer.putUser(new UserDAO(user));
        UserDAO userDao = response.getItem();
        if(userDao == null) {
            throw new WebApplicationException(response.getStatusCode());
        }
        else {
            return userDao.getNickname();
        }
    }

    @DELETE
    @Path("/{nickname}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@PathParam("nickname") String nickname, @QueryParam("password") String password) { // return User ?
        // TODO: After a user is deleted, auctions and bids from the user appear as been performed by a default “Deleted User” user.

        if(nickname == null || password == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Get User
        CosmosPagedIterable<UserDAO> response1 = cosmosDBLayer.getUserById(nickname);
        Iterator<UserDAO> it = response1.stream().iterator();
        UserDAO userDao = null;
        if(it.hasNext()) {
            userDao = it.next();
        }

        if(userDao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else if(userDao.getPwd().equals(password)) {
            // Delete User
            CosmosItemResponse<Object> response2 = cosmosDBLayer.delUserById(nickname);
            Object obj = response2.getItem();
            if(obj == null) {
                throw new WebApplicationException(response2.getStatusCode());
            }
            UserDAO deletedUser =  (UserDAO) obj;
            return new User(deletedUser.getNickname(), deletedUser.getName(), deletedUser.getPwd(), deletedUser.getPhotoId());
        }
        else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

    }


    @PUT
    @Path("/{nickname}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@PathParam("nickname") String nickname, @QueryParam("password") String password, User user) { // void  ?
        if(nickname == null || password == null || user == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Get User TODO: (To see if the password is the same, this will need to be optimized by auth mechanisms!)
        CosmosPagedIterable<UserDAO> response1 = cosmosDBLayer.getUserById(nickname);
        Iterator<UserDAO> it = response1.stream().iterator();
        UserDAO userDao = null;
        if(it.hasNext()) {
            userDao = it.next();
        }

        if(userDao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else if(!(userDao.getPwd().equals(password)) || (blobStorageLayer.existsBlob(user.getPhotoId()) && !user.getPhotoId().equals(userDao.getPhotoId()))) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Delete User TODO: Need to be changed, because document says that when deleting a user all the other things happen, so it is needed to distinguish that events
        CosmosItemResponse<Object> response2 = cosmosDBLayer.delUserById(nickname);
        Object obj = response2.getItem();
        if(obj == null) {
            throw new WebApplicationException(response2.getStatusCode());
        }
        // Create User
        CosmosItemResponse<UserDAO> response3 = cosmosDBLayer.putUser(new UserDAO(user));
        if(response3.getItem() == null) {
            throw new WebApplicationException(response3.getStatusCode());
        }
    }
}
