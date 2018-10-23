/*
The model follows this control flow: Dispatch sends alarm following 911 call ( uniform dist )
Engines: primary firefighting
Trucks: primarily external rescuing, some firefighting.
Rescue: internal rescuing, some firefighting.
The model only considers how long it takes and how many resources are used to put out the fire.

Vehicles are batched into alarms. After an alarm has arrived, they go into a staging area if the previous alarm is still working, else they work on the fire.  If the fire is still growing after an alarm has arrived, another one is sent immediately. 

*/

import java.util.PriorityQueue;

public class Simulation {
    int sysTime = 0;
    double totalFire = 0,currentFrie=0;
    double maxGrowth=0, maxFire=0;
    PriorityQueue <Event> p;
    int alarmsSent = 0; //Used in the repeated dispatch subroutine.
    int vehiclesOnScene = 0,perviousTime=0,Tankcycles = 0;;
    int vehicleMax = 0; //the total number of vehicles that should be on the scene after all arrivals. 
    boolean visualMode;
    double[] fireStatus=new double[200];
    boolean alarmOnWay = false,fireout=false,fistEngine = true;
    int E=0, T=0,R=0,k=0, Eout=0,Tout=0,Rout=0,fail=0;//test
    int ewait=0,twait=0,rwait=0,waitTime;//for stage
    int FFrehab=0, FFin=0, ffout, maxOut,numFF = 0;;//for tracking crew
    int Ecrew=0,Tcrew=0, Rcrew=0; 
    int alarmDelay;//call the greater alarm after First Engine for each alarm and import from main class 
    
    Result conclusion = null; 
    public Simulation() {
        p = new PriorityQueue <Event> (10, new EventComparator());
        scheduleDispatch((int)Math.floor(Math.random()*240+60));//911 received
    }
   
    //Primary running method. grabs event, updates fire if its out. If its out, make conclusion stats and the run is over. 
    public void tick() throws RuntimeException{
        Event e = p.poll(); //Grab event
        if(e==null) {  //Fail-fast
            System.out.println("Error: no events to handle.");
            throw new RuntimeException();
        }
        double growth = fireGrowth(e.getTime());
        sysTime = e.getTime();
        if(growth + totalFire<=0 && Ecrew > 0) {
            int j = 1;
            double fDiff = fireGrowth(sysTime+10*j);
            while(fDiff>totalFire) {
                j++;
                fDiff = fireGrowth(sysTime+10*j);
            }
            sysTime = sysTime+10*j;
            processFireOut();
            return;
        }
        
         //update to event time
        waitTime+=(sysTime-perviousTime)*ewait*3+(sysTime-perviousTime)*twait*2+
        			(sysTime-perviousTime)*rwait*4; 
        double growthrate=growth/(sysTime-perviousTime); 
        if(maxGrowth<growth) {
        	maxGrowth=growth;
        }
        totalFire += growth;
        if(maxFire<totalFire) {
        	maxFire=totalFire;
        }
        if(totalFire <= 0 && sysTime > 0) {
            processFireOut();
            return;
        } else {
            switch(e.getType()) {
                case ENGINEARR:      processEngineArr();     E++;     	break;
                case TRUCKARR:       processTruckArr();      T++;       break;
                case RESCUEARR:      processRescueArr();     R++;  	    break;
                case TANKOUT:        processTankOut();       k++; 	    break;
                case EOUT: 			 processEngineOut();     Eout++;    break;
                case TOUT:           processTruckOut();      Tout++;    break;
                case ROUT:			 processRescueOut();     Rout++;    break;
                case DISPATCH:       processDispatch();   		        break;
                default:
                    System.out.println("ERROR: Unrecognized event type.");
                    System.exit(1); //Fail-fast
            }
        }
        ffout=ewait*3+twait*2+rwait*4;
      	if(ffout>maxOut) { //stat collection
      		maxOut=ffout;
      	}
        if(visualMode&&(totalFire >= 0) ) {// testing 
        	double time=e.getTime();
        	String t= String.format("%.2f",time/60);
        	if(sysTime==perviousTime) {
        		System.out.print("Also, ");
        	}
        	else System.out.print(t+" min,");
        	
        	if(e.getType()==EventTypes.Events.ENGINEARR){
        		System.out.print("E"+E+" OS");
        	}
        	else if(e.getType()==EventTypes.Events.TRUCKARR){
        		System.out.print("T"+T+" OS");
        	}
        	else if(e.getType()==EventTypes.Events.RESCUEARR){
        		System.out.print("R"+R+" OS");
        	}
        	else if(e.getType()==EventTypes.Events.EOUT){
        		System.out.print("E"+Eout+" went to rehab");
        	}
        	else if(e.getType()==EventTypes.Events.TOUT){
        		System.out.print("T"+Tout+" went to rehab");
        	}
        	else if(e.getType()==EventTypes.Events.ROUT){
        		System.out.print("R"+Rout+" went to rehab");
        	}
        	else if(e.getType()==EventTypes.Events.TANKOUT){
        		System.out.print("Tank swap "+k);
        	}
        	else if(e.getType()==EventTypes.Events.DISPATCH){	
        		System.out.print("Alarm "+alarmsSent);
        	}
        	else 
        		System.out.print(t+" min,"+e.getType().toString()+","+numFF+","+FFin+","+waitTime +","+Tankcycles+",");
        	if(sysTime==perviousTime) {
        		System.out.print("\n");
        	}
        	else {System.out.print(",total # FFs "+numFF+", #FFs inside "+FFin+", #FFs outside "+ffout+" , #FFs in rehab "+FFrehab+", total waiting time "+waitTime +", remaining SCBA cycles "+Tankcycles+",");
        		System.out.println(" ,current growth rate "+growthrate+" ,current fire size "+totalFire);
        	}
        	}
        perviousTime=sysTime;
        if(vehiclesOnScene == vehicleMax) {
        	alarmOnWay=false;
        }
    }
    //Computes how much the fire would grow between sysTime and @param newTime
    public double fireGrowth(int newTime) throws RuntimeException {
        if(newTime < sysTime) { //Fail-fast
            System.out.printf("\nError, bad call to fireGrowth. new = %d, sys = %d", newTime, sysTime);
            throw new RuntimeException();
        } 
        if(newTime == sysTime) {
            return 0;
        }
        double weib = fireGrowthCalcs(1.0*newTime/60)-fireGrowthCalcs(1.0*sysTime/60);  //equation is in minutes NOT SECONDS 
        double coeff = 0.000057;
        double ans = weib - coeff*Ecrew*(newTime-sysTime);
        return ans;
    }
    //This is a computational formula doing calculations of a Weibull, original is in the report. 
    private static double fireGrowthCalcs(double t) {
        int scale = 24;
        int shape = 3; 
        return 1.0-Math.pow(Math.E,-1.0*Math.pow((t/scale),shape));
    }
    
    private void scheduleEngineArr(int delay) throws RuntimeException{
        Event e = new Event();
        e.setType(EventTypes.Events.ENGINEARR);
        int turnoutTime = (int)Math.floor(Distributions.normalDist(59.5,7.83));
        int travelTime  = delay+(int)Math.floor(Distributions.normalDist(383,146));
        while(travelTime<=0) {
            travelTime  = delay+(int)Math.floor(Distributions.normalDist(383,146));
        }
        while(turnoutTime<=0) {
            turnoutTime = (int)Math.floor(Distributions.normalDist(59.5,7.83));
        }
        
        int newEventTime = sysTime + turnoutTime + travelTime;
        if(newEventTime<sysTime) { //Fail-fast
            System.out.println("Error: scheduleEngineArr scheduled for the past");
            System.out.println("sysTime: " + sysTime + ", turnoutTime: " + turnoutTime + ", travelTime: " + travelTime);
            throw new RuntimeException();
        }
        e.setTime(newEventTime);
        p.add(e);
    }
    private  void processEngineArr() {
        vehiclesOnScene++;
        if(fistEngine) {
        	fistEngine = false;
        	alarmOnWay = false;
        	scheduleDispatch(alarmDelay);
        }
        numFF+=3;
        Tankcycles+=2;
        scheduleworking("e");
    }
    private  void processEngineOut() {
    	FFrehab+=3;
    	Ecrew--;
    	FFin=FFin-3;
    	if(ewait>0&&Ecrew>0) {
    		scheduleworking("e");
    		ewait--;
    	}
    	if(Ecrew==0&&Tcrew==0&&Rcrew==0) {   
             fail=1;
    		scheduleDispatch(0);
    	}
    }
    private  void scheduleTruckArr(int delay, int travel) throws RuntimeException {
        Event e = new Event();
        e.setType(EventTypes.Events.TRUCKARR);
        int turnoutTime = (int)Math.floor(Distributions.normalDist(59.5,7.83));
        int travelTime  = delay + travel;
        int newEventTime = sysTime + turnoutTime + travelTime;
        while(travelTime<=0) {
        	travelTime  = delay + 184 + (int)Math.floor(Distributions.exponential(174));
        }
        while(turnoutTime<=0) {
            turnoutTime = (int)Math.floor(Distributions.normalDist(59.5,7.83));
        }
        if(newEventTime<sysTime) {
            System.out.println("Error: scheduleTruckArr scheduled for the past");
            System.out.println("sysTime: " + sysTime + ", turnoutTime: " + turnoutTime + ", travelTime: " + travelTime);
            throw new RuntimeException();
        }
        e.setTime(newEventTime);
        p.add(e);
    }
    private  void processTruckArr() {
        vehiclesOnScene++;
        numFF+=2;
        Tankcycles+=2;
        scheduleworking("t");
    }
    private  void processTruckOut() {
    	FFrehab+=2;
    	Tcrew--;
    	FFin=FFin-2;
    	if(twait>0) {
    		scheduleworking("t");
    		twait--;
    	}
    	else if(rwait>0) {
    		scheduleworking("r");
    		rwait--;
    	}
    	if(Ecrew==0&&Tcrew==0&&Rcrew==0) {  
            fail=1;
    		scheduleDispatch(0);
    	}
    }        
    private  void scheduleRescueArr(int delay, int travel) throws RuntimeException{
        Event e = new Event();
        e.setType(EventTypes.Events.RESCUEARR);
        int turnoutTime = (int)Math.floor(Distributions.normalDist(59.5,7.83));
        int travelTime  = delay + travel;
        int newEventTime = sysTime + turnoutTime + travelTime;
        while(travelTime<=0) {
        	travelTime  = delay + 184 + (int)Math.floor(Distributions.exponential(174));
        }
        while(turnoutTime<=0) {
            turnoutTime = (int)Math.floor(Distributions.normalDist(59.5,7.83));
        }
        if(newEventTime<sysTime) {
            System.out.println("Error: scheduleRescueArr scheduled for the past");
            System.out.println("sysTime: " + sysTime + ", turnoutTime: " + turnoutTime + ", travelTime: " + travelTime);
            throw new RuntimeException();
        }
        e.setTime(newEventTime);
        p.add(e);
    }
    private  void processRescueArr() {
        vehiclesOnScene++;
        numFF+=4;
        Tankcycles+=2;
        scheduleworking("r");
    }
    private  void processRescueOut() {
    	FFrehab+=4;
    	Rcrew--;
    	FFin=FFin-4;
    	if(rwait>0) {
    		scheduleworking("r");
    		rwait--;
    	}else if(twait>0) {
    		scheduleworking("t");
    		twait--;
    	}
    	if(Ecrew==0&&Tcrew==0&&Rcrew==0) {
            fail=1;
    		scheduleDispatch(0);
    	}
    }
    
    private void stage(String a) {
    	if(a=="e") {ewait++;}
    	else if(a=="t") {twait++;}
    	else if(a=="r") {rwait++;}
    }
    private  void scheduleworking(String a) {
    	if(a=="e") {
    		if(Ecrew<3) {
    			scheduleout("e");
    			FFin+=3;
    			Ecrew++;
    		} 
    		else stage("e");
    	}
    	else if(a=="t") {
    		if(Tcrew<2||Rcrew<1) {
    			scheduleout("t");
    			FFin+=2;
    			Tcrew++;
    		}
    		else stage("t");
    	}
    	else if(a=="r") {
    		if(Tcrew<2||Rcrew<1) {
    		scheduleout("r");
    		FFin+=4;
    		Rcrew++;
    		}
    		else stage("r");
    		} 
    }
 
    private void scheduleDispatch(int time) {
    	if(!alarmOnWay) {
    		Event e = new Event();
    		e.setType(EventTypes.Events.DISPATCH);
    		e.setTime(sysTime + time);
    		p.add(e);
    	}
    }    
    //Sends an alarm 
    public void processDispatch() {
    	fireStatus[alarmsSent]=totalFire;
    	alarmsSent++;
        scheduleVehiclesPerAlarm(alarmsSent);
        alarmOnWay = true;
        fistEngine = true;
    }
    
    //Collects stats once the fire is out.
    public void processFireOut() {
        fireout = true;
    	if(visualMode) {
        	double time=sysTime;
        	String t= String.format("%.2f",time/60);
        	System.out.println(t+" min, fire out");
        	System.out.println("Fire out at "+t+"mins, total alarm "+alarmsSent+", total tank out "+k+", total engine "+E+", total truck "+T+", total rescue "+R);
        	System.out.println("total # of firefighters at sence "+numFF+" firefighter at rehab "+FFrehab+" # of firefighters still inside"+FFin+" # of firefighters outside "+ffout+", total unit dispatched "+ vehicleMax+", unit on scene "+vehiclesOnScene);
        }
        p.clear(); //fail-fast
    }
    //Schedules when the vehicles are out of resources. 
    private  void scheduleout(String t) {
    	int time=0;
    	Event e = new Event();
        if(t=="e") {
    		e.setType(EventTypes.Events.EOUT);
    		time+=SCBAtime(3);//for first bottle
    		scheduleTankOut(time);
    		time+=SCBAtime(3);//for second bottle
    		scheduleTankOut(time);
    	}
    	else if(t=="t") {
    		e.setType(EventTypes.Events.TOUT);
    		time+=SCBAtime(2);//for first bottle
    		scheduleTankOut(time);
    		time+=SCBAtime(2);//for second bottle
    		scheduleTankOut(time);
    	}
    	else if(t=="r") {
    		e.setType(EventTypes.Events.ROUT);
    		time+=SCBAtime(2);//for fist bottle
    		scheduleTankOut(time);
    		time+=SCBAtime(2);//for second bottle
    		scheduleTankOut(time);
    	}
        e.setTime(sysTime+time);
            p.add(e);
    }
    
    private  void scheduleVehiclesPerAlarm(int alarmNo) {
    	int travel=120;//add travel time gap for great alarm 
    	int R = 184 + (int)Math.floor(Distributions.exponential(174));
    	int T = 184 + (int)Math.floor(Distributions.exponential(174));
    	switch(alarmNo) {
            case 1:
                firstalarm();
                vehicleMax+=6;
                break;
            default:
                if(T<R) {//pick the closest unit
                    scheduleVehicles(3, 1, 0,travel*(alarmNo-1),T); // findDist, pick minimum between t and r
                } else {
                    scheduleVehicles(3, 0, 1,travel*(alarmNo-1),R);
                }
                vehicleMax+=4;
        }
    }
    //Schedules e engines, t trucks, and r rescues
    private  void scheduleVehicles(int e, int t, int r,int d, int travel) {
        for(int i = 0; i < Math.max(e, Math.max(t, r)); i++) {
            if(i < e) { scheduleEngineArr(d); }
            if(i < t) { scheduleTruckArr(d,t);  }
            if(i < r) { scheduleRescueArr(d,t); }
        }
    }
    private  void firstalarm() {//for fist alarm dispatch
    	int R = 184 + (int)Math.floor(Distributions.exponential(174));
    	int T = 184 + (int)Math.floor(Distributions.exponential(174));
    	for(int i = 0; i < 3; i++) {
            if(i < 3) { scheduleEngineArr(0); }
            if(i < 2) { scheduleTruckArr(0,T);
            T = 184 + (int)Math.floor(Distributions.exponential(174)); }
            if(i < 1) { scheduleRescueArr(0,R); }
        }
    }
   //find min(time) crew can stay inside
    private int SCBAtime(int crewsize) {
    	int i =0;
    	int ans=0;
    	int min=30;
    	while(i<crewsize) {
    		ans=Distributions.triangular(1200,1500,1800);
    		if(min<ans) {
    			min=ans;
    		} 
    		i++;
    	}
    	return min;
    }
    // tanks have a triangular distribution of 1200, 1500, 1800 seconds 
    private void scheduleTankOut(int numReps) {
        Event e = new Event();
        e.setType(EventTypes.Events.TANKOUT);
        e.setTime(sysTime+numReps);
        p.add(e);
    }
    private  void processTankOut() throws RuntimeException{
        if(Tankcycles<0) {// Failfast
            System.out.println("Error: tanks went negative");
            throw new RuntimeException();
        	}
        Tankcycles--;
    }    
 }
    

  
   
    

    
    
    
    