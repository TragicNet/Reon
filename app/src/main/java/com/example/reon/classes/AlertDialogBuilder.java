package com.example.reon.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reon.R;

public class AlertDialogBuilder extends AlertDialog.Builder {
    private View alertTitleView;

    public AlertDialogBuilder(Context context) {
        super(context, R.style.CustomAlertDialog);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        alertTitleView = inflater.inflate(R.layout.alert_title, null);
        setCustomTitle(alertTitleView);
        alertTitleView.findViewById(R.id.alert_icon).setVisibility(View.GONE);
    }

    @Override
    public AlertDialog.Builder setTitle(CharSequence title) {
        ((TextView) alertTitleView.findViewById(R.id.alert_title)).setText(title);
        return super.setTitle(title);
    }

    @Override
    public AlertDialog.Builder setIcon(int iconId) {
        alertTitleView.findViewById(R.id.alert_icon).setVisibility(View.VISIBLE);
        ((ImageView) alertTitleView.findViewById(R.id.alert_icon)).setImageResource(iconId);
        return super.setIcon(iconId);
    }
}
