package com.teyouale.objectdetection;



import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.teyouale.objectdetection.Utils.showDialog;

public class Permission {
    public static final String TAG = Permission.class.getSimpleName();
    public static final Map<String, Boolean> permissionsResults = new ConcurrentHashMap<>();
    // Permissions names
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    public static final String CAMERA = "android.permission.CAMERA";

    // All Permissions in ArrayList
    private static ArrayList<String> PERMISSIONS = new ArrayList<>(Arrays.asList(
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA
    ));

    // Request Code
    private static final int REQUEST_CODE = (Permission.class.hashCode() & 0xffff);

    // Checking Permission Status
    public static boolean isPermissionGranted(Context context, String permission) {
        // Check the Platform
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // it Granted On Installation Time
            return true;
        }
        Boolean result = permissionsResults.get(permission);
        if (result == null) {
            int permissionCheck = checkSelfPermission(context, permission);
            result = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            //permissionsResults.put(permission, result);
        }
        return result;
    }

    /**
     * Checks for permissions and Request  permission for Denied
     **/
    public static void checkAndRequest(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new LinkedList<>();
            for (String permission : PERMISSIONS) {
                if (!isPermissionGranted(context, permission)) {
                    permissions.add(permission);
                }
            }
            if (!permissions.isEmpty()) {
                String[] array = permissions.toArray(new String[permissions.size()]);
                ActivityCompat.requestPermissions((Activity) context, array, REQUEST_CODE);
            }
        }
    }

    /**
     * On Permission Denied
     **/
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, final Context context) {
        if (requestCode == REQUEST_CODE && permissions.length == grantResults.length) {
            HashMap<String, Integer> ps = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    ps.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }
            if (deniedCount == 0) {
                // All Permission are Granted
            } else {
                for (Map.Entry<String, Integer> entry : ps.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permName)) {
                        // AlertDialog Box
                        showDialog((Activity) context, "", "This App needs This Permissiion", "Yes ,Grant Permission",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        checkAndRequest(context);
                                    }
                                }, "No Exit App", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        ((Activity) context).finish();
                                    }
                                }
                                , false
                        );
                    } else {
                        showDialog((Activity) context, "", "You Have Denied Some Permissions Allow all permission at Setting ->Permission",
                                "Go to the Setting",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                        // Go to app Setting
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", context.getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                        ((Activity) context).finish();
                                    }
                                }, "No Exit App", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        ((Activity) context).finish();
                                    }
                                }
                                , false);
                        break;
                    }
                }
            }

        }
    }

    /**
     * In Case it Needed
     **/
    public static boolean notifyIfNotGranted(@NonNull final Context context, @NonNull String permission) {
        if (!isPermissionGranted(context, permission)) {
            showDialog((Activity) context, "", "This App needs This Permissiion", "Yes ,Grant Permission",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            checkAndRequest(context);
                        }
                    }, "No Exit App", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ((Activity) context).finish();
                        }
                    }
                    , false
            );
            return true;
        }
        return false;
    }


}
