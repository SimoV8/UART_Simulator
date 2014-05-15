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
    public static final int P_A2        = 26;//Register Address(2)
    public static final int P_A1        = 27;//Register Address(1)
    public static final int P_A0        = 28;//Register Address(0)
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
    public static final int REG_SIZE  = 8;
    
    public enum RegisterAddress{
        RBR(false,false,false,0,0),
        THR(false,false,false,0,1),
        IER(false,false,true),
        IIR(false,true,false,-1,0),
        FCR(false,true,false,-1,1),
        LCR(false,true,true),
        MCR(true,false,false),
        LSR(true,false,true),
        MSR(true,true,false),
        Scratch(true,true,true),
        DLLS(false,false,false,1,-1),//Divisor Latch Least Significant byte 
        DLMS(false,false,true,1,-1); //Divisor Latch Most Significant byte
        
        public final boolean a0,a1,a2;
        /*
         * dlab: -1 => both, 0 => disabled, 1 = enabled
         * rwMode: -1 => both, 0 => readMode, 1 = writeMode
         */
        public final byte dlab,rwMode;
        
        RegisterAddress(boolean a0,boolean a1,boolean a2){
            this(a0, a1, a2, -1, -1);
        }
        
        RegisterAddress(boolean a0,boolean a1,boolean a2,int dlab,int rwMode){
            this.a0 = a0;
            this.a1 = a1;
            this.a2 = a2;
            this.dlab = (byte) dlab;
            this.rwMode = (byte) rwMode;
        }
        
        public static RegisterAddress find(boolean a0,boolean a1,boolean a2,boolean dlab,boolean writeMode)
        {
            for(RegisterAddress ra : RegisterAddress.values())
            {
                if(ra.a0 == a0 & ra.a1 == a1 & ra.a2 == a2 &
                  (ra.dlab<0 | ra.dlab == (dlab?1:0)) & (ra.rwMode<0 | ra.rwMode == (writeMode?1:0)))
                    return ra;
            }
            return null;
        }
        
    }
    
    
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
     * Bit 1: Writing a 1 to FCR1 clears all bytes in the RCVR FIFO 
     * and resets its counter logic to 0. The shift register is not
     * cleared. The 1 that is written to this bit position is self-clearing.
     * Bit 2: Writing a 1 to FCR2 clears all bytes in the XMIT FIFO 
     * and resets its counter logic to 0. The shift register is not 
     * cleared. The 1 that is written to this bit position is self-clearing.
     * Bit 3: Setting FCR3 to a 1 will cause the RXRDY and TXRDY pins to change 
     * from mode 0 to mode 1 if FCR0=1 (see description of RXRDY and TXRDY pins).
     * Bit 4, 5: FCR4 to FCR5 are reserved for future use.
     * Bit 6, 7: FCR6 and FCR7 are used to set the trigger level for
     * the RCVR FIFO interrupt.
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
   
    /* 
     * These variables store the configuration of the line. They are updated 
     * when Master Reset is enabled. 
     */
    int characterLength,stopLength;
    boolean parityEnable,stickParity,evenParitySelect;
    
    
    boolean receptionEnabled,transmissionEnabled;
    int rcvrFifoErrorCounter;
    
    public UART_16550D(){
        super(40);
        initConnections();
        initRegisters();
        reset_();
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
        
        /*BaudGenerator Connections*/
        connections.add(new Connection(this,P_XIN,baudGenerator,P_CLK));
        connections.add(new Connection(this,P_MR,baudGenerator,P_RESET));
        connections.add(Connection.Not(baudGenerator,BaudGenerator.P_BAUDOUT,this,PN_BAUDOUT));
        
        /*Receiver Connections*/
        connections.add(new Connection(this,P_RCLK,RCVR,P_CLK));
        connections.add(new Connection(this,P_MR,XMIT,P_RESET));
        connections.add(new Connection(this,P_SIN,RCVR,ShiftRegister.P_IN));
        connections.add(new Connection(this,P_XIN,RCVR_FIFO,P_CLK));
        connections.add(new Connection(this, P_MR, RCVR_FIFO, P_RESET));
        
        /*Transmitter Connections*/
        connections.add(new Connection(baudGenerator,BaudGenerator.P_BAUDOUT,XMIT,P_CLK));
        connections.add(new Connection(this,P_MR,XMIT,P_RESET));
        connections.add(new Connection(XMIT,ShiftRegister.P_LINE,this,P_SOUT));
        connections.add(new Connection(this,P_XIN,XMIT_FIFO,P_CLK));
        connections.add(new Connection(this, P_MR, XMIT_FIFO, P_RESET));
        XMIT.setPin(ShiftRegister.P_IN,true);//When UART doesn't transmit anything, the line holds an high value
        
        //for(int i=0;i<XMIT_FIFO.getRegisterSize();i++)
        //    connections.add(new Connection(XMIT_FIFO,XMIT_FIFO.P_O_START+i,XMIT,ShiftRegister.P_INPUT_START+i));
        
    } //End initConnections()
    
    @Override
    public final void reset_(){
        MSR.setValue(new boolean[]{false,false,false,false});
        LSR.reset(false);
        rcvrFifoErrorCounter = 0;
        transmissionEnabled = false;
        receptionEnabled = false;
        for(int i=ShiftRegister.P_INPUT_START; i < XMIT.getPinCount(); ++i)
            XMIT.setPin(i, true);
        XMIT.setPin(ShiftRegister.P_LINE, true);
        XMIT.reset_();
        for(int i=ShiftRegister.P_INPUT_START; i < XMIT.getPinCount(); ++i)
            RCVR.setPin(i, true);
        RCVR.reset_();
        pins[P_SOUT] = true;
        pins[P_INTR] = false;
        pins[PN_RTS] = true;
        pins[PN_DTR] = true;
        pins[PN_OUT1]= true;
        pins[PN_OUT2]= true;
        
        pins[PN_DSR] = true;
        pins[PN_CTS] = true;
        pins[PN_DCD] = true;
        pins[PN_RI]  = true;
        pins[P_SIN] = true;
        
        characterLength = 5 + (LCR.getPin(0)?1:0)*2 + (LCR.getPin(1)?1:0);
        stopLength = LCR.getPin(2)?2:1;
        
        parityEnable= LCR.getPin(3);
        evenParitySelect = LCR.getPin(4);
        stickParity = LCR.getPin(5);
        RCVR.setMaxSize(1/*start bit*/+characterLength+stopLength+(parityEnable?1:0));
        XMIT.setMaxSize(1/*start bit*/+characterLength+stopLength+(parityEnable?1:0));     
    }
    
    @Override
    public boolean update_(int pinId){
        if(!super.update_(pinId))return false;
        if(LCR.getPin(6)/*Break Control bit*/)pins[P_SOUT] = false;
        return true;
    }
    
     @Override
     public void clock_(){
        pins[P_DDIS] = !isReadMode();
        updateRegisters();
        receiverControlLogic();
        transmitterControlLogic();
        interruptControlLogic();
        super.clock_();
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
        System.out.println("READ REGISTER");
        boolean[] value;
        RegisterAddress add = RegisterAddress.find(pins[P_A0],pins[P_A1],pins[P_A2],LCR.getPin(7)/*DLAB*/,false/*Write Mode*/);
        switch(add){
            case RBR:
                value= readReceiverBuffer();
                break;
            case DLLS:
                value = baudGenerator.getDivisorLatchLeastSignificantByte();
                break;
            case DLMS:
                value = baudGenerator.getDivisorLatchMostSignificantByte();
                break;
            case IER:
                value = IER.getValue();
                break;
            case IIR:
                value = IIR.getValue();
                break;
            case LCR:
                value = LCR.getValue();
                break;
            case LSR:
                value = LSR.getValue();
                LSR.setValue(new boolean[]{false,false,false,false}, 0);
                break;
            case MCR:
                value = MCR.getValue();
                break;
            case MSR:
                value = MSR.getValue();
                MSR.setValue(new boolean[]{false,false,false,false});
                break;
            case Scratch:
                value = scratchpad.getValue();
                break;
            default:
                value = new boolean[REG_SIZE];
        }        
        return value;
    }
    
    protected boolean[] readReceiverBuffer(){
        boolean[] value;
        if(FCR.getPin(0)/*FIFO Enabled*/)
        {
            RCVR_FIFO.setPin(FifoRegister.P_READ, true);
            value = new boolean[REG_SIZE];
            for(int i=0;i<REG_SIZE;i++)
                value[i] = RCVR_FIFO.getPin(RCVR_FIFO.P_O_START+i);
            RCVR_FIFO.setPin(FifoRegister.P_READ, false);
            if(RCVR_FIFO.getPin(FifoRegister.P_IS_EMPTY))
            {
                pins[PN_RXRDY] = true;
                LSR.setPin(0/*Data Ready*/, false);
            }
            // Updates Line Status Register with values stored at the top of receiver FIFO
            if(FCR.getPin(0)/*FIFO enabled*/&& !RCVR_FIFO.getPin(FifoRegister.P_IS_EMPTY))
            {
                LSR.setPin(2, RCVR_FIFO.getPin(RCVR_FIFO.P_O_START+8)); //Parity Error
                LSR.setPin(3, RCVR_FIFO.getPin(RCVR_FIFO.P_O_START+9)); //Framing Error
                LSR.setPin(4, RCVR_FIFO.getPin(RCVR_FIFO.P_O_START+10));//Break Interrupt
                if(LSR.getPin(2)|LSR.getPin(3)|LSR.getPin(4))
                    --rcvrFifoErrorCounter;
            }
        } else { //FIFO Disabled
            value = RBR.getValue();
            pins[PN_RXRDY] = true;
            LSR.setPin(0/*Data Ready*/, false);
        }        
        
        return value;
    }
    
    protected void writeRegister(boolean[] value){
        boolean a0 = pins[P_A0],a1 = pins[P_A1],a2 = pins[P_A2];
        RegisterAddress add = RegisterAddress.find(pins[P_A0],pins[P_A1],pins[P_A2],LCR.getPin(7)/*DLAB*/,true/*Write mode*/);
        switch(add){
            case THR: 
                writeTransmitterRegister(value);
                break;
            case DLLS:
                baudGenerator.setDivisorLatchLeastSignificantByte(value);
                break;
            case DLMS:
                baudGenerator.setDivisorLatchMostSignificantByte(value);
                break;
            case IER:
                IER.setValue(value);
                break;
            case FCR:
                writeFifoControlRegister(value);
                break;
            case LCR:
                LCR.setValue(value);
                break;
            case LSR:
                LSR.setValue(value);
                break;
            case MCR:
                MCR.setValue(value);
                break;
            case MSR:
                MSR.setValue(value);
                break;
            case Scratch:
                scratchpad.setValue(value);
                break;
        }
    }
    
    protected void writeFifoControlRegister(boolean[] value){
        FCR.setValue(value);
        if(FCR.getPin(0) == false){
            FCR.setPin(1, true);
            FCR.setPin(2, true);
            LSR.setPin(7, false); //If FIFO mode is disabled this bit is always 0
        }
        if(FCR.getPin(1)){
            FCR.setPin(1, false);
            RCVR_FIFO.reset_();
            rcvrFifoErrorCounter = 0;
            LSR.setPin(0/*Data Ready*/, false);
            pins[PN_RXRDY] = true;
        }
        if(FCR.getPin(2)){
            FCR.setPin(2, false);
            XMIT_FIFO.reset_();
            LSR.setPin(5/*Transmitter Holding Register Empty*/, false);
            pins[PN_TXRDY] = true;
        }
    }
    
    protected void writeTransmitterRegister(boolean[] value){
        THR.setValue(value);
        XMIT_FIFO.writeRegister(value);
        if(FCR.getPin(0) && FCR.getPin(3))
            pins[PN_TXRDY] = XMIT_FIFO.getPin(FifoRegister.P_IS_FULL);
        else
            pins[PN_TXRDY] = true;
        LSR.setPin(5/*Transmitter Holding Register Empty*/, false);
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
    
    public int getReceiverTriggerLevel(){
        if(!FCR.getPin(6) && !FCR.getPin(7))return 1;
        if(!FCR.getPin(6) &&  FCR.getPin(7))return 4;
        if( FCR.getPin(6) && !FCR.getPin(6))return 8;
        return 14;
    }
    
    private void receiverControlLogic(){
         if(RCVR.getPin(ShiftRegister.P_IS_EMPTY) && receptionEnabled)
         {
            manageReceiverShiftRegister();
            receptionEnabled = false;
         }
         if(pins[P_RCLK] && !receptionEnabled && !pins[P_SIN])
         {
           RCVR.setPin(P_RESET, true);
           RCVR.setPin(P_RESET, false);
           RCVR.nextClock();
           receptionEnabled = true;
         }
         LSR.setPin(7/*Errors in the fifo*/, rcvrFifoErrorCounter!=0);
    }
    
    private void transmitterControlLogic(){
        if(XMIT.getPin(ShiftRegister.P_IS_EMPTY) && MSR.getPin(4/*Clear to Send*/) 
                && baudGenerator.getPin(BaudGenerator.P_BAUDOUT)){
            manageTransmitterShiftRegister();
        }        
        transmissionEnabled = MSR.getPin(4/*Clear to Send*/) && !LSR.getPin(6/*Transmitter Empty*/);
    }
    
    private void manageReceiverShiftRegister(){
       boolean[] character = new boolean[8];
       boolean framingError = false,parityError = false,breakInterrupt = false;
       boolean[] rcvr = RCVR.getRegister().getValue();
       int index = rcvr.length - RCVR.getMaxSize();    
       for(int i=index; i<rcvr.length;i++)
           breakInterrupt |= rcvr[i];
       index++;//I ignore the start bit
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
       
       if(FCR.getPin(0)/*Fifo Enabled*/){
          LSR.setPin(1/*Overrun Error*/, RCVR_FIFO.getPin(FifoRegister.P_IS_FULL));
           for(int i=0;i<character.length;i++)
               RCVR_FIFO.setPin(FifoRegister.P_I_START+i, character[i]);
           RCVR_FIFO.setPin(FifoRegister.P_I_START+8, parityError);
           RCVR_FIFO.setPin(FifoRegister.P_I_START+9, framingError);
           RCVR_FIFO.setPin(FifoRegister.P_I_START+10, breakInterrupt);
           RCVR_FIFO.setPin(FifoRegister.P_WRITE,true);
           RCVR_FIFO.setPin(FifoRegister.P_WRITE,false);
           
           if(parityError|framingError|breakInterrupt)
               ++rcvrFifoErrorCounter;
           if(FCR.getPin(3)){
               if(RCVR_FIFO.getCount() >= getReceiverTriggerLevel())
                   pins[PN_RXRDY] = false;
           }
           else
               pins[PN_RXRDY] = false;
       }else{
           RBR.setValue(character);
           LSR.setPin(1/*Overrun Error*/, LSR.getPin(0/*Data Ready*/));
           LSR.setPin(2, parityError);
           LSR.setPin(3, framingError);
           LSR.setPin(4, breakInterrupt);
           pins[PN_RXRDY] = false;
       }
       LSR.setPin(0/*Data Ready*/, true);
       RCVR.reset_();
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
            if(XMIT_FIFO.getPin(FifoRegister.P_IS_EMPTY))
                pins[PN_TXRDY] = false;
        } else {
            if(LSR.getPin(5)){
                LSR.setPin(6/*Transmitter Empty*/, true);
                return;
            }
            character = THR.getValue();
            LSR.setPin(5, true); //Transmitter Holding Register Empty
            pins[PN_TXRDY] = false;
        }
        int index = ShiftRegister.P_INPUT_START;
        XMIT.setPin(index++, false); //Start bit
        boolean parity = false;
        for(int i=0;i<characterLength;i++){
            XMIT.setPin(index++, character[i]);
            parity^=character[i];
        }
        if(parityEnable)
            XMIT.setPin(index++, parity^evenParitySelect);
        for(int i=0; i < stopLength; ++i)
            XMIT.setPin(index++, true);
        XMIT.reset_();
        LSR.setPin(6/*Transmitter Empty*/, false);
    }   
    
    private void interruptControlLogic(){
        boolean[] interrupts = new boolean[]{
                LSR.getPin(2)|LSR.getPin(3)|LSR.getPin(4),
                pins[PN_RXRDY] == false,
                pins[PN_TXRDY] == false,
                MSR.getPin(0)|MSR.getPin(1)|MSR.getPin(2)|MSR.getPin(3)
            };
        int i;
        for(i=0;i<interrupts.length;i++)
            if(interrupts[i])break;
        IIR.setPin(0, !(interrupts[0]|interrupts[1]|interrupts[2]|interrupts[3]));
        switch(i){
            case 0:
                IIR.setPin(1, true);
                IIR.setPin(2, true);
                break;
            case 1:
                IIR.setPin(1, false);
                IIR.setPin(2, true);
                break;
            case 2:
                IIR.setPin(1, true);
                IIR.setPin(2, false);
                break;
            case 3:
                IIR.setPin(1, false);
                IIR.setPin(2, false);
                break;
       }
       pins[P_INTR] = (interrupts[0]&IER.getPin(2)) //Receiver Line Status Interrupt Enabled
                    | (interrupts[1]&IER.getPin(0)) //Received Data Available Interrupt Enabled
                    | (interrupts[2]&IER.getPin(1)) //Transmitter Holding Register Empty Interrupt Enabled
                    | (interrupts[3]&IER.getPin(0));//MODEM Status Interrupt Enabled
    }
    
    @Override
    public String toString(){
        String s = "UART_16550D\tcs: "+isChipSelected()+"\tr: "+isReadMode()+"\tw: "+isWriteMode();
        s+="\nLCR: "+LCR+"\tLSR: "+LSR+"\tFCR: "+FCR+"\tIER: "+IER+"\tIIR: "+IIR;
        s+="\nMSR: "+MSR+"\tMCR: "+MCR+"\t RBR: "+RBR+"\tTHR: "+THR;
        s+="\n"+baudGenerator;
        s+="\n Reception Enabled: "+receptionEnabled+"\tTransmission Enabled:"+transmissionEnabled;
        s+="\nRCVR: "+RCVR+"\nRCVR_FIFO: "+RCVR_FIFO;
        s+="\nXMIT: "+XMIT+"\nXMIT_FIFO: "+XMIT_FIFO;
        s+="\n";
        return s;
    }
    
}
