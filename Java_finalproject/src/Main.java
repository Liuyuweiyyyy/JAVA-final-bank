import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

public class Main {
    static JTextArea moneyArea;
    static JTextArea bankArea;
    static JPanel sidebar;
    static JLabel userLabel;
    static BankManager bankManager = BankManager.getInstance();
    static Account userManager = Account.getInstance();

    static ArrayList<Bank> banks = new ArrayList<>();
    static Vector<String> banksName = new Vector<>();

    static int total_money(){
        int sum = 0;
        for(Bank b : banks){ sum += b.getCoin(); }
        return sum;
    }

    static String total_banks(){
        String total = "";
        //如果是root模式，所有銀行顯示為null
        if(bankManager.isRootMode()){
            for(int i = 0;i < banks.size();i++){
                String s = (char)('A' + i) + "銀行 ";
                total += String.format("%s: null\n", s);
            }
        }else{
            for(int i = 0;i < banks.size();i++){
                String status = "";
                String s = (char)('A' + i) + "銀行 ";
                //檢查是否已開戶 開戶顯示金額 未開戶顯示尚未開戶
                String temp = banks.get(i).getOpen() ? String.format(" %d元", banks.get(i).getCoin()) : " 尚未開戶";
                status += String.format("%s ", temp);
                status = s + ":" + status;
                total += String.format("%s\n", status);
            }
        }
        return total;
    }

    //顯示內容
    static void printMoney(){
        // 如果是root模式，總餘額顯示為null
        if (bankManager.isRootMode()){
            moneyArea.setText("null");
        } else {
            int sum = total_money();
            moneyArea.setText(sum + "元");
        }
    }

    static void printBank(){ bankArea.setText(total_banks() + "\n"); }

    static void printMain(){
        printMoney();
        printBank();
    }

    //更新所有標籤
    static void updateLabels(){
        if(bankManager.isRootMode()){
            //root標籤
            userLabel.setText("當前使用者: root");
            userLabel.setForeground(Color.RED);
        }else{
            //使用者標籤
            String user = userManager.getCurrentUser();
            userLabel.setText("當前使用者: " + user);
            if(user.equals("遊客")){
                userLabel.setForeground(Color.GRAY);
            }else{
                userLabel.setForeground(Color.BLUE);
            }
        }
    }

    //更新按鈕面板
    static void updateSidebar(){
        sidebar.removeAll();    //清空側邊欄(下方按鈕區塊)

        //如果是root模式 顯示root相關按鈕
        if(bankManager.isRootMode()){
            JButton btn_handling = createHandlingFeeButton();   //設定手續費按鈕
            JButton btn_viewFees = createViewFeesButton();      //查看手續費按鈕

            sidebar.add(btn_handling);
            sidebar.add(btn_viewFees);
        }else{
            JButton btn_open = createOpenButton();          //開戶按鈕
            JButton btn_add = createTransferButton();       //轉帳按鈕
            JButton btn_deposit = createDepositButton();    //存款按鈕
            JButton btn_withdraw = createWithdrawButton();  //提款按鈕

            sidebar.add(btn_open);
            sidebar.add(btn_add);
            sidebar.add(btn_deposit);
            sidebar.add(btn_withdraw);
        }
        sidebar.revalidate();   //重新驗證佈局
        sidebar.repaint();      //重新繪製
    }

    //創建開戶按鈕
    static JButton createOpenButton(){
        //創建選擇銀行下拉選單和輸入金額欄位
        JComboBox<String> bankBox = new JComboBox<>(banksName); //銀行選擇下拉選單
        JTextField field1 = new JTextField(10);         //金額輸入欄位
        JPanel open = new JPanel();                             //裝載元件的面板
        open.add(bankBox);
        open.add(field1);

        //創建開戶按鈕
        JButton btn_open = new JButton("開戶");
        btn_open.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                /*
                //root模式下 不能開戶
                if(bankManager.isRootMode()){
                    JOptionPane.showMessageDialog(null, "root模式下不能開戶");
                    return;
                }
                */

                //重置選擇和輸入
                bankBox.setSelectedIndex(0);
                field1.setText("");

                //顯示對話框
                int result = JOptionPane.showConfirmDialog(null, open, "請選擇開戶銀行並輸入金額", JOptionPane.OK_CANCEL_OPTION);

                if(result == JOptionPane.OK_OPTION){
                    int select = bankBox.getSelectedIndex();    //取得選擇的銀行索引
                    try{
                        int money = Integer.parseInt(field1.getText());
                        if(banks.get(select).open_an_account(money)){
                            JOptionPane.showMessageDialog(null, "開戶成功");
                            userManager.saveUserData();
                        }else{
                            JOptionPane.showMessageDialog(null, "已經開戶");
                        }
                        printMain();
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(null, "輸入錯誤");
                    }
                }
            }
        });
        return btn_open;
    }

    //創建轉帳按鈕
    static JButton createTransferButton(){
        JComboBox<String> bankFrom = new JComboBox<>(banksName);
        JComboBox<String> bankTo = new JComboBox<>(banksName);
        JTextField field2 = new JTextField(10);
        JPanel transfer = new JPanel();

        transfer.add(bankFrom);
        transfer.add(new JLabel("→"));
        transfer.add(bankTo);
        transfer.add(field2);

        JButton btn_add = new JButton("轉帳");
        btn_add.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                // 如果在root模式，不能轉帳
                /*
                if(bankManager.isRootMode()){
                    JOptionPane.showMessageDialog(null, "root模式下不能轉帳");
                    return;
                }
                */

                bankFrom.setSelectedIndex(0);
                bankTo.setSelectedIndex(0);
                field2.setText("");
                int result = JOptionPane.showConfirmDialog(null, transfer,"請選擇兩間銀行並輸入金額",JOptionPane.OK_CANCEL_OPTION);
                if(result == JOptionPane.OK_OPTION){
                    int select1 = bankFrom.getSelectedIndex();
                    int select2 = bankTo.getSelectedIndex();
                    try{
                        int money = Integer.parseInt(field2.getText());
                        int fee = bankManager.getHandlingFee(select1, select2);
                        //顯示手續費資訊
                        JOptionPane.showMessageDialog(null,"手續費 : " + fee + "元\n" + "總扣款 : " + (money + fee) + "元");
                        if(banks.get(select1).transferMoney(banks.get(select2), money)){
                            JOptionPane.showMessageDialog(null, "轉帳成功");
                            userManager.saveUserData();
                        }else{
                            JOptionPane.showMessageDialog(null, "轉帳失敗");
                        }
                        printMain();
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(null, "輸入錯誤");
                    }
                }
            }
        });
        return btn_add;
    }

    //創建存錢按鈕
    static JButton createDepositButton(){
        JComboBox<String> depositBank = new JComboBox<>(banksName);
        JTextField depositField = new JTextField(10);
        JPanel depositPanel = new JPanel();

        depositPanel.add(depositBank);
        depositPanel.add(depositField);

        JButton btn_deposit = new JButton("存錢");
        btn_deposit.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                /*
                if (bankManager.isRootMode()) {
                    JOptionPane.showMessageDialog(null, "root模式下不能存錢");
                    return;
                }
                */

                depositBank.setSelectedIndex(0);
                depositField.setText("");

                int result = JOptionPane.showConfirmDialog(null,depositPanel,"請選擇銀行並輸入存款金額", JOptionPane.OK_CANCEL_OPTION);

                if(result == JOptionPane.OK_OPTION){
                    int select = depositBank.getSelectedIndex();
                    try{
                        int money = Integer.parseInt(depositField.getText());
                        if(money <= 0) {
                            JOptionPane.showMessageDialog(null, "存款金額必須大於0");
                            return;
                        }
                        if(banks.get(select).saveMoney(money)){
                            JOptionPane.showMessageDialog(null, "存款成功");
                            // 存款成功後儲存資料
                            userManager.saveUserData();
                        }else{
                            JOptionPane.showMessageDialog(null, "存款失敗，請確認該銀行是否已開戶");
                        }
                        printMain();
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(null, "輸入錯誤");
                    }
                }
            }
        });
        return btn_deposit;
    }

    //領錢按鈕
    static JButton createWithdrawButton(){
        JComboBox<String> withdrawBank = new JComboBox<>(banksName);
        JTextField withdrawField = new JTextField(10);
        JPanel withdrawPanel = new JPanel();

        withdrawPanel.add(withdrawBank);
        withdrawPanel.add(withdrawField);

        JButton btn_withdraw = new JButton("領錢");
        btn_withdraw.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                /*
                if (bankManager.isRootMode()) {
                    JOptionPane.showMessageDialog(null, "root模式下不能領錢");
                    return;
                }
                */

                withdrawBank.setSelectedIndex(0);
                withdrawField.setText("");

                int result = JOptionPane.showConfirmDialog(null,withdrawPanel,"請選擇銀行並輸入領取金額", JOptionPane.OK_CANCEL_OPTION);

                if(result == JOptionPane.OK_OPTION){
                    int select = withdrawBank.getSelectedIndex();
                    try{
                        int money = Integer.parseInt(withdrawField.getText());
                        if(money <= 0) {
                            JOptionPane.showMessageDialog(null, "領取金額必須大於0");
                            return;
                        }
                        if(banks.get(select).receiveMoney(money)){
                            JOptionPane.showMessageDialog(null, "領錢成功");
                            userManager.saveUserData();     //儲存資料
                        }else{
                            JOptionPane.showMessageDialog(null, "領錢失敗，請確認該銀行是否已開戶且餘額足夠");
                        }
                        printMain();
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(null, "輸入錯誤");
                    }
                }
            }
        });
        return btn_withdraw;
    }

    //創建手續費設定按鈕
    static JButton createHandlingFeeButton(){
        JButton btn_handling = new JButton("設定手續費");
        btn_handling.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> fromBox = new JComboBox<>(banksName);
                JComboBox<String> toBox = new JComboBox<>(banksName);
                JTextField feeField = new JTextField(8);
                JPanel handling = new JPanel();

                handling.add(fromBox);
                handling.add(new JLabel("→"));
                handling.add(toBox);
                handling.add(feeField);

                int result = JOptionPane.showConfirmDialog(null,handling, "設定轉帳手續費", JOptionPane.OK_CANCEL_OPTION);

                if(result != JOptionPane.OK_OPTION){ return; }

                int from = fromBox.getSelectedIndex();
                int to = toBox.getSelectedIndex();

                //這行不能註解 有用
                if(from == to){
                    JOptionPane.showMessageDialog(null, "同銀行不需手續費");
                    return;
                }

                try{
                    int fee = Integer.parseInt(feeField.getText());
                    if(fee < 0){
                        JOptionPane.showMessageDialog(null, "手續費不能為負數");
                        return;
                    }
                    if(bankManager.setHandlingFee(from,to,fee)){
                        JOptionPane.showMessageDialog(null,banksName.get(from) + " → " + banksName.get(to) + " 手續費設定為 : " + fee + "元");
                    }else{
                        JOptionPane.showMessageDialog(null, "設定失敗");
                    }

                }catch(NumberFormatException exception){
                    JOptionPane.showMessageDialog(null, "輸入錯誤");
                }
            }
        });
        return btn_handling;
    }

    //創建查看手續費按鈕
    static JButton createViewFeesButton() {
        JButton btn_viewFees = new JButton("查看手續費");
        btn_viewFees.addActionListener(e -> {JOptionPane.showMessageDialog(null, bankManager.getAllHandlingFees());});
        return btn_viewFees;
    }

    //切換帳號對話框
    static void showSwitchAccountDialog() {
        if (bankManager.isRootMode()) {
            JOptionPane.showMessageDialog(null, "root模式下不能切換帳號");
            return;
        }

        //建立分頁面板
        JTabbedPane tabbedPane = new JTabbedPane();

        //登入面板
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField loginUserField = new JTextField();
        JPasswordField loginPassField = new JPasswordField();   //密碼輸入框(隱藏顯示

        loginPanel.add(new JLabel("使用者名稱:"));
        loginPanel.add(loginUserField);
        loginPanel.add(new JLabel("密碼:"));
        loginPanel.add(loginPassField);

        JPanel loginButtonPanel = new JPanel();
        JButton loginButton = new JButton("登入");
        loginButtonPanel.add(loginButton);

        JPanel loginMainPanel = new JPanel(new BorderLayout());
        loginMainPanel.add(loginPanel, BorderLayout.CENTER);
        loginMainPanel.add(loginButtonPanel, BorderLayout.SOUTH);

        //註冊面板
        JPanel registerPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField registerUserField = new JTextField();
        JPasswordField registerPassField = new JPasswordField();
        JPasswordField registerConfirmPassField = new JPasswordField();

        registerPanel.add(new JLabel("使用者名稱:"));
        registerPanel.add(registerUserField);
        registerPanel.add(new JLabel("密碼:"));
        registerPanel.add(registerPassField);
        registerPanel.add(new JLabel("確認密碼:"));
        registerPanel.add(registerConfirmPassField);

        JPanel registerButtonPanel = new JPanel();
        JButton registerButton = new JButton("註冊");
        registerButtonPanel.add(registerButton);

        JPanel registerMainPanel = new JPanel(new BorderLayout());
        registerMainPanel.add(registerPanel, BorderLayout.CENTER);
        registerMainPanel.add(registerButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("登入", loginMainPanel);
        tabbedPane.addTab("註冊", registerMainPanel);

        //對話框
        JDialog dialog = new JDialog((Frame)null, "切換帳號", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton guestButton = new JButton("遊客模式");
        JButton logoutButton = new JButton("登出");
        JButton closeButton = new JButton("關閉");

        bottomPanel.add(guestButton);
        bottomPanel.add(logoutButton);
        bottomPanel.add(closeButton);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        //登入按鈕事件
        loginButton.addActionListener(e ->{
            String username = loginUserField.getText().trim();              //去除空白
            String password = new String(loginPassField.getPassword());     //取得密碼

            if(username.isEmpty() || password.isEmpty()){
                JOptionPane.showMessageDialog(dialog, "請輸入使用者名稱和密碼");
                return;
            }

            if(userManager.login(username, password)){
                JOptionPane.showMessageDialog(dialog, "登入成功！");
                userManager.loadUserData(); //載入使用者資料
                updateLabels();
                printMain();
                dialog.dispose();           //關閉對話框
            }else{
                JOptionPane.showMessageDialog(dialog, "登入失敗，請檢查使用者名稱和密碼");
            }
        });

        //註冊按鈕事件
        registerButton.addActionListener(e ->{
            String username = registerUserField.getText().trim();
            String password = new String(registerPassField.getPassword());
            String confirmPassword = new String(registerConfirmPassField.getPassword());

            if(username.isEmpty() || password.isEmpty()){
                JOptionPane.showMessageDialog(dialog, "請輸入使用者名稱和密碼");
                return;
            }

            if(!password.equals(confirmPassword)){
                JOptionPane.showMessageDialog(dialog, "兩次輸入的密碼不一致");
                return;
            }

            if(password.length() < 6){
                JOptionPane.showMessageDialog(dialog, "密碼長度至少6位");
                return;
            }

            if(userManager.register(username, password)){
                JOptionPane.showMessageDialog(dialog, "註冊成功！");
                registerUserField.setText("");
                registerPassField.setText("");
                registerConfirmPassField.setText("");
                tabbedPane.setSelectedIndex(0); // 切換到登入頁面
            }else{
                JOptionPane.showMessageDialog(dialog, "使用者名稱已存在");
            }
        });

        //遊客模式按鈕事件
        guestButton.addActionListener(e ->{
            userManager.switchToGuest();
            //切換到遊客模式後 自動載入遊客上次的資料
            updateLabels();
            printMain();
            dialog.dispose();
        });

        //登出按鈕事件
        logoutButton.addActionListener(e ->{
            int confirm = JOptionPane.showConfirmDialog(dialog,"確定要登出嗎？","確認登出",JOptionPane.YES_NO_OPTION);

            if(confirm == JOptionPane.YES_OPTION){
                //登出前會儲存當前登入者資料，並自動切換到遊客模式並載入遊客資料
                userManager.logout();
                updateLabels();
                printMain();
                dialog.dispose();
            }
        });

        //關閉按鈕事件
        closeButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    public static void main(String[] args){
        Bank a = new Bank();
        Bank b = new Bank();
        Bank c = new Bank();

        banks.add(a);
        banks.add(b);
        banks.add(c);

        banksName.add("A銀行");
        banksName.add("B銀行");
        banksName.add("C銀行");

        //GUI介面
        //創建主視窗
        JFrame frame = new JFrame("Bank System");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   //關閉時結束程式

        //好像沒用到
        //視窗關閉事件，儲存使用者資料
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                //root模式 不儲存資料
                if (!bankManager.isRootMode()) { userManager.saveUserData(); }
            }
        });

        //創建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        //創建上半部分面板 包含使用者標籤和總餘額
        JPanel topPanel = new JPanel(new BorderLayout());

        //創建使用者標籤
        userLabel = new JLabel("當前使用者: 遊客");
        userLabel.setFont(new Font("新細明體", Font.BOLD, 14));
        userLabel.setForeground(Color.GRAY);

        //創建總餘額標籤
        JLabel balanceLabel = new JLabel("總餘額 : ");
        balanceLabel.setFont(new Font("新細明體", Font.PLAIN, 18));

        //創建總餘額顯示區域
        moneyArea = new JTextArea();
        moneyArea.setFont(new Font("新細明體", Font.PLAIN, 18));
        moneyArea.setEditable(false);       //不能編輯
        moneyArea.setFocusable(false);      //不能被滑鼠點擊
        moneyArea.setText("0元");

        //創建使用者面板 顯示使用者標籤
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.add(userLabel);

        //創建餘額顯示面板
        JPanel balanceDisplayPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        balanceDisplayPanel.add(balanceLabel);
        balanceDisplayPanel.add(moneyArea);

        //將所有元件添加到topPanel
        topPanel.add(userPanel, BorderLayout.WEST);
        topPanel.add(balanceDisplayPanel, BorderLayout.EAST);   //EAST使其靠右

        //創建銀行資訊顯示區域
        bankArea = new JTextArea();
        bankArea.setFont(new Font("新細明體", Font.PLAIN, 18));
        bankArea.setEditable(false);
        bankArea.setFocusable(false);

        //創建內容面板
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(bankArea), BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        //創建設定按鈕和下拉選單
        JMenuBar menuBar = new JMenuBar();
        JButton settingsBtn = new JButton("設定");
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(settingsBtn);

        //下拉選單
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem switchAccountItem = new JMenuItem("切換帳號");
        JMenuItem rootModeItem = new JMenuItem("進入root管理模式");
        JMenuItem exitRootModeItem = new JMenuItem("退出root管理模式");
        JMenuItem exitItem = new JMenuItem("退出");

        popupMenu.add(switchAccountItem);
        popupMenu.addSeparator();       //分隔線
        popupMenu.add(rootModeItem);
        popupMenu.add(exitRootModeItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        //設定按鈕事件
        settingsBtn.addActionListener(e -> {popupMenu.show(settingsBtn, 0, settingsBtn.getHeight());});

        //選單項目事件
        switchAccountItem.addActionListener(e -> {showSwitchAccountDialog();});

        rootModeItem.addActionListener(e ->{
            bankManager.enterRootMode();
            JOptionPane.showMessageDialog(null, "已進入root管理模式");
            updateLabels();
            updateSidebar();
            printMain();
        });

        exitRootModeItem.addActionListener(e ->{
            bankManager.exitRootMode();
            JOptionPane.showMessageDialog(null, "已退出root管理模式");
            updateLabels();
            updateSidebar();
            printMain();
        });

        exitItem.addActionListener(e -> {
            //如果不是root模式 儲存使用者資料
            if(!bankManager.isRootMode()){ userManager.saveUserData(); }
            int confirm = JOptionPane.showConfirmDialog(null,"確定要退出程式嗎？","確認退出",JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION){ System.exit(0); }
        });

        //初始化側邊欄
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(60, 60));
        sidebar.setLayout(new FlowLayout());    //流式佈局

        //初始狀態為User模式
        updateSidebar();
        updateLabels();

        mainPanel.add(sidebar, BorderLayout.SOUTH);

        //設定視窗選單列
        frame.setJMenuBar(menuBar);             //設定選單列
        frame.add(mainPanel);                   //加入主面板
        frame.setLocationRelativeTo(null);      //視窗置中
        frame.setVisible(true);                 //顯示視窗

        // 程式啟動時，載入遊客資料（如果已存在）
        userManager.switchToGuest();
        printMain();
    }
}
