package shay.example.com.director_master;

/**
 * Created by Shay on 19/10/2017.
 */



class User {


    private  String name;
    private String email;
    private String url;

    private Double lat;
    private Double lon;
    private Boolean muteState;
    private String muteString;


    public String getMuteString() {
        return getMuteState().toString();
    }

    public void setMuteString(String muteString) {
        this.muteString = muteString;
    }


    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }


    public Boolean getMuteState() {
        return muteState;
    }

    public void setMuteState(Boolean muteState) {
        this.muteState = muteState;
    }


    public String getUrl() {
        return url;
    }



    public User(String email, String name, String url, Boolean muteState, Double lat, Double lon) {
        this.email = email;
        this.name = name;
        this.url = url; // image url from oauth
        this.lat = lat;
        this.lon = lon;
        this.muteState = muteState; // true if muted used to set image of speaker
        this.muteString = muteState +"";
    }

    final String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // default required ...
    public User() {
    }
}
