package com.teyouale.objectdetection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static android.content.Context.CAMERA_SERVICE;

public class Question extends Fragment implements View.OnClickListener {


    private String questionId,questionName,answers;
    private View view;
    public FirebaseFirestore db;
    private String groupId;
    private TextView x;
    private static final String TAG = "TAG";
    public Button btakepicture, brecording;
    PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private ImageLabeler labeler;
    private ObjectDetector objectDetector;
    private GoogleSignInAccount signInAccount;

    public Question() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Permission.checkAndRequest(getContext());

        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();
        objectDetector = ObjectDetection.getClient(options);
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCamerX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());

        if(getArguments() != null){
           questionId = getArguments().getString("Id");
           groupId = getArguments().getString("groupID");

           Log.d("woo", "onCreate: "+ questionId + groupId);

           db = FirebaseFirestore.getInstance();
           db.collection("Groups").document(groupId).collection("Questions").document(questionId)
                   .addSnapshotListener(new EventListener<DocumentSnapshot>() {
               @Override
               public void onEvent(@Nullable DocumentSnapshot snapshot,
                                   @Nullable FirebaseFirestoreException e) {
                   if (e != null) {
                       Log.w("TAG", "Listen failed.", e);
                       return;
                   }

                   if (snapshot != null && snapshot.exists()) {
                       Log.d("TAG", "Current data: " + snapshot.getData());
                       questionName = (String) snapshot.get("question");
                       answers = (String) snapshot.get("answer");
                       x.setText(questionName);
                   } else {
                       Log.d("TAG", "Current data: null");
                   }
               }
           });

       }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(getContext());
    }

    @SuppressLint("RestrictedApi")
    private void startCamerX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

        imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, imageAnalysis, preview);
        //  videoCapture = new VideoCapture.Builder().setVideoFrameRate(30).build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_question, container, false);
         x=  view.findViewById(R.id.question);

        btakepicture = view.findViewById(R.id.bcapture);
        brecording = view.findViewById(R.id.brecord);
        previewView = view.findViewById(R.id.previewView);

        btakepicture.setOnClickListener(this);
        brecording.setOnClickListener(this);

    signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permission.onRequestPermissionsResult(requestCode, permissions, grantResults, getContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bcapture:
                capturePhoto();
                break;
            case R.id.brecord:
                break;
        }
    }
    private void capturePhoto() {
        imageCapture.takePicture(getExecutor(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
//                DetectImage(image);
                DetectImageLabeling(image);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
            }
        });
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void DetectImageLabeling(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        InputImage image =
                InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> labels) {
                // Task completed successfully
                // ...
                ArrayList<String> list = new ArrayList<>();
                for (ImageLabel label : labels) {
                    String text = label.getText();
                    float confidence = label.getConfidence();
                    int index = label.getIndex();
                    Log.d(TAG, "onSuccess: " + text + confidence + index);
                    list.add(text);
//                    Toast.makeText(getContext(), text + "  " + confidence, Toast.LENGTH_SHORT).show();
                }
                String[] listitem = list.toArray(new String[0]);
                if(!list.isEmpty()) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                    mBuilder.setTitle("Choose an MI Item");
                    mBuilder.setSingleChoiceItems( listitem, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if(answers.equals(listitem[i])){
                                    final DocumentReference docRef3 = db.collection("Groups").document(groupId).collection("Members").document(signInAccount.getEmail());
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", signInAccount.getDisplayName());
                                    user.put("email", signInAccount.getEmail());
                                    docRef3.set(user)
                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   Log.d(TAG, "DocumentSnapshot successfully written!");
                                               }
                                           })
                                           .addOnFailureListener(new OnFailureListener() {
                                               @Override
                                               public void onFailure(@NonNull Exception e) {
                                                   Log.w(TAG, "Error writing document", e);
                                               }
                                           });
                                }

                        }
                    });
                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }
}