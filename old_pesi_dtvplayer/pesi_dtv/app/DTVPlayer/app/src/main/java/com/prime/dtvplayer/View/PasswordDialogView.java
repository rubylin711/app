package com.prime.dtvplayer.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.dtvplayer.R;

import java.util.Locale;

abstract public class PasswordDialogView{
	private static final String TAG="PasswordDialog";

	// Johnny Add for two type password 20180125
	public static final int TYPE_PINCODE = 0;
	public static final int TYPE_FIXEDPASSWORD = 1;

	private Dialog mDialog = null;
	private Context mContext = null;
	private Toast toast=null;
	private TextView mPinTextView = null;
	private Button mCancel,mOk;

	// Johnny Add for two type password 20180125
	private int mPin = 0;
	private int mFixed = 0;
	private int mType = TYPE_PINCODE;
	private int mChangeChannel = 0;

	public PasswordDialogView(Context context, int pin, int type, int changeChannel) {
		mContext = context;
		mPin = pin;
		mType = type;
		mChangeChannel = changeChannel;//on normaliview, changeChannel == 1 ; otherwise == 0;
		mDialog = new Dialog(mContext, R.style.transparentDialog);

		mDialog.setCanceledOnTouchOutside(true);
		mDialog.setOnKeyListener( new DialogInterface.OnKeyListener(){
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
						case KeyEvent.KEYCODE_MENU:
							if (onDealUpDownKey() || (mChangeChannel == 1)) {
								((Activity) mContext).onKeyDown(keyCode, event);
								dismissDialog();
								return true;
							}
							break;
						case KeyEvent.KEYCODE_CHANNEL_UP:
						case KeyEvent.KEYCODE_CHANNEL_DOWN:
							if (onDealUpDownKey() || (mChangeChannel == 1)) {
								((Activity) mContext).onKeyDown(keyCode, event);
								dismissDialog();
								return true;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							if (mPinTextView.hasFocus()) {
								checkPassword();
								return true;
							}
							break;
					}
				}
				return false;
			}
		});
		new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
			@Override
			public void run () {
				mDialog.show();
			}
		}, 150);
		//mDialog.show();
		mDialog.setContentView(R.layout.vchip_pin);
		Window window = mDialog.getWindow();
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		mOk = (Button) window.findViewById(R.id.ok_icon);
		mCancel = (Button) window.findViewById(R.id.return_icon);
		pin_button_init(window);

	}

	private void pin_button_init(Window window ){
		mPinTextView = (TextView)window.findViewById(R.id.pin_textview);

		mOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				checkPassword();
			}
		});

		mCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissDialog();
			}
		});
	}

	public void checkPassword() {
		if (check_pin()) {
			dismissDialog();
			onCheckPasswordIsRight();
		} else {
			onCheckPasswordIsFalse();
            mPinTextView.setText("");
            mPinTextView.requestFocus();

			/*if(toast!=null)
				toast.cancel();
			toast = Toast.makeText(	mContext,
	    		R.string.STR_INVALID_PASSWORD,
	    		Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();*/
		}
	}

	private boolean check_pin(){
		String cur_pin = mPinTextView.getText().toString();

		String database_pin;

		// // Johnny Modify for two type password 20180125
		if (mType == TYPE_PINCODE)
		{
			database_pin = String.format(Locale.US, "%04d", mPin);
		}
		else
		{
			database_pin = String.format(Locale.US, "%04d", mFixed);
		}

		//Log.d(TAG,"cur_pin = "+cur_pin+"----"+"database_ping = "+database_pin);
		if(database_pin==null){
			if(cur_pin.equals("0000"))
				return true;
		}
		else{
			if(cur_pin.equals(database_pin)){
				return true;
			}
		}
		return false;
	}

	public void dismissDialog(){
		if(toast!=null)
			toast.cancel();
		if(mDialog!=null&& mDialog.isShowing()){
			mDialog.dismiss();
		}
	}

	public void cancelDialog(){
		if(toast!=null)
			toast.cancel();
		if(mDialog!=null&& mDialog.isShowing()){
			mDialog.cancel();
		}
	}

	public void showDialog(){
		if(mDialog!=null&& mDialog.isShowing()==false){
			mDialog.show();
		}
	}


	public boolean isShowing(){
		if(mDialog!=null&&mDialog.isShowing()){
			return mDialog.isShowing();
		}
		return false;
	}

	abstract public void onCheckPasswordIsRight();
	abstract public void onCheckPasswordIsFalse();
	abstract public boolean onDealUpDownKey();
	//abstract public void onChangeChannel(int playUpDown);
}

 
