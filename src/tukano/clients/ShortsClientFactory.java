package tukano.clients;

import java.net.URI;

import tukano.Discovery;
import tukano.api.servers.java.Shorts;
import tukano.clients.rest.RestShortsClient;

public class ShortsClientFactory{
    // Discovery mechanism to fetch the URI dynamically

    public static Shorts getClient() {
        Discovery discovery = Discovery.getInstance();
        var svURIs = discovery.knownUrisOf("shorts", 1);
        URI serverURI =  svURIs.get(0);

        if( serverURI.toString().endsWith("rest"))
            return new RestShortsClient(serverURI); // Discover the URI dynamically
        else {
            return null; // Handle case where URI discovery fails
        }
    }
}