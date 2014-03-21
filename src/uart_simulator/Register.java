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
    
    public Register(int size){
        super(size);
    }
    
    public boolean[] getValue(){
        return pins;
    }
    
    public void setValue(boolean[] value){
        System.arraycopy(value, 0, pins, 0, pins.length);
    }
    
}