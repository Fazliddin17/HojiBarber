package com.company.BroBarber.telgramBot;


import com.company.BroBarber.db.domain.Barber;
import com.company.BroBarber.db.domain.Time;
import com.company.BroBarber.db.domain.User;
import com.company.BroBarber.db.repositories.LocationRepository;
import com.company.BroBarber.db.repositories.TimeRepository;
import com.company.BroBarber.dto.json.read.GetLocation;
import com.company.BroBarber.service.BarberService;
import com.company.BroBarber.service.UserService;
import com.company.BroBarber.telgramBot.users.UserKyb;
import com.company.BroBarber.telgramBot.users.UserMsg;
import com.company.BroBarber.telgramBot.users.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.*;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class MyBot extends TelegramLongPollingBot {
    @Autowired
    private LocationRepository locationRepository;
    protected static Kyb kyb = new Kyb();
    @Autowired
    private UserService userService;
    @Autowired
    private BarberService barberService;
    @Autowired
    private UserMsg userMsg;
    @Autowired
    private UserKyb userKyb;
    @Autowired
    private TimeRepository timeRepository;
    @Value("${bot.username}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
    @Value("${admin.chat.id}")
    private Long adminChatId;

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        long chatId;
        String username, nickname;
        int messageID;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            username = update.getMessage().getFrom().getUserName();
            nickname = update.getMessage().getFrom().getFirstName() + " " + (update.getMessage().getFrom().getLastName() == null ? "" : update.getMessage().getFrom().getLastName());
            messageID = update.getMessage().getMessageId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            username = update.getCallbackQuery().getFrom().getUserName();
            nickname = update.getCallbackQuery().getMessage().getFrom().getFirstName() + " " + (update.getCallbackQuery().getMessage().getFrom().getLastName() == null ? "" : update.getCallbackQuery().getMessage().getFrom().getLastName());
            messageID = update.getCallbackQuery().getMessage().getMessageId();
        } else return;
        for (Barber datum : barberService.findAll().getData()) {
            if (datum.getNowDay() == null) {
                datum.setNowDay(LocalDate.now());
                barberService.save(datum);
            }
            if (!datum.getNowDay().equals(LocalDate.now())) {
                datum.setNowDay(LocalDate.now());
                barberService.save(datum);
                timeRepository.deleteAllByBarberId(datum.getId());
            }
        }
        User user = userService.findByChatId(chatId).getData();
        if (user == null) {
            userService.save(User.builder()
                    .chatId(chatId)
                    .username(username)
                    .nickname(nickname)
                    .role(chatId == adminChatId ? "salom" : "user")
                    .eventCode("new user")
                    .active(true)
                    .day(LocalDate.now())
                    .build());

            user = userService.findByChatId(chatId).getData();
        }
        if (!nickname.equals(user.getNickname())) {
            user.setNickname(nickname);
            userService.save(user);
        }

        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                String eventCode = user.getEventCode();
                String role = user.getRole();
                if (role.equals("barber")) {
                    if (text.equals("/start")) {
                        sendMessage(chatId, "Asosiy menyudasiz", kyb.barberMenu());
                        user.setEventCode("menu");
                        userService.save(user);
                        return;
                    }
                    if (eventCode.equals("get image")) errorCommend(chatId, messageID);
                    if (eventCode.equals("menu")) {
                        String[] a = {
                                "\uD83C\uDFDE Rasmni o'zgartirish",
                                "\uD83D\uDCDE Telefon raqamni o'zgartirish",
                                "Narxni o'zgartirish",
                                "Ismni o'zgartirish",
                                "Bugungi bo'sh vaqtlarim",
                        };
                        if (inArray(a, text)) {
                            if (text.equals(a[0])) {
                                String[] b = {"✅ Ha", "❌ Yo'q"};
                                Barber barber = barberService.findByChatId(user.getChatId()).getData();
                                sendPhoto(
                                        chatId, barber.getImage(),
                                        "Ushbu rasmni haqiqatdan ham almashtirmoqchimisiz",
                                        kyb.setKeyboards(b, 2)
                                );
                                eventCode(user, "is edit image");
                            } else if (text.equals(a[1])) {
                                String[] b = {"✅ Ha", "❌ Yo'q"};
                                Barber barber = barberService.findByChatId(user.getChatId()).getData();
                                sendMessage(chatId, "Haqiqatdan ham telefon raqamingizni almashtirmoqchimisiz \n\nAvavlgi telefon raqamingiz: " + barber.getPhone(), messageID, kyb.setKeyboards(b, 2));
                                eventCode(user, "is edit phone");
                            } else if (text.equals(a[2])) {
                                String[] b = {"✅ Ha", "❌ Yo'q"};
                                Barber barber = barberService.findByChatId(user.getChatId()).getData();
                                sendMessage(chatId, "Haqiqatdan ham narxni o'zgartirmoqchimisiz \n\nAvavlgi narx: " + barber.getPrice(), messageID, kyb.setKeyboards(b, 2));
                                eventCode(user, "is edit price");
                            } else if (text.equals(a[3])) {
                                String[] b = {"✅ Ha", "❌ Yo'q"};
                                Barber barber = barberService.findByChatId(user.getChatId()).getData();
                                sendMessage(chatId, "Haqiqatdan ham ismingizni o'zgartirmoqchimisiz \n\nAvavlgi ismingiz: " + barber.getFullName(), messageID, kyb.setKeyboards(b, 2));
                                eventCode(user, "is edit full name");
                            } else if (text.equals(a[4])) {
                                Barber barber = barberService.findByChatId(chatId).getData();
                                List<String> list = new ArrayList<>();
                                for (Time time : barber.getTimes()) {
                                    list.add(time.getTime());
                                }
                                List<String> b = new ArrayList<>();
                                for (int i = 0; i < consts.times.length; i++) {
                                    b.add(consts.times[i]);
                                }
                                List<String> res = filter(b, barber.getTimes(), true);
                                res.add("\uD83D\uDD19 Orqaga qaytish");
                                sendMessage(chatId,
                                        "O'z vaqtingizni band qilish uchun quyidagilarni bosing",
                                        kyb.setKeyboards(res, 2)
                                );
                                eventCode(user, "choose time barber");
                            }
                        } else {
                            sendMessage(chatId, "Iltimos, tugmalardan foydalaning", messageID, kyb.barberMenu());
                        }
                    } else if (eventCode.equals("choose time barber")) {
                        if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
                            eventCode(user, "menu");
                            sendMessage(chatId, "Orqaga qaydingiz", kyb.barberMenu());
                        } else {
                            Barber barber = barberService.findByChatId(chatId).getData();
                            List<Time> times = barber.getTimes();
                            Time time = new Time();
                            time.setBarber(barber);
                            time.setTime(text);
                            times.add(time);
                            barber.setTimes(times);
                            barberService.save(barber);
                            user.setTime(text);
                            userService.save(user);
                            sendMessage(chatId, text + " vaqtni o'ziz band qidingiz", messageID, kyb.barberMenu());
                            eventCode(user, "menu");
                        }
                    } else if (eventCode.equals("is edit full name")) {
                        String[] b = {"✅ Ha", "❌ Yo'q"};
                        if (text.equals(b[0])) {
                            String[] s = {user.getNickname()};
                            sendMessage(chatId, "To'liq ismingizni kiriting", messageID, kyb.setKeyboards(s, 1));
                            eventCode(user, "get full name");
                        } else if (text.equals(b[1])) {
                            eventCode(user, "menu");
                            sendMessage(chatId, "Rasmni o'zgartirmadingiz", kyb.barberMenu());
                        } else errorCommend(chatId, messageID);
                    } else if (eventCode.equals("get full name")) {
                        Barber barber = barberService.findByChatId(chatId).getData();

                        sendMessage(chatId, "Ismingiz " + barber.getFullName() + "dan " + text + "ga muvaffaqiatli o'zgartirildi", kyb.barberMenu());
                        eventCode(user, "menu");
                        barber.setFullName(text);
                        barberService.save(barber);
                    } else if (eventCode.equals("is edit price")) {
                        String[] b = {"✅ Ha", "❌ Yo'q"};
                        if (b[0].equals(text)) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText(
                                    "Juda yaxshi endi narxni kiriting\n\nEslatib o'tamiz narxni faqat onda kiritishingiz kerak\nMisol uchun 50000 => 50 000 so'm"
                            );
                            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                            executes(sendMessage);
                            eventCode(user, "get price");
                        } else if (text.equals(b[1])) {
                            eventCode(user, "menu");
                            sendMessage(chatId, "Rasmni o'zgartirmadingiz", kyb.barberMenu());
                        } else {
                            sendMessage(chatId, "Iltimos, tugmalardan foydalaning", messageID, kyb.setKeyboards(b, 2));
                        }

                    } else if (eventCode.equals("get price")) {
                        try {
                            Barber barber = barberService.findByChatId(chatId).getData();
                            barber.setPrice(text);
                            barberService.save(barber);
                            sendMessage(chatId, "✅ Muvaffaqiyatli narxni o'zgrtirdingiz", kyb.barberMenu());
                            eventCode(user, "menu");
                        } catch (NumberFormatException e) {
                            errorCommend(chatId, messageID);
                        }
                    } else if (eventCode.equals("is edit phone")) {
                        String[] b = {"✅ Ha", "❌ Yo'q"};
                        if (b[0].equals(text)) {
                            String[] c = {user.getPhone()};
                            sendMessage(chatId, "Juda yaxshi, endi menga telefon raqamingi" +
                                    "zni yuboring", kyb.setKeyboards(c, 1));
                            eventCode(user, "get phone number");
                        } else if (text.equals(b[1])) {
                            eventCode(user, "menu");
                            sendMessage(chatId, "Rasmni o'zgartirmadingiz", kyb.barberMenu());
                        } else {
                            sendMessage(chatId, "Iltimos, tugmalardan foydalaning", messageID, kyb.setKeyboards(b, 2));
                        }
                    } else if (eventCode.equals("get phone number")) {
                        Barber barber = barberService.findByChatId(chatId).getData();
                        barber.setPhone(text);
                        barberService.save(barber);
                        sendMessage(chatId, "Telefon raqamingiz muvaffaqiyatli o'zgartirildi", kyb.barberMenu());
                        eventCode(user, "menu");
                    } else if (eventCode.equals("is edit image")) {
                        String[] b = {"✅ Ha", "❌ Yo'q"};
                        if (text.equals(b[0])) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setText("Juda yaxshi, endi menga biror bir rasm yuboring");
                            sendMessage.setChatId(chatId);
                            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                            sendMessage.setReplyToMessageId(messageID);
                            executes(sendMessage);
                            eventCode(user, "get image");
                        } else if (b[1].equals(text)) {
                            eventCode(user, "menu");
                            sendMessage(chatId, "Rasmni o'zgartirmadingiz", kyb.barberMenu());
                        } else {
                            sendMessage(chatId, "Iltimos, tugmalardan foydalaning", messageID, kyb.setKeyboards(b, 2));
                        }
                    }
                    return;
                }

            } else if (message.hasPhoto()) {
                if (user.getRole().equals("barber")) {
                    if (user.getEventCode().equals("get image")) {
                        PhotoSize photoSize = update.getMessage().getPhoto().get(0);
                        String fileId = photoSize.getFileId();
                        Barber barber = barberService.findByChatId(chatId).getData();
                        barber.setImage(fileId);
                        barberService.save(barber);
                        eventCode(user, "menu");
                        sendMessage(chatId, "✅ Rasmingiz muvaffaqiyatli o'zgartirildi", messageID, kyb.barberMenu());
                    }
                }
            }
        }

        if (user.getRole().equals("user")) {
            if (!user.getActive() && !user.getDay().equals(LocalDate.now())) {
                user.setDay(LocalDate.now());
                user.setActive(true);
                userService.save(user);
            }
            UserRole userRole = new UserRole(
                    this, userService, barberService, userMsg, userKyb, locationRepository, timeRepository
            );
            userRole.mainMenu(user, update);
            return;
        }


        String eventCode = user.getEventCode();
        boolean suc = false;
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (update.getMessage().hasText()) {
                suc = !message.getText().equals("/start");
            } else {
                suc = true;
            }
        }

        if (eventCode.equals("get reklama type") && chatId == (adminChatId) && suc) {
            long abs = userService.findAll().getData().size();
            int c = 0;
            for (User user1 : userService.findAll().getData()) {
                try {
                    copyMessage(chatId, user1.getChatId(), messageID);
                    c++;
                } catch (Exception e) {
                    log.error(e);
                }
            }
            sendMessage(chatId,
                    "Reklamangiz hammaga muvafaqiyatli yuborildi \n\n Barcha foydalanuvchilar soni: %d ta\nFaol foydalanuvchilarning soni: %d ta \n\nQuyidagilardan birini tanlang".formatted(abs, c),
                    kyb.superAdminMenu()
            );
            eventCode(user, "super admin menu");
        }
        List<Barber> barbers = barberService.findAll().getData();
        for (Barber barber : barbers) {
            if (!barber.getNowDay().equals(LocalDate.now())) {
                barber.setNowDay(LocalDate.now());
                barberService.save(barber);
            }
        }
        if (chatId == (adminChatId)) {
            if (update.hasMessage()) {
                if (update.getMessage().hasText()) {
                    String text = update.getMessage().getText();
                    if (text.equals("/start")) {
                        sendMessage(chatId,
                                "Quyidagilardan birini tanlang",
                                kyb.superAdminMenu()
                        );
                        eventCode(user, "super admin menu");
                    } else if (text.equals("/reklama")) {
                        sendMessage(chatId,
                                "Reklamani yuborish uchun quyidagilardan birini tanlang");
                        eventCode(user, "get reklama type");
                    } else {

                        if (eventCode.equals("get reklama type")) {

                        } else if (eventCode.equals("reklama matni")) {
                            user.setReklamaType("matn");
                            userService.save(user);
                            sendMessage(chatId, "Reklama qilmoqchi bo'lgan matnni yuboring", true);
                            eventCode(user, "get text");
                        } else if (eventCode.equals("get text")) {
                            user.setReklamaText(text);
                            userService.save(user);
                            sendMessage(chatId, "Ushbu reklamaga button qo'shilsinmi");
                            eventCode(user, "is kyb matn");
                        } else if (eventCode.equals("is kyb matn")) {
                            String[] a = {"✅ Ha", "❌ Yo'q"};
                            if (text.equals(a[0])) {
                                sendMessage(chatId, "Havolani yuboring agar havolangizning soni 1 tadan ko'p bo'lsa o'rtasiga 1 dona probel qo'ying va har doim boshhi http yoki https bilan boshlansin", true);
                                eventCode(user, "get url");
                            } else if (a[1].equals(text)) {
                                user.setReklamaUrl(text);
                                user.setReklamaUrl(null);
                                userService.save(user);
                                sendMessage(chatId, user.getReklamaText());
                                sendMessage(chatId, "Ushbu reklama hammaga yuborilsinmi", kyb.isKyb());
                                eventCode(user, "set reklama with url");
                            } else deleteMessage(chatId, messageID);
                        } else if (eventCode.equals("get url")) {
                            user.setReklamaUrl(text);
                            userService.save(user);
                            sendMessage(chatId, user.getReklamaText(), kyb.setUrl(user.getReklamaUrl().split(" ")));
                            sendMessage(chatId, "Ushbu reklama hammaga yuborilsinmi", kyb.isKyb());
                            eventCode(user, "set reklama with url");
                        } else if (eventCode.equals("set reklama with url")) {
                            if (text.equals("✅ Ha")) {
                                boolean success = true;
                                if (user.getReklamaUrl() == null) success = false;
                                for (User user1 : userService.findAll().getData()) {
                                    try {
                                        sendMessage(user1.getChatId(), user.getReklamaText(), success ? kyb.setUrl(user.getReklamaUrl().split(" ")) : null);
                                    } catch (Exception e) {
                                        sendMessage(chatId, "<a href=\"tg://user?id=" + user1.getChatId() + "\">" + user1.getNickname() + "</a> ga xabar yuborilmadi.\n\nXabar yuborilmasligining sababi: <code>" + e.getMessage() + "</code>");
                                    }
                                }
                                sendMessage(chatId,
                                        "Reklama hammaga muvaffaqiyatli yuborildi\n\nQuyidagilardan birini tanlang",
                                        kyb.superAdminMenu()
                                );
                                eventCode(user, "super admin menu");
                            } else if (text.equals("❌ Yo'q")) {

                            } else deleteMessage(chatId, messageID);
                        } else if (
                                eventCode.equals("super admin menu") ||
                                        eventCode.equals("barber lists")
                        ) {

                            if (text.equals("Sartarosh qo'shish")) {
                                SendMessage sendMessage = new SendMessage();
                                sendMessage.setText("Sartaroshning to'liq ismini kiriting");
                                sendMessage.setChatId(chatId);
                                sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                                executes(sendMessage);
                                eventCode(user, "get full name");
                            } else if (text.equals("\uD83D\uDCCD Manzilni o'zgartirish")) {

                                Optional<com.company.BroBarber.db.domain.Location> location = locationRepository.findById(1L);
                                if (location.isEmpty()) {
                                    sendMessage(chatId, "Manzil yo'q");
                                    sendMessage(chatId, "Manzilni o'zgartirish uchun lokatsiyani yuboring", kyb.setLocation());
                                    eventCode(user, "get location");
                                    return;
                                }
                                SendVenue sendVenue = new SendVenue();
                                sendVenue.setChatId(chatId);
                                sendVenue.setLatitude(location.get().getLat());
                                sendVenue.setLongitude(location.get().getLon());
                                sendVenue.setTitle(location.get().getTitle());
                                sendVenue.setAddress(location.get().getAddress());
                                execute(sendVenue);
                                sendMessage(chatId, "Manzilni o'zgartirish uchun lokatsiyani yuboring", kyb.setLocation());
                                eventCode(user, "get location");
                            } else if (text.equals("\uD83D\uDCDC sartaroshlarning ro'yxati")) {
                                barbers = barberService.findAll().getData();
                                if (barbers.isEmpty()) {
                                    sendMessage(chatId, "Sartaroshlar hozircha yo'q", messageID);
                                    eventCode(user, "barber lists");
                                    return;
                                }
                                sendMessage(chatId, "Sartaroshlarning ro'yxati", kyb.userMenu(barbers));
                                eventCode(user, "barber lists");
                            } else if (text.equals("Reklama qilish")) {
                                sendMessage(chatId,
                                        "Reklamangizni yuboring", true);
                                eventCode(user, "get reklama type");
                            } else {
                                deleteMessage(chatId, messageID);
                                return;
                            }
                        } else if (eventCode.equals("get full name")) {
                            boolean successfully;
                            successfully = barberService.getdraftBarber().getData() == null;
                            Barber barber = new Barber();

                            barber.setFullName(text);
                            barber.setSuccess(false);
                            barber.setLevel(3L);
                            if (!successfully)
                                barber.setId(barberService.getdraftBarber().getData().getId());
                            barberService.save(barber);
                            sendMessage(chatId, "Telefon raqamini kiriting");
                            eventCode(user, "get phone");
                        } else if (eventCode.equals("get phone")) {
                            Barber barber = barberService.getdraftBarber().getData();
                            barber.setPhone(text);
                            barberService.save(barber);
                            sendMessage(chatId, "Sartaroshning usernamesini kiriting");
                            eventCode(user, "get username");
                        } else if (eventCode.equals("get username")) {
                            Barber barber = barberService.getdraftBarber().getData();
                            barber.setUsername(text);
                            barberService.save(barber);
                            sendMessage(chatId, "Sartaroshning soch olish narxini kiriting");
                            eventCode(user, "get price");
                        } else if (eventCode.equals("get price")) {
                            try {
                                Barber barber = barberService.getdraftBarber().getData();
                                barber.setPrice(text);
                                barberService.save(barber);
                                sendMessage(chatId, "Sartasrosh chat id sini kiriting");
                                eventCode(user, "get chat id");
                            } catch (NumberFormatException e) {
                                sendMessage(chatId, "Sartaroshning soch olish narxini sonda kiriting");
                            }
                        } else if (eventCode.equals("get chat id")) {
                            try {
                                Barber barber = barberService.getdraftBarber().getData();
                                Long barberChatId = Long.valueOf(text);
                                barber.setChatId(barberChatId);
                                barberService.save(barber);
                                sendMessage(chatId, "Sartaroshning rasmini kiriting");
                                eventCode(user, "get image");
                            } catch (Exception e) {
                                sendMessage(chatId, "Sartaroshning soch olish narxini sonda kiriting");
                            }
                        } else if (eventCode.equals("get image")) {
                            deleteMessage(chatId, messageID);
                        } else if (eventCode.equals("get title")) {
                            com.company.BroBarber.db.domain.Location l = locationRepository.findById(1L).get();
                            l.setTitle(text);
                            locationRepository.save(l);
                            sendMessage(
                                    chatId,
                                    "Lokatsiya o'zgartirildi\n\nquyidagilardan birini tanlang"
                                    , kyb.superAdminMenu()
                            );
                            eventCode(user, "super admin menu");
                        } else if (eventCode.equals("get address")) {
                            com.company.BroBarber.db.domain.Location l = locationRepository.findById(1L).get();
                            l.setAddress(text);
                            locationRepository.save(l);
                        }
                    }
                } else if (update.getMessage().hasPhoto()) {
                    String fileId = update.getMessage().getPhoto().get(0).getFileId();
                    Barber barber = barberService.getdraftBarber().getData();
                    barber.setImage(fileId);
                    barberService.save(barber);
                    sendPhoto(chatId, fileId, Msg.aboutBarber(barber) + "\n\nQo'shish kerak bo'lsa ha tugmasini bosing aks holda yo'q tugmasini",
                            kyb.isAdd());
                    eventCode(user, "add barber");
                } else if (update.getMessage().hasLocation()) {
                    Location location = update.getMessage().getLocation();
                    Optional<com.company.BroBarber.db.domain.Location> l1 = locationRepository.findById(1L);
                    com.company.BroBarber.db.domain.Location l;
                    if (l1.isEmpty()) {
                        l = new com.company.BroBarber.db.domain.Location();
                        l.setLat(location.getLatitude());
                        l.setLon(location.getLongitude());
                    } else {
                        l = l1.get();
                        l.setLat(location.getLatitude());
                        l.setLon(location.getLongitude());
                    }
                    l.setAddress(GetLocation.getLocation(l.getLat(), l.getLon()).getDisplay_name());
                    locationRepository.save(l);
                    sendMessage(chatId, "<i>Siz kiritgan manzil:</i> <b>" + GetLocation.getLocation(l.getLat(), l.getLon()).getDisplay_name() + "</b>");
                    sendMessage(chatId, "Sartarosh nomini kiriting");
                    eventCode(user, "get title");
                }
            } else if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                if (eventCode.equals("barber lists")) {
                    if (data.equals("delete")) {
                        this.editMessageText(chatId, "Ushbu sartarosh o'chrilsinmi", messageID, kyb.isDelete());
                    } else if (data.equals("back")) {
                        editMessageText(chatId, "Sartaroshlar ro'yxati", messageID, kyb.userMenu(barbers));
                        eventCode(user, "barber lists");
                    } else if (data.equals("yes delete")) {
                        Barber barber = barberService.findById(user.getHelper()).getData();
                        for (User datum : userService.findAll().getData()) {
                            if (datum.getChatId().equals(barber.getChatId())) {
                                datum.setRole("user");
                                userService.save(datum);
                            }
                        }
                        sendMessage(chatId, "O'chirilmoqda");
                        barberService.deleteById(barber.getId());
                        barbers = barberService.findAll().getData();
                        if (barbers.isEmpty()) {
                            editMessageText(chatId, "Sartaroshlar hozircha yo'q", messageID);
                            return;
                        }
                        editMessageText(chatId, "O'chirildi\n\n\nSartaroshlarning ro'yxati", messageID, kyb.userMenu(barbers));
                        eventCode(user, "barber lists");
                    } else if (data.equals("no delete")) {
                        editMessageText(chatId, "O'chirilmadi\n\n\nSartaroshlarning ro'yxati", messageID, kyb.userMenu(barbers));
                        eventCode(user, "barber lists");
                    } else {
                        Long id = Long.valueOf(data);
                        user.setHelper(id);
                        userService.save(user);
                        editMessageText(chatId, Msg.aboutBarber(
                                        barberService.findById(id).getData()),
                                messageID, kyb.crudBarbers()
                        );
                    }
                } else if (eventCode.equals("add barber")) {
                    if (data.equals("yes add")) {
                        Barber barber = barberService.getdraftBarber().getData();
                        barber.setLevel(0L);
                        barber.setSuccess(true);
                        barber.setNowDay(LocalDate.now());
                        barber.setTimes(new ArrayList<>());
//                            Time time = new Time();
//                            time.se
                        barberService.save(barber);
                        for (User u : userService.findAll().getData()) {
                            if (u.getChatId().equals(barber.getChatId())) {
                                u.setRole("barber");
                                userService.save(u);
                            }
                        }
                        for (User item : userService.findAll().getData()) {
                            if (item.getChatId().equals(barber.getChatId())) {
                                item.setRole("barber");
                            }
                        }
                        sendMessage(chatId, "Muvaffaqiyatli qo'shildi");
                        sendMessage(chatId,
                                "Quyidagilardan birini tanlang",
                                kyb.superAdminMenu()
                        );
                        eventCode(user, "super admin menu");

                    } else if (data.equals("no add")) {
                        sendMessage(chatId, "Ushbu sartarosh qo'shilmadi");

                        sendMessage(chatId,
                                "Quyidagilardan birini tanlang",
                                kyb.superAdminMenu()
                        );
                        eventCode(user, "super admin menu");
                    }
                }
            }
            return;
        }
    }


    public void eventCode(User user, String text) {
        user.setEventCode(text);
        userService.save(user);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void executes(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void executes(CopyMessage copyMessage) {
        try {
            execute(copyMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void executes(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPhoto(long chatId, String photo, String caption, ReplyKeyboardMarkup markup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(photo));
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(caption);
        sendPhoto.setReplyMarkup(markup);
        sendPhoto.setParseMode("html");
        sendPhoto.setProtectContent(true);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    public void sendPhoto(long chatId, String photo, String caption, InlineKeyboardMarkup markup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(photo));
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(caption);
        sendPhoto.setReplyMarkup(markup);
        sendPhoto.setParseMode("html");
        sendPhoto.setProtectContent(true);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }


    public void sendMessage(Long chat_id, String text, Integer message_id, InlineKeyboardMarkup inlineKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat_id);
            sendMessage.setText(text);
            sendMessage.enableHtml(true);
            sendMessage.setDisableWebPagePreview(true);
            sendMessage.setReplyToMessageId(message_id);
            if (inlineKeyboardMarkup != null) sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendMessage(Long chat_id, String text, Integer message_id, ReplyKeyboardMarkup replyKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat_id);
            sendMessage.setText(text);
            sendMessage.enableHtml(true);
            sendMessage.setDisableWebPagePreview(true);
            sendMessage.setReplyToMessageId(message_id);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendMessage(Long chat_id, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat_id);
            sendMessage.setText(text);
            sendMessage.enableHtml(true);
            sendMessage.setDisableWebPagePreview(true);
            if (inlineKeyboardMarkup != null) sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendMessage(Long chat_id, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        executes(sendMessage);

    }


    public void sendMessage(Long chat_id, String text, Integer message_id) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setReplyToMessageId(message_id);
        executes(sendMessage);
    }


    public void sendMessage(Long chat_id, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        executes(sendMessage);
    }

    public void sendMessage(Long chat_id, String text, boolean isRemoveKyb) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(isRemoveKyb));
        executes(sendMessage);
    }


    //DELETE MESSAGE


//    COPY MESSAGE

    public void copyMessage(Long fromChatId, Long chatId, Integer message_id) {
        CopyMessage copyMessage = new CopyMessage();
        copyMessage.setFromChatId(fromChatId);
        copyMessage.setChatId(chatId);
        copyMessage.setMessageId(message_id);
        executes(copyMessage);
    }


    //////////////////////////////////////////////////////////////////////////////////


/////////////////EDIT MESSAGE TEXT

    public void editMessageText(Long chat_id, String text, Integer message_id, InlineKeyboardMarkup markup) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chat_id);
        editMessageText.setParseMode("html");
        editMessageText.setText(text);
        editMessageText.setMessageId(message_id);
        editMessageText.setReplyMarkup(markup);
        executes(editMessageText);
    }


    public void editMessageText(Long chat_id, String text, Integer message_id) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chat_id);
        editMessageText.setParseMode("html");
        editMessageText.setText(text);
        editMessageText.setMessageId(message_id);
        executes(editMessageText);
    }

    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage message = new DeleteMessage();
        message.setMessageId(messageId);
        message.setChatId(chatId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    public void errorCommend(Long chatId, Integer messageID) {
        deleteMessage(chatId, messageID);
    }

    public List<String> filter(List<String> times, List<Time> barberTimes, boolean isList) {
        List<String> a = new ArrayList<>();
        for (Time time : barberTimes) {
            a.add(time.getTime());
        }
        times.removeAll(a);
        return times;
    }

    public boolean inArray(String[] a, String text) {
        boolean success = false;
        for (String s : a) {
            if (s.equals(text)) success = true;
        }
        return success;
    }

    public boolean inArray(List<String> a, String text) {
        boolean success = false;
        for (String s : a) {
            if (s.equals(text)) success = true;
        }

        return success;
    }

}
