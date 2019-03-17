import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class MainFrame extends JFrame implements ActionListener{
	final int MAC_LENGTH = 12;
	final String MAC_PREFIX = "AAAAAA";
	final String MAC_CHARS = "0123456789abcdef";
    final String REG_ADD = "reg add HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}\\0005 /v NetworkAddress /t REG_SZ /d {mac} /f";
    final String REG_DELETE = "reg delete HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}\\0005 /v NetworkAddress /f";
	final String[] DEVICE_TYPE = {"Windows", "Android", "iOS"};
	final String[] MAC_MODE_TYPE = {"自定义", "使用现有配置"};
	final String configPath = System.getProperty("user.dir") + "/config.json";
	JSONObject config = new JSONObject();
	JSONObject device;
	boolean isDeviceConfigExist = true;
	StringBuilder macBuilder = new StringBuilder();	
	
	JMenuBar menuBar;
	JMenu mainMenu;
	JMenuItem openConfigFile;
	
	FlowLayout defaultFlow;
	
	JPanel modePanel;
	JPanel modeCustomPanel;
	JPanel modeUseConfigPanel;
	JPanel modeBtnGroupPanel;
    JLabel modeLabel;
    JComboBox modeBox;
    
    JComboBox macBox;
    JLabel macLabel;
    JTextField macField;
    JButton random;
    JButton apply;
	JButton reset;

    JPanel netPanel;
    JLabel deviceLabel;
    JComboBox deviceBox;
    JButton login;
    JButton logout;
    
    MainFrame() {
    	readConfig();
    	
        this.setTitle("Uneto_JXAU");
        this.setLayout(new GridLayout(4, 1));
        
        initComponent();

        loadNormalSetting();
    }
    
    void initComponent() {
        menuBar = new JMenuBar();
        
        mainMenu = new JMenu("主菜单");
        menuBar.add(mainMenu);
        openConfigFile = new JMenuItem("打开配置文件目录");
        openConfigFile.setActionCommand("openConfigFile");
        openConfigFile.addActionListener(this);
        
        mainMenu.add(openConfigFile);
        
        this.setJMenuBar(menuBar);
        
        defaultFlow = new FlowLayout(FlowLayout.LEFT);
        
        modePanel = new JPanel(defaultFlow);

        
        modeLabel = new JLabel("MAC模式");
        modeBox = new JComboBox(MAC_MODE_TYPE);
        modeBox.setSelectedIndex(0);
        
        modePanel.add(modeLabel);
        modePanel.add(modeBox);
        this.add(modePanel);

        macLabel = new JLabel("MAC地址");
        macField = new JTextField(12);
        random = new JButton("生成地址");
        random.setActionCommand("getRandomMac");
        random.addActionListener(this);
        modeCustomPanel = new JPanel(defaultFlow);
        modeCustomPanel.add(macLabel);
        modeCustomPanel.add(macField);
        modeCustomPanel.add(random);
        
        modePanel.add(modeCustomPanel);
        
        macBox = new JComboBox();
        modeUseConfigPanel = new JPanel(defaultFlow);
        modeUseConfigPanel.add(macBox);
        
        device = config.getJSONObject("device");
        if (device != null) {
        	String tmp = "";
        	for (String key : device.keySet()) {
        		tmp = device.getString(key);
        		if (tmp.equals("") || tmp == null ) {
        			continue;
        		}
        		else {
                	macBox.addItem(key);
        		}
        	}
        	if (macBox.getItemCount() == 0) {
        		isDeviceConfigExist = false;
        		macBox.addItem("暂无配置信息");
        	}
        }

        
        apply = new JButton("应用");
        apply.setActionCommand("applySetting");
        apply.addActionListener(this);
        reset = new JButton("重置");
        reset.setActionCommand("resetSetting");
        reset.addActionListener(this);
		modeBtnGroupPanel = new JPanel(defaultFlow);
		modeBtnGroupPanel.add(apply);
		modeBtnGroupPanel.add(reset);
		
		modePanel.add(modeBtnGroupPanel);
        
        netPanel = new JPanel(defaultFlow);
        deviceLabel = new JLabel("设备类型");
        deviceBox = new JComboBox(DEVICE_TYPE);
        login = new JButton("登陆");
        login.setActionCommand("doLogin");
        login.addActionListener(this);
        logout = new JButton("注销");
        logout.setActionCommand("doLogout");
        logout.addActionListener(this);
        
        netPanel.add(deviceLabel);
        netPanel.add(deviceBox);
        netPanel.add(login);
        netPanel.add(logout);
        
        this.add(netPanel);
        
        modeBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (modeBox.getSelectedIndex() == 0) {
					modePanel.remove(modeUseConfigPanel);
					modePanel.add(modeCustomPanel);
					modePanel.add(modeBtnGroupPanel);
				}
				else {
					modePanel.remove(modeCustomPanel);
					modePanel.add(modeUseConfigPanel);
					modePanel.add(modeBtnGroupPanel);
				}
				modePanel.updateUI();
			}
        	
        });
    }
    void loadNormalSetting() {
        this.pack();
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    void readConfig() {
    	File file = new File(configPath);
    	String tmp = null;
    	try (FileInputStream fis = new FileInputStream(file))
    	{
    		config = JSONObject.parseObject(new String(fis.readAllBytes(), "utf-8"));
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String command = e.getActionCommand();
		System.out.println("[INFO] actionCommand -> " + command);
		switch (command) {
			case "openConfigFile":
				openConfigFile();
				break;
			case "getRandomMac":
				getRandomMac();
				break;
			case "applySetting":
				applySetting();
				break;
			case "resetSetting":
				resetSetting();
				break;
			case "doLogin":
				doLogin();
				break;
			case "doLogout":
				doLogout();
				break;
			default:
		}
	}
	
	private String runCommand(String command) {
		Runtime run = Runtime.getRuntime();
		String input = "";
		int exitCode = 0;
		try {
			Process process = run.exec(command);
			try (InputStream is = process.getInputStream(); InputStream is2 = process.getErrorStream()) {
				input = new String(is.readAllBytes(), "GBK");
				if (input.equals("")) {
					input = new String(is2.readAllBytes(), "GBK");					
				}
				System.out.println("[INFO] runCommand input stream -> " + input);
			}
			catch (IOException e) {
				throw e;
			}
			exitCode = process.exitValue();
			process.destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject response = new JSONObject();
		
		if (exitCode == 0) {
			response.put("result", "success");
		}
		else {
			response.put("result", "fail");			
		}
		response.put("message", input);
		return response.toJSONString();
	}
	
	private void openConfigFile() {
		runCommand("cmd /c start " + System.getProperty("user.dir"));
	}
	
	private void getRandomMac() {
    	for (int i = 0; i < MAC_LENGTH / 2 ; i++) {
    		macBuilder.append(MAC_CHARS.charAt((int) Math.round(Math.random() * 15)));
    	}
        macField.setText(MAC_PREFIX + macBuilder.toString().toUpperCase());
        macBuilder.delete(0, macBuilder.length());
	}
	private void applySetting() {
		String mac = "";
		String mode = modeBox.getSelectedItem().toString();
		boolean isDataCorrect = false;
		switch (mode) {
			case "自定义":
				mac = macField.getText();
				if (mac.equals("")) {
					promptDialog("{'result':'fail','message':'请输入正确的mac地址'}");
				}
				else {
					isDataCorrect = true;
				}
				break;
			case "使用现有配置":
				if (isDeviceConfigExist) {
					isDataCorrect = true;
					mac = device.getString((String) macBox.getSelectedItem());
				}
				else {
					JSONObject modalInfo = new JSONObject();
					promptDialog("{'result':'fail','message':'请检查配置文件'}");
				}
				break;
				default:
		}
		if (isDataCorrect) {
			String dialogInfo = runCommand("cmd /c " + REG_ADD.replace("{mac}", mac));
			promptDialog(dialogInfo);					
		}
	}
	private void resetSetting() {
		String dialogInfo = runCommand("cmd /c " + REG_DELETE);
		promptDialog(dialogInfo);
	}
	private void doLogin() {
		String dialogInfo = AuthInterface.login(config.getString("user"), config.getString("password"), deviceBox.getSelectedItem().toString());
		promptDialog(dialogInfo);
	}
	private void doLogout() {
		promptDialog(AuthInterface.logout());
	}
	
	private void promptDialog(JSONObject response) {
		String result;
		String message;
		int messageType;
		
		result = response.getString("result");
		
		if (result.equals("fail")) {
			messageType = JOptionPane.ERROR_MESSAGE;
			message = "操作失败 -> 返回信息：";
		}
		else {
			messageType = JOptionPane.INFORMATION_MESSAGE;
			message = "操作成功 -> 返回信息：";
		}
		message += response.getString("message");
		JOptionPane.showMessageDialog(this, message, "提示", messageType);
	}
	private void promptDialog(String data) {
		promptDialog(JSONObject.parseObject(data));
	}
} 