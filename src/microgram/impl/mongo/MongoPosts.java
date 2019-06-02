package microgram.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import discovery.Discovery;
import javafx.geometry.Pos;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;
import static microgram.impl.mongo.MongoProfiles.MongoP;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;

public class MongoPosts implements Posts {
    final Datastore postsdatastore;
    final Datastore likes;
    final Datastore userposts;
    static MongoPosts pMongo;
    public MongoPosts() {
        MongoClientURI uri = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/?w=majority&readConcernLevel=majority&readPreference=secondary");
        MongoClient mongo = new MongoClient(uri);
        final Morphia morphia = new Morphia();
        morphia.mapPackage("Posts storage");
        postsdatastore = morphia.createDatastore(mongo, "post");
        postsdatastore.ensureIndexes();

        likes = morphia.createDatastore(mongo, "likes");
        likes.ensureIndexes();

        userposts = morphia.createDatastore(mongo, "userposts");
        userposts.ensureIndexes();
        pMongo= this;
    }


    @Override
    synchronized public Result<Post> getPost(String postId) {
        try {
            List<Post> poslist = postsdatastore.createQuery(Post.class).field("postId").equal(postId).asList();
            List<LikesPost> postlikes = likes.createQuery(LikesPost.class).field("postId").equal(postId).asList();
            Post res = poslist.get(0);
            res.setLikes(postlikes.size());
            return ok(res);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    synchronized public Result<String> createPost(Post post) {
        List<Post> posts = postsdatastore.createQuery(Post.class).field("userId").equal(post.getPostId()).asList();
        List<LikesPost> lieks = likes.createQuery(LikesPost.class).field("userId").equal(post.getPostId()).asList();
        List<UserPosts> uposts = userposts.createQuery(UserPosts.class).field("userId").equal(post.getOwnerId()).asList();
        if (!posts.isEmpty()) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        } else {
            postsdatastore.save(posts);
            likes.save(lieks);
            userposts.save(uposts);
            return ok(post.getPostId());
        }
    }

    @Override
    synchronized public Result<Void> deletePost(String postId) {

        List<Post> posts = postsdatastore.createQuery(Post.class).field("userId").equal(postId).asList();
        if (posts.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            List<UserPosts> uposts = userposts.createQuery(UserPosts.class).field("userId").equal(postId).asList();
            List<String> postsids = uposts.get(0).getPosts();
            if (postsids.contains(postId))
                postsids.remove(postId);

            UpdateOperations<UserPosts> updateOperations = userposts.createUpdateOperations(UserPosts.class).set("l", postsids);

            likes.delete(postId);
            postsdatastore.delete(postId);
            return ok();
        }
    }

    @Override
    synchronized public Result<Void> like(String postId, String userId, boolean isLiked) {

        List<LikesPost> lLikes = likes.createQuery(LikesPost.class).field("postId").equal(postId).asList();

        if (lLikes.isEmpty())
            return error(NOT_FOUND);

        List<String> lPost = lLikes.get(0).getLikes();

        if (isLiked) {
            if (!lPost.add(userId))
                return error(CONFLICT);
        } else {
            if (!lPost.remove(userId))
                return error(NOT_FOUND);
        }

        UpdateOperations<LikesPost> updateOperations = likes.createUpdateOperations(LikesPost.class).set("l", lPost);

        likes.update(likes.createQuery(LikesPost.class).field("postId").equal(postId), updateOperations);

        return ok();
    }

    @Override
    synchronized public Result<Boolean> isLiked(String postId, String userId) {
        try {
            List<LikesPost> lp = likes.createQuery(LikesPost.class).field("postId").equal(userId).asList();
            LikesPost l = lp.get(0);
            if (l != null)
                return ok(l.getLikes().contains(userId));
            else
                return error(NOT_FOUND);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    synchronized public Result<List<String>> getPosts(String userId) {
        List<UserPosts> uposts = userposts.createQuery(UserPosts.class).field("userId").equal(userId).asList();
        if (uposts.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            List<String> res = uposts.get(0).getPosts();
            return ok(res);
        }
    }

    @Override
    synchronized public Result<List<String>> getFeed(String userId) {
        List<String> l = new ArrayList<String>();
        List<String> following = MongoP.following(userId);
        try {
            if (following == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            } else {
                List<String> feed = new ArrayList<>();
                for (String followii : following) {
                    List<UserPosts> up = userposts.createQuery(UserPosts.class).field("userId").equal(userId).asList();
                    if (!up.isEmpty()) {
                        List<String> alluserPosts = up.get(0).getPosts();
                        feed.addAll(alluserPosts);
                    }
                }
                return ok(feed);
            }

        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    synchronized void deleteAllUserPosts(String userId){
        userposts.delete(userId);
    }
}
