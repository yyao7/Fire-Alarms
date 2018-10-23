//An event holds 2, a timestamp, and a type.
public class Event {
   private int time;
   private EventTypes.Events type;
   
   //Constructors
   public Event() {}
   public Event(int time, EventTypes.Events type) {
      this.time = time;
      this.type = type;
   }
   //Constructors
   
   //Getters
   public int getTime()              { return this.time;  }
   public EventTypes.Events getType(){ return this.type;  }
   //Getters
   
   //Setters
   public void setTime(int time) { this.time = time; }
   public void setType(EventTypes.Events type) { this.type = type; }
   //Setters
   
   public boolean equals(Event other) {
      boolean a = this.time==other.getTime();
      boolean b = this.type==other.getType();
      return a&&b;
   }
   
   @Override
   public String toString() {
        return type.toString()+", time: " + time;
   }
}