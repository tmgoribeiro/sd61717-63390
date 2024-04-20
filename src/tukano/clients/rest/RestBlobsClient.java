package tukano.clients.rest;

import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import tukano.api.servers.java.Blobs;
import tukano.api.servers.java.Result;
import tukano.api.rest.RestBlobs;




public class RestBlobsClient implements Blobs {

    final URI serverURI;
	final Client client;
	final ClientConfig config;

	final WebTarget target;
	
	public RestBlobsClient( URI serverURI ) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();
		this.client = ClientBuilder.newClient(config);

		target = client.target( serverURI ).path( RestBlobs.PATH );
	}


    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upload'");
    }

    @Override
    public Result<byte[]> download(String blobId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'download'");
    }
    
}
