package tukano.clients.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.servers.java.JavaUsers;
import tukano.api.servers.java.Result;
import tukano.api.rest.RestShorts;
import tukano.api.servers.java.Result.ErrorCode;
import tukano.api.servers.java.Shorts;
import tukano.api.Short;



public class RestShortsClient implements Shorts {

    final URI serverURI;
	final Client client;
	final ClientConfig config;

	final WebTarget target;
    private static Logger Log = Logger.getLogger(RestShortsClient.class.getName());


    public RestShortsClient( URI serverURI ) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();
		this.client = ClientBuilder.newClient(config);

		target = client.target( serverURI ).path( RestShorts.PATH );
	}

    @Override
    public Result<Short> createShort(String userId, String password) {
        //TODO
        Response response = target.path(userId)
                                  .queryParam(RestShorts.PWD, password)
                                  .request()
                                  .post(Entity.entity(userId, MediaType.APPLICATION_JSON));
        return handleResponse(response, Short.class);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Response response = target.path(shortId).queryParam(RestShorts.PWD, password)
                .request(MediaType.APPLICATION_JSON).delete();

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            ErrorCode errorCode = getErrorCodeFrom(status);
            return Result.error(errorCode);
        }
    }

    @Override
    public Result<Short> getShort(String shortId) {
        //TODO Auto-generated method stub
       Response response = target.path(shortId)
                                  .request(MediaType.APPLICATION_JSON)
                                  .get();
        return handleResponse(response, Short.class);

    }

    @Override
    public Result<List<String>> getShorts(String userId) {

        Log.info("ANTES DA RESPONSE");
        Response response = target.path(userId + RestShorts.SHORTS)
                .request().accept(MediaType.APPLICATION_JSON).get();
        Log.info("DEPOIS DA RESPONSE");

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            List<String> shortIds = response.readEntity(new GenericType<List<String>>() {
            });
            return Result.ok(shortIds);
        } else {
            ErrorCode errorCode = getErrorCodeFrom(status);
            return Result.error(errorCode);
        }
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'followers'");
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'like'");
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'likes'");
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Response response = target.path(userId + RestShorts.FEED)
                .queryParam(RestShorts.PWD, password)
                .request(MediaType.APPLICATION_JSON).get();

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            List<String> feedShorts = response.readEntity(new GenericType<List<String>>() {
            });
            return Result.ok(feedShorts);
        } else {
            ErrorCode errorCode = getErrorCodeFrom(status);
            return Result.error(errorCode);
        }
    }
/*
    @Override
    public Result<Void> removeAllFollows(String userId, String password) {
        Response response = target.path(userId + RestShorts.REMOVE_FOLLOWS).queryParam(RestShorts.PWD, password)
                .request(MediaType.APPLICATION_JSON).delete();

        int status = response.getStatus();
        if (status == Status.OK.getStatusCode()) {
            return Result.ok();
        } else {
            ErrorCode errorCode = getErrorCodeFrom(status);
            return Result.error(errorCode);
        }
    }
*/
    private <T> Result<T> handleResponse(Response response, Class<T> type) {
        if (response.getStatus() != Status.OK.getStatusCode()) {
            return Result.error(getErrorCodeFrom(response.getStatus()));
        } else {
            return Result.ok(response.readEntity(type));
        }
    }

    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
    
}
