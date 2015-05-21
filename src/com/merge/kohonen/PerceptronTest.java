package com.merge.kohonen;
/**
 *
 * @author Berni
 */
public class PerceptronTest {    
    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
//    	double[][] arrx = {{0, 1, 1}, {1, 1, 1}, {1, 0, 1}, {0, 0, 0}};
    	double[][] arrx = {{0, 0.05, 0.05}, {0,0, 0.25}, {0.5, 0, 0.5}, {0.6, 0.4, 0}};
        Matrix x = new Matrix(arrx);
         double[][] arry = {{0.1}, {0.25}, {1}, {1}};
         Matrix y = new Matrix(arry);
         PerceptronBuilder net = new PerceptronBuilder();
         
         net.makeNetwok(x.getHeigth() + "-6-" + y.getHeigth());
         
         double max = y.findMaxValue();
         double min = y.findMinValue();
         net.setOutputMinMax(min, max);
         net.setEpchos(6000);
         net.start_learning(x, y);
         
         System.out.print("Target outputs:\n" + y.print());
         Matrix out = net.getOutput(x, min, max);
         System.out.println("Computed outputs:\n" + out.print());
         
         /**
          * Kohonen test
          */
         
         //Matrix test = Matrix.createByCols("1.5 2.2; 1.2 3.2");
          Matrix test = Matrix.createByCols("1 0 0 ; 0 1 0");
         out = net.getOutput(test, min, max) ;
         System.out.println("Computed test:\n"+out.print());
         
         
         
    }
    
}
