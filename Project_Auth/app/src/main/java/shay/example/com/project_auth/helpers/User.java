package shay.example.com.project_auth.helpers;

import android.location.Location;

/**
 * Created by Shay on 18/10/2017.
 */

public class User {

    public User() {//default constructor
    }


    public User(String userName, String email, Location location, String photoUrl, String mute, String userId, Double latitude, Double longitude) {
        this.userName = userName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.mute = mute;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private static String userName;
    private static String email;
    private static String photoUrl;


    private static String mute;
    private static String userId;

    private static Double latitude;
    private static Double longitude;



    public String getMute() {
        return mute;
    }

    public void setMute(String mute) {
        this.mute = mute;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
