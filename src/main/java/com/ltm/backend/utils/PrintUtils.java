package com.ltm.backend.utils;

import com.ltm.MyUI;
import com.ltm.backend.controller.PackServiceImpl;
import com.ltm.backend.exception.UserException;
import com.ltm.backend.model.ConfigBean;
import com.ltm.backend.model.Parcel;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;

import java.util.Map;

public class PrintUtils {


    Logger LOGGER = Logger.getLogger(PrintUtils.class);

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");


    private  String serverName;
    private  String resourceName;
    private  String userId;
    private  String LblPrinter;
    private  String RptPrinter;
    private MyUI myUI;


    private final  String IDtype = "D";
    private final  String PrintAddLbl  = "Y";
    private final  String Labelcopies ="1";
    private final  String PrintCompliantLbl ="N";
    private final  String CLblPrinter =  "";
    private final  String Clabelcopies="1";
    private final  String PrintContentRpt = "Y";
    private final  String Rptcopies ="1";





    public  PrintUtils(String dropId, int num, int total, MyUI myUI, VaadinSession vaadinSession, String printContentRpt /* YM-180 Added printContentRpt flag*/) throws UserException, IOException{

        ConfigBean config = (ConfigBean) new ClassPathXmlApplicationContext("db/Spring-Module.xml").getBean("configBean");

        this.userId =  myUI.getCurrentSessionUtils().getUserId(vaadinSession);
        this.RptPrinter = myUI.getCurrentSessionUtils().getUserTable(vaadinSession).getDefaultReportPrinter();
        this.LblPrinter  = myUI.getCurrentSessionUtils().getUserTable(vaadinSession).getDefaulLabelPrinter();

        this.resourceName = config.getResourceName();
        this.serverName  = config.getServerName();

        this.myUI = myUI;

        ///// -==> 09/07/2019, Maxim Smurygin ROLL-BACK, START  /////
        // for roll-back purpose, show allways 1 из 1
        //
        num = 1;
        total = 1;
        ///// -==> 09/07/2019, Maxim Smurygin ROLL-BACK, END  /////
        print( dropId, num, total, printContentRpt);
    }


    public void  print(String dropId, int num, int total, String printContentRpt/* YM-180 Added printContentRpt flag*/) throws IOException {


        LOGGER.debug(":Печать начало:::: dropid "+ dropId );

        String ID = dropId;
        if ( LblPrinter ==  null || RptPrinter == null ) {
            this.myUI.showPopup("Ошбка печати","Не настроен принтер этикеток или отчетов в зоне, dropid не напечатан "+dropId, Notification.TYPE_ERROR_MESSAGE);
            LOGGER.error("Не настроен принтер этикеток или отчетов в зоне, dropid не напечатан "+dropId);
        }

        ConfigBean config = (ConfigBean) new ClassPathXmlApplicationContext("db/Spring-Module.xml").getBean("configBean");
        String apiPass = config.getApiPass();
        String apiUser = config.getApiUser();



        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(serverName + "/scprd/wmwebservice_rest/INFOR_SCPRD_wmwhse1/printer");

        String json = "{ \"dropId\": \""+dropId+"\", \"status\": false, \"labelcopies\": \"1\" , \"num\": \""+num+"\"  , \"total\": \""+total+"\" ,  \"rptPrinter\": \""+RptPrinter+"\", \"rptcopies\": \"1\", \"printCompliantLbl\": \"N\", \"clabelcopies\": \"1\", \"idtype\": \"D\", \"printContentRpt\": \""+printContentRpt+"\", \"printAddLbl\": \"Y\", \"lblPrinter\": \""+LblPrinter+"\"}";
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("tenant","INFOR");
        httpPost.setHeader("username",apiUser);
        httpPost.setHeader("password",apiPass);
        CloseableHttpResponse response = client.execute(httpPost);
        client.close();


        if (response.getStatusLine().getStatusCode() != 200){
            LOGGER.error("Ошибка печати "+ dropId );
            //throw new UserException("Ошибка печати ");

        }else{
            LOGGER.debug("Печать успешно!");
        }


    }


    private  Socket getSocket() throws IOException{
        SocketAddress address = new InetSocketAddress(this.serverName, 11123);
        Socket socket  = new Socket();
        socket.connect(address, 3000);
        LOGGER.debug("Connecting socket done!");
        return socket;
    }

    private String send(String msg) throws Exception {


        Socket socket = getSocket();

        boolean OK = true;
        String output = "";

        //Socket socket = null;
        OutputStreamWriter osw = null;
        BufferedInputStream bis = null;
        int read = -1;
        try {


            osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            osw.write(msg, 0, msg.length());


            InputStream is = socket.getInputStream();
            bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024 * 4];
            osw.flush();

            if ((read = bis.read(buffer)) > 0 ) {
                output = decodeUTF8(buffer);
                return output;

            }
        } catch (Exception e) {
            OK = false;
            socket.close();
        }finally {
            bis = null;

        }


        if (!OK) {
            //logger.error("[ERROR]:: CONNECTION ERROR ACCURED ::: END ");
            throw new Exception("Error connecting to server.");
        }


        return null;
    }


    private String decodeUTF8(byte[] bytes) {

        int index = 0;

        for (byte b : bytes) {
            if (b == 29) { // group separator
                bytes[index] = (byte) 124;
            }

            if (b == 31) { // unit separator
                bytes[index] = (byte) 126;
            }

            if (b == 3) { // end of text
                bytes[index] = (byte) 0;
            }
            if (b == 4) { // end of transmition
                bytes[index] = (byte) 0;
                //System.out.println("End of trasmition...");
            }
            if (b == 23) { // end of trans block
                bytes[index] = (byte) 0;
            }
            index++;
        }
        return new String(bytes, UTF8_CHARSET);
    }


    private SocketServerResponse sendData(String procName, String[] params) throws UserException{

        Map<String, String> attributes = new HashMap<String, String>();

        SocketServerResponse response = new SocketServerResponse();

        String msg = new SocketAPIMessage(this.resourceName, this.userId, this.serverName).forProcedure(procName).forParamsArray(params);
        LOGGER.error(msg);

        //-----------------------------------------------------------------------------
        //------------------            -----------------------------------------------
        String serverReply = null;
        try {
            serverReply = send(msg);
        } catch (Exception e) {
            throw new UserException(e.getMessage());
        }
        //------------------            -----------------------------------------------
        //-----------------------------------------------------------------------------

        String[] lines = serverReply.split("\r\n");

        for (String reply : lines) {
            String[] records = reply.split("~");

            if (records.length > 1) {
                for (String h : records) {

                    if (h.indexOf(BaseConstants.MESSAGE_HEADER) == -1) {
                        String[] value = h.split("\\|");
                    } else {
                        String tmp1 = h.replace("%s`", "");
                        String tmp2 = tmp1.replace("`%s", "");
                        String tmp3 = tmp2.replaceAll("\\u0000", "");
                        String[] systemParams = tmp3.split("`");
                        if (systemParams.length == 9) {
                            Proc procname = new Proc();
                            if (systemParams[0].startsWith("SocketStatus") && systemParams[0].contains("|")) {
                                String[] systemStatus = systemParams[0].split("\\|");
                                procname.setPtcid(systemStatus[1]);
                            }

                            procname.setUserid(systemParams[1].replace("|", ""));
                            procname.setTaskid(systemParams[2].replace("|", ""));
                            procname.setCompid(systemParams[3].replace("|", ""));
                            procname.setAppflag(systemParams[4].replace("|", ""));
                            procname.setRectype(systemParams[5].replace("|", ""));
                            procname.setServer(systemParams[6].replace("|", ""));
                            procname.setMiscmsg(systemParams[7].replace("|", ""));
                            procname.setRectotal(systemParams[8].replace("|", ""));
                            response.procName = procname;
                        }
                    }
                }
                // Saving results as flat string array
                response.setAttributeArrayProcValues(records);
            }else{
                if (records != null) {
                    try {

                        String tmp = records[0];
                        String tmp1 = tmp.replace("%s`", "");
                        String tmp2 = tmp1.replace("`%s", "");
                        String tmp3 = tmp2.replaceAll("\\u0000", "");
                        String[] tmpSplited = tmp3.split("`");

                        // if get error (retrec = 9) from server
                        if (tmpSplited.length == 3) {
                            response.rectype = Integer.valueOf(tmpSplited[1]);
                            response.miscmsg = tmpSplited[2];

                        } else {
                                Proc procname = new Proc();
                                procname.setPtcid(tmpSplited[0]);
                                procname.setUserid(tmpSplited[1]);
                                procname.setTaskid(tmpSplited[2]);
                                procname.setCompid(tmpSplited[3]);
                                procname.setAppflag(tmpSplited[4]);
                                procname.setRectype(tmpSplited[5]);
                                procname.setServer(tmpSplited[6]);
                                procname.setMiscmsg(tmpSplited[7]);
                                procname.setRectotal(tmpSplited[8]);
                                response.rectype = Integer.parseInt(procname.getRectype());
                                response.miscmsg = procname.getMiscmsg();

                                // if we have unnamed attributes in 11 version
                                if (tmpSplited.length > 9) {
                                    int j = 0;
                                    for (int i = 9; i < tmpSplited.length; i++) {
                                        attributes.put("" + (++j), tmpSplited[i]);
                                    }

                                    response.attr = attributes;
                                }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        return response;
    }


    public static void main(String[] args) throws IOException{
        String userId = "scetest";
        String rptPrinter = "PRN01";
        String lblPrinter = "PRN01";
        ConfigBean config = (ConfigBean) new ClassPathXmlApplicationContext("db/Spring-Module.xml").getBean("configBean");
        String apiPass = config.getApiPass();
        String apiUser = config.getApiUser();



        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://market-wms01.market.yandex.net/scprd/wmwebservice_rest/INFOR_SCPRD_wmwhse1/printer");

        String json = "{ \"dropId\": \"P000005391\", \"status\": false, \"labelcopies\": \"1\", \"rptPrinter\": \""+rptPrinter+"\", \"rptcopies\": \"1\", \"printCompliantLbl\": \"N\", \"clabelcopies\": \"1\", \"idtype\": \"D\", \"printContentRpt\": \"Y\", \"printAddLbl\": \"Y\", \"lblPrinter\": \""+lblPrinter+"\"}";
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("tenant","INFOR");
        httpPost.setHeader("username",apiUser);
        httpPost.setHeader("password",apiPass);
        CloseableHttpResponse response = client.execute(httpPost);
        client.close();

    }
}
