package uart_simulator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Simone Vuotto
 */
public class ShiftRegister extends Device {
    public static final int P_IS_EMPTY   = 2;
    public static final int P_LINE       = 3;
    public static final int P_IN         = 4;
    public static final int P_INPUT_START= 5;
    Register register;
    Counter counter;
    
    public ShiftRegister(int size){
        super(size + 4);
        counter = new Counter(size);
        Connection c1 = new Connection(this,P_RESET,counter,P_RESET);
        Connection c2 = new Connection(this,P_CLK,counter,P_CLK);
        
        connections.add(c2);
        connections.add(c1);
        //connections.add(c3);
        register = new Register(size+1);
        connections.add(new Connection(register,0,this,P_LINE));
        for(int i=1;i<register.getPinCount();i++)
            connections.add(new Connection(register,i,register,i-1));
        
        pins[P_IN] = true;
    }
    
    public void setMaxSize(int count){
       counter.setMaxSize(count);
    }
    
    public int getMaxSize(){
        return counter.getMaxSize();
    }
    
    public boolean isEmpty(){
        return pins[P_IS_EMPTY];
    }
    
    public boolean getCurrentValue(){
        return pins[P_LINE];
    }
    
    public Register getRegister(){
        return register;
    }
    
    @Override
    protected void reset(){
        for(int i=P_INPUT_START;i < register.getPinCount(); i++)
            register.setPin(i, pins[i]);
        pins[P_IS_EMPTY] = false;
    }
    
    @Override
    protected void clock(){
        register.setPin(register.getPinCount()-1, pins[P_IN]);     
        super.clock();
        pins[P_IS_EMPTY] = pins[P_IS_EMPTY] || counter.getPin(Counter.P_CARRY);
    }
    
    @Override
    public String toString(){
        String s = "Line:"+(pins[P_LINE]?"1":"0")+" Reset:"+(pins[P_RESET]?"1":"0")
                +" IsEmpty: "+(pins[P_IS_EMPTY]?"1":"0");
        s+="\n"+counter.toString();
        return s;
    }
    
}
