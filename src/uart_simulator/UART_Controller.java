/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

import uart_simulator.UART_16550D.RegisterAddress;

/**
 *
 * @author Simone Vuotto
 */
public class UART_Controller {
    
    
    
    private final UART_16550D uart;
    
    public UART_Controller(UART_16550D uart){
        this.uart = uart;
        reset();
    }
    
    public void initStandardConfiguration(){
        uart.setPin(UART_16550D.PN_RD, true);
        uart.setPin(UART_16550D.PN_WR, true);
        uart.setPin(UART_16550D.PN_ADS, false);
        uart.setPin(UART_16550D.PN_CS2, false);
        uart.setPin(UART_16550D.P_CS1, true);
    }
   
    
    public void defaultConfiguration(){
        uart.nextClock();
        boolean[] LCR = new boolean[]{true,true,true,true,true,false,false,true};
        writeRegister(RegisterAddress.LCR, LCR);
        uart.nextClock();
        writeRegister(RegisterAddress.DLMS, new boolean[]{false,false,false,false,false,false,false,false});
        uart.nextClock();
        writeRegister(RegisterAddress.DLLS, new boolean[]{false,false,false,false,false,false,false,true});
        uart.nextClock();
        LCR[7] = false; //Disable Dlab
        writeRegister(RegisterAddress.LCR, LCR);
        uart.nextClock();
        //I enable FIFO mode and set trigger level = 8
        writeRegister(RegisterAddress.FCR, new boolean[]{true,true,true,false,false,false,true,false});
        uart.nextClock();
        chipSelect(false);        
    }
    
    public void enableReading(){
        uart.setPin(UART_16550D.P_WR, false);
        uart.setPin(UART_16550D.P_RD, true);
        chipSelect(true);
    }
    
    public void enableWriting(){     
        uart.setPin(UART_16550D.P_RD, false);
        uart.setPin(UART_16550D.P_WR, true);
        chipSelect(true);
    }
    
    public void chipSelect(boolean value){
        uart.setPin(UART_16550D.P_CS0, value);
    }
    
    public void writeRegister(RegisterAddress add,boolean[] value){
        uart.setPin(UART_16550D.P_A0, add.a0);
        uart.setPin(UART_16550D.P_A1, add.a1);
        uart.setPin(UART_16550D.P_A2, add.a2);
        for(int i=0;i<UART_16550D.REG_SIZE;i++)
            if(value.length > i)
                uart.setPin(UART_16550D.P_D0+i, value[i]);
            else
                uart.setPin(UART_16550D.P_D0+i, false);
        enableWriting();
        uart.nextClock();
        chipSelect(false);
    }
    
    public boolean[] readRegister(RegisterAddress add){
        uart.setPin(UART_16550D.P_A0, add.a0);
        uart.setPin(UART_16550D.P_A1, add.a1);
        uart.setPin(UART_16550D.P_A2, add.a2);
        enableReading();
        System.out.println(uart);
        uart.nextClock();
        chipSelect(false);
        boolean[] value = new boolean[UART_16550D.REG_SIZE];
        for(int i=0;i<UART_16550D.REG_SIZE;i++)
                value[i] = uart.getPin(UART_16550D.P_D0+i);
        return value;
    }
    
    public final void reset(){
        uart.setPin(UART_16550D.P_MR, true);
        initStandardConfiguration();
        defaultConfiguration();
        uart.setPin(UART_16550D.P_MR, false);
        uart.nextClock();
    }
    
    public final void update(){
        uart.setPin(UART_16550D.P_RCLK, uart.getPin(UART_16550D.PN_BAUDOUT));
    }
    
}
