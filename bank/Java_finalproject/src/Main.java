import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

public class Main {
    static JTextArea moneyArea;
    static JTextArea bankArea;
    static BankManager bankManager = BankManager.getInstance();

    static JPanel sidebar; //改為全域變數以便更新
    static JLabel modeLabel;

    static ArrayList<Bank> banks = new ArrayList<>();
    static Vector<String> banksName = new Vector<>();

    static int total_money(){
        int sum = 0;
        for(Bank b : banks){ sum += b.getCoin(); }
        return sum;
    }

    static String total_banks(){
        String total = "";
        for(int i = 0 ; i < banks.size() ; i++){
            String status = "";
            String s = (char)('A' + i) + "銀行 ";
            String temp = banks.get(i).getOpen() ? String.format(" %d元", banks.get(i).getCoin()) : " 尚未開戶";
            status += String.format("%s ", temp);
            status = s + ":" + status;
            total += String.format("%s\n", status);
        }
        return total;
    }

    // 顯示內容
    static void printMoney(){
        int sum = total_money();
        moneyArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        moneyArea.setText("總餘額 : " + sum + "元");
    }

    static void printBank(){ bankArea.setText(total_banks() + "\n"); }

    static void printMain(){
        printMoney();
        printBank();
    }

    //手續費設定對話框
    static void showHandlingFeeDialog() {
        JComboBox<String> fromBox = new JComboBox<>(banksName);
        JComboBox<String> toBox = new JComboBox<>(banksName);
        JTextField feeField = new JTextField(8);

        JPanel handling = new JPanel();
        handling.add(fromBox);
        handling.add(new JLabel("→"));
        handling.add(toBox);
        handling.add(feeField);

        int result = JOptionPane.showConfirmDialog(null, handling, "設定轉帳手續費", JOptionPane.OK_CANCEL_OPTION);

        if(result != JOptionPane.OK_OPTION){ return; }

        int from = fromBox.getSelectedIndex();
        int to = toBox.getSelectedIndex();

        if(from == to){
            JOptionPane.showMessageDialog(null, "同銀行不需手續費");
            return;
        }

        try {
            int fee = Integer.parseInt(feeField.getText());
            if(fee < 0){
                JOptionPane.showMessageDialog(null, "手續費不能為負數");
                return;
            }
            if(bankManager.setHandlingFee(from, to, fee)){
                JOptionPane.showMessageDialog(null,
                        banksName.get(from) + " → " +
                                banksName.get(to) + " 手續費設定為 : " + fee + "元"
                );
            }else{
                JOptionPane.showMessageDialog(null, "設定失敗，請確認是否在root模式");
            }

        }catch(NumberFormatException exception){
            JOptionPane.showMessageDialog(null,"輸入錯誤");
        }
    }

    //更新模式標籤
    static void updateModeLabel(){
        if(bankManager.isRootMode()){
            modeLabel.setText("當前模式: Root");
            modeLabel.setForeground(Color.RED);
        }else{
            modeLabel.setText("當前模式: User");
            modeLabel.setForeground(Color.BLUE);
        }
    }

    // 更新按鈕面板
    static void updateSidebar(){
        sidebar.removeAll();

        JButton btn_open = createOpenButton();
        JButton btn_add = createTransferButton();
        JButton btn_deposit = createDepositButton();

        sidebar.add(btn_open);
        sidebar.add(btn_add);
        sidebar.add(btn_deposit);

        // 如果是root模式，顯示額外按鈕
        if (bankManager.isRootMode()) {
            JButton btn_handling = createHandlingFeeButton();
            JButton btn_viewFees = createViewFeesButton();
            sidebar.add(btn_handling);
            sidebar.add(btn_viewFees);
        }
        sidebar.revalidate();
        sidebar.repaint();
    }

    // 創建開戶按鈕
    static JButton createOpenButton() {
        JComboBox<String> bankBox = new JComboBox<>(banksName);
        JTextField field1 = new JTextField(10);
        JPanel open = new JPanel();
        open.add(bankBox);
        open.add(field1);

        JButton btn_open = new JButton("開戶");
        btn_open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bankBox.setSelectedIndex(0);
                field1.setText("");

                int result = JOptionPane.showConfirmDialog(
                        null,
                        open,
                        "請選擇開戶銀行並輸入金額",
                        JOptionPane.OK_CANCEL_OPTION
                );
                if(result == JOptionPane.OK_OPTION){
                    int select = bankBox.getSelectedIndex();
                    try{
                        int money = Integer.parseInt(field1.getText());
                        if(banks.get(select).open_an_account(money))
                            JOptionPane.showMessageDialog(null, "開戶成功");
                        else
                            JOptionPane.showMessageDialog(null, "已經開戶");
                        printMain();
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(null, "輸入錯誤");
                    }
                }
            }
        });
        return btn_open;
    }

    // 創建轉帳按鈕
    static JButton createTransferButton() {
        JComboBox<String> bankFrom = new JComboBox<>(banksName);
        JComboBox<String> bankTo = new JComboBox<>(banksName);
        JTextField field2 = new JTextField(10);
        JPanel transfer = new JPanel();
        transfer.add(bankFrom);
        transfer.add(new JLabel("→"));
        transfer.add(bankTo);
        transfer.add(field2);

        JButton btn_add = new JButton("轉帳");
        btn_add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bankFrom.setSelectedIndex(0);
                bankTo.setSelectedIndex(0);
                field2.setText("");
                int result = JOptionPane.showConfirmDialog(
                        null,
                        transfer,
                        "請選擇兩間銀行並輸入金額",
                        JOptionPane.OK_CANCEL_OPTION
                );
                if(result == JOptionPane.OK_OPTION){
                    int select1 = bankFrom.getSelectedIndex();
                    int select2 = bankTo.getSelectedIndex();
                    try{
                        int money = Integer.parseInt(field2.getText());
                        int fee = bankManager.getHandlingFee(select1, select2);
                        JOptionPane.showMessageDialog(null,
                                "手續費 : " + fee + "元\n" +
                                        "總扣款 : " + (money + fee) + "元");
                        if(banks.get(select1).transfer_money(banks.get(select2), select2, money))
                            JOptionPane.showMessageDialog(null, "轉帳成功");
                        else
                            JOptionPane.showMessageDialog(null, "轉帳失敗");
                        printMain();
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(null, "輸入錯誤");
                    }
                }
            }
        });
        return btn_add;
    }

    // 創存活期按鈕
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
                depositBank.setSelectedIndex(0);
                depositField.setText("");

                int result = JOptionPane.showConfirmDialog(
                        null,
                        depositPanel,
                        "請選擇銀行並輸入存款金額",
                        JOptionPane.OK_CANCEL_OPTION
                );

                if(result == JOptionPane.OK_OPTION){
                    int select = depositBank.getSelectedIndex();
                    try{
                        int money = Integer.parseInt(depositField.getText());
                        if(money <= 0) {
                            JOptionPane.showMessageDialog(null, "存款金額必須大於0");
                            return;
                        }
                        if(banks.get(select).deposit(money))
                            JOptionPane.showMessageDialog(null, "存款成功");
                        else
                            JOptionPane.showMessageDialog(null, "存款失敗，請確認該銀行是否已開戶");
                        printMain();
                    }catch(NumberFormatException exception){
                        JOptionPane.showMessageDialog(null, "輸入錯誤");
                    }
                }
            }
        });
        return btn_deposit;
    }

    //創建手續費設定按鈕
    static JButton createHandlingFeeButton() {
        JButton btn_handling = new JButton("設定手續費");
        btn_handling.addActionListener(e -> showHandlingFeeDialog());
        return btn_handling;
    }

    //創建查看手續費按鈕
    static JButton createViewFeesButton() {
        JButton btn_viewFees = new JButton("查看手續費");
        btn_viewFees.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, bankManager.getAllHandlingFees());
        });
        return btn_viewFees;
    }

    public static void main(String[] args) {
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
        JFrame frame = new JFrame("Bank System");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        //創建頂部面板，包含模式標籤
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modeLabel = new JLabel("當前模式: User");
        modeLabel.setFont(new Font("新細明體", Font.BOLD, 14));
        modeLabel.setForeground(Color.BLUE);
        topPanel.add(modeLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        moneyArea = new JTextArea();
        moneyArea.setFont(new Font("新細明體", Font.PLAIN, 18));
        moneyArea.setEditable(false);
        moneyArea.setFocusable(false);

        bankArea = new JTextArea();
        bankArea.setFont(new Font("新細明體", Font.PLAIN, 18));
        bankArea.setEditable(false);
        bankArea.setFocusable(false);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(moneyArea),
                new JScrollPane(bankArea)
        );
        split.setResizeWeight(0.1);
        split.setDividerSize(0);
        split.setEnabled(false);

        mainPanel.add(split, BorderLayout.CENTER);

        //創建設定按鈕和下拉選單
        JMenuBar menuBar = new JMenuBar();
        JButton settingsBtn = new JButton("設定");
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(settingsBtn);

        //下拉選單
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem rootModeItem = new JMenuItem("進入root管理模式");
        JMenuItem exitRootModeItem = new JMenuItem("退出root管理模式");
        JMenuItem exitItem = new JMenuItem("退出");

        popupMenu.add(rootModeItem);
        popupMenu.add(exitRootModeItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        //設定按鈕事件
        settingsBtn.addActionListener(e -> {
            popupMenu.show(settingsBtn, 0, settingsBtn.getHeight());
        });

        //選單項目事件
        rootModeItem.addActionListener(e ->{
            bankManager.enterRootMode();
            JOptionPane.showMessageDialog(null, "已進入root管理模式");
            updateModeLabel();
            updateSidebar();
        });

        exitRootModeItem.addActionListener(e ->{
            bankManager.exitRootMode();
            JOptionPane.showMessageDialog(null, "已退出root管理模式");
            updateModeLabel();
            updateSidebar();
        });

        exitItem.addActionListener(e ->{
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "確定要退出程式嗎？",
                    "確認退出",
                    JOptionPane.YES_NO_OPTION
            );
            if(confirm == JOptionPane.YES_OPTION){
                System.exit(0);
            }
        });

        //初始化側邊欄
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(60, 60));
        sidebar.setLayout(new FlowLayout());

        //初始狀態為User模式
        updateSidebar();

        mainPanel.add(sidebar, BorderLayout.SOUTH);

        //設定視窗選單列
        frame.setJMenuBar(menuBar);
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        printMain();
    }
}