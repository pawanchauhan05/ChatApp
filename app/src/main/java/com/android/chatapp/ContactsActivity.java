package com.android.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String USERS = "users";

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<User, MessageViewHolder> mFirebaseAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<User, MessageViewHolder>(
                User.class,
                R.layout.layout_user,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child(USERS)) {

            @Override
            protected User parseSnapshot(DataSnapshot snapshot) {
                User user = super.parseSnapshot(snapshot);
                return user;
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder, final User friendlyMessage, int position) {

                if (friendlyMessage.getName() != null) {
                    viewHolder.userTextView.setText(friendlyMessage.getName());
                    viewHolder.userTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent  = new Intent(ContactsActivity.this, ChatActivity.class);
                            intent.putExtra("CHAT", friendlyMessage);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                MessageViewHolder messageViewHolder = super.onCreateViewHolder(parent, viewType);
                return messageViewHolder;
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference.child(USERS).push().setValue(new User(mFirebaseUser.getDisplayName(), mFirebaseUser.getEmail()));
        Utils.savePreferenceData(getApplicationContext(), "EMAIL", mFirebaseUser.getEmail());

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView;

        public MessageViewHolder(View v) {
            super(v);
            userTextView = (TextView) itemView.findViewById(R.id.textViewUser);
        }
    }
}
