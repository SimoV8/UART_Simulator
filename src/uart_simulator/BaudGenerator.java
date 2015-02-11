/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class BaudGenerator extends Device{
    
    public final static int P_BAUDOUT = 2;
    private final Counter c1,c2;
    private final Register divisorLatch= new Register(16);
    boolean baudout;
    
    public BaudGenerator(){
        super(1);
        c1 = new Counter(65536);
        c2 = new Counter(8);
        connections.add(new Connection(this,P_CLK,c1,P_CLK));
        connections.add(new Connection(this,P_RESET,c1,P_RESET));
        connections.add(new Connection(this,P_RESET,c2,P_RESET));
        divisorLatch.setPin(15, true);
    }
    
    public void setDivisorLatchLeastSignificantByte(boolean[] value){
       divisorLatch.setValue(value, 8);
       c1.reset_();
       c2.reset_();
       baudout = false;
    }
    
    public boolean[] getDivisorLatchLeastSignificantByte(){
        return divisorLatch.getValue(8, 8);
    }
    
    public void setDivisorLatchMostSignificantByte(boolean[] value){
        divisorLatch.setValue(value,0);
        c1.reset_();
        c2.reset_();
        baudout = false;
    }
    
    public boolean[] getDivisorLatchMostSignificantByte(){
        return divisorLatch.getValue(0, 8);
    }
    
    @Override
    protected void clock_(){
        super.clock_();
        for(int i=0;i<divisorLatch.getSize();i++)
            if(c1.getPin(Counter.P_COUNT_START + i) != divisorLatch.getPin(i))
                return;
        c2.nextClock();
        if(c2.getPin(Counter.P_CARRY))
            baudout = !baudout;
        pins[P_BAUDOUT] = baudout;
        c1.reset_();
    }
    
    @Override
    public String toString(){
        String s = "BaudGenerator - baudout: "+pins[P_BAUDOUT]+"\tdivisorLatch: ";
        for(int i=0;i<divisorLatch.getSize(); ++i)
            s+= divisorLatch.getPin(i)?"1":"0";
        s+="\tc1: "+c1.getCurrentValue()+"\tc2: "+c2.getCurrentValue();
        return s;
    }
}
