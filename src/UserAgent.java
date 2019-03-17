
public class UserAgent {
	/**
	 * 定义不同设备的User-Agent
	 * Android和iOS等同于[我的设备],不知道运营商为什么识别不了这两种User-Agent
	 * Windows等同于[我的电脑]
	 */
	public static final String ANDROID = "Mozilla/5.0 (Linux; U; Android 9; zh-cn; MI 8 Build/PKQ1.180729.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.128 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.5.2";	
	public static final String IOS = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0_1 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A402 Safari/604.1";
	public static final String WINDOWS = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0";
}
