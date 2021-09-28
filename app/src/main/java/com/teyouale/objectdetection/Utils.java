package com.teyouale.objectdetection;



import android.app.Activity;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Utils {
    /**
     * AlertDialog Box
     **/
    public static AlertDialog showDialog(Activity context, String title, String msg, String postiveLable,
                                         DialogInterface.OnClickListener positivrOnClick,
                                         String negativeLabel, DialogInterface.OnClickListener negativeOnClick, boolean isCanceAble) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setCancelable(isCanceAble).setMessage(msg)
                .setPositiveButton(postiveLable, positivrOnClick)
                .setNegativeButton(negativeLabel, negativeOnClick);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    public static Toast showToast(Activity context, String message,int duration){
        Toast toast = Toast.makeText(context,message,duration);
        toast.show();
        return toast;
    }

    /**
     * Returns year-less date format
     */
    @SuppressWarnings("SimpleDateFormat")
    public static DateFormat getYearLessDateFormat(DateFormat dateFormat) {
        if (dateFormat instanceof SimpleDateFormat) {
            // creating year less date format
            String fullPattern = ((SimpleDateFormat)dateFormat).toPattern();
            // checking 'de' we omit problems with Spain locale
            String regex = fullPattern.contains("de") ? "[^Mm]*[Yy]+[^Mm]*" : "[^DdMm]*[Yy]+[^DdMm]*";
            String yearLessPattern = fullPattern.replaceAll(regex, "");
            return new SimpleDateFormat(yearLessPattern);
        }
        return dateFormat;
    }
}
