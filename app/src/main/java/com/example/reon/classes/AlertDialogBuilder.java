package com.example.reon.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.reon.R;

public class AlertDialogBuilder extends AlertDialog.Builder {
    private View alertTitleView;

    public AlertDialogBuilder(Context context) {
        super(context, R.style.CustomAlertDialog);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        alertTitleView = inflater.inflate(R.layout.alert_title, null);
        setCustomTitle(alertTitleView);
    }

    @Override
    public AlertDialog.Builder setTitle(CharSequence title) {
        ((TextView) alertTitleView.findViewById(R.id.alert_title)).setText(title);
        return super.setTitle(title);
    }

}
