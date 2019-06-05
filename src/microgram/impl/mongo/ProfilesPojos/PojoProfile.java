package microgram.impl.mongo.ProfilesPojos;

import microgram.api.Profile;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.LinkedList;
import java.util.List;

@Entity("pojoProfile")
public class PojoProfile {

    @Id
    private final String userId;
    private Profile prof;

    @Embedded
    private PojoProfRelations rel;

    public PojoProfile(Profile p){
        userId = p.getUserId();
        prof = p;
        rel = new PojoProfRelations(new LinkedList<>(), new LinkedList<>());

    }

    public void setRelFollowe(List<String> newl) {
        this.rel.setFollowers(newl);
    }

    public void setRelFollowing(List<String> newl) {
        this.rel.setFollowing(newl);
    }

    public String getUserId() {
        return userId;
    }

    public Profile getProf() {
        return prof;
    }

    public void setpojo(PojoProfRelations pj){
        this.rel = pj;
    }

    public PojoProfRelations getRel() {
        return rel;
    }
}
