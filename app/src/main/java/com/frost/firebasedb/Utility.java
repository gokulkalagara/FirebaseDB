package com.frost.firebasedb;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Gokul Kalagara (Mr. Pyscho) on 07-03-2020.
 * <p>
 * FROST
 */
public class Utility {


    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                return false;
                /* aka, do nothing */
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void showSnackBar(Context activity, View view, String text, int type) {
        if (view == null || text == null || activity == null) {
            return;
        }
        Snackbar snackBar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        TextView txtMessage = (TextView) snackBar.getView().findViewById(R.id.snackbar_text);
        txtMessage.setTextColor(ContextCompat.getColor(activity, R.color.white));
        if (type == 2)
            snackBar.getView().setBackgroundColor(ContextCompat.getColor(activity, R.color.black));
        else if (type == 1)
            snackBar.getView().setBackgroundColor(ContextCompat.getColor(activity, R.color.app_snack_bar_true));
        else {
            snackBar.getView().setBackgroundColor(ContextCompat.getColor(activity, R.color.bold_text_color));
        }
        snackBar.show();
    }


    public static boolean isValidEmail(CharSequence paramCharSequence) {
        if (paramCharSequence == null) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(paramCharSequence).matches();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = activity.getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    public static boolean isValidMobile(String paramString) {
        return paramString.matches("[0-9]{10}");
    }

    public static String getUTCTime() {

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(new Date());
    }

    public static int dpSize(Context context, int sizeInDp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInDp * scale + 0.5f);
    }
}
