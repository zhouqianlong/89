package com.serenegiant.usb;

import java.text.SimpleDateFormat;

public class TimerService {
    //��ʼʱ��
    long startTime = 0;
    //����ʱ��
    long stopTime = 0;
    //������
    long frameTime = 0;
    //��ͣʱ��
    long pauseTime = 0;
    //�ϴε�ʱ��
    long prevTime = 0;

    //״̬����ʼ����������ͣ
    public enum TimerStatus {
        START, STOP,PAUSE
    }
    TimerStatus status = TimerStatus.STOP;
    // ��ʼ��
	public TimerService() {
	}

    //��ʼ
    public void start(){
        startTime = System.currentTimeMillis();
        prevTime = startTime;
        status = TimerStatus.START;
        frameTime = 0;
    }

    //ֹͣ
    public void stop(){
        stopTime = System.currentTimeMillis();
        status = TimerStatus.STOP;
    }

    //��ͣ
    public void pause(){
        pauseTime = System.currentTimeMillis();
        status = TimerStatus.PAUSE;
    }

    //��ͣ
    public void startpause(){
        status = TimerStatus.START;
        startTime = System.currentTimeMillis();
        prevTime = startTime;
    }

    //��ȡ����
    public long getTimer(){
        long nowTimer = System.currentTimeMillis();
        frameTime += 1000 * (nowTimer - prevTime);
        prevTime = nowTimer;
        return  frameTime;
    }

    public long getCurrent(){
        return frameTime;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public String getTimeStr(){
        long n = (frameTime/1000)/1000;
        //SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");//��ʼ��Formatter��ת����ʽ��
        String hms = secToTime((int)n);// formatter.format(n);
        return hms;
    }


}
