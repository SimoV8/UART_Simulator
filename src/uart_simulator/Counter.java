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
    int currentValue;
    final int maxSize;
    public final static int P_RESET       = 0;
    public final static int P_CARRY       = 1;
    public final static int P_COUNT_START = 2;
    
    public Counter(int size){
        super(2 + (int) Math.ceil(Math.log(size)/Math.log(2)));
        maxSize = size;
    }
    
    public int getCurrentValue(){
        return currentValue;
    }
    
    @Override
    public void reset(){
        for(int i=0; i < pins.length; i++)
                pins[i] = false;
            currentValue = 0;
    }
    
    @Override
    public void clock(){
        super.clock();
        if(pins[P_RESET] == true)
            reset();
        else
        {
            currentValue++;
            if(currentValue == maxSize)
            {
                currentValue = 0;
                pins[P_CARRY] = true;
            }
            else
                pins[P_CARRY] = false;
            
            for(int i=P_COUNT_START; i < pins.length; i++)
                pins[i] = ((currentValue >> i-P_COUNT_START) & 0x0001) == 1;
        }
    }
    
}
