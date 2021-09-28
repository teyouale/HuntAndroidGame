package com.teyouale.objectdetection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WatingArea extends AppCompatActivity {
    public FirebaseFirestore db;
    private TextView groupname, createdby, totalCouont, status;
    private ListView listMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wating_area);
        db = FirebaseFirestore.getInstance();

        groupname = findViewById(R.id.groupname1);
        createdby = findViewById(R.id.createdby);
        status = findViewById(R.id.status);
        totalCouont = findViewById(R.id.totalcount);
        listMembers = findViewById(R.id.listmembers);


        Intent intent = getIntent();

        String groupId = intent.getStringExtra("groupId");

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        Map<String, Object> user = new HashMap<>();
        user.put("name", signInAccount.getDisplayName());
        user.put("email", signInAccount.getEmail());
        user.put("Rank", 1);


        final DocumentReference docRef = db.collection("Groups").document(groupId);
        final CollectionReference docRef2 = db.collection("Groups").document(groupId).collection("Members");
        final DocumentReference docRef3 = db.collection("Groups").document(groupId).collection("Members").document(signInAccount.getEmail());
        final String[] groupName = new String[2];

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d("TAG", "Current data: " + snapshot.getData());
                            groupName[0] = (String) snapshot.get("Group Name");
                            groupName[1] = (String) snapshot.get("Status");
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    groupname.setText(groupName[0]);
                                    status.setText(groupName[1]);
                                    Log.d("qwe", "run: " + groupName[1]);
                                    if(groupName[1].equals("Ready")){
                                        Log.d("qwe", "run: " + groupName[1]);
                                        Intent intent1 = new Intent(WatingArea.this,gameplay.class);
                                        intent1.putExtra("groupId",groupId);
                                        startActivity(intent1);
                                    }



                                }
                            });
                        } else {
                            Log.d("TAG", "Current data: null");
                        }
                    }
                });


                transaction.set(docRef3, user);
                db.collection("Groups").document(groupId).collection("Members")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w("TAG", "Listen failed.", e);
                                    return;
                                }

                                ArrayList<String> list = new ArrayList<>();
                                for (QueryDocumentSnapshot document : value) {
                                    list.add(document.getId());
//                                    Log.d("TAG", document.getId() + " => " + document.getData());
                                }

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Stuff that updates the UI
//                                        Log.d("TAG", "run: " + list.toString());
                                        String[] array = list.toArray(new String[0]);
                                        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, array);
                                        listMembers.setAdapter(adapter);
                                    }
                                });
                            }
                        });


                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "Transaction success!");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Transaction failure.", e);
            }
        });
        Toast.makeText(this, groupId, Toast.LENGTH_SHORT).show();

    }
}