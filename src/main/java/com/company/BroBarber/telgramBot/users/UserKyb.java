package com.company.BroBarber.telgramBot.users;

import com.company.BroBarber.db.domain.Barber;
import com.company.BroBarber.telgramBot.Kyb;
import com.company.BroBarber.telgramBot.consts;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class UserKyb extends Kyb {
    public static String back(String lang) {
        return lang.equals("uz") ? "\uD83D\uDD19 Orqaga qaytish" : "\uD83D\uDD19 Вернуться назад";
    }

    protected ReplyKeyboardMarkup userMenu(String lang) {
        return setKeyboards(new String[]{
                lang.equals("uz") ? "Sartaroshlar ro'yxat" : "Список парикмахерских",
                lang.equals("uz") ? "⚙\uFE0F Sozlamalar" : "⚙\uFE0F Настройки"
        }, 2);
    }

    protected ReplyKeyboardMarkup chooseLang() {
        String[] a = {"\uD83C\uDDFA\uD83C\uDDFF O'zbek tili", "\uD83C\uDDF7\uD83C\uDDFA русский язык"};
        return setKeyboards(a, 1);
    }

    protected ReplyKeyboardMarkup allBarbers(List<Barber> barbers, String lang) {
        KeyboardButton button;
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < barbers.size(); i++) {
            button = new KeyboardButton();
            button.setText(barbers.get(i).getFullName());
            row.add(button);
            if ((i + 1) % 2 == 0) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        rows.add(row);
        row = new KeyboardRow();
        button = new KeyboardButton();
        button.setText(back(lang));
        row.add(button);
        rows.add(row);
        return markup(rows);
    }

    protected ReplyKeyboardMarkup times(String lang, Barber barber) {
        if (barber.getTimes()==null) barber.setTimes(new ArrayList<>());
        KeyboardButton button;
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        String []barberTimes = new String[barber.getTimes().size()];
        for (int i = 0; i < barberTimes.length; i++) {
            barberTimes[i] = barber.getTimes().get(i).getTime();
        }
        String[] times = getTimes(consts.times,barberTimes);
        for (int i = 0; i < times.length; i++) {
            button = new KeyboardButton();
            button.setText(times[i]);
            row.add(button);
            if ((i + 1) % 2 == 0) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        rows.add(row);
        row = new KeyboardRow();
        button = new KeyboardButton();
        button.setText(back(lang));
        row.add(button);
        rows.add(row);
        return markup(rows);
    }

    public static String[] getTimes(String[] constTimes, String[] barberTimes) {
        Set<String> bSet = new HashSet<>();
        for (String s : barberTimes) {
            bSet.add(s);
        }

        List<String> resultList = new ArrayList<>();
        for (String s : constTimes) {
            if (!bSet.contains(s)) {
                resultList.add(s);
            }
        }
        String[] resultArray = new String[resultList.size()];
        resultList.toArray(resultArray);
        return resultArray;
    }

}
