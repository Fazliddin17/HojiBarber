package com.company.BroBarber.telgramBot.users;

import com.company.BroBarber.db.domain.Barber;
import com.company.BroBarber.db.domain.Location;
import com.company.BroBarber.db.domain.Time;
import com.company.BroBarber.db.domain.User;
import com.company.BroBarber.db.repositories.LocationRepository;
import com.company.BroBarber.db.repositories.TimeRepository;
import com.company.BroBarber.dto.ResponseDto;
import com.company.BroBarber.service.BarberService;
import com.company.BroBarber.service.UserService;
import com.company.BroBarber.telgramBot.MyBot;
import com.company.BroBarber.telgramBot.consts;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2

public class UserRole {
    private final MyBot bot;
    private final UserService userService;
    private final BarberService barberService;
    private final UserMsg msg;
    private final UserKyb kyb;
    private final LocationRepository locationRepository ;
    private final TimeRepository timeRepository;

    public void mainMenu(User user, Update update) {
        String eventCode = user.getEventCode();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals("/start")) {
                    startCommand(user);
                } else {
                    if (eventCode.equals("choose lang")) {
                        chooseLang(user, text);
                    } else if (eventCode.equals("request contact")) {
                        bot.sendMessage(user.getChatId(), msg.notFoundContact(user.getLang()), kyb.requestContact(user.getLang()));
                    } else if (eventCode.equals("menu")) {
                        menu(user, text);
                    } else if (eventCode.equals("edit lang")) {
                        editLang(user, text);
                    } else if (eventCode.equals("all barbers")) {
                        allBarbers(user, text);
                    } else if (eventCode.equals("get times")) {
                        getTimes(user, text);
                    } else if (eventCode.equals("phone number")) {
                        phoneNumber(user , text);
                    } else log.error("Kutilmagan xatolik");
                }
            } else if (message.hasContact()) {
                if (eventCode.equals("request contact")) {
                    requestContact(user, message.getContact());
                }
            }
        }
    }

    private void menu(User user, String text) {
        String lang = user.getLang();
        String[] buttons = new String[]{
                lang.equals("uz") ? "Sartaroshlar ro'yxat" : "Список парикмахерских",
                lang.equals("uz") ? "⚙\uFE0F Sozlamalar" : "⚙\uFE0F Настройки"
        };

        if (buttons[1].equals(text)) {
            sendMessage(user.getChatId(), msg.chooseLang(lang), kyb.chooseLang());
            eventCode(user, "edit lang");
        } else if (buttons[0].equals(text)) {
            ResponseDto<List<Barber>> a = barberService.findAll();
            if (!a.isSuccess()) {
                sendMessage(user.getChatId(), "<code>%s</code>".formatted(a.getMessage()));
                return;
            }
            List<Barber> list = a.getData();
            if (list == null) list = new ArrayList<>();
            sendMessage(user.getChatId(), msg.barberLists(lang), kyb.allBarbers(list, lang));
            eventCode(user, "all barbers");
        } else errorCommand(user, kyb.userMenu(lang));
    }

    public void allBarbers(User user, String text) {
        String back = UserKyb.back(user.getLang());
        if (text.equals(back)) {
            startCommand(user);
        } else {
            ResponseDto<Barber> x = barberService.findByName(text);
            if (!x.isSuccess()){
                sendMessage(user.getChatId(), "<code>%s</code>".formatted(x.getMessage()));
                return;
            }
            Barber barber = x.getData();
            if (barber == null) {
                errorCommand(user, kyb.allBarbers(barberService.findAll().getData(), user.getLang()));
                return;
            }
            if (user.getActive()) {
                user.setBarberId(barber.getId());
                user.setEventCode("get times");
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setParseMode("html");
                sendPhoto.setCaption(msg.aboutBarber(user.getLang(), barber) + "\n\n" + msg.getTime(user.getLang()));
                sendPhoto.setReplyMarkup(kyb.times(user.getLang(), barber));
                sendPhoto.setChatId(user.getChatId());
                sendPhoto.setPhoto(new InputFile(barber.getImage()));
                try {
                    bot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    log.error(e);
                    return;
                }
            } else {
                sendMessage(user.getChatId(), msg.notActive(user.getLang(), user.getTime()), kyb.times(user.getLang(), barber));
                startCommand(user);
                return;
            }
            userService.save(user);
        }
    }

    private void getTimes(User user, String text) {
        String lang = user.getLang();
        if (text.equals(UserKyb.back(user.getLang()))) {
            menu(user, lang.equals("uz") ? "Sartaroshlar ro'yxat" : "Список парикмахерских");
        } else {
            Barber barber = barberService.findById(user.getBarberId()).getData();
            String []a = new String[barber.getTimes().size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = barber.getTimes().get(i).getTime();
            }
            String[] getTimes = UserKyb.getTimes(consts.times,a);
            boolean success = false;
            for (String time : getTimes) {
                if (text.equals(time)) {
                    success = true;
                    break;
                }
            }
            if (!success) {
                errorCommand(user, kyb.times(lang, barber));
                return;
            }
            List<Time>times = barber.getTimes();
            times.add(new Time(text));
            barber.setTimes(times);
            Time time = new Time();
            time.setBarber(barber);
            time.setTime(text);
            timeRepository.save(time);
            barberService.save(barber);
            user.setTime(text);
            user.setEventCode("phone number");
            userService.save(user);
            sendMessage(user.getChatId(), msg.phoneNumber(lang), kyb.setKeyboards(new String[]{user.getPhone()}, 1));
        }
    }

    private void phoneNumber(User user , String text) {
        user.setActive(false);
        long chatId = user.getChatId();
        try {
            Location location = locationRepository.findAll().get(0);
            SendVenue sendVenue = new SendVenue();
            sendVenue.setChatId(chatId);
            sendVenue.setTitle(location.getTitle());
            sendVenue.setAddress(location.getAddress());
            sendVenue.setLatitude(location.getLat());
            sendVenue.setLongitude(location.getLon());
            bot.execute(sendVenue);
        } catch (Exception e) {
            log.error(e);
        }
        sendMessage(chatId , msg.queue(user.getLang(), user.getTime()));

        try {
            Barber barber = barberService.findById(user.getBarberId()).getData();
            String sendMsgToBarber = "Yangi foydalanuvchi o'zining joyini band qildi\n\n";
            sendMsgToBarber = sendMsgToBarber.concat("Telegramdagi niki: %s\n".formatted(user.getNickname()));
            sendMsgToBarber = sendMsgToBarber.concat("Mijoz bilan bog'lanish uchun telefon raqami: %s\n".formatted(text));
            sendMsgToBarber = sendMsgToBarber.concat("Band qilingan vaqt: %s\n".formatted(user.getTime()));
            sendMessage(barber.getChatId(), sendMsgToBarber);
            SendContact contact = new SendContact();
            contact.setChatId(barber.getChatId());
            contact.setPhoneNumber(user.getPhone());
            contact.setFirstName(user.getNickname());
            bot.execute(contact);
        } catch (Exception e) {
            log.error(e);
        }
        startCommand(user);
    }

    private void editLang(User user, String text) {
        String[] a = {"\uD83C\uDDFA\uD83C\uDDFF O'zbek tili", "\uD83C\uDDF7\uD83C\uDDFA русский язык"};
        if (text.equals(a[0]))
            user.setLang("uz");
        else if (text.equals(a[1]))
            user.setLang("ru");
        else {
            errorCommand(user, kyb.chooseLang());
            return;
        }
        userService.save(user);
        sendMessage(user.getChatId(), msg.successLang(user.getLang()));
        startCommand(user);
    }

    private void chooseLang(User user, String text) {
        if (text.equals("\uD83C\uDDFA\uD83C\uDDFF O'zbek tili")) {
            user.setLang("uz");
        } else if (text.equals("\uD83C\uDDF7\uD83C\uDDFA русский язык")) {
            user.setLang("ru");
        } else {
            errorCommand(user);
            return;
        }
        bot.sendMessage(user.getChatId(), msg.successLang(user.getLang()));
        userService.save(user);
        startCommand(user);
    }

    private void requestContact(User user, Contact contact) {
        user.setPhone(contact.getPhoneNumber().charAt(0) != '+' ? ("+" + contact.getPhoneNumber()) : contact.getPhoneNumber());
        userService.save(user);
        bot.sendMessage(user.getChatId(), msg.successPhone(user.getLang(), user.getPhone()));
        startCommand(user);
    }

    private void errorCommand(User user) {
        bot.sendMessage(user.getChatId(), msg.errorCommand(user.getLang()));
    }

    private void errorCommand(User user, ReplyKeyboardMarkup markup) {
        bot.sendMessage(user.getChatId(), msg.errorCommand(user.getLang()), markup);
    }

    private void startCommand(User user) {
        if (user.getLang() == null) {
            String[] a = {"\uD83C\uDDFA\uD83C\uDDFF O'zbek tili", "\uD83C\uDDF7\uD83C\uDDFA русский язык"};
            sendMessage(user.getChatId(), msg.chooseLang(), kyb.setKeyboards(a, 1));
            eventCode(user, "choose lang");
        } else {
            String lang = user.getLang(), role = user.getRole(),
                    phone = user.getPhone(),
                    nickname = user.getNickname();
            Long chatId = user.getChatId();
            if (phone == null) {
                sendMessage(chatId,
                        lang.equals("uz") ?
                                "\uD83D\uDC4B Assalomu aleykum, " + nickname + ". Botdan foydalanishingiz uchun ro'yxatdan o'ting" :
                                "\uD83D\uDC4B  Здравствуйте, " + nickname + ". Зарегистрируйтесь, чтобы использовать бот",
                        kyb.requestContact(user.getLang())
                );
                eventCode(user, "request contact");
            } else {
                if (role.equals("barber")) {
                    sendMessage(chatId, "\uD83D\uDD1D Asosiy Menyudasiz", kyb.barberMenu());
                } else {
                    sendMessage(chatId, lang.equals("uz") ? "Quyidagi sartaroshlardan birini tanlang" : "«Выберите мастера »", kyb.userMenu(user.getLang()));
                }
                eventCode(user, "menu");
            }
        }
    }

    public void sendMessage(long chatId, String text) {
        bot.sendMessage(chatId, text);
    }

    public void sendMessage(long chatId, String text, ReplyKeyboardMarkup markup) {
        bot.sendMessage(chatId, text, markup);
    }

    private void eventCode(User user, String text) {
        bot.eventCode(user, text);
    }
}
