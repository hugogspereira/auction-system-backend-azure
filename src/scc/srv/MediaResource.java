package scc.srv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import scc.layers.BlobStorageLayer;
import scc.utils.Hash;

import jakarta.ws.rs.core.MediaType;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource
{
	BlobStorageLayer blob = BlobStorageLayer.getInstance();
	ObjectMapper mapper = new ObjectMapper();

	/**
	 * Post a new image.The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String id = Hash.of(contents);
		blob.upload(id, contents);
		return id;
	}

	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		try {
			return blob.download(id);
		} catch (Exception e) {
			throw new ClientErrorException("Image not found", Response.Status.NOT_FOUND);
		}
	}

	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String list() {
		try {
			return mapper.writeValueAsString(blob.list());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
