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
    public static final int P_IS_EMPTY   = 0;
    public static final int P_LINE       = 1;
    public static final int P_RESET      = 2;
    public static final int P_INPUT_START= 3;
    Register register;
    
    public ShiftRegister(int size){
        super(size + 3);
        Counter counter = new Counter(size);
        components.add(counter);
        Connection c1 = new Connection(this,P_RESET,counter,Counter.P_RESET);
        Connection c2 = new Connection(counter,Counter.P_CARRY,this,P_IS_EMPTY);
        connections.add(c1);
        connections.add(c2);
        register = new Register(size+1);
        connections.add(new Connection(register,0,this,P_LINE));
        for(int i=1;i<register.getPinCount();i++)
            connections.add(new Connection(register,i,register,i-1));    
    }
    
    public boolean isEmpty(){
        return pins[P_IS_EMPTY];
    }
    
    public boolean getCurrentValue(){
        return pins[P_LINE];
    }
    
    @Override
    public void reset(){
        pins[P_RESET] = false;
        for(int i=P_INPUT_START;i < register.getPinCount(); i++)
            register.setPin(i, pins[i]);
    }
    
    @Override
    public String toString(){
        String s = "Line:"+(pins[P_LINE]?"1":"0")+" Reset:"+(pins[P_RESET]?"1":"0")
                +" IsEmpty: "+(pins[P_IS_EMPTY]?"1":"0");
        return s;
    }
    
    @Override
    public void clock(){
        super.clock();
        if(pins[P_RESET])
            reset();
        register.setPin(register.getPinCount()-1, true);
    }
    
}
