package com.ltm.backend.utils;

import org.apache.log4j.Logger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Created by maxim on 02.08.17.
 */
public class SocketServerResponse {

    public  boolean BREAKE_SCREEN_FLOW = false;
    public int rectype;
    public Map<String,String> attr = null;
    public String miscmsg;
    public Proc procName = null;
    public boolean xmitdone = false;
    public boolean newScreen = false;
    public boolean storeInRfSession = false;
    public String[] attributeArray = null;




    private final static Logger logger = Logger.getLogger(SocketServerResponse.class);


    public boolean isOK(){
        return rectype == 1;
    }

    public double getAttribValue(String attrname, double type){

        if (attr != null) {
            try {
                double val = parseDouble(attr.get(attrname));
                return  val;
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return  0D;
    }
    public SocketServerResponse(int rectype, String miscmsg){
        this.setMiscMsg(miscmsg);
        this.rectype = rectype;
    }


    public SocketServerResponse(){
        this.rectype = 1;
    }

    public String getAttribValue(String attrname, String type){

        if (attr != null) {
               String tmp =  attr.get(attrname);
               return tmp != null ? tmp.trim() : "";
        }
        return  "";
    }

    public String getAttribValue(String attrname){

        if (attr != null) {
            String tmp =  attr.get(attrname);
            return tmp != null ? tmp.trim() : "";
        }
        return  "";
    }


    public boolean getAttribValAsBool(String attrname){

        String theResult = null;

        if (attr != null) {
            String tmp =  attr.get(attrname);
            theResult =  tmp != null ? tmp.trim() : "";
        }

        Boolean theBoolVal = false;

        theBoolVal = theResult.equalsIgnoreCase("1") ? true: false;

        return  theBoolVal;
    }

    private String parseDouble(double value){
        try {
            Locale locale = Locale.ENGLISH;
            NumberFormat format = NumberFormat.getInstance(locale);
            format.setGroupingUsed(false);
            format.setMaximumFractionDigits(3);
            format.setMinimumFractionDigits(3);
            String s = format.format(value);
            return  s;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    private double parseDouble(String value){
        try{

            double d = Double.parseDouble(value);
            String tmpVal = parseDouble(d);
            Locale locale = Locale.getDefault();
            NumberFormat format = NumberFormat.getInstance(locale);
            format.setGroupingUsed(false);
            format.setMaximumFractionDigits(3);
            format.setMinimumFractionDigits(3);
            Number convertedValue =  format.parse(tmpVal);

            return convertedValue.doubleValue();
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0D;
    }


    public Map<String, String> getMap(){
        return attr;
    }

    public void setRecType(int recType) {
        this.rectype = recType;
    }

    public void setMiscMsg(String miscMsg) {

       // Обработчик для триггеров
        if (this.miscmsg!= null && this.miscmsg.contains("Trigger on")){
            int index = this.miscmsg.indexOf("Trigger on");
            String subMessage = this.miscmsg.substring(index, this.miscmsg.length() );
            miscMsg = " "+ subMessage;
        }

        this.miscmsg = miscMsg;
    }





    @Override
    public String toString() {


        return "SocketServerResponse{" +
                "rectype=" + rectype +
                ", miscmsg='" +isNull (miscmsg )+ '\'' +
                ", attributes=" + isNull(attr) +
                ", storeInRfSession=" + storeInRfSession +
                '}';
    }

    private String isNull(String s ){
        return  s == null ? "" : s;
    }

    private String isNull(Object s ){
        return  s == null ? "" : s.toString();
    }




    public void setAttributeArrayProcValues(String[] otherValues){
        int idx =0;
        int otherValuesIdex = 0;
        this.attributeArray = new String[7 + otherValues.length ];


        try {
            this.attributeArray[idx++] = procName.getPtcid();
            this.attributeArray[idx++] = procName.getUserid();
            this.attributeArray[idx++] = procName.getTaskid();
            this.attributeArray[idx++] = procName.getCompid();
            this.attributeArray[idx++] = procName.getAppflag();
            this.attributeArray[idx++] = procName.getRectype();
            this.attributeArray[idx++] = procName.getServer();
            this.attributeArray[idx++] = procName.getMiscmsg();
            this.attributeArray[idx++] = procName.getRectotal();

            for (; idx < otherValues.length + 7; idx++) {
                String value = null;
                String[] values = otherValues[++otherValuesIdex].split("\\|");

                if (values.length == 2)
                    value = values[1];
                else
                    value = "";


                attributeArray[idx] = value;
            }
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }

    }

}
