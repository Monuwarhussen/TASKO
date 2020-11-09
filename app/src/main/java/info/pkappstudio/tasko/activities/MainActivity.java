package info.pkappstudio.tasko.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.pkappstudio.tasko.R;
import info.pkappstudio.tasko.Todo;
import info.pkappstudio.tasko.TodoAdapter;
import info.pkappstudio.tasko.TodoViewmodel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    private View mContextView;
    private FirebaseAuth mAUth;
    private FirebaseUser mCurrentUser;
    private CircleImageView mProfileImage;
    private Uri imageUrl;
    private String userName;
    private BottomSheetDialog bottomSheetDialog;
    private RecyclerView recyclerView;
    private TodoViewmodel viewmodel;
    //private ImageView mNoTodo;
    private LottieAnimationView mAnimationView;
    private TodoAdapter adapter;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private boolean synced = false;
    private String timeStamp;
    private long time_stamp;
    int year;
    int month;
    int date;
    int min;
    int hour;
    boolean is24Hour;
    String date_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContextView = findViewById(R.id.layout);

        mProfileImage = findViewById(R.id.profile_image);
        //mNoTodo = findViewById(R.id.imageView3);
        mAnimationView = findViewById(R.id.animation_view);

        mAUth = FirebaseAuth.getInstance();
        mCurrentUser = mAUth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TodoAdapter();
        recyclerView.setAdapter(adapter);

        viewmodel = new ViewModelProvider(this).get(TodoViewmodel.class);
        viewmodel.getAllTodo().observe(MainActivity.this, todos -> {

            adapter.submitList(todos);

            if (todos.isEmpty()) {
                //mNoTodo.setVisibility(View.VISIBLE);
                mAnimationView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                if (checkUser()) {
                    sync();
                }
            } else {
                //mNoTodo.setVisibility(View.INVISIBLE);
                mAnimationView.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            if (checkUser()) {
                updateData(todos);
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Todo todo = adapter.getTodoAt(viewHolder.getLayoutPosition());
                switch (direction) {
                    case ItemTouchHelper.LEFT:

                        viewmodel.delete(todo);
                        if (checkUser()) {
                            try {
                                reference.child(Long.toString(todo.getId())).removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Log.i("todoUpdate", "Delete Success");
                                            } else {
                                                Log.i("todoUpdate", task.getException().getMessage());
                                            }
                                        });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Snackbar.make(mContextView, "Todo deleted", Snackbar.LENGTH_SHORT)
                                .setAction("Undo", v -> {
                                    viewmodel.insert(todo);
                                })
                                .show();
                        break;
                    case ItemTouchHelper.RIGHT:
                        int isCompleted = todo.getIsCompleted();
                        int isPinned = todo.getIsPinned();
                        if (isCompleted == 0) {
                            todo.setIsCompleted(1);
                            if (isPinned == 1) {
                                todo.setIsPinned(0);
                            }
                            viewmodel.update(todo);
                            adapter.notifyDataSetChanged();
                        } else {
                            todo.setIsCompleted(0);
                            viewmodel.update(todo);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                int rightIcon = R.drawable.check;
                try {
                    Todo todo = adapter.getTodoAt(viewHolder.getLayoutPosition());
                    int isCompleted = todo.getIsCompleted();
                    if (isCompleted == 1) {
                        rightIcon = R.drawable.cancel;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new RecyclerViewSwipeDecorator.Builder(MainActivity.this, c, recyclerView, viewHolder, dX, dY, actionState,
                        isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(getResources().getColor(R.color.red))
                        .addSwipeLeftActionIcon(R.drawable.delete_img)
                        .addSwipeRightBackgroundColor(getResources().getColor(R.color.green))
                        .addSwipeRightActionIcon(rightIcon)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(todo -> {
            bottomSheetDialog = new BottomSheetDialog(
                    MainActivity.this, R.style.BottomSheetDialogTheme
            );

            View bottomSheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.bottom_sheet, findViewById(R.id.bottom_sheet));

            TextView tv = bottomSheetView.findViewById(R.id.textView2);
            tv.setText("Edit Todo");
            EditText mTodoText = bottomSheetView.findViewById(R.id.todo_editext);
            mTodoText.setText(todo.getTodo());
            LinearLayout mTimeStamp = bottomSheetView.findViewById(R.id.timeStamp);
            LinearLayout mSave = bottomSheetView.findViewById(R.id.save_btn);
            TextView mTimeStampText = bottomSheetView.findViewById(R.id.timestamp_text);
            mTimeStampText.setText(todo.getTimestamp());
            mTimeStamp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDateTime(mTimeStampText);
                }
            });
            timeStamp = todo.getTimestamp();
            date_ = todo.getDate();
            time_stamp = todo.getTimeStamp1();
            mSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String todoText = mTodoText.getText().toString().trim();
                    if (!TextUtils.isEmpty(todoText) && timeStamp != null) {
                        Todo todo1 = new Todo(todoText, timeStamp);
                        todo1.setId(todo.getId());
                        todo1.setDate(date_);
                        todo1.setTimeStamp1(time_stamp);
                        viewmodel.update(todo1);
                        bottomSheetDialog.dismiss();
                    }
                }
            });
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });

        adapter.setOnPinClickListener(todo -> {
            int isPinned = todo.getIsPinned();
            if (isPinned == 1) {
                todo.setIsPinned(0);
                viewmodel.update(todo);
            } else {
                todo.setIsPinned(1);
                viewmodel.update(todo);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void sync() {

        if (!synced) {
            try {
                Snackbar.make(mContextView, "Syncing your tasko list", Snackbar.LENGTH_SHORT)
                        .show();
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String todoText = (String) snapshot.child("tasko").getValue();
                            String timestamp = (String) snapshot.child("timestamp").getValue();
                            long id = (long) snapshot.child("id").getValue();
                            long isCompleted = (long) snapshot.child("isCompleted").getValue();
                            long isPinned = (long) snapshot.child("isPinned").getValue();
                            String date = (String) snapshot.child("date").getValue();
                            long timeStamp1 = (long) snapshot.child("timeStamp1").getValue();

                            try {
                                Todo todo = new Todo(todoText, timestamp);
                                todo.setId((int) id);
                                todo.setIsCompleted((int) isCompleted);
                                todo.setIsPinned((int) isPinned);
                                todo.setDate(date);
                                todo.setTimeStamp1(timeStamp1);
                                viewmodel.insert(todo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        reference.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.i("todoUpdate", "Delete Success");
                            } else {
                                Log.i("todoUpdate", task.getException().getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i("todoSync", databaseError.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateData(List<Todo> todos) {
        for (Todo todo : todos) {
            try {
                reference.child(Long.toString(todo.getId())).setValue(todo)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.i("todoUpdate", "Add Success");
                            } else {
                                Log.i("todoUpdate", task.getException().getMessage());
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        CollapsingToolbarLayout mCollapsingToolBar = findViewById(R.id.collapse_toolbar);
        checkUser();
        if (checkUser()) {
            imageUrl = mCurrentUser.getPhotoUrl();
            userName = mCurrentUser.getDisplayName();
            String[] strings = userName.split(" ");
            String firstName = strings[0];
            Glide
                    .with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(mProfileImage);
            mCollapsingToolBar.setTitle(firstName + "'s" + " Todo");
            reference = database.getReference(mCurrentUser.getUid());
        }
    }

    private boolean checkUser() {
        if (mCurrentUser == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(mContextView, "Press back again to exit", Snackbar.LENGTH_SHORT)
                .setAction("Exit", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                })
                .show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }


    public void openEditor(View view) {

        timeStamp = null;

        bottomSheetDialog = new BottomSheetDialog(
                MainActivity.this, R.style.BottomSheetDialogTheme
        );

        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.bottom_sheet, findViewById(R.id.bottom_sheet));

        EditText mTodoText = bottomSheetView.findViewById(R.id.todo_editext);
        LinearLayout mTimeStamp = bottomSheetView.findViewById(R.id.timeStamp);
        LinearLayout mSave = bottomSheetView.findViewById(R.id.save_btn);
        TextView mTimeStampText = bottomSheetView.findViewById(R.id.timestamp_text);
        mTimeStamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateTime(mTimeStampText);
            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoText = mTodoText.getText().toString().trim();

                if (timeStamp == null) {
                    Calendar calendar = Calendar.getInstance();
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    date = calendar.get(Calendar.DATE);
                    min = calendar.get(Calendar.MINUTE);
                    hour = calendar.get(Calendar.HOUR);

                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.set(Calendar.YEAR, year);
                    calendar1.set(Calendar.MONTH, month);
                    calendar1.set(Calendar.DATE, date);
                    calendar1.set(Calendar.HOUR, hour);
                    calendar1.set(Calendar.MINUTE, min);

                    CharSequence charSequence = DateFormat.format("MMM d, h:mm a", calendar1);
                    timeStamp = charSequence.toString();

                    CharSequence charSequence1 = DateFormat.format("yyyy-MM-dd", calendar1);
                    date_ = charSequence1.toString();

                    time_stamp = calendar1.getTimeInMillis();
                    if (!TextUtils.isEmpty(todoText)) {
                        Todo todo = new Todo(todoText, timeStamp);
                        todo.setId((int) new Date().getTime());
                        todo.setTimeStamp1(time_stamp);
                        todo.setDate(date_);
                        todo.setIsPinned(0);
                        todo.setIsCompleted(0);
                        viewmodel.insert(todo);
                        bottomSheetDialog.dismiss();
                    }
                } else {

                    if (!TextUtils.isEmpty(todoText)) {
                        Todo todo = new Todo(todoText, timeStamp);
                        todo.setId((int) new Date().getTime());
                        todo.setTimeStamp1(time_stamp);
                        todo.setDate(date_);
                        todo.setIsPinned(0);
                        todo.setIsCompleted(0);
                        viewmodel.insert(todo);
                        bottomSheetDialog.dismiss();
                    }
                }
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void getDateTime(TextView mTimeStampText) {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        date = calendar.get(Calendar.DATE);
        min = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR);
        is24Hour = DateFormat.is24HourFormat(this);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {

            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, (view1, hourOfDay, minute) -> {

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.YEAR, year1);
                calendar1.set(Calendar.MONTH, month1);
                calendar1.set(Calendar.DATE, dayOfMonth);
                calendar1.set(Calendar.HOUR, hourOfDay);
                calendar1.set(Calendar.MINUTE, minute);

                CharSequence charSequence = DateFormat.format("MMM d, h:mm a", calendar1);
                timeStamp = charSequence.toString();
                mTimeStampText.setText(timeStamp);

                CharSequence charSequence1 = DateFormat.format("yyyy-MM-dd", calendar1);
                date_ = charSequence1.toString();

                time_stamp = calendar1.getTimeInMillis();
            }, hour, min, is24Hour);

            timePickerDialog.setTitle("Pick time");
            timePickerDialog.setCancelable(false);
            timePickerDialog.show();
        }, year, month, date);

        datePickerDialog.setTitle("Pick date");
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    public void openAbout(View view) {
        bottomSheetDialog = new BottomSheetDialog(
                MainActivity.this, R.style.BottomSheetDialogTheme
        );

        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.about_bottom, findViewById(R.id.about_bottom_sheet));

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
