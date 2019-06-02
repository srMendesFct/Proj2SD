package microgram.impl.rest.posts.replicated;
import microgram.api.java.Posts;
import microgram.impl.rest.replication.MicrogramOperationExecutor;
import microgram.impl.rest.replication.OrderedExecutor;

public class PostsReplicator implements MicrogramOperationExecutor, Posts {

	private static final int PostID = 0, UserID = 1;
	
	final MongoPosts localReplicaDB;
	final OrderedExecutor executor;
	
	PostsReplicator( Posts localDB, OrderedExecutor executor) {
		this.localReplicaDB = localDB;
		this.executor = executor.init(this);
	}
	
		
}
