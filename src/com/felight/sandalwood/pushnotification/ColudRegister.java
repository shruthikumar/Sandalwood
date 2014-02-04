package com.felight.sandalwood.pushnotification;

import java.io.IOException;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class ColudRegister {
	
	
	public static void register(Context context){
		String SENDER_ID = "682184441049";
		 GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		 try {
			String registrationId = gcm.register(SENDER_ID);
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
	}

}
