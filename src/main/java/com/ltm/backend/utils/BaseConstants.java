package com.ltm.backend.utils;

import java.util.Locale;

/**
 * Created by maxim on 10.08.17.
 */
public interface BaseConstants {

    public static final String CODELKUP_PROC = "NSPRFLOOKUP";

    public final boolean RET_CONTINUE = true;

    public final boolean RET_CANCEL = false;

    public final String MESSAGE_HEADER = "SocketStatus";

    public final String PROC_PREFIX = "NSPRF";

    public final String DELIM = " ` ";

    public final String D = "`";

    public final String COMMA_DELIM = ",";

    public final String WMWHSEID = "WHSEID";

    public final String userid = "USER_KEY";

    public final String SERVERNAME = /*"localhost" ;*/ "10.20.20.221";

    public final int SOCKET_CONNECTION_TIMEOUT = 3000; ////10023;


    public final int PORT = 10023; ////10023;


    public final int PORT1 = 10024;

    public final int PORT2 = 10025;

    public final int PORT3 = 10026;


    public final String HOSTNAME = /*"brwms02";*/  "brwms01";

    public final String EXEC = "EXEC ";

    public final String CLOSE_STREAM_PROC = "NSPRFPOPRFSESSIONDO";

    public final String PTCID = "1";

    public final String EOM ="``EOS";

    public final String SCREENFLOW  = "screenflow";

    public final String USER_LOCALE  = "USER_LOCALE";

    public final String SERVER_NAME = "SERVER_NAME";

    public final String HOST_NAME = "HOST_NAME";

    public final String VERSION = "SERVER_VERSION";

    public final String TENANT = "INFOR";

    public final String DEFAULT_SERVER_VERSION_VALUE = "10";

    public static final String GETSKUINFO = "GSI01";

    /**
     * |проверка участка права пользователя
     */
    public static final String CHECKUSERRIGHTS = "SSP1039";

    public static final String GETSKUINFO_LIGHT = "ASNGSI03";

    public static final String SKU = "SKU";

    public static final String STORERKEY = "STORERKEY";

    // de 22/08/2017 locale for date format
    //public final String INFOR_DATE_FORMAT = "dd.MM.yyyy";
    public final Locale WMS_DATE_FORMAT_LOCALE = Locale.ENGLISH;
    // Hint:
    // Locale.FRENCH = "dd.MM.yyyy"
    // Locale.ENGLISH = "MM/dd/yyyy"

    public final String LOTTABLE01 = "lottable01";
    public final String LOTTABLE02 = "lottable02";
    public final String LOTTABLE03 = "lottable03";
    public final String LOTTABLE04 = "lottable04";
    public final String LOTTABLE05 = "lottable05";
    public final String LOTTABLE06 = "lottable06";
    public final String LOTTABLE07 = "lottable07";
    public final String LOTTABLE08 = "lottable08";
    public final String LOTTABLE09 = "lottable09";
    public final String LOTTABLE10 = "lottable10";
    public final String LOTTABLE11 = "lottable11";
    public final String LOTTABLE12 = "lottable12";


    public final long GC_RUNTIME_THEAD = 300000;
    public final long GC_SESSION_TIMEOUT= 600000;

    public final static String SPROCEDUREMAP_DEFAULT_PARAMS = "sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,";

    public final static int SPROCEDUREMAP_DEFAULT_PARAMS_LENGTH = 73;

    public final static String DEFAULT_SCREEN_ATTRIBUTE = "screen";


    public final static String EMPTY_STRING = "";

    public final String  FBR = "fbr";

    public final static String  FBR_IMPL = "FBR_IMPL";

    public final static int SOCKET_THREAD_SLEEP_SHORT = 100;
    public final static int SOCKET_THREAD_SLEEP_LONG = 5000;

    public final static  String CHECKDROPID = "CDID01";

    public final static String CHECKLOCATION = "CL01";

    public final static String BROVARY ="BROVARY";





    public final static String SOCKET_UTIL ="SOCKET_UTIL_";

    public final static String INSTRUCTION_FACTORY ="INSTRUCTION_FACTORY_";

    public final static String USE_JWEBSOCKET = "userJwebSocket";

    public final static String WEBSOCKET_CLIENT = "clientendpoint";


    public final static String SOCKET = "SOCKET";

    public final static String MESSAGE_DELAY = "messageDelay";

    public final static String MESSAGE_STYLE = "mystyle";
}
