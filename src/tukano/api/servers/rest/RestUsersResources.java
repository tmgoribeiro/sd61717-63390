package tukano.api.servers.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tukano.api.User;
import tukano.api.servers.java.Result;
import tukano.api.servers.java.Users;
import tukano.api.rest.RestUsers;
import tukano.api.servers.java.JavaUsers;

import java.util.List;
import java.util.logging.Logger;

@Singleton
public class RestUsersResources implements RestUsers {

    private static Logger Log = Logger.getLogger(RestUsersResources.class.getName());
    final Users impl;

    public RestUsersResources() {
        this.impl = new JavaUsers();
    }

    protected <T> T resultOrThrow(Result<T> result) {
        if (result.isOK())
            return result.value();
        else
            throw new WebApplicationException(statusCodeFrom(result));
    }
    private static Response.Status statusCodeFrom(Result<?> result) {
        return switch (result.error()) {
            case CONFLICT -> Response.Status.CONFLICT;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case BAD_REQUEST -> Response.Status.BAD_REQUEST;
            case INTERNAL_ERROR -> Response.Status.INTERNAL_SERVER_ERROR;
            case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
            case OK -> result.value() == null ? Response.Status.NO_CONTENT : Response.Status.OK;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }


    @Override
    public String createUser(User user) {
        return resultOrThrow( impl.createUser(user) );
    }

    @Override
    public User getUser(String userId, String pwd) {
        return resultOrThrow( impl.getUser(userId, pwd));
    }

    @Override
    public User updateUser(String userId, String pwd, User user) {
        return resultOrThrow( impl.updateUser(userId, pwd, user));
    }

    @Override
    public User deleteUser(String userId, String pwd) {
        return resultOrThrow( impl.deleteUser(userId, pwd));
    }

    @Override
    public List<User> searchUsers(String pattern) {
        return resultOrThrow( impl.searchUsers(pattern));
    }

}
