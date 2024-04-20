package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Follows {

    @Id
    private String followerId;
    @Id
    private String followedId;


    public Follows(){}

    public Follows(String followerId, String followedId){

        this.followedId=followedId;
        this.followerId=followerId;
    }

    public String getFollowerId()   {
        return followerId;
    }

    public String getFollowedId()   {
        return followedId;
    }

}