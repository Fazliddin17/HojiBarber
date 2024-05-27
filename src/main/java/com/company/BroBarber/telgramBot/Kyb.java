package com.company.BroBarber.telgramBot;

import com.company.BroBarber.db.domain.Barber;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class Kyb {
    public ReplyKeyboardMarkup barberMenu() {
        String[] a = {
                "\uD83C\uDFDE Rasmni o'zgartirish",
                "\uD83D\uDCDE Telefon raqamni o'zgartirish",
                "Narxni o'zgartirish",
                "Ismni o'zgartirish",
                "Bugungi bo'sh vaqtlarim",
        };
        List<String> list = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            list.add(a[i]);
        }
        return setKeyboards(list, 2);
    }

    public ReplyKeyboardMarkup markup(List<KeyboardRow>rows){
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setKeyboard(rows);
        return markup;
    }


    public ReplyKeyboardMarkup superAdminMenu() {
        String[] a = {
                "\uD83D\uDCDC sartaroshlarning ro'yxati",
                "Sartarosh qo'shish",
                "\uD83D\uDCCD Manzilni o'zgartirish",
                "Reklama qilish"
        };
        return setKeyboards(a, 2);
    }

    public ReplyKeyboardMarkup setLocation() {
        KeyboardButton button = new KeyboardButton();
        button.setText("\uD83D\uDCCD Geolokatsiya yuborish");
        button.setRequestLocation(true);
        KeyboardRow row = new KeyboardRow();
        row.add(button);
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);
        return markup;
    }

    public InlineKeyboardMarkup crudBarbers() {
        String delete = "❌ O'chirish", back = "\uD83D\uDD19 Orqaga";
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(back);
        button.setCallbackData("back");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText(delete);
        button.setCallbackData("delete");
        row.add(button);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);
        InlineKeyboardMarkup m
                = new InlineKeyboardMarkup();
        m.setKeyboard(rows);
        return m;
    }

    public InlineKeyboardMarkup isDelete() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("❌ Ha o'chirilsin");
        button.setCallbackData("yes delete");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText("✅ Yo'q o'chirilmasin");
        button.setCallbackData("no delete");
        row.add(button);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);
        InlineKeyboardMarkup m
                = new InlineKeyboardMarkup();
        m.setKeyboard(rows);
        return m;
    }

    public InlineKeyboardMarkup isAdd() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("❌ Yo'q");
        button.setCallbackData("no add");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText("✅ Ha");
        button.setCallbackData("yes add");
        row.add(button);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);
        InlineKeyboardMarkup m
                = new InlineKeyboardMarkup();
        m.setKeyboard(rows);
        return m;
    }

    public ReplyKeyboardMarkup setKeyboards(List<String> words, int size) {
        KeyboardButton button;
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            button = new KeyboardButton();
            button.setText(words.get(i));
            row.add(button);
            if ((i + 1) % size == 0) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);
        return markup;
    }
    public InlineKeyboardMarkup userMenu(List<Barber> list1) {
        InlineKeyboardButton button;
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < list1.size(); i++) {
            String name = list1.get(i).getFullName(),
                    id = String.valueOf(list1.get(i).getId());
            button = new InlineKeyboardButton();
            button.setText(name);
            button.setCallbackData(id);
            row.add(button);
            rows.add(row);
            row = new ArrayList<>();
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }


    public ReplyKeyboardMarkup setKeyboards(String[] words, int size) {
        KeyboardButton button;
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            button = new KeyboardButton();
            button.setText(words[i]);
            row.add(button);
            if ((i + 1) % size == 0) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup requestContact(String lang) {
        KeyboardButton button = new KeyboardButton();
        button.setText(lang.equals("uz") ? "\uD83D\uDCDE Ro'yxatdan o'tish" : "\uD83D\uDCDE Регистрация");
        button.setRequestContact(true);
        KeyboardRow row = new KeyboardRow();
        row.add(button);
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup reklamaMenu() {
        String[] a = {
                "Matn yuborish",
                "Video yuborish",
                "Audio yuborish",
                "Rasm yuborish",
                "Ovozli xaabar yuborish",
                "\uD83D\uDD19 Back"
        };
        return setKeyboards(a, 2);
    }

    public ReplyKeyboardMarkup back() {
        String[] a = {"\uD83D\uDD19 Back"};
        return setKeyboards(a, 1);
    }

    public ReplyKeyboardMarkup isKyb() {
        String[] a = {"✅ Ha", "❌ Yo'q"};
        return setKeyboards(a, 2);
    }

    public InlineKeyboardMarkup setUrl(String[] urls) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String url : urls) {
            button.setText(url);
            button.setUrl(url);
            row.add(button);
            rows.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }


}
