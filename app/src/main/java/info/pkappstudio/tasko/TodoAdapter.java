package info.pkappstudio.tasko;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TodoAdapter extends ListAdapter<Todo, TodoAdapter.TodoViewHolder> {
    private OnItemClickListener listener;
    private OnPinClickListener pinClickListener;

    public TodoAdapter() {
        super(DIF_CALLBACK);
    }

    public static final DiffUtil.ItemCallback<Todo> DIF_CALLBACK = new DiffUtil.ItemCallback<Todo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return oldItem.getTodo().equals(newItem.getTodo()) &&
                    oldItem.getTimestamp().equals(newItem.getTimestamp()) && oldItem.getIsCompleted() == newItem.getIsCompleted();
        }
    };

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_view, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo currentTodo = getItem(position);

        int isPinned = currentTodo.getIsPinned();
        int isCompleted = currentTodo.getIsCompleted();
        Date d1 = null, d2 = null;


        String time = new SimpleDateFormat("yyyy-MM-dd")
                .format(new Timestamp(System.currentTimeMillis()));
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            d1 = sdformat.parse(currentTodo.getDate());
            d2 = sdformat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (isPinned == 1) {
            holder.mPin.setImageResource(R.drawable.pinned);
        } else {
            holder.mPin.setImageResource(R.drawable.pin);
        }

        if (isCompleted == 1) {
            holder.mTodo.setTextColor(Color.parseColor("#80767676"));
            holder.mTimestamp.setTextColor(Color.parseColor("#80767676"));
            holder.mPin.setEnabled(false);
            holder.itemView.setEnabled(false);
            holder.mStatus.setVisibility(View.INVISIBLE);
        } else {
            holder.mTodo.setTextColor(Color.parseColor("#767676"));
            holder.mTimestamp.setTextColor(Color.parseColor("#B0B0B0"));
            holder.mPin.setEnabled(true);
            holder.itemView.setEnabled(true);

            if (d1.compareTo(d2) == 0) {
                holder.mStatus.setVisibility(View.VISIBLE);
                holder.mStatus.setImageResource(R.drawable.today);
            } else if (d1.compareTo(d2) < 0) {
                holder.mStatus.setVisibility(View.VISIBLE);
                holder.mStatus.setImageResource(R.drawable.late);
            } else{
                holder.mStatus.setVisibility(View.INVISIBLE);
            }
        }
        holder.mTodo.setText(currentTodo.getTodo());
        holder.mTimestamp.setText(currentTodo.getTimestamp());

    }


    public Todo getTodoAt(int position) {
        return getItem(position);
    }

    public class TodoViewHolder extends RecyclerView.ViewHolder {

        TextView mTodo;
        TextView mTimestamp;
        ImageView mStatus;
        ImageView mPin;

        private TodoViewHolder(@NonNull View itemView) {
            super(itemView);

            mTodo = itemView.findViewById(R.id.todo_text);
            mTimestamp = itemView.findViewById(R.id.timestamp_text);
            mStatus = itemView.findViewById(R.id.status_img);
            mPin = itemView.findViewById(R.id.pin_Img);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            mPin.setOnClickListener(v -> {
                int position = getAdapterPosition();
                pinClickListener.onItemClick(getItem(position));
            });
        }
    }


    public interface OnItemClickListener {
        void onItemClick(Todo todo);
    }

    public interface OnPinClickListener {
        void onItemClick(Todo todo);
    }

    public void setOnPinClickListener(OnPinClickListener listener) {
        this.pinClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
