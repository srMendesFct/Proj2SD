package microgram.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.impl.mongo.MongoPosts.pMongo;

public class MongoProfiles implements Profiles {
    final Datastore profiledatastore;
    final Datastore followers;
    final Datastore following;
    static MongoProfiles MongoP;

    public MongoProfiles() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");

       // MongoClientURI uri = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/?w=majority&readConcernLevel=majority&readPreference=secondary");
        MongoClient mongo = new MongoClient("127.0.0.1");
        final Morphia morphia = new Morphia();
        morphia.mapPackage("Profile storage");
        profiledatastore = morphia.createDatastore(mongo, "profile");
        profiledatastore.ensureIndexes();
        followers = morphia.createDatastore(mongo, "followers");
        followers.ensureIndexes();
        following = morphia.createDatastore(mongo, "followings");
        following.ensureIndexes();
        MongoP = this;
    }


    @Override
    synchronized public Result<Profile> getProfile(String userId) {
        try {
            List<Profile> profiles = profiledatastore.createQuery(Profile.class).field("userId").equal(userId).asList();
            List<Followers> f = followers.createQuery(Followers.class).field("userId").equal(userId).asList();
            List<Following> fol = following.createQuery(Following.class).field("userId").equal(userId).asList();
            Profile res = profiles.get(0);
            res.setFollowers(f.size());
            res.setFollowing(fol.size());
            return ok(res);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    synchronized public Result<Void> createProfile(Profile profile) {
        List<Profile> profiles = profiledatastore.createQuery(Profile.class).field("userId").equal(profile.getUserId()).asList();
        List<Following> newFollowing = following.createQuery(Following.class).field("userId").equal(profile.getUserId()).asList();
        List<Followers> newFollowers = followers.createQuery(Followers.class).field("userId").equal(profile.getUserId()).asList();

        if (!profiles.isEmpty()) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        } else {
            profiledatastore.save(profile);
            following.save(newFollowing);
            followers.save(newFollowers);
            return ok();
        }
    }

    @Override
    synchronized public Result<Void> deleteProfile(String userId) {
        List<Profile> profiles = profiledatastore.createQuery(Profile.class).field("userId").equal(userId).asList();

        if (profiles.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {

            List<Following> delFollowing = following.createQuery(Following.class).field("userId").equal(userId).asList();

            List<String> fol = delFollowing.get(0).getFollwing();

            for (String s : fol) {
                List<Followers> forFollowers = followers.createQuery(Followers.class).field("userId").equal(s).asList();
                List<String> listFol = forFollowers.get(0).getFollowers();
                if (!listFol.isEmpty())
                    listFol.remove(userId);

                UpdateOperations<Followers> updateOperations = followers.createUpdateOperations(Followers.class).set("l", listFol);

                followers.update(followers.createQuery(Followers.class).field("userId").equal(s), updateOperations);
            }

            List<Followers> delFollowers = followers.createQuery(Followers.class).field("userId").equal(userId).asList();

            fol = delFollowers.get(0).getFollowers();

            for (String s : fol) {
                List<Following> forFollowing = following.createQuery(Following.class).field("userId").equal(s).asList();
                List<String> listFol = forFollowing.get(0).getFollwing();
                if (!listFol.isEmpty())
                    listFol.remove(userId);
                UpdateOperations<Followers> updateOperations = followers.createUpdateOperations(Followers.class).set("l", listFol);

                followers.update(followers.createQuery(Followers.class).field("userId").equal(s), updateOperations);
            }

            profiledatastore.delete(profiles);
            pMongo.deleteAllUserPosts(userId);
            return ok();
        }
    }

    @Override
    synchronized public Result<List<Profile>> search(String prefix) {

        List<Profile> sProfiles = profiledatastore.createQuery(Profile.class).field("userId").startsWith(prefix).asList();

        return ok(sProfiles);
    }

    @Override
    synchronized public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {

        List<Following> folFollowing = following.createQuery(Following.class).field("userId").equal(userId1).asList();

        List<Followers> folFollowers = followers.createQuery(Followers.class).field("userId").equal(userId2).asList();

        if (folFollowers.isEmpty() || folFollowing.isEmpty())
            return error(NOT_FOUND);

        List<String> lFollowing = folFollowing.get(0).getFollwing();

        List<String> lFollowers = folFollowers.get(0).getFollowers();

        if (isFollowing) {
            lFollowing.add(userId2);
            lFollowers.add(userId1);
        } else {
            lFollowing.remove(userId2);
            lFollowers.remove(userId1);
        }

        UpdateOperations<Followers> updateOperationsFollowers = followers.createUpdateOperations(Followers.class).set("l", lFollowers);

        followers.update(followers.createQuery(Followers.class).field("userId").equal(userId2), updateOperationsFollowers);

        UpdateOperations<Following> updateOperationsFollowing = following.createUpdateOperations(Following.class).set("l", lFollowing);

        following.update(following.createQuery(Following.class).field("userId").equal(userId1), updateOperationsFollowing);

        return ok();
    }

    @Override
    synchronized public Result<Boolean> isFollowing(String userId1, String userId2) {
        try {
            List<Following> fol = following.createQuery(Following.class).field("userId").equal(userId1).asList();
            Following f = fol.get(0);
            if (f != null)
                return ok(f.getFollwing().contains(userId2));
            else
                return error(NOT_FOUND);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    synchronized public List<String> following(String userId) {
        List<Following> f = following.createQuery(Following.class).field("userId").equal(userId).asList();
        if (f.isEmpty())
            return null;
        else
            return f.get(0).getFollwing();
    }

}
