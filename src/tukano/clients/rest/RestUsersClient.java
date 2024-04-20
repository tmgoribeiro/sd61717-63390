package tukano.clients.rest;

import java.net.URI;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientProperties;
import tukano.api.User;
import tukano.api.rest.RestShorts;
import tukano.api.servers.java.Result;
import tukano.api.servers.java.Users;
import tukano.api.rest.RestUsers;
import tukano.api.servers.java.Result.ErrorCode;



public class RestUsersClient extends RestClient implements Users {


    protected static final int MAX_RETRIES = 3;
    protected static final int RETRY_SLEEP = 1000;
    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;
	final URI serverURI;
	final Client client;
	final ClientConfig config;


	final WebTarget target;
	
	public RestUsersClient( URI serverURI ) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

		target = client.target( serverURI ).path( RestUsers.PATH );
	}
		
	@Override
	public Result<String> createUser(User user) {
		Response r = target.request()
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return Result.error( getErrorCodeFrom(status));
		else
			return Result.ok( r.readEntity( String.class ));
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		Response r = target.path( name )
				.queryParam(RestUsers.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return Result.error( getErrorCodeFrom(status));
		else
			return Result.ok( r.readEntity( User.class ));
	}
	

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        try (Response response = target.path(userId)
                                       .queryParam(RestUsers.PWD, password)
                                        .request()
                                        .accept(MediaType.APPLICATION_JSON)
                                       .put(Entity.entity(user, MediaType.APPLICATION_JSON))) {
            return handleResponse(response, User.class);
        }
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        Response response = target.path(userId).queryParam(RestUsers.PWD, password)
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
    public Result<List<User>> searchUsers(String pattern) {
        try (Response response = target.queryParam(RestUsers.QUERY, pattern)
                                       .request()
                                       .accept(MediaType.APPLICATION_JSON)
                                       .get()) {
            return handleResponse(response, new GenericType<List<User>>() {});
        }
    }

    private <T> Result<T> handleResponse(Response response, Class<T> type) {
        int status = response.getStatus();
        if (status != Status.OK.getStatusCode()) {
            return Result.error(getErrorCodeFrom(status));
        } else {
            return Result.ok(response.readEntity(type));
        }
    }

    private <T> Result<T> handleResponse(Response response, GenericType<T> genericType) {
        int status = response.getStatus();
        if (status != Status.OK.getStatusCode()) {
            return Result.error(getErrorCodeFrom(status));
        } else {
            return Result.ok(response.readEntity(genericType));
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