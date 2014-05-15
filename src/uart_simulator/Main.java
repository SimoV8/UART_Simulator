/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uart_simulator;

/**
 *
 * @author Simone Vuotto
 */
public class Main {
    
    public static void testCounter(){
        Counter c = new Counter(8);
        for(int i=0; i<= 20; i++){
            c.nextClock();
            System.out.println(i);
            if(i == 10){
                c.setPin(Counter.P_RESET, true);
            }
            if(i == 12){
                c.setPin(Counter.P_RESET, false);
            }
            
            System.out.println(c);
        }      
    }
    
    public static void testShiftRegister(){
        ShiftRegister sr = new ShiftRegister(5);
        sr.setPin(Device.P_RESET, true);
        for(int i= ShiftRegister.P_INPUT_START; i < sr.getPinCount();i++)
            sr.setPin(i, false);
        sr.setPin(ShiftRegister.P_INPUT_START+2, true);
        sr.setPin(ShiftRegister.P_INPUT_START+4, true);
        //sr.setPin(7, true);
        sr.nextClock();
        System.out.println(sr);
        for(int i=0;i < 15;i++)
        {
            sr.setPin(ShiftRegister.P_RESET, false);
            sr.setPin(Device.P_CLK, true);
            sr.setPin(ShiftRegister.P_IN, i%2 == 0);
            if(i==10){//sr.getPin(ShiftRegister.P_IS_EMPTY)){
                sr.setPin(ShiftRegister.P_INPUT_START,true);
                sr.setPin(ShiftRegister.P_RESET, true);
            }
            sr.setPin(Device.P_CLK, false);
            System.out.println(sr);
            
        }
    }
    
    public static void testFifoRegister(){
        FifoRegister fr = new FifoRegister(4, 4);
        fr.setPin(FifoRegister.P_RESET, true);
        fr.nextClock();
        fr.setPin(FifoRegister.P_RESET, false);
        fr.setPin(FifoRegister.P_I_START,true);
        fr.setPin(FifoRegister.P_WRITE, true);
        fr.nextClock();
        fr.setPin(FifoRegister.P_I_START+1,true);
        fr.nextClock();
        fr.setPin(FifoRegister.P_I_START+2,true);
        fr.nextClock();
        System.out.println(fr);
        fr.setPin(FifoRegister.P_I_START+3,true);
        fr.nextClock();
         System.out.println(fr);
        fr.setPin(FifoRegister.P_WRITE, false);
        fr.nextClock();
        System.out.println(fr);
        fr.setPin(FifoRegister.P_READ, true);
        for(int i=0;i<6;i++)
        {
            fr.nextClock();
            System.out.println(fr);
        }
        fr.setPin(FifoRegister.P_I_START,false);
        fr.setPin(FifoRegister.P_READ, false);
        fr.setPin(FifoRegister.P_WRITE, true);
        fr.nextClock();
        System.out.println(fr);
        fr.setPin(FifoRegister.P_I_START+1,false);
        fr.nextClock();
        System.out.println(fr);
        fr.setPin(FifoRegister.P_I_START+2,false);
        fr.nextClock();
        System.out.println(fr);
        fr.setPin(FifoRegister.P_I_START,true);
        fr.nextClock();
        fr.setPin(FifoRegister.P_WRITE, false);
        fr.setPin(FifoRegister.P_READ, true);
        System.out.println(fr);
         for(int i=0;i<6;i++)
        {
            fr.nextClock();
            System.out.println(fr);
        }
    }
    
    public static void testBaudGenerator(){
        BaudGenerator bg = new BaudGenerator();
        for(int i=0; i < 20; i++)
        {
            bg.nextClock();
            System.out.println(bg);
        }
        bg.setDivisorLatchLeastSignificantByte(new boolean[]{false,false,false,false,false,false,true,true});
        for(int i=0; i < 100; i++)
        {
            bg.nextClock();
            System.out.println(bg);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        testShiftRegister();
    }
}
