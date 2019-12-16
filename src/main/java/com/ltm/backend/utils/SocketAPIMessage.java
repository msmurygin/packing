package com.ltm.backend.utils;
import com.vaadin.server.VaadinSession;

/**
 * Created by maxim on 30.10.17.
 */
public class SocketAPIMessage implements BaseConstants {

    private String usersWarehouse;
    private String userID;
    private String hostName;


    public String msgId;
    public String procName;
    public String message;
    public String data;


    private String xmit ;

    private  boolean useSocktet=false;



    public SocketAPIMessage(String usersWarehouse, String userID, String hostName){
        this.usersWarehouse = usersWarehouse;
        this.userID = userID;
        this.hostName = hostName;
    }



    public SocketAPIMessage forProcedure(String PROC){
        this.procName = procName(PROC);
        String appFlag = appFlag(PROC);
        xmit = buildXMITHeader(PROC, appFlag);

        return this;
    }



    private String generateId(){
        int rnd = (int) Math.floor(Math.random()*999999);
        String tmp = "00000" +rnd;
        String val = tmp.substring(tmp.length() - 6 , tmp.length());
        return  val;
    }


    private String buildXMITHeader(String PROC, String appFlag){

        if (appFlag == null)
            appFlag = PROC;

        int aLength  = appFlag.length();
        String appflag = "";
        String  rectype = "01";
        if (aLength >=4){
            appflag = appFlag.substring(0, aLength -2);
            rectype = appFlag.substring(aLength - 2);
        }else {
            appflag = appFlag;
        }

        this.msgId = generateId();
        String xmit = addDataParameters("1", userID, this.msgId, usersWarehouse, TENANT, appflag, rectype, hostName);
        return xmit;
    }

    private String  addDataParameters(String... arguments) {

        String  str = "";
        int idx = 0;
        String arg = "";
        int l = arguments.length;

        for ( ; idx < l; ++idx ) {
            arg = arguments[idx];
            if (arg instanceof  String ) {
                arg = arg.replace("<([a-z][a-z0-9]*)[^>]*>(.*?)", "" );
            }
            str = str + arg +  COMMA_DELIM; // TODO: WEBSOCKET CONNECTION NEXT RELEASE, build EXEC PARAMS // (useSocktet ? COMMA_DELIM: D);
        }

        return str;
    }

    private String appFlag(String PROC ) {
        String  appFlag = "";
        if (PROC.startsWith(PROC_PREFIX)) {
            appFlag = PROC.substring(PROC_PREFIX.length());
        } else {
            appFlag = PROC;
        }

        return appFlag;
    }

    private String procName(String PROC ) {
        String  theProc = "";
        if (!PROC.startsWith(PROC_PREFIX)) {
            theProc = PROC_PREFIX + PROC;
        }else {
            theProc  = PROC;
        }
        return theProc;
    }



    public String forParamsArray(String... theParams){
        String message = "";
        // TODO: WEBSOCKET CONNECTION NEXT RELEASE, build EXEC PARAMS
        message  = this.msgId +" "+  EXEC +  this.procName +" " +  this.xmit +  this.formatString(theParams);
        /*if (useSocktet){
            message  = this.msgId +" "+  EXEC +  this.procName +" " +  this.xmit +  this.formatString(theParams);
        }else{*/
            //message =  EXEC +  this.procName + DELIM +  this.xmit +  this.formatString(theParams);
        //}

        return message;

    }


    public  String nextDataResultSet(){
        String message = EXEC +  this.procName + DELIM +  this.xmit +  "1"+ EOM;
        return message;
    }

    public  String closeStream(){
        String message = EXEC + CLOSE_STREAM_PROC +DELIM+  this.xmit +  EOM;
        return message;
    }



    private String  formatString ( String... theParams){

        //EXEC + theProcName + DELIM + PTCID + D + userid1 + D +theMessage.msgId + D + usersWarehouse + D + MESSAGE_HEADER + "`03`" + hostName + D;

        String theResult ="";// theMessage.data;

        String[] var2 = theParams;

        int var3 = theParams.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String c;

            if (var2[var4] == null)
                c = "";
            else
                c = var2[var4];


            c = c.replace("`","");

            theResult = theResult + c + COMMA_DELIM; /// // TODO: WEBSOCKET CONNECTION NEXT RELEASE DELIMETER (useSocktet ? COMMA_DELIM: D);;
        }

        if (var3 > 0) {
            // check if procedure have input parameters
            theResult = theResult.substring(0, theResult.length() - 1);
            theResult = theResult + ""; // TODO: WEBSOCKET CONNECTION NEXT RELEASE EOM  (useSocktet ? "": EOM);;
        }else
        {
            theResult = COMMA_DELIM;// "`EOS";// TODO: WEBSOCKET CONNECTION NEXT RELEASE EOS //(useSocktet ? COMMA_DELIM: "`EOS"); ;
        }

        return theResult;
    }



    public void reset() {
        this.procName =  null;
        this.xmit = null;
        this.msgId = null;
        this.data = null;
        this.message = null;
    }
}
