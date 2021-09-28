package com.teyouale.objectdetection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class gameplay extends AppCompatActivity {
    public Button button;
    private String groupId;
    private FirebaseFirestore db;
    public String score;
    private DocumentReference docRef;

    public int i = 0;
    public ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        Intent intent1 = getIntent();
        groupId = intent1.getStringExtra("groupId");

        db = FirebaseFirestore.getInstance();
//        Log.d("12as", "onCreate: " + groupId);
            db.collection("Groups").document(groupId).collection("Questions")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }


                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {
                                list.add(doc.getId());
                                Log.d("sad", "onCreate: " + list);
                            }
                        }
                        Bundle bundle = new Bundle();
                        Log.d("sad", "onCreate: " + list);
                        bundle.putString("Id", list.get(0));
                        bundle.putString("groupID", groupId);
                        Question question = new Question();
                        question.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, question).commit();

                    }
                });


//
//       bundle.putString("Id", list.get(0));
//        Question question = new Question();
//        question.setArguments(bundle);
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,question).commit();
        button = findViewById(R.id.next);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Yes", "onClick: " +  i + ' '+ list.size());
                if(i== list.size()-2){
                    button.setText("Result");
                    i++;

                }else if(i < list.size() -1){
                    Log.d("Yes", "onClick: " +  i + ' '+ list.size());
                    i++;
                    Log.d("Yes", "onClick: " +  i + ' '+ list.size());
                    Bundle bundle = new Bundle();
                    bundle.putString("Id", list.get(i));
                    bundle.putString("groupID", groupId);
                    Question question = new Question();
                    question.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, question).commit();
                }else {
                    Intent i = new Intent(gameplay.this,Result.class);
                    startActivity(i);
                }


            }
        });


    }
}