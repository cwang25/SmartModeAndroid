package com.dolphin.smartmodeelite;


import android.content.Context;

import com.dolphin.dao.SmartModeDAO;

public class DAOSingleton {
	private static DAOSingleton db;
	private SmartModeDAO dao;
	private DAOSingleton(SmartModeDAO db) {
		// TODO Auto-generated constructor stub
		this.dao = db;
	}
	
	public synchronized static DAOSingleton getInstance(Context t){
		if(db == null){
			db = new DAOSingleton(new SmartModeDAO(t));
			return db;
		} else {
			return db;
		}
	}
	public SmartModeDAO getDb() {
		return dao;
	}

	public void setDb(SmartModeDAO db) {
		this.dao = db;
	}

}
