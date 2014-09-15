package com.dolphin.smartmodeelite;

public class ServiceStatus {
	String toggle;
	String serviceStatus;
	String lastTime;
	String beenScheduled;
	
	public String getBeenScheduled() {
		return beenScheduled;
	}
	public void setBeenScheduled(String beenScheduled) {
		this.beenScheduled = beenScheduled;
	}
	public String getToggle() {
		return toggle;
	}
	public void setToggle(String toggle) {
		this.toggle = toggle;
	}
	public String getStatus() {
		return serviceStatus;
	}
	public void setStatus(String serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	public ServiceStatus(String toggle, String serviceStatus, String lastTime, String b) {
		// TODO Auto-generated constructor stub
		this.toggle = toggle;
		this.serviceStatus =  serviceStatus;
		this.lastTime = lastTime;
		this.beenScheduled = b;
	}

}
