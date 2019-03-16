
public class UserAgent {
	/**
	 * 定义不同设备的User-Agent
	 * IOS会被识别成[我的设备]，而非苹果手机，但是还是属于手机类型
	 */
	public static final String ANDROID = "Mozilla/5.0 (Linux; Android 7.0; PLUS Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.98 Mobile Safari/537.36";
	public static final String IOS = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0_1 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A402 Safari/604.1";
	public static final String WINDOWS = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0";
}
