package shay.example.com.project_auth.helpers;

/**
 * Created by Shay on 21/11/2017.
 */

//hold the details of the master for use in dialog
public class MasterDetails {

    private String name;
    private String email;
    private String url;


    private boolean muteService;
    private long createdAt;

    public MasterDetails() {
    }

    public MasterDetails(String name, String email, String url, boolean muteService, long createdAt) {
        this.name = name;
        this.email = email;
        this.url = url;
        this.muteService = muteService;
        this.createdAt = createdAt;
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



    public boolean isMuteService() {
        return muteService;
    }

    public void setMuteService(boolean muteService) {
        this.muteService = muteService;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
