package microgram.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.mongo.ProfilesPojos.PojoProfRelations;
import microgram.impl.mongo.ProfilesPojos.PojoProfile;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import static microgram.impl.mongo.MongoPosts.pMongo;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;

public class MongoProfiles implements Profiles {
    private final Datastore profiledatastore;
    static MongoProfiles MongoP;

    public MongoProfiles() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");

        MongoClientURI uri = new MongoClientURI("mongodb://mongo1,mongo2,mongo3/?w=majority&readConcernLevel=majority&readPreference=secondary");
        MongoClient mongo = new MongoClient(uri);
        final Morphia morphia = new Morphia();
        morphia.mapPackage("microgram.impl.mongo.ProfilesPojos");
        profiledatastore = morphia.createDatastore(mongo, "PojoProfile");
        profiledatastore.ensureIndexes();
        MongoP = this;
    }


    @Override
    synchronized public Result<Profile> getProfile(String userId) {
        try {
            List<PojoProfile> profiles = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(userId).asList();
            Profile res = profiles.get(0).getProf();
            PojoProfRelations pj = profiles.get(0).getRel();
            res.setFollowers(pj.getFollowers().size());
            res.setFollowing(pj.getFollowing().size());
            return ok(res);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    synchronized public Result<Void> createProfile(Profile profile) {
        List<PojoProfile> profiles = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(profile.getUserId()).asList();

        if (!profiles.isEmpty()) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        } else {
            PojoProfile pp = new PojoProfile(profile);
            PojoProfRelations pj = new PojoProfRelations(new LinkedList<>(), new LinkedList<>());
            pp.setpojo(pj);
            profiledatastore.save(profile);
            return ok();
        }
    }

    @Override
    synchronized public Result<Void> deleteProfile(String userId) {

        try {
            List<PojoProfile> profiles = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(userId).asList();

            PojoProfile p = profiles.get(0);

            p.getRel().getFollowers().forEach(String -> {
                if(p.getRel().getFollowing().contains(userId)){
                    p.getRel().getFollowing().remove(userId);
                }
            });

            p.getRel().getFollowing().forEach(String -> {
                if(p.getRel().getFollowers().contains(userId)){
                    p.getRel().getFollowers().remove(userId);
                }
            });

            pMongo.deleteAllUserPosts(userId);

        }catch (Exception e){
            return error(NOT_FOUND);
        }

        return ok();
    }

    @Override
    synchronized public Result<List<Profile>> search(String prefix) {

        List<PojoProfile> sProfiles = profiledatastore.createQuery(PojoProfile.class).field("userId").startsWith(prefix).asList();
        if (!sProfiles.isEmpty()) {
            List<Profile> l = new LinkedList<>();
            sProfiles.forEach(pojoProfile -> {
                l.add(pojoProfile.getProf());
            });
            return ok(l);
        } else {
            return error(NOT_FOUND);
        }

    }

    @Override
    synchronized public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {

        List<PojoProfile> sProfilesofid1 = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(userId1).asList();

        List<PojoProfile> sProfilesofid2 = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(userId2).asList();

        if (sProfilesofid1.isEmpty() || sProfilesofid2.isEmpty())
            return error(NOT_FOUND);

        List<String> lFollowing = sProfilesofid1.get(0).getRel().getFollowing();

        List<String> lFollowers = sProfilesofid2.get(0).getRel().getFollowers();

        if (isFollowing) {
            lFollowing.add(userId2);
            lFollowers.add(userId1);
        } else {
            lFollowing.remove(userId2);
            lFollowers.remove(userId1);
        }


        sProfilesofid1.get(0).setRelFollowing(lFollowing);
        sProfilesofid2.get(0).setRelFollowe(lFollowers);

        UpdateOperations<PojoProfile> updateOperationsFollowings = profiledatastore.createUpdateOperations(PojoProfile.class).set(userId1, sProfilesofid1);
        UpdateOperations<PojoProfile> updateOperationsFollowers = profiledatastore.createUpdateOperations(PojoProfile.class).set(userId2, sProfilesofid2);

        profiledatastore.save(updateOperationsFollowings);
        profiledatastore.save(updateOperationsFollowers);

        return ok();

    }

    @Override
    synchronized public Result<Boolean> isFollowing(String userId1, String userId2) {
        try {


            List<PojoProfile> fol = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(userId1).asList();

            List<String> l = fol.get(0).getRel().getFollowing();

            if (!fol.isEmpty() && !l.isEmpty())
                return ok(l.contains(userId2));
            else
                return error(NOT_FOUND);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    synchronized public List<String> following(String userId) {
        List<PojoProfile> f = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(userId).asList();
        if (f.isEmpty())
            return null;
        else {
            return f.get(0).getRel().getFollowing();
        }

    }

    synchronized public void removeUserPost(String userId, String postId){
        List<PojoProfile> f = profiledatastore.createQuery(PojoProfile.class).field("userId").equal(userId).asList();
        List<String>l =  f.get(0).getRel().getUserPostss();
        if(l.contains(postId)) {
            l.remove(postId);
            UpdateOperations<PojoProfile> updateOperationsFollowings = profiledatastore.createUpdateOperations(PojoProfile.class).set(userId, l);
            profiledatastore.save(updateOperationsFollowings);

        }
    }

}
