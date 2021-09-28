package com.teyouale.objectdetection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class JoinGroupDialog extends DialogFragment implements View.OnClickListener {

    private JoinGroupDialog.Callback callback;
    public FirebaseFirestore db;
    public EditText passcode;
    private TextView email;
    private GoogleSignInAccount signInAccount;
    public WatingArea activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        this.activity = activity;
    }

    static JoinGroupDialog newInstance() {

        return new JoinGroupDialog();
    }

    public void setCallback(JoinGroupDialog.Callback callback) {
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
        View view = inflater.inflate(R.layout.fragment_joingroupdialog, container, false);

        db = FirebaseFirestore.getInstance();

        ImageButton close = view.findViewById(R.id.fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.fullscreen_dialog_action);
        email = view.findViewById(R.id.emailaddress);
        passcode = view.findViewById(R.id.passcode);

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

                String number = passcode.getText().toString();
               db.collection("Groups")
                        .whereEqualTo("PassCode", number)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {
                                    String groupId = null;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("TAG", document.getId() + " => " + document.getData() + number);
                                        groupId = document.getId();
                                        break;
                                    }
                                    callback.onActionClick(groupId);

                                } else {
                                    Log.d("TAG", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                dismiss();
                break;

        }

    }

    public interface Callback {

        void onActionClick(String name);

    }
}