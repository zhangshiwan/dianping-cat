package com.site.helper;

import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Administrator on 2016/12/22.
 */
public class ParametersHelper {
    private static final Set<String> NAMES = ImmutableSet.of("Boolean", "Character", "Byte", "Short", "Long", "Integer", new String[]{"Byte", "Float", "Double", "Void", "String"});
    private static final TimeZone CHINA_ZONE = TimeZone.getTimeZone("GMT+08:00");
    private static final Locale CHINA_LOCALE = Locale.CHINA;

    public static String getParameters(Object[] args) {
        if (args == null) {
            return "";
        }
        StringBuilder sbd = new StringBuilder();
        if (args.length > 0) {
            for (Object i : args) {
                if (i == null) {
                    sbd.append("null");
                } else {
                    Class clz = i.getClass();
                    if (isPrimitive(clz)) {
                        sbd.append(evalPrimitive(i));
                    } else if (clz.isArray()) {
                        evalArray(i, sbd);
                    } else if (Collection.class.isAssignableFrom(clz)) {
                        Object[] arr = ((Collection) i).toArray();
                        evalArray(arr, sbd);
                    } else if ((i instanceof Date)) {
                        sbd.append('"').append(formatYmdHis((Date) i)).append('"');
                    } else {
                        sbd.append(clz.getSimpleName()).append(":OBJ");
                    }
                }
                sbd.append(',');
            }
            sbd.setLength(sbd.length() - 1);
        }
        return sbd.toString();
    }

    private static boolean isPrimitive(Class clz) {
        return (clz.isPrimitive()) || (NAMES.contains(clz.getSimpleName()));
    }

    private static String evalPrimitive(Object obj) {
        String s = String.valueOf(obj);
        if (s.length() > 32) {
            return new StringBuilder().append(s.substring(0, 32)).append("...").toString();
        }
        return s;
    }

    private static void evalArray(Object arr, StringBuilder sbd) {
        int sz = Array.getLength(arr);
        if (sz == 0) {
            sbd.append("[]");
            return;
        }
        Class clz = Array.get(arr, 0).getClass();
        if (clz == Byte.class) {
            sbd.append("Byte[").append(sz).append(']');
            return;
        }
        if (isPrimitive(clz)) {
            sbd.append('[');
            int len = Math.min(sz, 10);
            for (int i = 0; i < len; i++) {
                Object obj = Array.get(arr, i);
                if (isPrimitive(obj.getClass()))
                    sbd.append(evalPrimitive(obj));
                else {
                    sbd.append(obj.getClass().getSimpleName()).append(":OBJ");
                }
                sbd.append(',');
            }
            if (sz > 10) {
                sbd.append(",...,len=").append(sz);
            }
            if (sbd.charAt(sbd.length() - 1) == ',')
                sbd.setCharAt(sbd.length() - 1, ']');
            else
                sbd.append(']');
        } else {
            sbd.append("[len=").append(sz).append(']');
        }
    }

    private static String formatYmdHis(Date date) {
        Calendar ca = Calendar.getInstance(CHINA_ZONE, CHINA_LOCALE);
        ca.setTimeInMillis(date.getTime());
        StringBuilder sbd = new StringBuilder();
        sbd.append(ca.get(1)).append('-');
        int month = 1 + ca.get(2);
        if (month < 10) {
            sbd.append('0');
        }
        sbd.append(month).append('-');
        int day = ca.get(5);
        if (day < 10) {
            sbd.append('0');
        }
        sbd.append(day).append(' ');
        int hour = ca.get(11);
        if (hour < 10) {
            sbd.append('0');
        }
        sbd.append(hour).append(':');
        int minute = ca.get(12);
        if (minute < 10) {
            sbd.append('0');
        }
        sbd.append(minute).append(':');
        int second = ca.get(13);
        if (second < 10) {
            sbd.append('0');
        }
        sbd.append(second);
        return sbd.toString();
    }
}
