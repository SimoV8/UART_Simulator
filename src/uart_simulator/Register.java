/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class Register extends Device{
    private final static int DEFAULT_SIZE = 8;
    
    public Register(){
        pins = new boolean[DEFAULT_SIZE];
    }
    
    public Register(int size){
        pins = new boolean[size];
        reset(false);
    }
    
    public final void reset(boolean value){
        for(int i=0;i<pins.length;i++)
            pins[i] = value;
    }
         
    @Override
    protected boolean update_(int pinId){return true;}
    
    public boolean[] getValue(){
        boolean[] value = new boolean[getSize()];
        System.arraycopy(pins, 0, value, 0, getSize());
        return value;
    }
    
    public boolean[] getValue(int offset,int length){
        boolean[] value = new boolean[length];
        System.arraycopy(pins, offset, value, 0, length);
        return value;
    }
    
    public void setValue(boolean[] value){
        System.arraycopy(value, 0, pins, 0, Math.min(pins.length, value.length));
    }
    
    public void setValue(boolean[] value,int offset){
        System.arraycopy(value, 0, pins, offset, Math.min(pins.length - offset, value.length));
    }
    
    public int getSize(){
        return pins.length;
    }
    
    @Override
    public String toString(){
        String s = new String();
        for(int i=0;i<pins.length;i++)
            s +=(pins[i]?"1":"0");
        return s;
    }
    
}