/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

import static uart_simulator.Device.P_CLK;
import static uart_simulator.Device.P_RESET;

/**
 *
 * @author Simone Vuotto
 */
public class UART_16550D extends Device {
    
    public static final int P_XIN       = 0;//(16)External Crystal Input
    public static final int P_MR        = 1;//(35)Master Reset
    public static final int P_D0        = 2;//Data pins (1 - 8)
    public static final int P_D7        = 9;
    public static final int P_RCLK      = 16; //(9)Receiver Clock
    public static final int P_SIN       = 10;//Serial Input
    public static final int P_SOUT      = 11;//Serial Output
    public static final int P_CS0       = 12;//Chip Select
    public static final int P_CS1       = 13;//Chip Select
    public static final int PN_CS2      = 14;//Chip Select
    public static final int PN_BAUDOUT  = 15;
    public static final int P_XOUT      = 17;//External Crystal Output
    public static final int PN_WR       = 18;
    public static final int P_WR        = 19;//Write
    public static final int PN_RD       = 21;
    public static final int P_RD        = 22;//Read
    public static final int P_DDIS      = 23;//Driver Disable
    public static final int PN_TXRDY    = 24;
    public static final int PN_ADS      = 25;//Address Strobe - Not Required
    public static final int P_A2        = 26;
    public static final int P_A1        = 27;
    public static final int P_A0        = 28;
    public static final int PN_RXRDY    = 29;
    public static final int P_INTR      = 30;//Interrupt
    public static final int PN_OUT2     = 31;
    public static final int PN_RTS      = 32;//Request To Send
    public static final int PN_DTR      = 33;//Data Terminal Ready
    public static final int PN_OUT1     = 34;
    public static final int PN_CTS      = 36;//Clear to send
    public static final int PN_DSR      = 37;//Data Set Ready
    public static final int PN_DCD      = 38;//Data Carrier Detect
    public static final int PN_RI       = 39;//Ring Indicator
    
    public static final int FIFO_SIZE = 16;
    
    
    /* Line Control Register
     * bits 0-1: specify the number of bits in each transmitted or received serial character.
     *  * 00 = 5 bits; 01 = 6 bits; 10 = 7 bits; 11 = 8 bits;
     * bit 2: speifies the number of stop bits: if 0 there is one stop bit, otherwhise two
     * bit 3: parity bit enabled
     * bit 4: even parity select bit. When it's 0 a odd number of 1s is transmitted or checked, 
     * when it's 1 an even number of 1s is transmitted or checked
     * bit 5: stick parity bit. When bits 3, 4 and 5 are logic 1 the Parity bit is transmitted 
     * and checked as a logic 0. If bits 3 and 5 are 1 and bit 4 is a logic 0 then the Parity bit is 
     * transmitted and checked as a logic 1. If bit 5 is a logic 0 Stick Parity is disabled.
     * bit 6: Break Control bit. It causes a break condition to be transmitted to the receiving UART. 
     * When it is set to a logic 1, the serial output (SOUT) is forced to the Spacing (logic 0) state.
     * The break is disabled by setting bit 6 to a logic 0. The Break Control bit acts only on SOUT 
     * and has no effect on the transmitter logic.
     * bit 7: Divisor Latch Access Bit (DLAB). It must be set high (logic 1) to access the Divisor
     * Latches of the Baud Generator during a Read or Write operation. It must be set low (logic 0)
     * to access the Receiver Buffer, the Transmitter Holding Register, or the Interrupt Enable Register.
     */
    final Register LCR = new Register();
    
    final Register LSR = new Register(); //Line Status Register
    /**
     * Bit 0: Writing a 1 to FCR0 enables both the XMIT and RCVR FIFOs 
     * Resetting FCR0 will clear all bytes in both FIFOs
     */
    final Register FCR = new Register(); //Fifo Control Register
    final Register IER = new Register(); //Interrupt Enable Register
    final Register IIR = new Register(); //Interrupt Identification Register
    final Register MCR = new Register(); //Modem Control Register
    final Register MSR = new Register(); //Modem Status Register
    
    final Register RBR = new Register(); //Receiver Buffer Register
    final Register THR = new Register(); //Transmitter Holding Register
    
    //It is intended as a scratchpad register to be used by the programmer to hold data temporarily
    final Register scratchpad = new Register(); // This register doesn't control the UART in anyway
    
    final ShiftRegister RCVR = new ShiftRegister(12);
    final ShiftRegister XMIT = new ShiftRegister(12);
    final FifoRegister RCVR_FIFO = new FifoRegister(FIFO_SIZE,11);
    final FifoRegister XMIT_FIFO = new FifoRegister(FIFO_SIZE,8);
   
    final BaudGenerator baudGenerator = new BaudGenerator();
    
    boolean rbrFlag,thrFlag;
    int characterLength,stopLength;
    boolean parityEnable,stickParity,evenParitySelect;
    
    public UART_16550D(){
        super(40);
        initConnections();
        initRegisters();
        reset();
    }
    
    private void initRegisters(){
        LSR.setPin(0, true);
        LSR.setPin(1, true);
    }
    
    private void initConnections(){
        connections.add(Connection.Not(this, PN_DSR, LSR, 0));
        
        //Modem Control Register Connections
        connections.add(Connection.Not(MCR, 0, this, PN_DTR));
        connections.add(Connection.Not(MCR, 1, this, PN_RTS));
        connections.add(Connection.Not(MCR, 2, this, PN_OUT1));
        connections.add(Connection.Not(MCR, 3, this, PN_OUT2));
        //Bit 4 is used for debug mode
        //Bit 5 to 7 of MCR are permanently set to 0.
        
        //Modem Status Register Connections
        connections.add(Connection.Delta(this, PN_CTS, MSR, 0));
        connections.add(Connection.Delta(this, PN_DSR, MSR, 1));
        connections.add(Connection.Delta(this, PN_RI, MSR, 2));
        connections.add(Connection.Delta(this, PN_DCD, MSR, 3));
        connections.add(Connection.Not(this, PN_CTS, MSR, 4));
        connections.add(Connection.Not(this, PN_DSR, MSR, 5));
        connections.add(Connection.Not(this, PN_RI, MSR, 6));
        connections.add(Connection.Not(this, PN_DCD, MSR, 7));
        
        /*Receiver Connections*/
        connections.add(new Connection(this,P_RCLK,RCVR,P_CLK));
        connections.add(new Connection(this,P_SIN,RCVR,ShiftRegister.P_IN));
        connections.add(new Connection(this,P_XIN,RCVR_FIFO,P_CLK));
        connections.add(Connection.Not(FCR, 0, RCVR_FIFO, P_RESET));
        
        /*Transmitter Connections*/
        connections.add(new Connection(this,PN_BAUDOUT,XMIT,P_CLK));
        connections.add(new Connection(XMIT,ShiftRegister.P_LINE,this,P_SOUT));
        connections.add(new Connection(this,P_XIN,XMIT_FIFO,P_CLK));
        connections.add(Connection.Not(FCR, 0, XMIT_FIFO, P_RESET));
        
        for(int i=0;i<XMIT_FIFO.getRegisterSize();i++)
            connections.add(new Connection(XMIT_FIFO,XMIT_FIFO.P_O_START+i,XMIT,ShiftRegister.P_INPUT_START+i));
        
        /*BaudGenerator Connections*/
        connections.add(new Connection(this,P_XIN,baudGenerator,P_CLK));
        connections.add(new Connection(this,P_MR,baudGenerator,P_RESET));
        connections.add(Connection.Not(baudGenerator,BaudGenerator.P_BAUDOUT,this,PN_BAUDOUT));
        
    } //End initConnections()
    
    @Override
    public final void reset(){
        MSR.setValue(new boolean[]{true,true,true,true});
        characterLength = 5 + (LSR.getPin(0)?1:0)*2 + (LSR.getPin(1)?1:0);
        stopLength = LSR.getPin(2)?2:1;
        
        parityEnable= LSR.getPin(3);
        evenParitySelect = LSR.getPin(4);
        stickParity = LSR.getPin(5);
        RCVR.setMaxSize(1/*start bit*/+characterLength+stopLength+(parityEnable?1:0));
        XMIT.setMaxSize(1/*start bit*/+characterLength+stopLength+(parityEnable?1:0));
        rbrFlag = false;
        thrFlag = false;   
    }
    
    @Override
    public boolean update(int pinId){
        if(!super.update(pinId))return false;
        updateRegisters();
        if(LCR.getPin(6)/*Break Control bit*/)pins[P_SOUT] = false;
        return true;
    }
    
    protected void updateRegisters(){
        if(isReadMode()){
            boolean[] value = readRegister();
            if(value!= null) System.arraycopy(value, 0, pins, P_D0, 8);
        } else if(isWriteMode()){
            boolean [] value = new boolean[8];
            System.arraycopy(pins, P_D0, value, 0, 8);
            writeRegister(value);
        }
    }
    
    protected boolean[] readRegister(){
        boolean[] value = null;
        boolean a0 = pins[P_A0],a1 = pins[P_A1],a2 = pins[P_A2];
        if(!a0 && !a1 && !a2){
            if(!LSR.getPin(7)/*DLAB*/) value = readReceiverBuffer();
            else value = baudGenerator.getDivisorLatchLeastSignificantByte();
        }
        if(!a0 && !a1 && a2)  {
            if(!LSR.getPin(7)/*DLAB*/) value = IER.getValue();
            else value = baudGenerator.getDivisorLatchLeastSignificantByte();
        }
        if(!a0 && a1 && !a2)  value = IIR.getValue();
        if(!a0 &&  a1 &&  a2) value = LCR.getValue();
        if( a0 && !a1 && !a2) value = MCR.getValue();
        if( a0 && !a1 &&  a2) value = LSR.getValue();
        if( a0 &&  a1 && !a2) value = MSR.getValue();
        if( a0 &&  a1 &&  a2) value =scratchpad.getValue();
        return value;
    }
    
    protected boolean[] readReceiverBuffer(){
        if(FCR.getPin(0)){
            rbrFlag = false;
            return RBR.getValue();
        }
        else
            return RCVR_FIFO.readRegister();
    }
    
    protected void writeRegister(boolean[] value){
        boolean a0 = pins[P_A0],a1 = pins[P_A1],a2 = pins[P_A2];
        if(!a0 && !a1 && !a2){
            if(!LSR.getPin(7)/*DLAB*/) writeTransmitterRegister(value);
            else baudGenerator.setDivisorLatchLeastSignificantByte(value);
        }
        if(!a0 && !a1 && a2){
            if(!LSR.getPin(7)/*DLAB*/) IER.setValue(value);
            else baudGenerator.setDivisorLatchLeastSignificantByte(value);
        }
        if(!a0 &&  a1 && !a2) FCR.setValue(value);
        if(!a0 &&  a1 &&  a2) LCR.setValue(value);
        if( a0 && !a1 && !a2) MCR.setValue(value);
        if( a0 && !a1 &&  a2) LSR.setValue(value);
        if( a0 &&  a1 && !a2) MSR.setValue(value);
        if( a0 &&  a1 &&  a2) scratchpad.setValue(value);
    }
    
    protected void writeTransmitterRegister(boolean[] value){
        THR.setValue(value);
        XMIT_FIFO.writeRegister(value);
        thrFlag = true;
    }
    
    public boolean isChipSelected(){
        return pins[P_CS0] && pins[P_CS1] && !pins[PN_CS2];
    }
    
    public boolean isWriteMode(){
        return (pins[P_WR] || !pins[PN_WR]) && isChipSelected();
    }
    
    public boolean isReadMode(){
        return (pins[P_RD] || !pins[PN_RD]) && isChipSelected() && !isWriteMode();
    }
    
    @Override
    public void clock(){
        pins[P_DDIS] = !isReadMode();
       
        receiverControlLogic();
        transmitterControlLogic();
        modemControlLogic();
        interruptControlLogic();
        super.clock();
    }
    
    private void receiverControlLogic(){
         if(RCVR.getPin(ShiftRegister.P_IS_EMPTY))
            manageReceiverShiftRegister();
    }
    
    private void transmitterControlLogic(){
        if(XMIT.getPin(ShiftRegister.P_IS_EMPTY))
            manageTransmitterShiftRegister();
    }
    
    private void manageReceiverShiftRegister(){
       boolean[] character = new boolean[8];
       boolean framingError = false,parityError = false,breakInterrupt = false;
       boolean[] rcvr = RCVR.getRegister().getValue();
       int index = rcvr.length - RCVR.getMaxSize();    
       for(int i=index; i<rcvr.length;i++)
           breakInterrupt |= rcvr[i];
       
       System.arraycopy(rcvr, index, character, 0, characterLength);
       index+=characterLength;
       if(parityEnable){
           boolean parityBit = rcvr[index]; 
           for(int i=0;i< characterLength;++i)
               parityBit ^= character[i];
           parityError = parityBit != evenParitySelect;
           ++index;
       }
       for(;index<rcvr.length;++index)
            framingError |= rcvr[index];
       RBR.setValue(character);
       
       LSR.setPin(1/*Overrun Error*/, LSR.getPin(0/*Data Ready*/));
       if(FCR.getPin(0)/*Fifo Enabled*/){
           for(int i=0;i<character.length;i++)
               RCVR_FIFO.setPin(FifoRegister.P_I_START+i, character[i]);
           RCVR_FIFO.setPin(FifoRegister.P_I_START+8, parityError);
           RCVR_FIFO.setPin(FifoRegister.P_I_START+9, framingError);
           RCVR_FIFO.setPin(FifoRegister.P_I_START+10, breakInterrupt);
           RCVR_FIFO.setPin(FifoRegister.P_WRITE,true);
           RCVR_FIFO.setPin(FifoRegister.P_WRITE,false);
           LSR.setPin(0/*Data Ready*/, RCVR_FIFO.getPin(FifoRegister.P_IS_FULL));
           LSR.setPin(7, parityError|framingError|breakInterrupt);
       }else{
           RBR.setValue(character);
           LSR.setPin(0/*Data Ready*/, true);
           LSR.setPin(2, parityError);
           LSR.setPin(3, framingError);
           LSR.setPin(4, breakInterrupt);
       }
       RCVR.reset();
    }
           
    private void manageTransmitterShiftRegister(){
        boolean [] character; 
        if(FCR.getPin(0)/*Fifo Enabled*/){
            if(XMIT_FIFO.getPin(FifoRegister.P_IS_EMPTY)){
                LSR.setPin(6/*Transmitter Empty*/, true);
                return;
            }
            character = XMIT_FIFO.readRegister();
            LSR.setPin(5, XMIT_FIFO.getPin(FifoRegister.P_IS_EMPTY));//Transmitter Holding Register Empty    
        } else {
            if(LSR.getPin(5)){
                LSR.setPin(6/*Transmitter Empty*/, true);
                return;
            }
            character = THR.getValue();
            LSR.setPin(5, true);
        }
        int index = ShiftRegister.P_INPUT_START;
        XMIT.setPin(index++, false);
        boolean parity = false;
        for(int i=0;i<characterLength;i++){
            XMIT.setPin(index++, character[i]);
            parity^=character[i];
        }
        if(parityEnable)
            XMIT.setPin(index++, parity^evenParitySelect);
        for(;index<XMIT.getPinCount();++index)
            XMIT.setPin(index, true);
        XMIT.reset();
    }   
    
    private void modemControlLogic(){
        
    }
    
    private void interruptControlLogic(){
        
    }
    
}
