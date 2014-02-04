package com.felight.sandalwood.pushnotification;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("sandalwood", "notification received ");
		
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		 String messageType = gcm.getMessageType(intent);
		 // Filter messages based on message type. It is likely that GCM will be extended in the future
		 // with new message types, so just ignore message types you're not interested in, or that you
		 // don't recognize.
		 if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			 Log.i("sandalwood", "notification received ");
		 } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
			 Log.i("sandalwood", "notification received ");
		 } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			 Log.i("sandalwood", "notification received ");
		 }
		 
		 
	}

}
