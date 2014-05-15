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
        //Connection c1 = new Connection(this,P_RESET,counter,P_RESET);
        Connection c2 = new Connection(this,P_CLK,counter,P_CLK);   
        connections.add(c2);
        //connections.add(c1);
        register = new Register(size+1);
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
    protected void reset_(){
        counter.reset_();
        for(int i=0; i < register.getPinCount(); i++)
            register.setPin(i, pins[P_INPUT_START+i]);
        pins[P_IS_EMPTY] = false;
    }
    
    @Override
    protected void clock_(){
        pins[P_LINE] = register.getPin(0);
         for(int i=1;i<register.getPinCount();i++)
             register.setPin(i-1, register.getPin(i));
        register.setPin(register.getPinCount()-1, pins[P_IN]);
        super.clock_();
        pins[P_IS_EMPTY] = pins[P_IS_EMPTY] || counter.getPin(Counter.P_CARRY);
    }
    
    @Override
    public String toString(){
        String s = "Line:"+(pins[P_LINE]?"1":"0")+" Reset:"+(pins[P_RESET]?"1":"0")
                +" IsEmpty: "+(pins[P_IS_EMPTY]?"1":"0");
        s+=" "+counter.toString()+" reg: "+register;
        return s;
    }
    
}
