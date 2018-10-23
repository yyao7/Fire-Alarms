//Holds various useful methods 
import java.util.Random;
public class Distributions {

   //Triangular distribution
   //Should be returning double. Cant be bothered since i would have to cast back.
   public static int triangular(double a, double b, double c) throws RuntimeException{
      if(a>=b||b>=c||a>=c) {
          System.out.println("Exception found: triangular dist is strictly a<b<c");
          throw new RuntimeException(); //Strictly a<b<c.
      }
      //min 1200 mode 1500 max 1800 for air tanks
      double u = Math.random();
      double ret;
      if(u<(c-a)/(b-a)) {
         ret = a + Math.sqrt(u*(b-a)*(c-a)); 
      } else {
         ret = b - Math.sqrt((1-u)*(b-a)*(b-c));
      }  
        return (int)Math.floor(ret); //Math.floor may be redundant, but no harm no foul
   }
   //Expo distribution
   public static double exponential(double mean) {
      return (-1.0*mean)*Math.log(Math.random()); //Mean = a / lambda
   }


   //Computes normal distribution
   static Random rd = new Random();
   public static double normalDist(double mean, double stdev) {
      return rd.nextGaussian()*stdev+mean; 
   }

}