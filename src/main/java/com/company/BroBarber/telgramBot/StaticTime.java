package com.company.BroBarber.telgramBot;

import java.util.ArrayList;
import java.util.List;

public class StaticTime {
    protected static List<String> getTimes() {
        List<String> list = new ArrayList<>();
        String[] a = {
                "10:00", "11:00", "12:00", "13:00",
                "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00"
        };
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        return list;
    }
}
