package tukano.api.servers.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.hsqldb.persist.Log;
import tukano.api.rest.RestShorts;
import tukano.api.servers.java.JavaShorts;
import tukano.api.servers.java.JavaUsers;
import tukano.api.servers.java.Result;
import tukano.api.servers.java.Shorts;
import tukano.api.Short;

import java.util.List;
import java.util.logging.Logger;

@Singleton
public class RestShortsResources implements RestShorts {
    private static Logger Log = Logger.getLogger(RestShortsResources.class.getName());

    final Shorts impl;

    public RestShortsResources() {
        this.impl = new JavaShorts();
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
    public synchronized Short createShort(String userId, String password) {
        return resultOrThrow( impl.createShort(userId, password) );
    }

    @Override
    public synchronized void deleteShort(String shortId, String password) {
        resultOrThrow( impl.deleteShort(shortId, password) );
    }

    @Override
    public synchronized Short getShort(String shortId) {
        return resultOrThrow( impl.getShort(shortId) );
    }

    @Override
    public synchronized List<String> getShorts( String userId ) {
        Log.info("ENTROU SHORTS RESOURCE");
        return resultOrThrow( impl.getShorts( userId ) );
    }

    @Override
    public synchronized void follow(String userId1, String userId2, boolean isFollowing, String password) {
        resultOrThrow( impl.follow(userId1, userId2, isFollowing, password) );
    }

    @Override
    public synchronized List<String> followers(String userId, String password) {
        return resultOrThrow(impl.followers(userId, password));
    }

    @Override
    public synchronized void like(String shortId, String userId, boolean isLiked, String password) {
        resultOrThrow( impl.like(shortId, userId, isLiked, password) );
    }

    @Override
    public synchronized List<String> likes(String shortId, String password) {
        return resultOrThrow( impl.likes(shortId, password) );
    }

    @Override
    public synchronized List<String> getFeed(String userId, String password) {
        return resultOrThrow( impl.getFeed(userId, password) );
    }
}
