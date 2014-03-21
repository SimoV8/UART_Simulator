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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*Counter c = new Counter(9);
        System.out.println(c);
        for(int i=0; i< 20; i++){
            if(i == 10){
                c.setPin(Counter.P_RESET, true);
            }
            c.clock();
            System.out.println(c);
        }
         */
        ShiftRegister sr = new ShiftRegister(10);
        sr.setPin(ShiftRegister.P_RESET, true);
        for(int i= ShiftRegister.P_INPUT_START; i < sr.getPinCount();i++)
            sr.setPin(i, false);
        sr.setPin(10, true);
        sr.setPin(9, true);
        sr.setPin(7, true);
        sr.clock();
        for(int i=0;i < 15;i++)
        {
            sr.clock();
            if(sr.getPin(ShiftRegister.P_IS_EMPTY)){
                sr.setPin(ShiftRegister.P_INPUT_START,false);
                sr.setPin(ShiftRegister.P_RESET, true);
            }
            System.out.println(sr);
            
        }
        
    }
}
