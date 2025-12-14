import java.util.HashMap;
import java.util.Map;

public class BankManager{
    //private
    private static BankManager instance;
    private Map<Integer, Map<Integer, Integer>> handlingFees; //各個銀行間的手續費
    private boolean isRootMode = false;
    private BankManager() {
        handlingFees = new HashMap<>();
        // 初始化所有銀行間手續費為5
        for(int i = 0;i < 3;i++){
            handlingFees.put(i, new HashMap<>());
            for(int j = 0;j < 3;j++){
                if(i != j){
                    handlingFees.get(i).put(j,5);
                }
            }
        }
    }
    //public
    //單例模式
    public static BankManager getInstance() {
        if(instance == null){ instance = new BankManager(); }
        return instance;
    }
    //設定手續費
    public boolean setHandlingFee(int fromBankIndex, int toBankIndex, int fee) {
        if(!isRootMode) {
            System.out.println("尚未進入root管理模式");
            return false;
        }
        if(fromBankIndex == toBankIndex) {
            System.out.println("同銀行不需手續費");
            return false;
        }
        if(fee < 0) {
            System.out.println("手續費需大於等於0");
            return false;
        }
        if(handlingFees.containsKey(fromBankIndex)) {
            handlingFees.get(fromBankIndex).put(toBankIndex,fee);
            return true;
        }
        return false;
    }
    //獲取手續費
    public int getHandlingFee(int fromBankIndex, int toBankIndex) {
        if(fromBankIndex == toBankIndex) return 0;
        if(handlingFees.containsKey(fromBankIndex)) {
            return handlingFees.get(fromBankIndex).getOrDefault(toBankIndex,0);
        }
        return 0;
    }
    //進入root模式
    public void enterRootMode() {
        isRootMode = true;
        System.out.println("進入root管理模式");
    }
    //退出root模式
    public void exitRootMode() {
        isRootMode = false;
        System.out.println("退出root管理模式");
    }
    // 檢查是否為root模式
    public boolean isRootMode() { return isRootMode; }
    // 顯示所有手續費
    public String getAllHandlingFees() {
        StringBuilder sb = new StringBuilder();
        sb.append("當前手續費設定:\n");
        for(int i = 0;i < 3;i++) {
            for(int j = 0;j < 3;j++) {
                if(i != j) {
                    char fromBank = (char)('A' + i);
                    char toBank = (char)('A' + j);
                    sb.append(String.format("銀行%c -> 銀行%c: %d元\n",
                            fromBank, toBank, getHandlingFee(i,j)));
                }
            }
        }
        return sb.toString();
    }
}