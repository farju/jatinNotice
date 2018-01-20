package jatin.firebasepractice.com.firebase;

/**
 * Created by ( Jatin Bansal ) on 15-12-2016.
 */

public class Blog {

    private  String title,desc,imageurl;
    private  String username;

// Can be a point of error


    public  Blog() {

    }
    public Blog(String title, String desc, String imageurl) {
        this.title = title;
        this.desc = desc;
        this.imageurl = imageurl;
        this.username=username;

    }

    public String getImageurl() {

        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
