/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class ModemController {
    
    private UART_16550D uart;
    
    ModemController(UART_16550D uart){
        this.uart = uart;
    }
    
    public boolean isDataTerminalReady(){
        return uart.getPin(UART_16550D.PN_DTR);
    }
    
    public void setRI(boolean value){
        uart.setPin(UART_16550D.PN_RI, value);
    }
    
    public void setInputValue(boolean value){
        uart.setPin(UART_16550D.P_SIN, value);
    }
    
    public boolean getOutputValue(){
        return uart.getPin(UART_16550D.P_SOUT);
    }
    
    public void update(){
        //Data Terminal Ready => Data Set Ready
        uart.setPin(UART_16550D.PN_DSR, isDataTerminalReady());
        //Clear To Send (CTS) must be active low (0) only if both RI and RTS are active low
        uart.setPin(UART_16550D.PN_CTS, uart.getPin(UART_16550D.PN_RI) || uart.getPin(UART_16550D.PN_RTS));
        
        uart.setPin(UART_16550D.PN_DCD, uart.getPin(UART_16550D.PN_RI));
    }
    
}
