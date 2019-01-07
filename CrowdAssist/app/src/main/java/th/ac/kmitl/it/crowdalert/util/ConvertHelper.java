package th.ac.kmitl.it.crowdalert.util;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ConvertHelper {
    public static String ConvertTimestampToDate(Long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy เวลา HH:mm น.", Locale.forLanguageTag("th-TH"));
        return formatter.format(date);
    }
    public static Double ConvertToRadius(int time){
        Double radius = 0.0;
        switch(time){
            case 1:
                radius = 0.5;
                break;
            case 2:
                radius = 1.0;
                break;
        }
        return radius;
    }

    public static String ConvertStatusToThai(String status){
        switch (status){
            case "wait":
                return "รอการตอบรับ";
            case "open":
                return "มีผู้ตอบรับ";
            case "close":
                return "ได้รับการช่วยเหลือแล้ว";
        }
        return "null";
    }
    public static String ConvertTypeToThai(String type){
        switch (type){
            case "type1":
                return "อัคคีภัย";
            case "type2":
                return "อุบัติเหตุทางถนน";
            case "type3":
                return "อุบัติเหตุทางน้ำ";
            case "type4":
                return "อื่นๆ";
        }
        return "อื่นๆ";
    }
}
