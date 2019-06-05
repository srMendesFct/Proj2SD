package microgram.impl.rest.posts.replicated;
import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.impl.mongo.MongoPosts;
import microgram.impl.rest.replication.MicrogramOperation;
import microgram.impl.rest.replication.MicrogramOperationExecutor;
import microgram.impl.rest.replication.OrderedExecutor;

import java.util.List;

import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;
import static microgram.api.java.Result.error;
import static microgram.impl.rest.replication.MicrogramOperation.Operation.*;

public class PostsReplicator implements MicrogramOperationExecutor, Posts {

	private static final int PostID = 0, UserID = 1;
	
	final MongoPosts localReplicaDB;
	final OrderedExecutor executor;
	
	PostsReplicator( MongoPosts localDB, OrderedExecutor executor) {
		this.localReplicaDB = localDB;
		this.executor = executor.init(this);
	}


	@Override
	public Result<Post> getPost(String postId) {
		return executor.replicate(new MicrogramOperation(GetPost, postId));
	}

	@Override
	public Result<String> createPost(Post post) {
		return executor.replicate(new MicrogramOperation(CreatePost, post));
	}

	@Override
	public Result<Void> deletePost(String postId) {
		return executor.replicate(new MicrogramOperation(DeletePost, postId));
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		String[] s = new String[2];
		s[PostID] = postId;
		s[UserID]= userId;
		if(isLiked)
			return executor.replicate(new MicrogramOperation(LikePost, s));
		else
			return executor.replicate(new MicrogramOperation(UnLikePost, s));
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		String[] s = new String[2];
		s[PostID] = postId;
		s[UserID]= userId;
		return executor.replicate(new MicrogramOperation(LikePost, s));
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		return executor.replicate(new MicrogramOperation(GetPosts, userId));
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		return executor.replicate(new MicrogramOperation(GetFeed, userId));
	}

	@Override
	public Result<?> execute(MicrogramOperation op) {
		switch( op.type ) {
			case CreatePost: {
				return localReplicaDB.createPost(op.arg(Post.class));
			}
			case GetPosts: {
				String user = op.args(String.class);
				return localReplicaDB.getPosts(user);
			}

			case GetPost: {
				String postid = op.args(String.class);
				return localReplicaDB.getPost(postid);
			}
			case DeletePost: {
				String postid = op.args(String.class);
				return localReplicaDB.deletePost(postid);
			}

			case GetFeed: {
				String userid = op.args(String.class);
				return localReplicaDB.getFeed(userid);
			}

			case LikePost: {
				String[] post = op.args(String[].class);
				return localReplicaDB.like(post[PostID], post[UserID], true);
			}

			case UnLikePost: {
				String[] post = op.args(String[].class);
				return localReplicaDB.like(post[PostID], post[UserID], false);
			}

			case IsLiked: {
				String[] post = op.args(String[].class);
				return localReplicaDB.isLiked(post[PostID], post[UserID]);
			}
			default:
				return error(NOT_IMPLEMENTED);
		}
	}
}
