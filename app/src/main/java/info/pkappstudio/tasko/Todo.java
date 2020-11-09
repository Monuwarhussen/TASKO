package info.pkappstudio.tasko;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_table")
public class Todo {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String todo;

    @NonNull
    private String timestamp;

    private long timeStamp1;

    private int isCompleted;

    private int isPinned;

    private String date;

    public Todo(@NonNull String todo, @NonNull String timestamp) {
        this.todo = todo;
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }

    public int getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(int isPinned) {
        this.isPinned = isPinned;
    }

    @NonNull
    public String getTodo() {
        return todo;
    }

    @NonNull
    public String getTimestamp() {
        return timestamp;
    }

    public void setTodo(@NonNull String todo) {
        this.todo = todo;
    }

    public void setTimestamp(@NonNull String timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimeStamp1() {
        return timeStamp1;
    }

    public void setTimeStamp1(long timeStamp1) {
        this.timeStamp1 = timeStamp1;
    }
}
