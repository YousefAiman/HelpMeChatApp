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

public class ChattersAdapter extends RecyclerView.Adapter<ChattersAdapter.ChattersViewHolder> {

    ArrayList<User> chatters;
    Context context;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
    DocumentSnapshot document;
    int height;

    ChattersAdapter(Context context, ArrayList<User> chatters) {
        this.chatters = chatters;
        this.context = context;
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        height = displaymetrics.heightPixels;
    }

    @NonNull
    @Override
    public ChattersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.near_me_chatters_2, parent, false);
        return new ChattersAdapter.ChattersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChattersViewHolder holder, final int position) {
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = height / 10;
        holder.itemView.setLayoutParams(params);
        final User chatter = chatters.get(position);
        if (chatter.getLocationHidden() != 0) {
            if (((System.currentTimeMillis() / 1000) - chatter.getLocationHidden()) < 86400) {
                holder.countryTv.setText(context.getString(R.string.hidden));
            } else {
                holder.countryTv.setText(chatter.getCountry() + "-" + chatter.getCity());
            }
        } else {
            holder.countryTv.setText(chatter.getCountry() + "-" + chatter.getCity());
        }

        if (chatter.getGender().equals("Male") || chatter.getGender().equals("ذكر")) {
            holder.genderTv.setText(context.getResources().getString(R.string.Male));
        } else {
            holder.genderTv.setText(context.getResources().getString(R.string.Female));
        }

        String username = chatter.getUsername();

        if (chatter.getImageUrl() != null) {
//            holder.progressBar.setVisibility(View.VISIBLE);
            Picasso.get().load(chatter.getImageUrl()).fit().centerInside().into(holder.chatterIv, new Callback() {
                @Override
                public void onSuccess() {
//                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {

                }
            });
        } else {

            holder.chatterIv.setImageResource(R.color.white);
            holder.firstLetterMessageTv.setText(Character.toString(username.charAt(0)).toUpperCase());
        }
        holder.usernameTv.setText(username);

        holder.itemView.setOnClickListener(view -> userRef.whereEqualTo("id", currentUserId).limit(1).get().addOnCompleteListener(task -> {
            document = task.getResult().getDocuments().get(0);
            if (task.isSuccessful()) {
                long currentPoints = document.getLong("points");
                if (currentPoints >= 9) {
                    userRef.document(document.getId()).update("points", currentPoints - 9).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Intent intent = new Intent(holder.itemView.getContext(), MessagingActivity.class);
                            intent.putExtra("chatterName", chatter.getUsername());
                            intent.putExtra("chatterId", chatter.getId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });

                } else {

                    AlertDialog.Builder alert = new AlertDialog.Builder(view.getRootView().getContext());
                    alert.setTitle(context.getString(R.string.not_enough) + currentPoints);
                    alert.setMessage(context.getString(R.string.not_enough_points));
                    alert.setPositiveButton(context.getString(R.string.get_points), (dialogInterface, i) -> {
                        if (context instanceof NearMeChattersActivity) {
                            ((NearMeChattersActivity) context).showAdd();
                        }

                    });
                    alert.setNegativeButton(context.getString(R.string.cancel), (dialogInterface, i) -> {

                    });
                    alert.create().show();

                }
            }
        }));
    }

    @Override
    public int getItemCount() {
        return chatters.size();
    }

    public class ChattersViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv;
        ImageView chatterIv;
        TextView countryTv;
        TextView genderTv;
        TextView firstLetterMessageTv;
//        ProgressBar progressBar;

        public ChattersViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            chatterIv = itemView.findViewById(R.id.profileMessageIv);
            genderTv = itemView.findViewById(R.id.genderTv);
            countryTv = itemView.findViewById(R.id.countryTv);
            firstLetterMessageTv = itemView.findViewById(R.id.firstLetterMessageTv);
//            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

}
