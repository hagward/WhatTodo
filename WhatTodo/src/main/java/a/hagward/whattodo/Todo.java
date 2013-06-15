package a.hagward.whattodo;

/**
 * Created by Anders on 2013-06-08.
 */
public class Todo {
    private long mId;
    private long mUtime;
    private int mCompleted;
    private String mTitle;

    public Todo() {}

    public Todo(long id, long utime, int completed, String title) {
        mId = id;
        mUtime = utime;
        mCompleted = completed;
        mTitle = title;
    }

    public long getId() {
        return mId;
    }

    public long getUtime() {
        return mUtime;
    }

    public int getCompleted() {
        return mCompleted;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setId(long id) {
        mId = id;
    }

    public void setUtime(long utime) {
        mUtime = utime;
    }

    public void setCompleted(int completed) {
        mCompleted = completed;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
