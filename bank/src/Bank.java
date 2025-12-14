import java.util.HashMap;
import java.util.Map;

public class Bank {
    int coin;
    boolean open;
    Map<Integer, Integer> handlingFee;

    public Bank(){
        coin = 0;
        open = false;
        handlingFee = new HashMap<>();
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

    public void setHandlingFee(int index, int money){
        this.handlingFee.put(index, money);
    }

    public boolean open_an_account(int money){
        if(open)
            return false;
        this.coin = money;
        this.open = true;
        return true;
    }
    public boolean transfer_money(Bank to,int index, int money){
        money += getHandlingFee(index);
        if(!this.getOpen() || !to.getOpen() || this.getCoin() < money)
            return false;
        this.changeCoin(-money);
        to.changeCoin(money);
        return true;
    }

    public int getHandlingFee(int index){
        return this.handlingFee.getOrDefault(index, 0);
    }
}
