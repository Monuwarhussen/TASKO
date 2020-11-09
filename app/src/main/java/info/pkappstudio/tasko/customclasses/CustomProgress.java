package info.pkappstudio.tasko.customclasses;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;

import info.pkappstudio.tasko.R;


public class CustomProgress {
    private Activity activity;
    private Dialog mDialog;

    public CustomProgress(Activity mActivity){
        this.activity = mActivity;
    }


    public void startProgress(){
        mDialog = new Dialog(activity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);
        mDialog.setContentView(R.layout.custom_progress);

        mDialog.show();
    }

    public void stopProgress(){
        mDialog.dismiss();
    }
}
