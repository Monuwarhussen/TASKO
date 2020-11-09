package info.pkappstudio.tasko;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TodoViewmodel extends AndroidViewModel {

    private TodoRepository repository;
    private LiveData<List<Todo>> mList;

    public TodoViewmodel(@NonNull Application application) {
        super(application);

        repository = new TodoRepository(application);
        mList = repository.getAllTodo();
    }

    public LiveData<List<Todo>> getAllTodo() {
        return mList;
    }

    public void insert(Todo todo) {
        repository.insert(todo);
    }

    public void update(Todo todo) {
        repository.update(todo);
    }

    public void delete(Todo todo) {
        repository.delete(todo);
    }
}
