package app.alhamad.helpmechat;

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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    ArrayList<Order> orders;
    Context context;
    int height;
    LinearLayout.LayoutParams params;
    String replies;

    public OrdersAdapter(Context context, ArrayList<Order> orders) {
        this.orders = orders;
        this.context = context;
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        height = displaymetrics.heightPixels;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = height / 10;
        replies = context.getResources().getString(R.string.replies);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderViewHolder holder, final int position) {


        holder.itemView.setLayoutParams(params);

        Order order = orders.get(position);
        String username = order.getUsername();
        String imageUrl = order.getPhotoUrl();
        if (imageUrl != null) {
            holder.progressBar.setVisibility(View.VISIBLE);
            Picasso.get().load(imageUrl).fit().centerCrop().into(holder.orderProfileIv, new Callback() {
                @Override
                public void onSuccess() {
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                }
            });
        } else {
            holder.orderProfileIv.setImageResource(R.color.white);
            holder.firstLetterMessageTv.setText(Character.toString(username.charAt(0)).toUpperCase());
        }
        holder.usernameOrderTv.setText(username);
        holder.commentsOrderTv.setText(replies + " (" + order.getComments().size() + ")");
        holder.orderQuestionTv.setText(order.getTitle() + "");
        holder.timeOrderTv.setText(TimeConvertor.getTimeAgo(order.getPublishTime(), context));
        //  holder.ratingOrderTv.setText("Rating: "+order.getRating()+"");
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(holder.itemView.getContext(), OrderInfoActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            // intent.putExtra("userId", savedMessage.getMessagingUserId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView usernameOrderTv;
        ImageView orderProfileIv;
        //  TextView ratingOrderTv;
        TextView commentsOrderTv;
        TextView orderQuestionTv;
        TextView timeOrderTv;
        TextView firstLetterMessageTv;
        ProgressBar progressBar;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameOrderTv = itemView.findViewById(R.id.usernameOrderTv);
            orderProfileIv = itemView.findViewById(R.id.orderProfileIv);
            // ratingOrderTv = itemView.findViewById(R.id.ratingOrderTv);
            commentsOrderTv = itemView.findViewById(R.id.commentsOrderTv);
            orderQuestionTv = itemView.findViewById(R.id.orderQuestionTv);
            timeOrderTv = itemView.findViewById(R.id.timeOrderTv);
            firstLetterMessageTv = itemView.findViewById(R.id.firstLetterMessageTv);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
