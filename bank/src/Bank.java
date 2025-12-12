public class Bank {
    int coin;
    boolean open;

    public Bank(){
        coin = 0;
        open = false;
    }

    public int getCoin(){
        return this.coin;
    }
    public boolean getOpen(){
        return this.open;
    }

    public void changeCoin(int m){
        this.coin += m;
    }


    public boolean open_an_account(int money){
        if(open)
            return false;
        this.coin = money;
        this.open = true;
        return true;
    }
    public boolean transfer_money(Bank to, int money){
        if(!this.getOpen() || !to.getOpen() || this.getCoin() < money)
            return false;
        this.changeCoin(-money);
        to.changeCoin(money);
        return true;
    }

}
