/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author rebecca
 */
public class TimeTranslator {
    public static void main(String[] args) {
        Date d = new Date(1453229931201l);
        DateFormat df = new SimpleDateFormat("\"yyyy.MM.dd 'at' HH:mm:ss z\"");
        System.out.println("Date is " + df.format(d));
    }
}
