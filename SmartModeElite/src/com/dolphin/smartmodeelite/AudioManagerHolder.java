package com.dolphin.smartmodeelite;

import android.media.AudioManager;

public class AudioManagerHolder {
	private static AudioManagerHolder ah;
	private AudioManager manager;
	public AudioManager getManager() {
		return manager;
	}


	public void setManager(AudioManager manager) {
		this.manager = manager;
	}


	private AudioManagerHolder(AudioManager t) {
		// TODO Auto-generated constructor stub
		manager = t;
	}
	public static AudioManagerHolder getInstance(){
		if(ah == null){
			ah = new AudioManagerHolder(null);
			return ah;
		} else {
			return ah;
		}
	}
	
	public static AudioManagerHolder getInstance(AudioManager t){
		if(ah == null){
			ah = new AudioManagerHolder(t);
			return ah;
		} else {
			ah.setManager(t);
			return ah;
		}
	}
}
