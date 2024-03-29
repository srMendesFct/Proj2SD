package microgram.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * REST API of the media storage service...
 * 
 * @author smd
 *
 */
@Path(RestMedia.PATH)
public interface RestMedia {

	public static final String PATH = "/media";

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	String upload(byte[] bytes) throws InterruptedException, ExecutionException, IOException;

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] download(@PathParam("id") String id) throws InterruptedException, ExecutionException, IOException;

	@DELETE
	@Path("/{id}")
	void delete(@PathParam("id") String id) throws InterruptedException, ExecutionException, IOException;
}
