package com.antcloud.app.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;

import com.antcloud.app.R;

public class Dialog implements Runnable {
    private final String title;
    private final String message;
    private final Activity activity;
    private final Runnable runOnDismiss;
    private AlertDialog alert;
    private static final ArrayList<Dialog> rundownDialogs = new ArrayList<>();

    private Dialog(Activity activity, String title, String message, Runnable runOnDismiss){
        this.activity = activity;
        this.title = title;
        this.message = message;
        this.runOnDismiss = runOnDismiss;
    }

    public static void closeDialogs() {
        synchronized (rundownDialogs) {
            for (Dialog d : rundownDialogs) {
                if (d.alert.isShowing()) {
                    d.alert.dismiss();
                }
            }
            rundownDialogs.clear();
        }
    }

    public static void displayDialog(final Activity activity, String title, String message, final boolean endAfterDismiss){
        activity.runOnUiThread(new Dialog(activity, title, message, new Runnable() {
            @Override
            public void run() {
                if (endAfterDismiss) {
                    activity.finish();
                }
            }
        }));
    }

    public static void displayDialog(Activity activity, String title, String message, Runnable runOnDismiss){
        activity.runOnUiThread(new Dialog(activity, title, message, runOnDismiss));
    }

    @Override
    public void run() {
        if (activity.isFinishing())
            return;
        alert = new AlertDialog.Builder(activity).create();
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
 
        alert.setButton(AlertDialog.BUTTON_POSITIVE, activity.getResources().getText(android.R.string.ok), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                  synchronized (rundownDialogs) {
                      rundownDialogs.remove(Dialog.this);
                      alert.dismiss();
                  }
                  runOnDismiss.run();

              }
        });
        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "Help", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                synchronized (rundownDialogs) {
                    rundownDialogs.remove(Dialog.this);
                    alert.dismiss();
                }
                runOnDismiss.run();
               HelpLauncher.launchTroubleshooting(activity);
            }
        });
        alert.setOnShowListener(dialog -> {
            Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setFocusable(true);
            button.setFocusableInTouchMode(true);
            button.requestFocus();
        });

        synchronized (rundownDialogs) {
            rundownDialogs.add(this);
            alert.show();
        }
    }

}
