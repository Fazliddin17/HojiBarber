package com.company.BroBarber.telgramBot;

import com.company.BroBarber.db.domain.Barber;

public class Msg {
    protected static String chooseLang(){
        String msg = "\uD83C\uDDFA\uD83C\uDDFF Itimos, tilni tanlang\n\n";
        msg += "\uD83C\uDDF7\uD83C\uDDFA Пожалуйста, выберите язык";
        return  msg ;
    }
    protected static String aboutBarber(Barber barber){
        String msg = "" ;
        msg += "<i>Sartaroshning to'liq ismi:</i> <code>" + barber.getFullName()  + "</code>\n";

        msg += "telefon raqami: " + barber.getPhone() + "\n" ;
        msg += "Username: " + barber.getUsername() + "\n" ;
        msg += "narx: " + barber.getPrice() ;
        return msg ;
    }
}
