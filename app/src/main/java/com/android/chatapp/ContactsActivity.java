package com.android.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String USERS = "users";

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<User, MessageViewHolder> mFirebaseAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private List<User> userList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.userRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference.child(USERS).push().setValue(new User(mFirebaseUser.getDisplayName(), mFirebaseUser.getEmail()));
        Utils.savePreferenceData(getApplicationContext(), "EMAIL", mFirebaseUser.getEmail());
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
                if(!userList.contains(friendlyMessage)) {
                    if (friendlyMessage.getName() != null) {
                        viewHolder.userTextView.setText(friendlyMessage.getName());
                        viewHolder.userTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                                intent.putExtra("CHAT", friendlyMessage);
                                startActivity(intent);
                            }
                        });
                        if(friendlyMessage.getEmail().equals(Utils.readPreferenceData(getApplicationContext(), "EMAIL", "")))
                            viewHolder.userTextView.setVisibility(View.GONE);
                    }
                    userList.add(friendlyMessage);
                } else {
                    viewHolder.userTextView.setVisibility(View.GONE);
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
                /*if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }*/
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        Utils.savePreferenceData(getApplicationContext(), "LOG_IN", "tr");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView;

        public MessageViewHolder(View v) {
            super(v);
            userTextView = (TextView) itemView.findViewById(R.id.textViewUser);
        }
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mFirebaseUser = null;
        startActivity(new Intent(this, SignInActivity.class));
        Utils.clearPreferences(getApplicationContext());
    }
}
