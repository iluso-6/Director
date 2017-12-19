package helper_classes;

/**
 * Created by Shay on 20/11/2017.
 */

public class Master {

    private String name;
    private String email;
    private String url;

    public String getMasterID() {
        return masterID;
    }

    public void setMasterID(String masterID) {
        this.masterID = masterID;
    }

    private String masterID;



    public Master(String name, String email, String url, String masterID) {
        this.name = name;
        this.email = email;
        this.url = url;
        this.masterID = masterID;
    }

    public Master() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
