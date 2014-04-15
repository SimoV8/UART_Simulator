/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class Counter extends Device {
    public final static int P_CARRY       = 2;
    public final static int P_COUNT_START = 3;
    
    protected int currentValue;
    protected int maxSize;
    
    public Counter(int size){
        super(1 + (int) Math.ceil(Math.log(size)/Math.log(2)));
        maxSize = size;
    }
    
    public int getCurrentValue(){
        return currentValue;
    }
    
    public void setMaxSize(int size){
        maxSize = size;
    }
    
    public int getMaxSize(){
        return maxSize;
    }
    
    @Override
    protected void reset(){
        for(int i=P_COUNT_START; i < pins.length; i++)
                pins[i] = false;
        currentValue = 0;
        pins[P_CARRY] = false;
    }
    
    @Override
    protected void clock(){
        super.clock();
        if(!pins[P_RESET])
        {
            currentValue++;
            if(currentValue >= maxSize)
            {
                currentValue = 0;
                pins[P_CARRY] = true;
            }
            else
                pins[P_CARRY] = false;
            
            for(int i=P_COUNT_START; i < pins.length; i++){
                pins[i] = ((currentValue >> pins.length-1-i) & 0x0001) == 1;
            }
        }
    }
    
    @Override
    public String toString(){
        String s = "Reset:"+pins[P_RESET]+"\tCarry:"+pins[P_CARRY]+"\tCounter:";
        for(int i=P_COUNT_START;i < pins.length; ++i)
            s+= pins[i]?"1":"0";
        return s;
    }
    
}
