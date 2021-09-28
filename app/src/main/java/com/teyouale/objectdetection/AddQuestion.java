package com.teyouale.objectdetection;


import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class AddQuestion extends DialogFragment implements View.OnClickListener {
    private AddQuestion.Callback callback;
    public FirebaseFirestore db;
    public EditText question, answer;
    private TextView email;
    private GoogleSignInAccount signInAccount;

    static AddQuestion newInstance() {

        return new AddQuestion();
    }

    public void setCallback(com.teyouale.objectdetection.FullScreenDialog.Callback callback) {
        this.callback = (Callback) callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_MaterialComponents_MaterialCalendar_Fullscreen);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_question, container, false);

        db = FirebaseFirestore.getInstance();

        ImageButton close = view.findViewById(R.id.fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.fullscreen_dialog_action);
        email = view.findViewById(R.id.emailaddress);
        question = view.findViewById(R.id.question);
        answer = view.findViewById(R.id.answer);

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
                callback.onActionClick(question.getText().toString(), answer.getText().toString());


                dismiss();
                break;

        }

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {

        void onActionClick(String passCode, String name);

    }

}