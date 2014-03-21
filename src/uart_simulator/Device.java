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
    protected ArrayList<Device> components;
    protected ArrayList<Connection> connections;
    //public final static int P_CLK   = 0;
    //public final static int P_RESET = 1;
    
    public Device(int pSize){
        pins = new boolean[pSize];
        components = new ArrayList<Device>();
        connections = new ArrayList<Connection>();
    }
    
    public void setPin(int index, boolean pinValue){
        pins[index] = pinValue;
        /*if(pins[index] != pinValue)
        {
            pins[index] = pinValue;
            update(index);
        }*/
    }
    
    public boolean getPin(int index){
        return pins[index];
    }
    
    public int getPinCount(){
        return pins.length;
    }
      
    /*public boolean update(int pinId){
        if(pinId == P_CLK && pins[pinId]){
            clock();
            return true;
        }
        if(pinId == P_RESET && pins[pinId]){
            reset();
            return true;
        }
        return false;
    }*/
    
    public void reset(){}
    
    public void clock(){
        for(int i=0; i < components.size(); i++)
            components.get(i).clock();
        for(int i=0; i < connections.size(); i++)
            connections.get(i).update();
    }
    
    @Override
    public String toString(){
        String s = "";
        for(int i=0;i < pins.length; i++)
            s+= pins[i]?"1 ":"0 ";
        return s;
    }
}
