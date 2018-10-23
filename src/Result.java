//Struct for result statistics
public class Result {
   int timeToFireOut,totalAlarm, totalTank, totalEngine,totalTruck,totalRescue,rehabFF, unitDispatch, unitOS,idelTime,count,maxWait;
   double growthRate, maxFire, alarmFire,t,timedelay;
   String fale;
   int fails,Replication;
   int failes;
   double idel[],fireMax[],numWait[], alarm[],tank[],unit[],eventTime[];
   
 
   public void STAT() {
	   idel[count]=idelTime/60;
	   numWait[count]=maxWait;
	   alarm[count]=totalAlarm;
	   failes+=fails;
	   fireMax[count]=maxFire;
	   tank[count]=totalTank;
	   unit[count]=unitDispatch;
	   eventTime[count]=timeToFireOut;
   }
   
   private double mean(double data[]) {// Calculating mean
	   double sum=0;
	   int length=data.length;
	   for(int i=0; i<length;i++) {
		   sum+=data[i];
	   }
	   return sum/length;
   }
   private double stv(double data[]) {// Calculating STDV
	   double sum=0;
	   double mean=mean(data);
	   int length=data.length;
	   for(int i=0; i<length;i++) {
		   sum+=Math.pow(data[i]-mean,2);
	   }
	   double stv=Math.sqrt(sum/(length-1));
	   return stv;
   }
   public String OutPut(){ 
	   double avgidelTime=mean(idel);
	   double avgNumWait=mean(numWait);
	   double avgalarm=mean(alarm);
	   double avgTank = mean(tank);
	   double avgDispatch=mean(unit);
	   double avgEventTime=mean(eventTime);
	   double failerate=(failes/(double)Replication)*100;
	   double avgfire=mean(fireMax);
	   double upperBound=avgalarm+(t*stv(alarm)/Math.sqrt(Replication));
	   double lowerBound=avgalarm-(t*stv(alarm)/Math.sqrt(Replication));	  
	   String temp1=String.format("%f",avgalarm) +",["+String.format("%f",lowerBound)+"~"+ String.format("%f",upperBound)+"]";
	   upperBound=avgDispatch+(t*stv(unit)/Math.sqrt(Replication));
	   lowerBound=avgDispatch-(t*stv(unit)/Math.sqrt(Replication));
	   String temp2=String.format("%f",avgDispatch) +",["+String.format("%f",lowerBound)+"~"+ String.format("%f",upperBound)+"]";
	   upperBound=avgTank+(t*stv(tank)/Math.sqrt(Replication));
	   lowerBound=avgTank-(t*stv(tank)/Math.sqrt(Replication));
	   String temp3=String.format("%f",avgTank) +",["+String.format("%f",lowerBound)+"~"+ String.format("%f",upperBound)+"]";
	   upperBound=avgEventTime+(t*stv(eventTime)/Math.sqrt(Replication));
	   lowerBound=avgEventTime-(t*stv(eventTime)/Math.sqrt(Replication));
	   String temp4=String.format("%f",avgEventTime) +",["+String.format("%f",lowerBound)+"~"+ String.format("%f",upperBound)+"]";
	   upperBound=avgNumWait+(t*stv(numWait)/Math.sqrt(Replication));
	   lowerBound=avgNumWait-(t*stv(numWait)/Math.sqrt(Replication));
	   String temp5=String.format("%f",avgNumWait) +",["+String.format("%f",lowerBound)+"~"+ String.format("%f",upperBound)+"]";
	   upperBound=avgidelTime+(t*stv(idel)/Math.sqrt(Replication));
	   lowerBound=avgidelTime-(t*stv(idel)/Math.sqrt(Replication));
	   String temp6=String.format("%f",avgidelTime) +",["+String.format("%f",lowerBound)+"~"+ String.format("%f",upperBound)+"]";
	   upperBound=avgfire+(t*stv(fireMax)/Math.sqrt(Replication));
	   lowerBound=avgfire-(t*stv(fireMax)/Math.sqrt(Replication));
	   String temp7=String.format("%f",avgfire) +",["+String.format("%f",lowerBound)+"~"+ String.format("%f",upperBound)+"]";
	   String output =String.format("%.2f",timedelay)+ ","+temp1+","+temp2+","+temp3+","+temp4+","+temp5+","+temp6+","+temp7+","+ String.format("%f",failerate)+"%\n";
	   return output;
   }  
}
   