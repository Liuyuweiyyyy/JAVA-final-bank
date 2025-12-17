public class Bank {
    //private
    private int coin;
    private boolean open;
    private static BankManager bankManager = BankManager.getInstance();

    //public
    public Bank(){
        coin = 0;
        open = false;
    }
    public int getCoin(){ return this.coin; }
    public boolean getOpen(){ return this.open; }
    public void changeCoin(int m){ this.coin += m; }
    public void setCoin(int coin) { this.coin = coin; }
    public void setOpen(boolean open) { this.open = open; }
    //獲取銀行索引
    private int getBankIndex(Bank bank) {
        for(int i = 0;i < Main.banks.size();i++) {
            if(Main.banks.get(i) == bank){
                return i;
            }
        }
        return -1;
    }

    public boolean open_an_account(int money){
        if(open){ return false; }
        this.coin = money;
        this.open = true;
        return true;
    }

    //存錢功能
    public boolean saveMoney(int money){
        if(!open){ return false; }
        if(money <= 0){ return false; }
        this.coin += money;
        return true;
    }

    public boolean transfer_money(Bank to, int money){
        int totalDeduction = money + bankManager.getHandlingFee(getBankIndex(this), getBankIndex(to));

        if(!this.getOpen() || !to.getOpen() || this.getCoin() < totalDeduction){ return false; }
        //轉出方扣除金額和手續費
        this.changeCoin(-totalDeduction);
        to.changeCoin(money);
        return true;
    }
}