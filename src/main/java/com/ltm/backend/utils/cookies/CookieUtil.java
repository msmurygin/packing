package com.ltm.backend.utils.cookies;

import com.epiphany.shr.sso.SessionObject;
import com.epiphany.shr.sso.common.SerializeSesssion;
import com.epiphany.shr.sso.common.SessionStruct;
import com.epiphany.shr.sso.exception.InvalidData;
import com.epiphany.shr.sso.exception.InvalidSession;
import com.epiphany.shr.sso.exception.SSORuntimeException;
import com.epiphany.shr.sso.exception.SessionExpired;
import com.ltm.MyUI;
import com.ltm.backend.utils.SessionUtils;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.servlet.http.Cookie;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;

public class CookieUtil {
    private static final Logger logger = Logger.getLogger(CookieUtil.class);
    private static final String COOKIE_NAME = "com.epiphany.SessionID";
    private static final String ENCODING = "UTF-8";
    private static final boolean IS_DEV_MODE = Boolean.parseBoolean(System.getProperty("packing.dev.mode", "false"));

    /**
     * Session validation
     */
    public boolean validateSession() throws Exception {
        SessionUtils sessionUtils = ((MyUI) UI.getCurrent()).getCurrentSessionUtils();

        if (IS_DEV_MODE) {
            logger.debug("In dev mode");
            sessionUtils.setAttr(SessionUtils.USER_ID, "devUser");
        } else {
            logger.debug("Validating uses session");
            Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
            Arrays.stream(cookies).forEach(item -> logger.debug(item.getName() + " " + item.getValue()));
            logger.debug("Cookies read, size " + cookies.length);
            Cookie cookie = Arrays.stream(cookies)
                .filter(item -> item.getName().equalsIgnoreCase(COOKIE_NAME))
                .findFirst()
                .orElseThrow(() -> new Exception("Ошибка сессии"));
            logger.debug(COOKIE_NAME + " value found " + cookie.getValue());
            logger.debug(ssoTokenInfo(URLDecoder.decode(cookie.getValue(), "UTF-8")));
        }
        return sessionUtils.getUserId() != null;
    }


    /**
     * Parse cookie token and save user id to the session
     * @param serializable
     * @return
     * @throws Exception
     */
    private String ssoTokenInfo(Serializable serializable) throws Exception{
        StringWriter tokenInfo = new StringWriter();
        SessionObject materialize = materialize((String) serializable);
        tokenInfo.write("Generating Session " + materialize.generatingSession + "\n");
        tokenInfo.write("Session ID " + materialize.sessionID + "\n");
        tokenInfo.write("Session Ticket " + materialize.sessionTicket + "\n");
        tokenInfo.write("User ID " + materialize.userID + "\n");
        tokenInfo.write("Expiry Date " + materialize.expiryDate + "\n");
        String userId = getUserId(materialize);
        Date expiryDate = materialize.expiryDate;
        Date currentDate  = new Date();
        if (currentDate.compareTo(expiryDate) < 0 ){
            // ---------------------------------------------------------------------------------
            logger.debug("Session is alive, saving vaadin session for user "+userId);
            ((MyUI)UI.getCurrent()).getCurrentSessionUtils().setAttr("userid", userId);
            //-----------------------------------------------------------------------------------
        }else {
            logger.error("Session expired for user");
            throw new Exception("Session expired");
        }
        return tokenInfo.toString();
    }


    /**
     * Gettign userer from cookie token
     * @param materialize
     * @return
     * @throws Exception
     */
    private String getUserId( SessionObject materialize) throws Exception{
        String ldapDN  = materialize.userID;
        String userId = "";

        LdapName ln= new LdapName(ldapDN);
        for(Rdn rdn : ln.getRdns()) {
            if(rdn.getType().equalsIgnoreCase("CN")) {
                userId = (String) rdn.getValue();
                break;
            }
        }

        return userId;
    }




    /**
     * Deserialisation of the cookie token
     * @param a_serializedSession
     * @return
     * @throws InvalidSession
     * @throws SessionExpired
     */
    private SessionObject materialize(String a_serializedSession) throws InvalidSession, SessionExpired {
        byte[] sessionID = null;

        try {
            SessionObject ssObj = new SessionObject();
            sessionID = a_serializedSession.getBytes(ENCODING);
            SessionStruct ss = SerializeSesssion.materializeSession(sessionID);
            ssObj.generatingSession = a_serializedSession;
            ssObj.sessionID = new String(SerializeSesssion.getImmutablePart(sessionID), ENCODING);
            ssObj.sessionTicket = new String(SerializeSesssion.getMutablePart(sessionID), ENCODING);
            ssObj.userID = ss.UserID;
            ssObj.expiryDate = new Date(ss.ExpiryEndTime * 60000L);
            return ssObj;
        } catch (InvalidData var5) {
            throw new InvalidSession("EXP_SESSION_IS_INVALID3", "Session id {0} is invalid while materializing the session, to get the session object", var5, new Object[]{sessionID != null ? new String((byte[])sessionID) : null});
        } catch (UnsupportedEncodingException var6) {
            throw new SSORuntimeException("EXP_FAILED_ENCODING_SESSION_SSID10", "Failed retriving session while materializing session due to failure in encoding", var6);
        }
    }






}
