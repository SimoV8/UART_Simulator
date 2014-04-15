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

    public Device getFromDev() {
        return fromDev;
    }

    public void setFromDev(Device fromDev) {
        this.fromDev = fromDev;
    }

    public Device getToDev() {
        return toDev;
    }

    public void setToDev(Device toDev) {
        this.toDev = toDev;
    }

    public int getFromPin() {
        return fromPin;
    }

    public void setFromPin(int fromPin) {
        this.fromPin = fromPin;
    }

    public int getToPin() {
        return toPin;
    }

    public void setToPin(int toPin) {
        this.toPin = toPin;
    }
    
    public void update(){
        update(fromDev, fromPin, toDev, toPin);
    }
    
    protected void update(Device fromDev, int fromPin, Device toDev, int toPin){
        toDev.setPin(toPin, fromDev.getPin(fromPin));
    }
    
    public static Connection Not(Device fromDev, int fromPin, Device toDev, int toPin){
        return new Connection(fromDev, fromPin, toDev, toPin){
                @Override
                protected void update(Device fromDev, int fromPin, Device toDev, int toPin){
                    toDev.setPin(toPin, !fromDev.getPin(fromPin));
                }   
        }; 
    }
    
     public static Connection Delta(Device fromDev, int fromPin, Device toDev, int toPin){
        return new Connection(fromDev, fromPin, toDev, toPin){
                boolean previousValue = false;
                @Override
                protected void update(Device fromDev, int fromPin, Device toDev, int toPin){
                    if(previousValue != fromDev.getPin(toPin)){
                        previousValue = fromDev.getPin(toPin);
                        toDev.setPin(toPin, true);
                    }
                }   
        }; 
    }
    
}


