package com.company.BroBarber.telgramBot.users;

import com.company.BroBarber.db.domain.Barber;
import org.springframework.stereotype.Controller;

@Controller
public class UserMsg {
    protected String chooseLang() {
        String msg = "\uD83C\uDDFA\uD83C\uDDFF Itimos, tilni tanlang\n\n";
        msg += "\uD83C\uDDF7\uD83C\uDDFA Пожалуйста, выберите язык";
        return msg;
    }

    protected String chooseLang(String lang) {
        if (lang.equals("uz")) return "\uD83C\uDDFA\uD83C\uDDFF O'zingizga kerakli tilni tanlang\n\n";
        else return "\uD83C\uDDFA\uD83C\uDDFF Выберите нужный язык";
    }

    protected String successLang(String lang) {
        if (lang.equals("uz"))
            return "✅ Til muvaffaqiyatli o'zgartirildi";
        else
            return "✅ Язык успешно изменен";
    }

    protected String successPhone(String lang, String phone) {
        return lang.equals("uz") ? """
                Sizning telefon raqamingiz: <code>%s</code>
                                
                ✅ Muvaffaqiyatli ro'yxatdan o'tdingiz""".formatted(phone) : """
                Ваш номер телефона: <code>%s</code>
                                
                 ✅ Вы успешно зарегистрировались""".formatted(phone);


    }

    protected String notFoundContact(String lang) {
        return lang.equals("uz") ? "Telefon raqamingizni yuborishingiz kerak ks holda botdan foydalana olmaysiz" : "Вы должны отправить свой номер телефона, иначе вы не сможете использовать бота.";
    }

    protected String errorCommand(String lang) {
        if (lang.equals("uz")) return "❌ Noto'g'ri buyruq\n\nIltimos, tugmalardan foydalaning";
        else return "❌ Неверная команда\n" +
                "\n" +
                "Пожалуйста, используйте кнопки";
    }

    protected String barberLists(String lang) {
        if (lang.equals("uz")) return "Barcha sartaroshlarning ro'yxati\n\nQuyidagilardan birini tanlang";
        else return """
                Список всех парикмахерских
                                
                Выберите один из следующих""";
    }

    protected String getTime(String lang) {
        if (lang.equals("uz")) return "O'zingizga uchun qulay vaqtni tanlang";
        else return "Выберите удобное для вас время";
    }

    protected String notActive(String lang, String time) {
        if (lang.equals("uz"))
            return "Siz bugungi vaqtingizni band qilib bo'lgansiz, buyurtma berish uchun ertaga harakat qilib ko'ring\n\n Buyurtma qilingan vaqt: %s".formatted(time);
        else
            return "«Вы забронированы сегодня, попробуйте завтра разместить заказ» \n\n Время заказа: %s".formatted(time);
    }

    protected String phoneNumber(String lang) {
        if (lang.equals("uz")) return "Siz bilan bog'lanishimiz uchun telefon raqamingizni qoldiring";
        else return "Оставьте свой номер телефона, чтобы мы могли связаться с вами";
    }

    protected String queue(String lang , String time ) {
        if (lang.equals("uz")) return """
                ✅ O'zingizni joyingizni muvaffaqiyatli band qildingiz
                
                Sizni soat %s da sartaroshxonamizda kutib qolamiz 😊😊
                """.formatted(time);
        else return """
                ✅ Вы успешно забронировали место
                               \s
                                Мы будем ждать вас по адресу %s в нашей парикмахерской 😊😊
                """.formatted(time);
    }

    protected String aboutBarber(String lang, Barber barber) {
        if (lang.equals("uz")) return """
                Sartaroshning to'liq ismi: <b>%s</b>
                Sartaroshning telefon raqami: <b>%s</b>
                Soch olish narxi: <b>%s</b>
                Sartarosh bilan bog'lanish:  <a href='tg://user?id=%d'>%s</a>
                """.formatted(barber.getFullName(), barber.getPhone(), barber.getPrice(), barber.getChatId(), barber.getFullName());

        else return """
                Полное имя парикмахера: <b>%s</b>
                                Телефон парикмахерской: <b>%s</b>
                                Цена стрижки: <b>%s</b>
                                Свяжитесь с парикмахером: <a href='tg://user?id=%d'>%s</a>
                                """.formatted(barber.getFullName(), barber.getPhone(), barber.getPrice(), barber.getChatId(), barber.getFullName());
    }
}
