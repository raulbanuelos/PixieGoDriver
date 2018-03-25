package com.pixie.driver.model;

/**
 * Created by raulb on 08/11/2017.
 */

import java.util.Date;
import java.util.GregorianCalendar;
public class TimeCalculator {

    public double getDiff(Date dateInicial, Date dateFinal){
        double minutos = 0;
        GregorianCalendar dtInicial = new GregorianCalendar();
        GregorianCalendar dtFinal = new GregorianCalendar();

        dtInicial.set(dateInicial.getYear(),dateInicial.getMonth(),dateInicial.getDate(),dateInicial.getHours(),dateInicial.getMinutes());
        dtFinal.set(dateFinal.getYear(),dateFinal.getMonth(),dateFinal.getDate(),dateFinal.getHours(),dateFinal.getMinutes());

        long diff = dtFinal.getTime().getTime() - dtInicial.getTime().getTime();
        minutos = diff / (1000*60);

        return minutos;
    }
}
