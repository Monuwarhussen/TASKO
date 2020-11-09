package info.pkappstudio.tasko;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {


    @Insert
    void insert(Todo todo);

    @Update
    void update(Todo todo);

    @Query("SELECT * FROM todo_table ORDER BY isPinned DESC, isCompleted ASC, timestamp1 DESC")
    LiveData<List<Todo>> getAllTodo();

    @Delete
    void delete(Todo todo);

}
