package com.icnslab.message;

/**
 * Created by alicek106 on 2017-08-03.
 */
public class ContainerCommitResponse {
    private int responseCode;
    private String userUid;
    private String imageName;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
