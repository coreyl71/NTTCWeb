package com.dlab.nttcweb;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * Created by Corey on 2018/09/05.
 */

public class HeadDialog extends Dialog {
    private TextView tv_getCamera, tv_getPic, tv_cancel;

    private Context context;
    private ClickListenerInterface clickListenerInterface;

    public interface ClickListenerInterface {

        public void doGetCamera();

        public void doGetPic();

        public void doCancel();

    }

    public HeadDialog(Context context) {
        super(context, R.style.MyDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_head, null);
        setContentView(view);

        tv_getCamera = (TextView) view.findViewById(R.id.tv_getCamera);
        tv_getPic = (TextView) view.findViewById(R.id.tv_getPic);
        tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);

        tv_getCamera.setText(context.getResources().getString(R.string.str_identity_manual_img_dialog_camera));
        tv_getPic.setText(context.getResources().getString(R.string.str_identity_manual_img_dialog_gallary));
        tv_cancel.setText(context.getResources().getString(R.string.dialog_personal_center_logout_cancel));

        tv_getCamera.setOnClickListener(new HeadDialogClickListener());
        tv_getPic.setOnClickListener(new HeadDialogClickListener());
        tv_cancel.setOnClickListener(new HeadDialogClickListener());

        Window dialogWindow = getWindow();

        // 获取屏幕宽高
        DisplayMetrics d = context.getResources().getDisplayMetrics();

        WindowManager.LayoutParams lp = null;
        if (dialogWindow != null) {
            lp = dialogWindow.getAttributes();
            // 设置宽高
            lp.width = (int) (d.widthPixels * 0.8);
            dialogWindow.setAttributes(lp);
        }

    }

    public void setClickListener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    private class HeadDialogClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.tv_getCamera:
                    clickListenerInterface.doGetCamera();
                    break;

                case R.id.tv_getPic:
                    clickListenerInterface.doGetPic();
                    break;

                case R.id.tv_cancel:
                    clickListenerInterface.doCancel();
                    break;

                default:
                    break;

            }

        }

    }
}
