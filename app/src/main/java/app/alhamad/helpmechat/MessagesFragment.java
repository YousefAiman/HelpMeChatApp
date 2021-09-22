package app.alhamad.helpmechat;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class MessagesFragment extends Fragment {
    RecyclerView SentMessagesRv;
    ArrayList<SavedMessage> savedMessages;
    SavedMessagesAdapter adapter;
    CollectionReference messagesRef;
    TextView emptyMessagesTv;

    public MessagesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        SentMessagesRv = view.findViewById(R.id.SentMessagesRv);
        emptyMessagesTv = view.findViewById(R.id.emptyMessagesTv);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        savedMessages = new ArrayList<>();
        adapter = new SavedMessagesAdapter(getContext(), savedMessages);
        SentMessagesRv.setLayoutManager(new LinearLayoutManager(getContext()));
        SentMessagesRv.setAdapter(adapter);

        messagesRef = FirebaseFirestore.getInstance().collection("messages");
        final String currentUserid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Query query = messagesRef.whereEqualTo("sender", currentUserid);


        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshots : task.getResult().getDocuments()) {
                    SavedMessage sm = new SavedMessage();
                    ArrayList<String> messages = (ArrayList<String>) snapshots.get("messages");
                    sm.setLatestMessage(messages.get(messages.size() - 1));
                    sm.setMessagingUserId(snapshots.getString("receiver"));
                    savedMessages.add(sm);
                }
                adapter.notifyDataSetChanged();
                if (task.isComplete()) {
                    query.addSnapshotListener((queryDocumentSnapshots, e) -> {
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            DocumentSnapshot document = dc.getDocument();
                            SavedMessage sm = new SavedMessage();
                            String sender = document.getString("receiver");
                            ArrayList<String> messages = (ArrayList<String>) document.get("messages");
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                sm.setLatestMessage(messages.get(messages.size() - 1));
                                sm.setMessagingUserId(sender);
                                boolean exits = false;
                                for (SavedMessage savedMessage : savedMessages) {
                                    if (savedMessage.getMessagingUserId().equals(sm.getMessagingUserId())) {
                                        exits = true;
                                    }
                                }
                                if (!exits) {
                                    savedMessages.add(sm);
                                    adapter.notifyItemInserted(savedMessages.size());
                                }
                            }
                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                for (SavedMessage savedMessage : savedMessages) {
                                    if (savedMessage.getMessagingUserId().equals(sender)) {
                                        savedMessage.setLatestMessage(messages.get(messages.size() - 1));
                                        adapter.notifyItemChanged(savedMessages.indexOf(savedMessage));
                                    }
                                }
                            }
                        }

                        if (!savedMessages.isEmpty()) {
                            emptyMessagesTv.setVisibility(View.GONE);
                        } else {
                            emptyMessagesTv.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
            if (!savedMessages.isEmpty()) {
                emptyMessagesTv.setVisibility(View.GONE);
            } else {
                emptyMessagesTv.setVisibility(View.VISIBLE);
            }

            final Query newquery = messagesRef.whereEqualTo("receiver", currentUserid);

            newquery.get().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    for (DocumentSnapshot snapshots : task1.getResult().getDocuments()) {

                        SavedMessage sm = new SavedMessage();
                        ArrayList<String> messages = (ArrayList<String>) snapshots.get("messages");
                        sm.setLatestMessage(messages.get(messages.size() - 1));
                        sm.setMessagingUserId(snapshots.getString("sender"));
                        savedMessages.add(sm);
                    }
                    adapter.notifyDataSetChanged();
                    if (task1.isComplete()) {
                        newquery.addSnapshotListener((queryDocumentSnapshots, e) -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                    DocumentSnapshot document = dc.getDocument();
                                    SavedMessage sm = new SavedMessage();
                                    String sender = document.getString("sender");
                                    ArrayList<String> messages = (ArrayList<String>) document.get("messages");

                                    if (dc.getType() == DocumentChange.Type.ADDED) {
                                        sm.setLatestMessage(messages.get(messages.size() - 1));
                                        sm.setMessagingUserId(sender);
                                        boolean exits = false;
                                        for (SavedMessage savedMessage : savedMessages) {
                                            if (savedMessage.getMessagingUserId().equals(sm.getMessagingUserId())) {
                                                exits = true;
                                            }
                                        }
                                        if (!exits) {
                                            savedMessages.add(sm);
                                            adapter.notifyItemInserted(savedMessages.size());
                                        }
                                    }
                                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                        for (SavedMessage savedMessage : savedMessages) {
                                            if (savedMessage.getMessagingUserId().equals(sender)) {
                                                savedMessage.setLatestMessage(messages.get(messages.size() - 1));
                                                adapter.notifyItemChanged(savedMessages.indexOf(savedMessage));
                                            }
                                        }
                                    }

                                }
                            }

                            if (!savedMessages.isEmpty()) {
                                emptyMessagesTv.setVisibility(View.GONE);
                            } else {
                                emptyMessagesTv.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                if (!savedMessages.isEmpty()) {
                    emptyMessagesTv.setVisibility(View.GONE);
                } else {
                    emptyMessagesTv.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }
}
