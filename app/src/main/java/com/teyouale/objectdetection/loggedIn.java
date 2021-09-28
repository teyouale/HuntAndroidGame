package com.teyouale.objectdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class loggedIn extends AppCompatActivity {
    public TextView name;
    public Button logoutbtn,creategroup,joingroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        logoutbtn = findViewById(R.id.logoutbtn);
        creategroup = findViewById(R.id.creategroup);
        joingroup = findViewById(R.id.joingroup);

        name = findViewById(R.id.name);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null){
            name.setText(  signInAccount.getDisplayName().toUpperCase());
        }
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        creategroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = FullScreenDialog.newInstance();
                ((FullScreenDialog) dialog).setCallback(new FullScreenDialog.Callback() {
                    @Override
                    public void onActionClick(String name,String ref) {
                        Toast.makeText(loggedIn.this, name, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(loggedIn.this,Admin.class);
                        intent.putExtra("newgid",ref);
                        startActivity(intent);
                    }
                });
                dialog.show(getSupportFragmentManager(), "tag");
            }
        });

        joingroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = JoinGroupDialog.newInstance();
                ((JoinGroupDialog) dialog).setCallback(new JoinGroupDialog.Callback() {
                    @Override
                    public void onActionClick(String groupId) {
                        if(groupId != null){
                            Intent intent = new Intent(loggedIn.this,WatingArea.class);
                            intent.putExtra("groupId",groupId);
                            startActivity(intent);
                        }else{
                            Toast.makeText(loggedIn.this, "You Entered Incorrect Group ID", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(loggedIn.this, groupId, Toast.LENGTH_LONG).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "tag");
            }
        });

    }
}