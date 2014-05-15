/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class Channel {
    
    ModemController modem1,modem2;
    
    public Channel(ModemController m1, ModemController m2){
        modem1 = m1;
        modem2 = m2;
    }
    
    public void update(){
        modem1.setRI(modem2.isDataTerminalReady());
        modem2.setRI(modem1.isDataTerminalReady());
        modem1.setInputValue(modem2.getOutputValue());
        modem2.setInputValue(modem1.getOutputValue());
    }
    
}
