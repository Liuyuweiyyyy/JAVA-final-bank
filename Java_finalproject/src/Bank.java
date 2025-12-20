public class Bank{
    //private
    private int coin;
    private boolean open;
    private static BankManager bankManager = BankManager.getInstance();     //取得手續費

    //public
    public Bank(){
        coin = 0;
        open = false;
    }
    public int getCoin(){ return this.coin; }
    public boolean getOpen(){ return this.open; }
    public void changeCoin(int m){ this.coin += m; }
    public void setCoin(int coin){ this.coin = coin; }
    public void setOpen(boolean open){ this.open = open; }

    //獲取bank在banks列表中的索引位置
    private int getBankIndex(Bank bank){
        for(int i = 0;i < Main.banks.size();i++){
            if(Main.banks.get(i) == bank){
                return i;
            }
        }
        return -1;
    }

    //開戶
    public boolean open_an_account(int money){
        if(open){ return false; }
        this.coin = money;
        this.open = true;
        return true;
    }

    //存錢
    public boolean saveMoney(int money){
        if(!open){ return false; }
        if(money <= 0){ return false; }
        this.coin += money;
        return true;
    }

    //領錢
    public boolean receiveMoney(int money){
        if(!open) { return false; }
        if(money <= 0) { return false; }
        if(this.coin < money) { return false; }
        this.coin -= money;
        return true;
    }

    //轉帳
    public boolean transferMoney(Bank to, int money){
        //總扣款 金額+手續費
        int totalDeduction = money + bankManager.getHandlingFee(getBankIndex(this), getBankIndex(to));
        //檢查雙方是否都已經開戶且餘額足夠
        if(!this.getOpen() || !to.getOpen() || this.getCoin() < totalDeduction){ return false; }
        this.changeCoin(-totalDeduction);   //扣除 金額+手續費
        to.changeCoin(money);
        return true;
    }
}
