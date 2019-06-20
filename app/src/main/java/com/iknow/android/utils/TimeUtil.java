package com.iknow.android.utils;

/**
 * author : J.Chou
 * github : https://github.com/iknow4
 * e-mail : who_know_me@163.com
 * time   : 2019/06/20 15:53
 * version: 1.0
 * description:
 */
public class TimeUtil {

  public static String convertSecondsToTime(long seconds) {
    String timeStr;
    int hour;
    int minute;
    int second;
    if (seconds <= 0) {
      return "00:00";
    } else {
      minute = (int) seconds / 60;
      if (minute < 60) {
        second = (int) seconds % 60;
        timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
      } else {
        hour = minute / 60;
        if (hour > 99) return "99:59:59";
        minute = minute % 60;
        second = (int) (seconds - hour * 3600 - minute * 60);
        timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
      }
    }
    return timeStr;
  }

  private static String unitFormat(int i) {
    String retStr;
    if (i >= 0 && i < 10) {
      retStr = "0" + Integer.toString(i);
    } else {
      retStr = "" + i;
    }
    return retStr;
  }
}
