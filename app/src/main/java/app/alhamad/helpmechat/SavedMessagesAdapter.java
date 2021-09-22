package app.alhamad.helpmechat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SavedMessagesAdapter extends RecyclerView.Adapter<SavedMessagesAdapter.SavedMesssagesViewHolder> {

    ArrayList<SavedMessage> savedMessages;
    Context context;
    Long time;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");
    DocumentSnapshot document;
    int height;
    User user;

    public SavedMessagesAdapter(Context context, ArrayList<SavedMessage> savedMessages) {
        this.savedMessages = savedMessages;
        this.context = context;
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        height = displaymetrics.heightPixels / 10;

    }

    @NonNull
    @Override
    public SavedMesssagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_message1, parent, false);
        return new SavedMesssagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SavedMesssagesViewHolder holder, final int position) {
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = height;
        holder.itemView.setLayoutParams(params);

        final SavedMessage savedMessage = savedMessages.get(position);
        usersRef.whereEqualTo("id", savedMessage.getMessagingUserId()).get().addOnCompleteListener(task -> {
            if (!task.getResult().isEmpty()) {
                user = task.getResult().getDocuments().get(0).toObject(User.class);

                final String username = user.getUsername();
                String imageUrl = user.getImageUrl();
                if (imageUrl != null) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    Picasso.get().load(imageUrl).fit().centerInside().into(holder.profileMessageIv, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                } else {
                    holder.profileMessageIv.setImageResource(R.color.white);
                    holder.firstLetterMessageTv.setText(Character.toString(username.charAt(0)).toUpperCase());
                }
                String latestMessage = savedMessage.getLatestMessage();

//                if (user.getLocationHidden() != 0) {
//                    if (((System.currentTimeMillis() / 1000) - user.getLocationHidden()) < 86400) {
//
//                        holder.areaMessageTv.setText(context.getString(R.string.hidden));
//                    } else {
//                        holder.areaMessageTv.setText(user.getCity());
//                    }
//                } else {
//                    holder.areaMessageTv.setText(user.getCity());
//                }

                holder.usernameMessageTv.setText(username);


                holder.contentMessageTv.setText(latestMessage.split("--")[0]);
                time = Long.parseLong(latestMessage.split("--")[1]);
                holder.timeMessageTv.setText(TimeConvertor.getTimeAgo(time, context));

                holder.itemView.setOnClickListener(view -> {
                    if (System.currentTimeMillis() / 1000 - time > 86400) {
                        usersRef.whereEqualTo("id", currentUserId).limit(1).get().addOnCompleteListener(task1 -> {
                            document = task1.getResult().getDocuments().get(0);
                            if (task1.isSuccessful()) {
                                long currentPoints = document.getLong("points");
                                if (currentPoints >= 9) {
                                    usersRef.document(document.getId()).update("points", currentPoints - 9).addOnCompleteListener(task11 -> {
                                        if (task11.isSuccessful()) {
                                            Intent intent = new Intent(holder.itemView.getContext(), MessagingActivity.class);

                                            intent.putExtra("chatterName", username);
                                            intent.putExtra("chatterId", savedMessage.getMessagingUserId());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(intent);
                                        }
                                    });
                                } else {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(view.getRootView().getContext());
                                    alert.setTitle(context.getString(R.string.not_enough) + currentPoints);
                                    alert.setMessage(context.getString(R.string.not_enough_points));
                                    alert.setPositiveButton(context.getString(R.string.get_points), (dialogInterface, i) -> {
                                        if (context instanceof HomeActivity) {
                                            ((HomeActivity) context).showAdd();
                                        }
                                    });
                                    alert.setNegativeButton(context.getString(R.string.cancel), (dialogInterface, i) -> {
                                    });
                                    alert.create().show();
                                }
                            }
                        });
                    } else {
                        Intent intent = new Intent(holder.itemView.getContext(), MessagingActivity.class);
                        intent.putExtra("chatterName", username);
                        intent.putExtra("chatterId", savedMessage.getMessagingUserId());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedMessages.size();
    }

    public static class SavedMesssagesViewHolder extends RecyclerView.ViewHolder {
        TextView usernameMessageTv;
        ImageView profileMessageIv;
        //  TextView areaMessageTv;
        TextView contentMessageTv;
        TextView timeMessageTv;
        TextView firstLetterMessageTv;
        ProgressBar progressBar;

        public SavedMesssagesViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameMessageTv = itemView.findViewById(R.id.usernameMessageTv);
            profileMessageIv = itemView.findViewById(R.id.profileMessageIv);
            contentMessageTv = itemView.findViewById(R.id.contentMessageTv);
            timeMessageTv = itemView.findViewById(R.id.timeMessageTv);
            firstLetterMessageTv = itemView.findViewById(R.id.firstLetterMessageTv);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
