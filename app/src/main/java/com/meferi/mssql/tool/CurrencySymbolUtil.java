package com.meferi.mssql.tool;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrencySymbolUtil {

    private static final Map<String, String> symbolMap = new HashMap<>();

    static {
        symbolMap.put("USD", "$");        // 美元 - United States
        symbolMap.put("EUR", "€");        // 欧元 - Germany, France, etc.
        symbolMap.put("GBP", "£");        // 英镑 - United Kingdom
        symbolMap.put("JPY", "¥");        // 日元 - Japan
        symbolMap.put("CNY", "¥");        // 人民币 - China
        symbolMap.put("TWD", "NT$");      // 新台币 - Taiwan
        symbolMap.put("HKD", "HK$");      // 港币 - Hong Kong
        symbolMap.put("KRW", "₩");        // 韩元 - South Korea
        symbolMap.put("INR", "₹");        // 印度卢比 - India
        symbolMap.put("RUB", "₽");        // 卢布 - Russia
        symbolMap.put("BRL", "R$");       // 巴西雷亚尔 - Brazil
        symbolMap.put("AUD", "A$");       // 澳元 - Australia
        symbolMap.put("CAD", "C$");       // 加元 - Canada
        symbolMap.put("SGD", "S$");       // 新加坡元 - Singapore
        symbolMap.put("MYR", "RM");       // 马来西亚林吉特 - Malaysia
        symbolMap.put("THB", "฿");        // 泰铢 - Thailand
        symbolMap.put("IDR", "Rp");       // 印尼盾 - Indonesia
        symbolMap.put("ZAR", "R");        // 南非兰特 - South Africa
        symbolMap.put("MXN", "Mex$");     // 墨西哥比索 - Mexico
        symbolMap.put("PLN", "zł");       // 波兰兹罗提 - Poland
        symbolMap.put("TRY", "₺");        // 土耳其里拉 - Turkey
        symbolMap.put("CHF", "CHF");      // 瑞士法郎 - Switzerland
        symbolMap.put("SEK", "kr");       // 瑞典克朗 - Sweden
        symbolMap.put("NOK", "kr");       // 挪威克朗 - Norway
        symbolMap.put("DKK", "kr");       // 丹麦克朗 - Denmark
        symbolMap.put("CZK", "Kč");       // 捷克克朗 - Czech Republic
        symbolMap.put("HUF", "Ft");       // 匈牙利福林 - Hungary
        symbolMap.put("PHP", "₱");        // 菲律宾比索 - Philippines
        symbolMap.put("ILS", "₪");        // 以色列新谢克尔 - Israel
        symbolMap.put("SAR", "﷼");        // 沙特里亚尔 - Saudi Arabia
        symbolMap.put("AED", "د.إ");      // 阿联酋迪拉姆 - UAE
        symbolMap.put("VND", "₫");        // 越南盾 - Vietnam
    }

    /**
     * 获取货币符号
     * @param currencyCode 货币代码，如 "USD", "CNY"
     * @return 符号，如 "$" 或 "¥"
     */
    public static String getSymbol(String currencyCode) {
        if (currencyCode == null) return "";
        String upper = currencyCode.toUpperCase(Locale.ROOT);
        return symbolMap.getOrDefault(upper, upper);  // 未匹配则返回原始代码
    }
}
