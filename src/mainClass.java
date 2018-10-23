// Main Class
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
public class mainClass {
    static double totalRun;
    static int count = 0;
	public static void main(String[] args) {
        @SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
        try {
        System.out.println("Welcome!\nThis simluation output result as the .txt file with name as alarm delay time(after 1st Engine OS for each alarm)\n");
        System.out.println("The simulation dat will store in rawData folder to be made where the application is and Statistic result will store in 'Statistics.csv' file.\n");
        System.out.println("Enter fast mode or visual mode. (f or v)\nNote: visual mode only run one replication and one delay time");
        char m = sc.next().charAt(0);
        	while(m!='f'&& m!='v') {
        		System.out.println("Bad input try again");
                m = sc.next().charAt(0); //No exception throwing because we cannot send any inputs that fail to be parsed as strings
            }                
        int count= 0;
        int file = -1;
        int max = -1;
        double intval = -1;
        int repsToDo = -1;
        if(m=='f') {
        	System.out.println("set minimum inter-alarm delay time (mins). Note: 30 secs = 0.5 mins ");             
        	while(file<1) {
        		while(!sc.hasNextInt()) {
        			System.out.println("bad input try again ");             
        			sc.next(); //Throw away bad token
        		}
        		file = sc.nextInt();
        		if(file<1) {
        			System.out.println("bad input try again, value must be positive");
        			continue;
        		}
        	}
        
        	System.out.println("set maximum inter-alarm delay time (mins). Note: 30 secs = 0.5 mins ");             
        	while(max<file) {
        		while(!sc.hasNextInt()) {
        			System.out.println("bad input try again");             
        			sc.next(); //Throw away bad token
        		}
        		max = sc.nextInt();
        		if(max<file) {
        			System.out.println("bad input try again, value must be >= to min");
        			continue;
        		}
        	}
        	System.out.println("Enter interval to use.(mins). Note: 30 secs = 0.5 mins ");             
        	while(intval<=0) {
        		while(!sc.hasNextDouble()) {
        			System.out.println("bad input try again");
        			sc.next(); //Throw away bad token
        		}
        		intval = sc.nextDouble();
        		if(intval<=0) {
        			System.out.println("bad input try again, value must be positive");
        			continue;
        		}
        	}
        }
        else {
        	System.out.println("set inter-alarm delay time (mins). Note: 30 secs = 0.5 mins ");             
            while(file<1) {
                while(!sc.hasNextInt()) {
                    System.out.println("bad input try again ");             
                    sc.next(); //Throw away bad token
                }
                file = sc.nextInt();
                if(file<1) {
                    System.out.println("bad input try again, value must be positive");
                    continue;
                }
            }
            max=file;
            intval=1;
        }
        if(m=='v'){repsToDo = 1;}
        else repsToDo = 10000;
      
        FileWriter FW2 = new FileWriter("Statistics.csv");//file writer for statistics data
        String title2="Delay time, Num of Alarm, 95% CI,Units Dispatched, 95% CI, SCBA Swaped,  95% CI,Event Time,  95% CI, # off FF outside,  95% CI, FF idle Time, 95% CI, Size of Fire, 95% CI, Failure Rate";
        FW2.write(title2);
        FW2.write(System.getProperty( "line.separator" ));
        int maxRuns = repsToDo*(max-file+1)*(int)(1.0/intval);
        long t = System.currentTimeMillis(); //Used later to determine the time the simulation took to run.
        for(double i=file;i<=max;i+=intval) {
        	File dir = new File("rawData");
        	dir.mkdir();
            FileWriter FW = new FileWriter("rawData/"+(int)(i*60)+".txt"); //1 file writer per condition
           
        	if(m=='v') {
        		String title="Time(min)\tAlarm\tSCAB-swaps\t#-of-Engine\t#-of-Truck\t#-of-Rescue\t#-of-FF in rehab\t"+
                "total units dispatiched\ttotal unit arrival\tmax fire growth rate\tmax Fire size\ttotal FF idle time\t"+
                "max # FF waiting\tthe fire size when lasted alarm dispatched\tisFaile\n"; //Format ( file header)
                FW.write(title);
        	}
        	int timeDelay= (int)i*60;//min to sec
        int numReps = 0;
        Result r= new Result();//Copy data and generate result.
        r.t=2.24174034;//DF=9999,a=0.05/2=0.025
        r.idel= new double[repsToDo];r.numWait= new double[repsToDo];
        r.alarm= new double[repsToDo];r.eventTime=new double[repsToDo];
        r.tank=new double[repsToDo];r.eventTime=new double[repsToDo];
        r.unit=new double[repsToDo];
        r.fireMax= new double[repsToDo];r.timedelay=i;
        r.Replication=repsToDo;
        while(numReps<repsToDo) {
            Simulation s = new Simulation();
            
            s.alarmDelay = timeDelay;
            if(m =='f') {
            	s.visualMode = false;
            } else {
                s.visualMode = true;
            }
          
           while(!s.fireout) { //check is fire out to end simulation
                s.tick();
            }
            r.timeToFireOut = s.sysTime;r.totalAlarm=s.alarmsSent;
            r.totalTank=s.k;r.totalEngine=s.E;
            r.totalTruck=s.T;r.totalRescue=s.R;r.rehabFF=s.FFrehab;
            r.unitDispatch=s.vehicleMax; r.unitOS=s.vehiclesOnScene;
            r.growthRate=s.maxGrowth;r.maxFire=s.maxFire;
            r.fails=s.fail; r.idelTime=s.waitTime;
            r.maxWait=s.maxOut;r.alarmFire=s.fireStatus[s.alarmsSent-1];
            r.count=numReps;
            r.STAT();
            String str = String.format("%d",r.timeToFireOut)+"\t"+ //total runtime
            		String.format("%d",r.totalAlarm)+"\t"+ //alarm
            		String.format("%d",r.totalTank)+"\t"+ //# of SCBA swap 
            		String.format("%d",r.totalEngine)+"\t"+ //total engine OS
            		String.format("%d",r.totalTruck)+"\t"+ // total truck OS
            		String.format("%d",r.totalRescue)+"\t"+//total Rescue OS
            		String.format("%d",r.rehabFF)+"\t"+// total # of FF at rehab
            		String.format("%d",r.unitDispatch)+"\t"+// total unit dispatched
            		String.format("%d",r.unitOS)+"\t"+//total unit arrival
                    String.format("%f",r.growthRate)+"\t"+//max fire growth rate
                    String.format("%f",r.maxFire)+"\t"+//max Fire size
                    String.format("%d",r.idelTime)+"\t"+//total FF idle time
                    String.format("%d",r.maxWait)+"\t"+//max # FF was wait outside
                    String.format("%f",r.alarmFire)+"\t";//the fire size when lasted alarm dispatched
            if(r.fails==1) {
            	str+="1\n";//1 means fail in fire fighting
            } else { 
                str+="0\n";
            }
            FW.write(str);
            FW.flush();
            numReps++;
            count++;
            if(count%2500==0) {
                double x = 100.0*count/maxRuns;
                String xstr = String.format("%3.3g", x);
                System.out.println(xstr+ "% complete");
            }
        }
        FW.close();
        String out=r.OutPut();
        FW2.write(out);
        FW2.flush();
        }
        FW2.close();
        System.out.print("\n"+count+" runs done, operation took: " + (1.0*(System.currentTimeMillis()-t)/1000) + " seconds to complete.");		
        }catch(Exception e) { //error catch
                e.printStackTrace();
                System.out.println("Couldnt write/ write to file.");
                System.exit(1);
            
        	}
        }     
}