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
public abstract class Device {
    protected boolean [] pins;
    protected ArrayList<Connection> connections;
    public final static int P_CLK   = 0;
    public final static int P_RESET = 1;
    
    public Device(){
        pins = null;
        connections = new ArrayList<Connection>();
    }
    
    public Device(int pSize){
        pins = new boolean[2+pSize];
        connections = new ArrayList<Connection>();
    }
    
    public void setPin(int index, boolean pinValue){
        if(pins[index] != pinValue)
        {
            pins[index] = pinValue;
            update_(index);
        }
    }
    
    public boolean getPin(int index){
        return pins[index];
    }
    
    public int getPinCount(){
        return pins.length;
    }
      
    public void nextClock(){
        this.setPin(P_CLK, true);
        this.setPin(P_CLK, false);
    }
    
    
    protected void updateConnections(){
        for(int i=0; i < connections.size(); i++)
            connections.get(i).update();
    }
    
    protected boolean update_(int pinIndex){
        updateConnections();
        if(!pins[P_CLK])return false;
        if(pinIndex == P_CLK)
            clock_();
        if(pinIndex == P_RESET && pins[pinIndex])
            reset_();
        return true;
    }
    
    protected void reset_(){}
    
    protected void clock_(){
        if(pins[P_RESET])
            reset_();
    }
    
    @Override
    public String toString(){
        String s = "";
        for(int i=0;i < pins.length; i++)
            s+= pins[i]?"1 ":"0 ";
        return s;
    }
}
