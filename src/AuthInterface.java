import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.alibaba.fastjson.JSONObject;

class AuthInterface {
    static final String AUTH_INTERFACE_URL = "http://10.255.240.8/eportal/InterFace.do?method=";
    static final String BASE_URL = "http://10.255.240.8/";
    static final String GO_LOGOUT_URL = "http://10.255.240.8/eportal/gologout.jsp";
    static final String HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    private static String queryString;
    private static String userIndex;
    private static String userAgent;
    private static String response;
    // 获取跳转链接里的查询字符串
    public static void setQueryString() {
    	HttpURLConnection.setFollowRedirects(true);
    	String response = GET(BASE_URL);
    	queryString = response.substring(response.indexOf('?') + 1, response.lastIndexOf('\''));
        System.out.println("[INFO] setQueryString -> \n" + queryString);
    }
    
    public static String login(String user, String password, String deviceType) {
        userAgent = getUserAgent(deviceType);
        setQueryString();
        StringBuilder data = new StringBuilder();
        data.append("userId="+user);
        data.append("&password="+password);
        data.append("&service=");
        data.append("&queryString="+queryString);
        // 暂时不支持验证码输入
        data.append("&operatorPwd=&operatorUserId=&validcode=&passwordEncrypt=");
        
        System.out.println("[INFO] device type -> " + deviceType);
        System.out.println("[INFO] user agent -> " + userAgent);
        
        response = POST("login", data.toString(), userAgent);
    	userIndex = JSONObject.parseObject(response).getString("userIndex");    	

    	System.out.println("[INFO] userIndex -> \n" + userIndex);
        
        return response;
    }
    
    public static String logout() {
    	if (userIndex == null) {
    		// 禁止自动跳转，这样才能从Header中获取跳转链接
    		HttpURLConnection.setFollowRedirects(false);
    		String tmpResponse = GET(GO_LOGOUT_URL);
    		userIndex = JSONObject.parseObject(tmpResponse).getString("userIndex");
        	System.out.println("[INFO] getUserIndex -> \n" + tmpResponse);
    	}
    	// 注销时不填user-agent
    	return POST("logout", "userIndex=" + userIndex, "");
    }
    
    private static HttpURLConnection getConnection(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
        }
        catch(MalformedURLException e) {
            System.out.println("转换URL时发生错误。> URL:"+url);
            e.printStackTrace();
        }
        catch(IOException e) {
            System.out.println("打开链接时发生错误。");
            e.printStackTrace();
        }
        return conn;
    }
    
    private static void output(HttpURLConnection conn, String content) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(content);
            osw.close();
        } catch (IOException e) {
            System.out.println("获取链接的输出流|写入数据 错误");
            e.printStackTrace();
        }
    }
    
    public static String GET(String url) {
    	HttpURLConnection conn = getConnection(url);
        conn.setRequestProperty("User-Agent", userAgent);
        try (InputStream is = conn.getInputStream()) 
        {
        	System.out.println("[INFO] GET -> " + url);
        	System.out.println(conn.getResponseMessage());
        	System.out.println(conn.getHeaderField("Location"));
            return new String(is.readAllBytes(), "utf-8");
        }
        catch (IOException e)
        {
            System.out.println("获得输入流或读取内容失败。");
            e.printStackTrace();
            return null;
        }
        finally {
        	conn.disconnect();
        }
    }
    
    private static String GET(String url, String queryString) {
    	return GET(url + "/?" + queryString);
    }
    
    private static String POST(String method, String data, String userAgent) {
    	HttpURLConnection conn = getConnection(AUTH_INTERFACE_URL + method);
        conn.setRequestProperty("Content-Type", HEADER_CONTENT_TYPE);
        conn.setRequestProperty("User-Agent", userAgent);
    	conn.setDoOutput(true);
    	output(conn, data);
        try  {
            System.out.println("[INFO] POST -> " + method);
        	return new String(conn.getInputStream().readAllBytes(), "utf-8");
        }
        catch(IOException e) {
            System.out.println("读取输入流时发生错误。");
            e.printStackTrace();
            return null;
        }
    }
    
    private static String getUserAgent(String deviceType) {
    	String userAgent;
        switch(deviceType.toUpperCase()) {
	    	case "WINDOWS":
	    		userAgent = UserAgent.WINDOWS;
	    		break;
	    	case "ANDROID":
	    		userAgent = UserAgent.ANDROID;
	    		break;
	    	case "IOS":
	    		userAgent = UserAgent.IOS;
	    	default:
	    		userAgent = UserAgent.WINDOWS;
        }
        return userAgent;
    }
}