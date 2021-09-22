package app.alhamad.helpmechat;

import android.app.AlertDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.commentsviewholder> {

    ArrayList<String> comments;
    Context context;
    CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");
    int height;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String orderUidId;

    public CommentsAdapter(ArrayList<String> comments, Context context, String orderUidId) {
        this.comments = comments;
        this.context = context;
        this.orderUidId = orderUidId;
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        height = displaymetrics.heightPixels / 11;
    }

    @NonNull
    @Override
    public commentsviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new commentsviewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull commentsviewholder holder, final int position) {
        holder.itemView.getLayoutParams().height = height;
        String c = comments.get(position);
        String[] contentSplit = c.split("~");
        usersRef.whereEqualTo("id", contentSplit[1]).get().addOnCompleteListener(task -> {
            if (!task.getResult().isEmpty()) {
                final DocumentSnapshot ds = task.getResult().getDocuments().get(0);

                final String username = ds.getString("username");
                holder.commentAuthorTv.setText(username);
                final String imageUrl = ds.getString("imageUrl");
                if (imageUrl != null) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.firstLetterMessageTv.setVisibility(View.INVISIBLE);
                    Picasso.get().load(imageUrl).fit().centerInside().into(holder.commentuserImg, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                } else {
                    holder.firstLetterMessageTv.setVisibility(View.VISIBLE);
                    holder.commentuserImg.setImageResource(R.color.white);
                    holder.firstLetterMessageTv.setText(Character.toString(username.charAt(0)).toUpperCase());
                }
            }
        });
        holder.commetnInfoTv.setText(contentSplit[0]);
        holder.commentTimeTv.setText(TimeConvertor.getTimeAgo(Long.parseLong(contentSplit[2]), context));
        if (!orderUidId.equals(currentUserId)) {
            holder.likeImage.setVisibility(View.GONE);
        } else {
            holder.likeImage.setOnClickListener(view -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getRootView().getContext());
                alert.setTitle(context.getString(R.string.reward_user));
                alert.setMessage(context.getString(R.string.reward_user_info));
                alert.setPositiveButton(context.getString(R.string.yes), (dialogInterface, i) -> {
                    usersRef.whereEqualTo("id", currentUserId).limit(1).get().addOnSuccessListener(snapshots -> {
                        DocumentSnapshot ds = snapshots.getDocuments().get(0);
                        long points = ds.getLong("points");
                        if (points >= 5) {
                            usersRef.whereEqualTo("id", contentSplit[1]).limit(1).get().addOnSuccessListener(snapshots1 -> usersRef.document(snapshots1.getDocuments().get(0).getId()).update("points", FieldValue.increment(5)).addOnSuccessListener(aVoid -> usersRef.document(ds.getId()).update("points", FieldValue.increment(-5)).addOnSuccessListener(aVoid1 -> Toast.makeText(context, context.getString(R.string.reward_success), Toast.LENGTH_SHORT).show())));
                        } else {
                            Toast.makeText(context, context.getString(R.string.not_enough) + points, Toast.LENGTH_SHORT).show();
                        }
                    });

                });
                alert.setNegativeButton(context.getString(R.string.cancel), (dialogInterface, i) -> {
                });
                alert.create().show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class commentsviewholder extends RecyclerView.ViewHolder {
        ImageView commentuserImg;
        TextView commentAuthorTv;
        TextView commetnInfoTv;
        TextView commentTimeTv;
        ProgressBar progressBar;
        TextView firstLetterMessageTv;
        ImageView likeImage;

        //        ConstraintLayout constraintLayout;
//        LinearLayout commentLayout;
//        Guideline guideline26;
        public commentsviewholder(@NonNull View itemView) {
            super(itemView);
            commentuserImg = itemView.findViewById(R.id.commentUserImage);
            commentAuthorTv = itemView.findViewById(R.id.commentUsernameTv);
            commetnInfoTv = itemView.findViewById(R.id.commentInfoTv);
            commentTimeTv = itemView.findViewById(R.id.commentTimeTv);
            progressBar = itemView.findViewById(R.id.progressBar);
            firstLetterMessageTv = itemView.findViewById(R.id.firstLetterMessageTv);
            likeImage = itemView.findViewById(R.id.likeImage);
//            constraintLayout = itemView.findViewById(R.id.constraintLayout);
//            commentLayout = itemView.findViewById(R.id.commentLayout);
//            guideline26 = itemView.findViewById(R.id.guideline26);
        }
    }
}
