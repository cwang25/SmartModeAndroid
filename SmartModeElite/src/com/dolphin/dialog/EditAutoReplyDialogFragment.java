package com.dolphin.dialog;

import com.dolphin.smartmodeelite.EditDefaultSet;
import com.dolphin.smartmodeelite.EditScheduleActivity;
import com.dolphin.smartmodeelite.EditWifiActivity;
import com.dolphin.smartmodeelite.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class EditAutoReplyDialogFragment extends DialogFragment implements OnCheckedChangeListener{
	private EditText mEditText;
	private CheckBox enableCheckBox;
	public interface EditAutoReplyDialogListener{
		public void onDialogPositiveClick(DialogFragment dialog);
		public void onDialogNegativeClick(DialogFragment dialog);
	}
	EditAutoReplyDialogListener mListener;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		 // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (EditAutoReplyDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View customView = getActivity().getLayoutInflater().inflate(R.layout.edit_auto_reply_dialog, null);
		builder.setTitle(R.string.auto_replay_setup_dialog_title)
				.setView(customView)
				.setPositiveButton(R.string.save,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
								mListener.onDialogPositiveClick(EditAutoReplyDialogFragment.this);
							}
						});
		//08/21  here, emulator has error.
		Dialog tmpD = builder.create();
		mEditText = (EditText)customView.findViewById(R.id.replyMessageEditTextDialog);
		enableCheckBox = (CheckBox)customView.findViewById(R.id.enableAutoReplyCheckDialog);
		Activity a = this.getActivity();
		if(a instanceof EditWifiActivity){
			enableCheckBox.setChecked(((EditWifiActivity)a).isEnable());
			mEditText.setText(((EditWifiActivity)a).getAutoReplyMsg());
		}else if(a instanceof EditScheduleActivity){
			enableCheckBox.setChecked(((EditScheduleActivity)a).isEnable());
			mEditText.setText(((EditScheduleActivity)a).getAutoReplyMsg());
		}else if(a instanceof EditDefaultSet){
			enableCheckBox.setChecked(((EditDefaultSet)a).isEnable());
			mEditText.setText(((EditDefaultSet)a).getAutoReplyMsg());
		}
		enableCheckBox.setOnCheckedChangeListener(this);
		mEditText.requestFocus();
		tmpD.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return tmpD;
	}

	public void printMessage(Context ctx, String msg){
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView , boolean isChecked) {
		// TODO Auto-generated method stub
		if(isChecked && mEditText.getText().toString().length() == 0){
			printMessage(this.getActivity(), "Can't enable auto reply with empty message.");
			buttonView.setChecked(false);
		}
	}

}