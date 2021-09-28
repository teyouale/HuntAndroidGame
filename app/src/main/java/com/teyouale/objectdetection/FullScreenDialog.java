package com.teyouale.objectdetection;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FullScreenDialog extends DialogFragment implements View.OnClickListener {

    private Callback callback;
    public FirebaseFirestore db;
    public EditText name;
    private TextView email;
    private GoogleSignInAccount signInAccount;

    static FullScreenDialog newInstance() {

        return new FullScreenDialog();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MaterialComponents_MaterialCalendar_Fullscreen);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_dialog, container, false);

        db = FirebaseFirestore.getInstance();

        ImageButton close = view.findViewById(R.id.fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.fullscreen_dialog_action);
        email = view.findViewById(R.id.emailaddress);
        name = view.findViewById(R.id.groupname);

        signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (signInAccount != null) {
            email.setText(signInAccount.getEmail());
        }
        close.setOnClickListener(this);
        action.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {

            case R.id.fullscreen_dialog_close:
                dismiss();
                break;

            case R.id.fullscreen_dialog_action:
//                callback.onActionClick("Whatever");

                String groupname = name.getText().toString();
//                Toast.makeText(getContext(), name.getText(), Toast.LENGTH_SHORT).show();

                String passcode = String.format("%04d", new Random().nextInt(10000));
                Map<String, Object> group = new HashMap<>();
                group.put("Group Name", groupname);
                group.put("PassCode", passcode);
                group.put("Status", "Pending");
                group.put("Owner", signInAccount.getDisplayName());
                //  group.put("country", "USA");

                db.collection("Groups")
                        .add(group)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("TAG", "DocumentSnapshot written with ID: " + documentReference.getId());

                                Task<DocumentSnapshot> documentSnapshot = documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                Log.d("TAG", "DocumentSnapshot data: " + document.get("PassCode"));
                                                callback.onActionClick((String) document.get("PassCode"), documentReference.getId().toString());
                                            } else {
                                                Log.d("TAG", "No such document");
                                            }
                                        } else {
                                            Log.d("TAG", "get failed with ", task.getException());
                                        }
                                    }
                                });

                                Log.d("TAG", "DocumentSnapshot written with ID: " + documentSnapshot);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error adding document", e);
                            }
                        });
                dismiss();
                break;

        }

    }

    public interface Callback {

        void onActionClick(String passCode, String name);

    }

}