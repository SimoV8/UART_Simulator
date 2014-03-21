/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class Connection {
    private Device fromDev,toDev;
    private int fromPin,toPin;
    
    public Connection(Device fromDev, int fromPin, Device toDev, int toPin){
        this.fromDev = fromDev;
        this.fromPin = fromPin;
        this.toDev = toDev;
        this.toPin = toPin;
    }
    
    public void update(){
        toDev.setPin(toPin, fromDev.getPin(fromPin));
    }
        
}
