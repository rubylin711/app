package com.prime.datastructure.ticker.CNS;

import android.graphics.Color;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CNSTickerData {
    public static final String TAG = "CNSTickerData";

    public static String test_data = "0322,0344,0002,0560,0561,0005,0006,0007,0008,0009,0010,0011,0012,0013,0014,0015,0016,0017,0204,0019,0020,0021,00"
            +
            "22,0023,0024,0025,0026,0027,0028,0029,0030,0031,0032,0033,0034,0035,0036,0037,0038,0039,0040,0041,0042,0043,0044"
            +
            ",0047,0048,0049,0084,0045,0050,0051,0052,0053,0054,0055,0056,0057,0058,0059,0060\n" +
            "0000-2355/2 #E36C09#FFFF0000 32 1 3 3 1 000 0560,0561,0005,0006,0007,0008,0009,0010 na\n" +
            "[#E36C09] SO01第一則2024台南龍崎光節空山祭，點亮龍崎區虎形山公園，以《龍火餘燼》為主題，17件作品打造最美的山林燈" +
            "節，展期至2/18，歡迎來欣賞精彩萬分的視覺饗宴。 [/]\n" +
            "0001-2355/2 #ffff00#FFFF0000 32 1 3 1 1 000 * na\n" +
            "[#ffff00] SO01第二則2024台南龍崎光節空山祭，點亮龍崎區虎形山公園，以《龍火餘燼》為主題，17件作品打造最美的山林燈節，" +
            "展期至2/18，歡迎來欣賞精彩萬分的視覺饗宴。 [/]";

    /*
     * o zipcode 前2 位為SO 代碼，不足兩碼首位補0，例如SO 9為09
     * o zipcode 後3 位為區域碼
     */
    private List<String> channelList = new ArrayList<>();// 作用頻道(4位數字) , ex: 0002, 0219 ...

    private long lastPlayTimeMillis = 0;
    private int startTime; // 有效時間 ,ex : 0100
    private int endTime; // 有效時間 ,ex : 1359
    private int period; // 顯示間隔時間(分) , 如果數字為5,則5分鐘顯示timesOfPeriod次
    private int timesOfPeriod; // 顯示次數
    private String fontColor; // 文字顏色
    private String bgColor; // 文字背景顏色
    private String fontSize; // 字體大小
    private List<TextData> texts = new ArrayList<>(); // The actual ticker message

    /*
     * •0=>靜止
     * •1=>最慢(1 pixel per movement, 25 pixels/sec)
     * •2=>慢(2 pixel per movement, 50 pixels/sec)
     * •3=>一般(3 pixel per movement, 75 pixels/sec)
     * •4=>快(4 pixel per movement, 100 pixels/sec)
     * •5=>最快(5 pixel per movement, 125 pixels/sec)
     */
    private int scrollSpeed = 0; // 捲動速度

    /*
     * •1=> (螢幕上方 由右到左)
     * •2=> (螢幕上方 由左到右)
     * •3 => (螢幕左方 由下到上)
     * •4 => (螢幕右方 由下到上)
     */
    private int displayLocation = 1; // 顯示位置

    private int messageSegment; // 訊息分段數量

    private String region; // 節點(區域碼)

    private List<String> actionOfChannels; // 運行(限制)頻道 : 頻道數字前有"^"符號如"^0219"，為不顯示的頻道，若只有"*"符號為所有頻道

    private String logoPath; // 2 顯示SO Logo 圖檔與否 : 若無圖檔時，填入na 字串 ，如果有則顯示圖檔名稱(如:resources/logo.png)

    private int logoX; // logo的x座標

    private int logoY; // logo的Y座標

    public long getLastPlayTimeMillis() {
        return lastPlayTimeMillis;
    }

    public void setLastPlayTimeMillis(long lastPlayTimeMillis) {
        this.lastPlayTimeMillis = lastPlayTimeMillis;
    }

    public static class TextData {
        public String text;
        public String fontColor;
        public int fontRealColor;
        public float width; // Cached width for horizontal mode
        public float height; // Cached height for vertical mode
        public char[] charArray; // Cached char array to avoid String allocations
        public android.graphics.Bitmap bitmap; // Cached bitmap for faster drawing
    }

    public List<TextData> getTexts() {
        return texts;
    }

    public void setTexts(List<TextData> texts) {
        this.texts = texts;
    }

    public int getRealScrollSpeed() {
        return scrollSpeed;
    }

    public int getScrollSpeed() {
        switch (scrollSpeed) {
            // case 0 -> 0;
            case 1:
                return 25;
            case 2:
                return 50;
            case 3:
                return 75;
            case 4:
                return 100;
            case 5:
                return 125;
            default:
                return 0;
        }
    }

    public void setScrollSpeed(int speed) {
        this.scrollSpeed = speed;
    }

    public static boolean data_assert(String value) {
        return value == null || value.isEmpty();
    }

    public static List<CNSTickerData> parseData(String data) {
        List<CNSTickerData> tickerDataList = new ArrayList<>();

        // 1. 根據換行符分割資料，得到不同的行
        String[] lines = data.split("\n");
        int line_index = 0;
        if (lines.length > 1) {
            // 2. 處理第一行：頻道列表
            String channelLine = lines[0].trim();
            String[] channels = channelLine.split(",");
            // 移除每個頻道編號可能存在的空白或 \r
            for (int i = 0; i < channels.length; i++) {
                channels[i] = channels[i].trim();
            }

            line_index++;
            while (line_index < lines.length) {
                String configLine = lines[line_index].trim();
                if (configLine.isEmpty()) {
                    line_index++;
                    continue;
                }

                CNSTickerData tickerData = new CNSTickerData();
                tickerData.setChannelList(new ArrayList<>(Arrays.asList(channels)));

                // 使用正則表達式分割空白，處理多個空白或 \r
                String[] configs = configLine.split("\\s+");
                if (configs.length < 10) {
                    line_index += 2; // 跳過這組資料
                    continue;
                }

                // 時間
                String[] timeConfig = configs[0].split("/");
                if (timeConfig.length == 2) {
                    String[] time = timeConfig[0].split("-");
                    if (time.length == 2) {
                        tickerData.setStartTime(Integer.parseInt(time[0].trim()));
                        tickerData.setEndTime(Integer.parseInt(time[1].trim()));
                    }
                    tickerData.setPeriod(Integer.parseInt(timeConfig[1].trim()));
                }

                tickerData.setFontColor("#" + configs[1].replace("#", "").substring(0, 6));
                // 背景色處理邏輯保持原樣或稍微優化
                String[] colorParts = configs[1].split("#");
                if (colorParts.length >= 3) {
                    tickerData.setFontColor("#" + colorParts[1]);
                    tickerData.setBgColor("#" + colorParts[2]);
                }

                tickerData.setFontSize(configs[2]);
                tickerData.setTimesOfPeriod(Integer.parseInt(configs[3]));
                tickerData.setScrollSpeed(Integer.parseInt(configs[4]));
                tickerData.setDisplayLocation(Integer.parseInt(configs[5]));
                tickerData.setMessageSegment(Integer.parseInt(configs[6]));
                tickerData.setRegion(configs[7]);

                String[] actionConfig = configs[8].split(",");
                for (int i = 0; i < actionConfig.length; i++) {
                    actionConfig[i] = actionConfig[i].trim();
                }
                tickerData.setActionOfChannels(new ArrayList<>(Arrays.asList(actionConfig)));

                if (configs[9].equalsIgnoreCase("na"))
                    tickerData.setLogoPath(null);
                else
                    tickerData.setLogoPath(configs[9]);

                if (line_index + 1 < lines.length) {
                    String messageLine = lines[line_index + 1].trim();
                    String regex = "\\[(#\\w{6})\\](.*?)\\[/\\]";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(messageLine);

                    while (matcher.find()) {
                        TextData textData = new TextData();
                        textData.fontColor = matcher.group(1);
                        textData.fontRealColor = Color.parseColor(textData.fontColor);
                        textData.text = matcher.group(2);
                        tickerData.getTexts().add(textData);
                    }
                }

                Log.d(TAG, "ticker data parsed: " + tickerData.ToString());
                tickerDataList.add(tickerData);
                line_index += 2;
            }
        }
        return tickerDataList;
    }

    public List<String> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<String> channelList) {
        this.channelList = channelList;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getTimesOfPeriod() {
        return timesOfPeriod;
    }

    public void setTimesOfPeriod(int timesOfPeriod) {
        this.timesOfPeriod = timesOfPeriod;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public int getDisplayLocation() {
        return displayLocation;
    }

    public void setDisplayLocation(int displayLocation) {
        this.displayLocation = displayLocation;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getActionOfChannels() {
        return actionOfChannels;
    }

    public void setActionOfChannels(List<String> actionOfChannels) {
        this.actionOfChannels = actionOfChannels;
    }

    public int getMessageSegment() {
        return messageSegment;
    }

    public void setMessageSegment(int messageSegment) {
        this.messageSegment = messageSegment;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public boolean isActiveAt(long now) {
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        String formattedTime = sdf.format(date);
        int now_time = Integer.parseInt(formattedTime);

        return (now_time >= startTime && now_time <= endTime) && (now >= (lastPlayTimeMillis + period * 60 * 1000L));
    }

    /**
     * 判斷當前頻道是否匹配此 Ticker
     * 
     * @param displayNumber 當前頻道的顯示編號 (例如 "2", "19")
     * @return true 如果匹配，false 則否
     */
    public boolean matchesChannel(String displayNumber) {
        if (displayNumber == null || displayNumber.isEmpty()) {
            return false;
        }

        // 1. 標準化頻道編號為 4 位數字 (例如 "2" -> "0002")
        String formatted;
        try {
            int val = Integer.parseInt(displayNumber);
            formatted = String.format("%04d", val);
        } catch (Exception e) {
            formatted = displayNumber;
        }

        // 2. 檢查是否在 Session 的頻道清單 (channelList) 中
        if (channelList == null || !channelList.contains(formatted)) {
            return false;
        }

        // 3. 檢查 actionOfChannels 邏輯
        if (actionOfChannels == null || actionOfChannels.isEmpty()) {
            return false;
        }

        // 如果包含 "*"，代表適用於 channelList 中的所有頻道
        if (actionOfChannels.contains("*")) {
            return true;
        }

        // 檢查是否有明確排除此頻道 (例如 "^0219")
        if (actionOfChannels.contains("^" + formatted)) {
            return false;
        }

        // 檢查是否有任何排除項。如果有排除項且當前頻道未被排除，則視為允許。
        for (String action : actionOfChannels) {
            if (action != null && action.startsWith("^")) {
                return true;
            }
        }

        // 若無排除項且無 "*"，則必須明確包含在清單中
        return actionOfChannels.contains(formatted);
    }

    public String ToString() {
        String str = "channelList = " + (channelList != null ? channelList.toString() : "null")
                + "\nstartTime = " + startTime
                + "\nendTime = " + endTime
                + "\nperiod = " + period
                + "\ntimesOfPeriod = " + timesOfPeriod
                + "\nfontColor = " + fontColor
                + "\nbgColor = " + bgColor
                + "\nfontSize = " + fontSize
                + "\nscrollSpeed = " + scrollSpeed
                + "\ndisplayLocation = " + displayLocation
                + "\nmessageSegment = " + messageSegment
                + "\nregion = " + region
                + "\nactionOfChannels = " + (actionOfChannels != null ? actionOfChannels.toString() : "null")
                + "\nlogoPath = " + logoPath;
        for (TextData textData : texts) {
            str += "\ntext = " + textData.text + "\nfontColor = " + textData.fontColor;
        }
        ;
        str += "\n";
        return str;
    }
}
