package microgram.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import discovery.Discovery;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;

import static microgram.impl.mongo.MongoProfiles.MongoP;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
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
    static MongoPosts pMongo;

    public MongoPosts() {
        // MongoClientURI uri = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/?w=majority&readConcernLevel=majority&readPreference=secondary");
        MongoClient mongo = new MongoClient("127.0.0.1");
        final Morphia morphia = new Morphia();
        morphia.mapPackage("Posts storage");
        postsdatastore = morphia.createDatastore(mongo, "PojoPost");
        postsdatastore.ensureIndexes();
        pMongo = this;

    }


    @Override
    synchronized public Result<Post> getPost(String postId) {
        try {
            List<PojoPost> poslist = postsdatastore.createQuery(PojoPost.class).field("postId").equal(postId).asList();
            Post res = poslist.get(0).getPost();
            res.setLikes(poslist.get(0).getRel().getlikes().size());
            return ok(res);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    synchronized public Result<String> createPost(Post post) {
        List<PojoPost> posts = postsdatastore.createQuery(PojoPost.class).field("postId").equal(post.getPostId()).asList();

        if (!posts.isEmpty()) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        } else {
            PojoPost p = new PojoPost(post);
            postsdatastore.save(posts);
            return ok(post.getPostId());
        }
    }

    @Override
    synchronized public Result<Void> deletePost(String postId) {

        List<PojoPost> posts = postsdatastore.createQuery(PojoPost.class).field("postId").equal(postId).asList();
        if (posts.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            MongoP.removeUserPost(posts.get(0).getPost().getOwnerId(), postId);
            postsdatastore.delete(postId);
            return ok();
        }
    }

    @Override
    synchronized public Result<Void> like(String postId, String userId, boolean isLiked) {

        List<PojoPost> lLikes = postsdatastore.createQuery(PojoPost.class).field("postId").equal(postId).asList();

        if (lLikes.isEmpty())
            return error(NOT_FOUND);

        List<String> lPost = lLikes.get(0).getRel().getlikes();

        if (isLiked) {
            if (!lPost.contains(userId))
                return error(CONFLICT);
            else {
                lPost.remove(userId);
            }
        } else {
            if (lPost.contains(userId))
                return error(NOT_FOUND);
            else {
                lPost.add(userId);
            }
        }

        lLikes.get(0).getRel().setLikes(lPost);
        UpdateOperations<PojoPost> updateOperations = postsdatastore.createUpdateOperations(PojoPost.class).set(postId, lLikes);
        postsdatastore.save(updateOperations);

        return ok();
    }

    @Override
    synchronized public Result<Boolean> isLiked(String postId, String userId) {
        try {
            List<PojoPost> lp = postsdatastore.createQuery(PojoPost.class).field("postId").equal(postId).asList();
            if (!lp.isEmpty())
                return ok(lp.get(0).getRel().getlikes().contains(userId));
            else
                return error(NOT_FOUND);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    synchronized public Result<List<String>> getPosts(String userId) {

        List<PojoPost> uposts = postsdatastore.createQuery(PojoPost.class).field("ownerId").equal(userId).asList();
        if (uposts.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            List<String> res = uposts.get(0).getRel().getUserposts();
            return ok(res);
        }
    }

    @Override
    synchronized public Result<List<String>> getFeed(String userId) {
        List<String> following = MongoP.following(userId);
        try {
            if (following.isEmpty()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            } else {
                List<String> feed = new ArrayList<>();
                for (String followii : following) {
                    List<PojoPost> up = postsdatastore.createQuery(PojoPost.class).field("userId").equal(userId).asList();
                    if (!up.isEmpty()) {
                        for(PojoPost pp: up){
                            feed.add(pp.getPostId());
                        }

                    }
                }
                return ok(feed);
            }

        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    synchronized void deleteAllUserPosts(String userId) {
        List<PojoPost> posts = postsdatastore.createQuery(PojoPost.class).field("userId").equal(userId).asList();
        postsdatastore.delete(posts);
    }
}
