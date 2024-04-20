package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Likes {

    @Id
    private String userId;
    @Id
    private String shortId;

    public Likes()  {}

    public Likes(String userId, String shortId)  {

        this.userId=userId;
        this.shortId=shortId;
    }


    public String getUserId()   {
        return userId;
    }

    public String getShortId()  {
        return shortId;
    }


}
