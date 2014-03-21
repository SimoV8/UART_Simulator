/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class FifoRegister extends Device{
    public final static int IS_EMPTY   = 0;
    public final static int IS_FULL    = 1;
    protected int from,to;
    
    public FifoRegister(int dimension, int registerSize){
        super(2);
        for(int i=0; i< dimension; i++)
            components.add(new Register(registerSize));
        pins[IS_EMPTY] = true;
        pins[IS_FULL] = false;   
        from = 0; 
        to = 0;
    }
    
    public void writeRegister(boolean[] value){
        if(!pins[IS_FULL])
        {
            ((Register)components.get(to)).setValue(value);
            to = (to + 1)%components.size();
            pins[IS_FULL]  =  (to + 1)%components.size() == from;
            pins[IS_EMPTY] = false;
        }    
    }
    
    public boolean[] readRegister(){
        if(!pins[IS_EMPTY])
        {
            boolean[] value = ((Register)components.get(from)).getValue();
            from = (from + 1)%components.size();
            pins[IS_FULL] = false;
            pins[IS_EMPTY] = from == to;
            return value;
        }
        else
            return new boolean[components.get(0).getPinCount()];
    }
    
    
}
