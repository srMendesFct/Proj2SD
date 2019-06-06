package microgram.impl.rest;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static utils.Log.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import discovery.Discovery;
import microgram.api.rest.RestPosts;
import microgram.api.rest.RestProfiles;
import microgram.impl.rest.posts.replicated.ReplicatedPostsResources;
import microgram.impl.rest.profiles.replicated.ReplicatedProfilesResources;
import utils.Args;
import utils.IP;

public class MicrogramRestServer {
    public static final int PORT = 18888;
    private static final String POSTS_SERVICE = "Microgram-Posts";
    private static final String PROFILES_SERVICE = "Microgram-Profiles";

    public static String SERVER_BASE_URI = "https://%s:%s/rest";
    private static MongoClient mongo;
    public static MongoDatabase dbName;
    static PojoCodecProvider codec;
    static CodecRegistry pojoCodecReg;

    private static final String DB_NAME = "microgram";
    private static final String DB_PROFILES = "profiles";
    private static final String DB_BOTH = "both";

    public static void main(String[] args) throws Exception {
        Args.use(args);

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");
        System.setProperty("java.net.preferIPv4Stack", "true");

        Log.setLevel(Level.INFO);

        String ip = IP.hostAddress();

        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);
        ServerAddress mongo1 = new ServerAddress("mongo1");
        ServerAddress mongo2 = new ServerAddress("mongo2");
        ServerAddress mongo3 = new ServerAddress("mongo3");
        List<ServerAddress> sa= new ArrayList<>();
        sa.add(mongo1);
        sa.add(mongo2);
        sa.add(mongo3);
        mongo = new MongoClient(sa);
        codec = PojoCodecProvider.builder().automatic(true).build();
        pojoCodecReg = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(codec));

        dbName = mongo.getDatabase(DB_NAME).withCodecRegistry(pojoCodecReg);

        Discovery.announce(POSTS_SERVICE, serverURI + RestPosts.PATH);
        Discovery.announce(PROFILES_SERVICE, serverURI + RestProfiles.PATH);

        ResourceConfig config = new ResourceConfig();

        config.register(new ReplicatedPostsResources());
        config.register(new ReplicatedProfilesResources());

//		config.register(new PrematchingRequestFilter());
//		config.register(new GenericExceptionMapper());

        JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, "0.0.0.0")), config);

        Log.fine(String.format("Posts+Profiles Combined Rest Server ready @ %s\n", serverURI));
    }

}
