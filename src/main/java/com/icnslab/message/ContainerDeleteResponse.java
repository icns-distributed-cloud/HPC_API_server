package com.icnslab.message;

/**
 * Created by alicek106 on 2017-08-03.
 */
public class ContainerDeleteResponse {
    private int responseCode;
    private String userUid;

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
}
