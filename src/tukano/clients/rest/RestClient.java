package tukano.clients.rest;

import io.grpc.Status;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import tukano.api.servers.java.Result;

import java.util.function.Supplier;

import static tukano.api.servers.java.Result.ErrorCode.INTERNAL_ERROR;
import static tukano.api.servers.java.Result.ErrorCode.TIMEOUT;
import static tukano.clients.rest.RestUsersClient.getErrorCodeFrom;

public class RestClient {

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 1000;

    protected <T> Result<T> reTry(Supplier<Result<T>> func) throws InterruptedException {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (ProcessingException x) {
                Thread.sleep(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
                return Result.error(INTERNAL_ERROR);
            }
        return Result.error(TIMEOUT);
    }

    /*
    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Status.OK && r.hasEntity())
                return Result.ok(r.readEntity(entityType));
            else if (status == Status.NO_CONTENT) return Result.ok();

            return Result.error(getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

     */
}