import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

public class Main {
    static JTextArea moneyArea;
    static JTextArea bankArea;

    static ArrayList<Bank> banks = new ArrayList<>();
    static Vector<String> banksName = new Vector<>();

    static int total_money(){
        int sum = 0;
        for(Bank b : banks){
            sum += b.getCoin();
        }
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

    static void printBank(){
        bankArea.setText(total_banks() + "\n");
    }

    static void printMain(){
        printMoney();
        printBank();
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

        // 開戶按鈕排版
        JComboBox<String> bankBox = new JComboBox<>(banksName);
        JTextField field1 = new JTextField(10);
        JPanel open = new JPanel();
        open.add(bankBox);
        open.add(field1);

        // 轉帳按鈕
        JComboBox<String> bankFrom = new JComboBox<>(banksName);
        JComboBox<String> bankTo = new JComboBox<>(banksName);
        JTextField field2 = new JTextField(10);
        JPanel transfer = new JPanel();
        transfer.add(bankFrom);
        transfer.add(new JLabel("→"));
        transfer.add(bankTo);
        transfer.add(field2);

        // 手續費按鈕
        JComboBox<String> fromBox = new JComboBox<>(banksName);
        JComboBox<String> toBox = new JComboBox<>(banksName);
        JTextField feeField = new JTextField(8);

        JPanel handling = new JPanel();
        handling.add(fromBox);
        handling.add(new JLabel("→"));
        handling.add(toBox);
        handling.add(feeField);

        // 視窗
        JFrame frame = new JFrame("Bank System");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        moneyArea = new JTextArea();
        moneyArea.setFont(new Font("新細明體", Font.PLAIN, 18));
        moneyArea.setEditable(false); // 不可編輯
        moneyArea.setFocusable(false); // 不可聚焦

        bankArea = new JTextArea();
        bankArea.setFont(new Font("新細明體", Font.PLAIN, 18));
        bankArea.setEditable(false); // 不可編輯
        bankArea.setFocusable(false); // 不可聚焦

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(moneyArea),
                new JScrollPane(bankArea)
        );
        split.setResizeWeight(0.1);
        split.setDividerSize(0); // 沒分隔線
        split.setEnabled(false); // 不能拖分隔線

        panel.add(split, BorderLayout.CENTER);

        JButton btn_open = new JButton("開戶");
        btn_open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 清空輸入
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

        JButton btn_add = new JButton("轉帳");
        btn_add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 清空輸入
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
                        JOptionPane.showMessageDialog(null, "手續費 : " + banks.get(select1).getHandlingFee(select2) + "元\n總扣款 : " + (money + banks.get(select1).getHandlingFee(select2)) + "元");
                        if(banks.get(select1).transfer_money(banks.get(select2),select2, money))
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

        JButton btn_handling = new JButton("手續費");
        btn_handling.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    handling,
                    "設定轉帳手續費",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION)
                return;

            int from = fromBox.getSelectedIndex();
            int to = toBox.getSelectedIndex();

            if (from == to) {
                JOptionPane.showMessageDialog(null, "同銀行不需手續費");
                return;
            }

            try {
                int fee = Integer.parseInt(feeField.getText());
                if (fee <= 0) {
                    JOptionPane.showMessageDialog(null, "手續費必須大於 0");
                    return;
                }

                banks.get(from).setHandlingFee(to, fee);
                JOptionPane.showMessageDialog(null,
                        banksName.get(from) + " → " +
                                banksName.get(to) + " 手續費設定為 : " + fee + "元"
                );

            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(null, "輸入錯誤");
            }
        });

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(60, 60));
        sidebar.setLayout(new FlowLayout());
        sidebar.add(btn_open);
        sidebar.add(btn_add);
        sidebar.add(btn_handling);

        panel.add(sidebar, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setLocationRelativeTo(null); // 顯示在正中央
        frame.setVisible(true);

        printMain();
    }
}