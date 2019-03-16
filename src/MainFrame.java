import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
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

public class MainFrame extends JFrame implements ActionListener{
	final int MAC_LENGTH = 12;
	final String MAC_PREFIX = "AAAAAA";
	final String MAC_CHARS = "0123456789abcdef";
    final String REG_ADD = "reg add HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}\\0005 /v NetworkAddress /t REG_SZ /d {mac} /f";
    final String REG_DELETE = "reg delete HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}\\0005 /v NetworkAddress /f";
	final String[] DEVICE_TYPE = {"Windows", "Android", "iOS"};
	final String[] MAC_MODE_TYPE = {"自定义", "使用现有配置"};
	final String configFilePath = System.getProperty("user.dir") + "/config";
	Map<String, String> configMap = new HashMap<String, String>();
	static StringBuilder logBuffer = new StringBuilder();
	StringBuilder macBuilder = new StringBuilder();
	
	JMenuBar menuBar;
	JMenu mainMenu;
	JMenuItem openConfigFile;
	
	FlowLayout defaultFlow;
	
	JPanel modePanel;
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
    
    JPanel logPanel;
    JScrollPane logScrollPane;
    JLabel logLabel;
    JTextArea logArea;
    
    MainFrame() {
    	readConfig();
    	
        this.setTitle("Uneto_JXAU");
        this.setLayout(new GridLayout(4, 1));
        
        initComponent();

        loadNormalSetting();
    }
    
    void initComponent() {
        menuBar = new JMenuBar();
        
        mainMenu = new JMenu("菜单");
        menuBar.add(mainMenu);
        openConfigFile = new JMenuItem("打开配置文件");
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
        
        macBox = new JComboBox();
        if (configMap.containsKey("mac_android") && configMap.get("mac_android") != null) {
            macBox.addItem("Android -> " + configMap.get("mac_android"));        	
        }
        if (configMap.containsKey("mac_ios") && configMap.get("mac_ios") != null) {
            macBox.addItem("iOS -> " + configMap.get("mac_ios"));        	
        }
        if (macBox.getItemCount() == 0) {
        	macBox.addItem("没有找到配置信息");
        }
        macLabel = new JLabel("MAC地址");
        macField = new JTextField(12);
        random = new JButton("生成地址");
        random.setActionCommand("getRandomMac");
        random.addActionListener(this);
        
        apply = new JButton("应用");
        apply.setActionCommand("applySetting");
        apply.addActionListener(this);
        reset = new JButton("重置");
        reset.setActionCommand("resetSetting");
        reset.addActionListener(this);
        
		modePanel.add(macLabel);
		modePanel.add(macField);
		modePanel.add(random);
		
		modePanel.add(apply);
		modePanel.add(reset);
        
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
        
        logPanel = new JPanel(defaultFlow);
        logLabel = new JLabel("日志：");
        logArea = new JTextArea(5, 30);
        logScrollPane = new JScrollPane(logArea);
        
        logPanel.add(logLabel);
        logPanel.add(logScrollPane);
        
        this.add(logPanel);
        
        modeBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (modeBox.getSelectedIndex() == 0) {
					modePanel.remove(macBox);
					modePanel.add(macLabel);
					modePanel.add(macField);
					modePanel.add(random);
					modePanel.add(apply);
					modePanel.add(reset);
				}
				else {
					modePanel.remove(macLabel);
					modePanel.remove(macField);
					modePanel.remove(random);
					modePanel.remove(apply);
					modePanel.remove(reset);
					modePanel.add(macBox);
					modePanel.add(apply);
					modePanel.add(reset);
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
    	File config = new File(System.getProperty("user.dir") + "/config");
    	String tmp = null;
    	try (BufferedReader reader = new BufferedReader(new FileReader(config));)
    	{
    		while ( (tmp = reader.readLine()) != null) {
    			String[] pair = tmp.split("=");
    			// 参数值不为空，读取并设置其值
    			if (pair.length > 1) {
        			configMap.put(pair[0], pair[1]);    				
    			}
    			// 参数值为空，则将其值设置为null
    			else {
    				configMap.put(pair[0], null);
    			}
    		}
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
		logArea.setText(logBuffer.toString());
	}
	
	private void runCommand(String command) {
		Runtime run = Runtime.getRuntime();
		String input = "";
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
			process.destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MainFrame.logBuffer.append(input + "\t\n");
		
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
		switch (mode) {
			case "自定义":
				mac = macField.getText();
				break;
			case "使用现有配置":
				if (macBox.getSelectedIndex() == 0) {
					mac = configMap.get("mac_android");
					break;
				}
				mac = configMap.get("mac_ios");					
				break;
				default:
		}
		runCommand("cmd /c " + REG_ADD.replace("{mac}", mac));
	}
	private void resetSetting() {
		runCommand("cmd /c " + REG_DELETE);
		
	}
	private void doLogin() {
		AuthInterface.login(configMap.get("user"), configMap.get("password"), deviceBox.getSelectedItem().toString());
	}
	private void doLogout() {
		AuthInterface.logout();
	}
} 