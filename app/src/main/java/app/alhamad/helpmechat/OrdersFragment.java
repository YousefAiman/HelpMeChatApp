package app.alhamad.helpmechat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OrdersFragment extends Fragment {
    RecyclerView ordersRv;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    CollectionReference usersRef;
    CollectionReference ordersRef;
    ArrayList<Order> orders;
    Long points;
    DocumentReference dr;

    public OrdersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        ordersRv = view.findViewById(R.id.ordersRv);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orders = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersRef = firestore.collection("users");
        ordersRef = firestore.collection("orders");

        OrdersAdapter ordersAdapter = new OrdersAdapter(getContext(), orders);
        ordersRv.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersRv.setAdapter(ordersAdapter);

//        ConstraintLayout.LayoutParams params = new
//                ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
//       int height = displaymetrics.heightPixels;
//        params.height = height / 20;
//        params.width = height / 20;
        ImageView imageView2 = view.findViewById(R.id.imageView);

        //    imageView2.setLayoutParams(params);

        usersRef.whereEqualTo("id", auth.getCurrentUser().getUid()).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                DocumentSnapshot ds = snapshots.getDocuments().get(0);
                dr = usersRef.document(ds.getId());

                ordersRef.whereEqualTo("city", ds.getString("city")).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {

                                orders.add(dc.getDocument().toObject(Order.class));
                                ordersAdapter.notifyItemInserted(orders.size());
                            } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                orders.remove(dc.getDocument().toObject(Order.class));
                                ordersAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        });


        View view1 = view.findViewById(R.id.view5);
        TextView textView2 = view.findViewById(R.id.textView2);
        view1.setOnClickListener(view2 -> imageView2.performClick());
        textView2.setOnClickListener(view23 -> imageView2.performClick());
        imageView2.setOnClickListener(view24 -> dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long points = documentSnapshot.getLong("points");
                if (points >= 50) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(view24.getRootView().getContext());
                    alert.setTitle(getContext().getString(R.string.add_order));
                    alert.setMessage(getContext().getString(R.string.add_order_desc));
                    alert.setPositiveButton(getContext().getString(R.string.yes), (dialogInterface, i) -> {
                        dr.update("points", FieldValue.increment(-50)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getContext(), AddOrderActivity.class));
                            }
                        });
                    });
                    alert.setNegativeButton(getContext().getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    alert.create().show();


                } else {

                    AlertDialog.Builder alert = new AlertDialog.Builder(view24.getRootView().getContext());
                    alert.setTitle(getContext().getString(R.string.not_enough) + points);
                    alert.setMessage(getContext().getString(R.string.add_order_points));
                    alert.setPositiveButton(getContext().getString(R.string.get_points), (dialogInterface, i) -> {
                        ((HomeActivity) getActivity()).showAdd();
                    });
                    alert.setNegativeButton(getContext().getString(R.string.cancel), (dialogInterface, i) -> {
                    });
                    alert.create().show();


                }

            }
        }));

    }
}
