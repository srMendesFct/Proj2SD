package microgram.impl.rest.media;

import static utils.Log.Log;

import java.net.URI;
import java.util.logging.Level;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import discovery.Discovery;
import microgram.api.rest.RestMedia;
import utils.IP;

import javax.net.ssl.SSLContext;

public class MediaRestServer {
    public static final int PORT = 12222;
    public static final String SERVICE = "Microgram-MediaStorage";
    public static String SERVER_BASE_URI = "https://%s:%s/rest";

    static {
    	System.setProperty("java.net.preferIPv4Stack", "true");
    }
    
	private static Logger Log = Logger.getLogger(MediaRestServer.class.getName());

    public static void main(String[] args) throws Exception {
        

        Log.setLevel(Level.FINER);

        String ip = IP.hostAddress();
        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

        String serviceURI = serverURI + RestMedia.PATH;

        
        ResourceConfig config = new ResourceConfig();

        config.register(new RestMediaResources(serviceURI));

//		config.register(new GenericExceptionMapper());
//		config.register(new PrematchingRequestFilter());

        JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, "0.0.0.0")), config, SSLContext.getDefault());
        
        Discovery.announce(SERVICE, serviceURI);
        
        Log.fine(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));

    }
}
