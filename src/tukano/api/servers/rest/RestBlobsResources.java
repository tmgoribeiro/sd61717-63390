package tukano.api.servers.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tukano.api.rest.RestBlobs;
import tukano.api.servers.java.JavaBlobs;
import tukano.api.servers.java.Result;
import tukano.api.servers.java.Blobs;


@Singleton
public class RestBlobsResources implements RestBlobs {

    final Blobs impl;

    public RestBlobsResources() {
        this.impl = new JavaBlobs();
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
    public synchronized void upload(String blobId, byte[] bytes) {
        resultOrThrow(impl.upload(blobId, bytes));
    }

    @Override
    public synchronized byte[] download(String blobId) {
        return resultOrThrow(impl.download(blobId));
    }

}
