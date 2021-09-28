package com.teyouale.objectdetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class Admin extends AppCompatActivity {
    public FirebaseFirestore db;
    private TextView groupname, createdby,pass;
    private Button addQuestion, ready;
    private ListView listMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        db = FirebaseFirestore.getInstance();

        groupname = findViewById(R.id.groupname1);
        createdby = findViewById(R.id.createdby);
        addQuestion = findViewById(R.id.addQu);
        pass = findViewById(R.id.pass);
        ready = findViewById(R.id.Ready);
        listMembers = findViewById(R.id.listmembers);

        Intent intent = getIntent();
        String groupId = intent.getStringExtra("newgid");
        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = AddQuestion.newInstance();
                ((AddQuestion) dialog).setCallback(new AddQuestion.Callback() {
                    @Override
                    public void onActionClick(String question, String answer) {
                        Map<String, Object> questions = new HashMap<>();
                        questions.put("question", question);
                        questions.put("answer",answer);
                        final Task<DocumentReference> docRef3 = db.collection("Groups").document(groupId).collection("Questions")
                                .add(questions)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("TAG", "DocumentSnapshot written with ID: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("TAG", "Error adding document", e);
                                    }
                                });
                    }
                });
                dialog.show(getSupportFragmentManager(), "tag");
            }
        });



        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        createdby.setText("Created By " + signInAccount.getDisplayName());


        final DocumentReference docRef = db.collection("Groups").document(groupId);
        final String[] groupName = new String[3];

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
                            groupName[2] = (String) snapshot.get("PassCode");

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    groupname.setText(groupName[0]);
                                    pass.setText(groupName[2]);
                                    Log.d("qwe", "run: " + groupName[1]);
                                    if (groupName[1].equals("Ready")) {
                                        ready.setText("Running");
                                        ready.setEnabled(false);
//                                        Log.d("qwe", "run: " + groupName[1]);
//                                        Intent intent1 = new Intent(Admin.this, gameplay.class);
//                                        intent1.putExtra("groupId", groupId);
//                                        startActivity(intent1);
                                    }


                                }
                            });
                        } else {
                            Log.d("TAG", "Current data: null");
                        }
                    }
                });


                //  transaction.set(docRef3, user);
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
        ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference readystate = db.collection("Groups").document(groupId);
                Log.d("TAG", "onClick: " + groupId);
                Log.d("TAG", "onClick: " + groupId);
                readystate
                        .update("Status", "Ready")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "DocumentSnapshot successfully updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error updating document", e);
                            }
                        });
            }
        });



    }
}


