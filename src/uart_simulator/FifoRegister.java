/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

import java.util.ArrayList;

/**
 *
 * @author Simone Vuotto
 */
public class FifoRegister extends Device{
    public final static int P_IS_EMPTY   = 2;
    public final static int P_IS_FULL    = 3;
    public final static int P_READ       = 4;
    public final static int P_WRITE      = 5;
    public final static int P_I_START    = 6;
    public final int P_I_END;
    public final int P_O_START;
    public final int P_O_END;
    
    protected int from,to;
    protected ArrayList<Register> fifo;
    
    public FifoRegister(int dimension, int registerSize){
        super(4+registerSize*2);
        fifo = new ArrayList<Register>();
        for(int i=0; i< dimension+1; i++)
            fifo.add(new Register(registerSize));
        pins[P_IS_EMPTY] = true;
        pins[P_IS_FULL]  = false;
        pins[P_READ]     = false;
        pins[P_WRITE]    = false;
        P_I_END = P_I_START + registerSize;
        P_O_START = P_I_END;
        P_O_END = P_O_START + registerSize;
        from = 0; 
        to = 0;
    }
    
    public int getRegisterSize(){
        return fifo.get(0).getPinCount();
    }
    
    public int getCount(){
        return (to - from + fifo.size())%fifo.size();
    }
    
    @Override
    protected void reset_(){
        pins[P_IS_EMPTY] = true;
        pins[P_IS_FULL]  = false;
        from = 0; 
        to = 0;
    }
    
    @Override
    protected boolean update_(int pinId){
        if(!super.update_(pinId))return false;
        if(pins[P_READ]){
           boolean out[] = readRegister();
           System.arraycopy(out, 0, pins, P_O_START, out.length);
        }
        if(pins[P_WRITE]){
            boolean in[] = new boolean[P_I_END - P_I_START];
            System.arraycopy(pins, P_I_START, in, 0, in.length);
            writeRegister(in);
        }
        return true;
    }
    
    public void writeRegister(boolean[] value){
        if(!pins[P_IS_FULL])
        {
            fifo.get(to).setValue(value);
            to = (to + 1)%fifo.size();
            pins[P_IS_FULL]  =  (to + 1)%fifo.size() == from;
            pins[P_IS_EMPTY] = false;
        }    
    }
    
    public boolean[] readRegister(){
        if(!pins[P_IS_EMPTY])
        {
            boolean[] value = fifo.get(from).getValue();
            from = (from + 1)%fifo.size();
            pins[P_IS_FULL] = false;
            pins[P_IS_EMPTY] = from == to;
            return value;
        }
        else
            return new boolean[P_O_END - P_O_START];
    }
    
     @Override
    public String toString(){
        String s = "W:"+pins[P_WRITE]+"\tR:"+pins[P_READ]
                +"\tIsEmpty:"+pins[P_IS_EMPTY]+"\tIsFull:"+pins[P_IS_FULL];
        s+="\tInput:";
        for(int i=P_I_START;i<P_I_END; i++)
            s+=pins[i]?"1":"0";
         s+="\tOutput:";
        for(int i=P_O_START;i<P_O_END; i++)
            s+=pins[i]?"1":"0";
        s+="\tCount:"+getCount();
        return s;
    }
    
}
