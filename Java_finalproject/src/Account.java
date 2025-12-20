import java.util.HashMap;
import java.util.Map;

public class Account{
    //private
    private static Account instance;
    private String currentUser = "遊客";  //當前用戶
    private boolean isLogIn = false;
    private Map<String,UserData> users;     //使用者名稱到UserData的映射

    //儲存使用者資料
    private static class UserData{
        String username;
        String password;
        int[] bankBalances; //銀行餘額陣列
        boolean[] isBankOpened; //銀行開戶狀態陣列

        UserData(String username, String password) {
            this.username = username;
            this.password = password;

            //三家銀行
            this.bankBalances = new int[3];
            this.isBankOpened = new boolean[3];
            for (int i = 0; i < 3; i++){
                bankBalances[i] = 0;
                isBankOpened[i] = false;
            }
        }
    }

    private Account() {
        users = new HashMap<>();
        register("test", "test123");    //建立一個測試帳號
        users.put("遊客",new UserData("遊客", ""));
    }

    //public
    //單例模式
    public static Account getInstance(){
        if(instance == null){ instance = new Account(); }
        return instance;
    }

    //註冊
    public boolean register(String username,String password){
        if(username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()){ return false; }

        username = username.trim();

        //檢查使用者名稱是否已存在
        if(users.containsKey(username)) {
            return false;   //使用者名稱已存在
        }

        //創建新使用者
        UserData newUser = new UserData(username,password);
        users.put(username,newUser);
        return true;
    }

    //登入
    public boolean login(String username,String password){
        if(username == null || password == null){ return false; }

        saveUserData();     //登入前先儲存當前使用者資料

        UserData user = users.get(username);
        if(user != null && user.password.equals(password)){
            currentUser = username;
            isLogIn = true;
            return true;
        }
        return false;
    }

    //登出 回到遊客模式
    public void logout(){
        saveUserData();     //登入前先儲存當前使用者資料
        currentUser = "遊客";
        isLogIn = false;
        loadUserData();     //登出後自動載入遊客資料
    }

    //切換到遊客模式
    public void switchToGuest() {
        saveUserData();     //儲存當前使用者資料

        currentUser = "遊客";
        isLogIn = false;
        loadUserData();     //載入遊客模式的資料
    }

    //儲存當前使用者銀行資料
    public void saveUserData(){
        UserData user = users.get(currentUser);     //取得當前使用者的UserData
        if(user != null){
            //儲存銀行資料
            for(int i = 0;i < Main.banks.size();i++) {
                Bank bank = Main.banks.get(i);  //取得第i家銀行
                //複製資料到UserData
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
    public boolean isLogIn(){ return isLogIn; }

    //獲取使用者銀行資料
    public String getUserBankInfo(String username) {
        UserData user = users.get(username);    //從Map取得使用者資料
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
                sb.append("尚未開戶");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
