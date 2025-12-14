import java.util.HashMap;
import java.util.Map;

public class Account{
    private static Account instance;
    private String currentUser = "遊客";
    private boolean isLoggedIn = false;

    //儲存使用者資料
    private static class UserData {
        String username;
        String password;
        int[] bankBalances; //銀行餘額陣列
        boolean[] isBankOpened; //銀行開戶狀態陣列

        UserData(String username, String password) {
            this.username = username;
            this.password = password;
            this.bankBalances = new int[3]; //三家銀行
            this.isBankOpened = new boolean[3];

            for (int i = 0; i < 3; i++) {
                bankBalances[i] = 0;
                isBankOpened[i] = false;
            }
        }
    }

    private Map<String, UserData> users;

    private Account() {
        users = new HashMap<>();

        register("test", "test123");    //預設建立一個測試帳號
        users.put("遊客", new UserData("遊客", ""));
    }

    //單例模式
    public static Account getInstance() {
        if(instance == null){ instance = new Account(); }
        return instance;
    }

    //註冊
    public boolean register(String username, String password){
        if(username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()){ return false; }

        username = username.trim();

        //檢查使用者名稱是否已存在
        if(users.containsKey(username)) {
            return false;   //使用者名稱已存在
        }

        //創建新使用者
        UserData newUser = new UserData(username,password);
        users.put(username, newUser);
        return true;
    }

    //登入
    public boolean login(String username, String password){
        if (username == null || password == null) { return false; }

        saveUserData(); //登入前，儲存當前使用者資料

        UserData user = users.get(username);
        if(user != null && user.password.equals(password)){
            currentUser = username;
            isLoggedIn = true;
            return true;
        }
        return false;
    }

    //登出
    public void logout(){
        saveUserData();
        currentUser = "遊客";
        isLoggedIn = false;
        loadUserData(); //登出後自動載入遊客資料
    }

    //切換到遊客模式
    public void switchToGuest() {
        saveUserData(); //儲存當前使用者資料

        currentUser = "遊客";
        isLoggedIn = false;

        loadUserData(); // 載入遊客模式的資料
    }

    //儲存當前使用者銀行資料
    public void saveUserData(){
        UserData user = users.get(currentUser);
        if(user != null){
            //儲存銀行資料
            for(int i = 0;i < Main.banks.size();i++) {
                Bank bank = Main.banks.get(i);
                user.bankBalances[i] = bank.getCoin();
                user.isBankOpened[i] = bank.getOpen();
            }
        }
    }

    //載入當前使用者銀行資料
    public void loadUserData(){
        UserData user = users.get(currentUser);
        if(user != null){
            //載入銀行資料
            for(int i = 0; i < Main.banks.size(); i++){
                Main.banks.get(i).setCoin(user.bankBalances[i]);
                Main.banks.get(i).setOpen(user.isBankOpened[i]);
            }
        }
    }

    public String getCurrentUser(){ return currentUser; }

    public boolean isLoggedIn(){ return isLoggedIn; }

    // 獲取使用者銀行資料 用於顯示
    public String getUserBankInfo(String username) {
        UserData user = users.get(username);
        if(user == null) { return "使用者不存在"; }

        StringBuilder sb = new StringBuilder();
        sb.append("使用者: ").append(username).append("\n");
        sb.append("銀行狀態:\n");

        char[] bankNames = {'A', 'B', 'C'};
        for(int i = 0; i < 3; i++){
            sb.append("  ").append(bankNames[i]).append("銀行: ");
            if(user.isBankOpened[i]){
                sb.append(user.bankBalances[i]).append("元");
            }else{
                sb.append("未開戶");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //檢查使用者是否存在
    public boolean userExists(String username) { return users.containsKey(username); }
}