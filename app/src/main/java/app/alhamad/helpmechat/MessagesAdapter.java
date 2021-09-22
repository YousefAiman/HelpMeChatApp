package app.alhamad.helpmechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    ArrayList<String> messages;
    String sender;
    View view;

    public MessagesAdapter(ArrayList<String> messages, String sender) {
        this.messages = messages;
        this.sender = sender;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message, parent, false);
        }
        return new MessagesAdapter.MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, final int position) {
        holder.messageContentTv.setText(messages.get(position).split("-")[0]);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        String message = messages.get(position);
        String sender = message.split("--")[2];
        if (sender.equals(this.sender)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        TextView messageContentTv;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContentTv = itemView.findViewById(R.id.messageContentTv);
        }
    }
}
