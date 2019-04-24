package com.example.android.peto;

import java.util.Date;

public class PetoPost extends PetoPostId {
    private String user_id, image_post_url, post_desc, image_thumb;
    private Date timestamp;

    public PetoPost() {
    }

    public PetoPost(String user_id, String image_post_url, String post_desc, Date timestamp, String image_thumb) {
        this.user_id = user_id;
        this.image_post_url = image_post_url;
        this.post_desc = post_desc;
        this.timestamp = timestamp;
        this.image_thumb = image_thumb;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_post_url() {
        return image_post_url;
    }

    public void setImage_post_url(String image_post_url) {
        this.image_post_url = image_post_url;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage_thumb() {
        return this.image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }
}
