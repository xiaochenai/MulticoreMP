package IDACS_Common;

   public class Timeout
   {
		private long startTime;
		private long expireTime;
		private long duration;
		private boolean running;
		
   
	   //Constructor
      public Timeout(int delay, boolean runNow)
      {
			this.startTime = System.currentTimeMillis();
			this.expireTime = this.startTime + delay;
			this.duration = delay;
			
			if(runNow == true)
				{this.running = true;}
			else
				{this.running = false;}
      }
   
		// check if Timeout is still running
		public boolean isRunning()
		{
			if((this.running) && (System.currentTimeMillis() < expireTime))
			{
				return true;
			}
			else
			{
				this.running = false;
				return false;
			}					
		}
		
		// reset the Timeout according to the preset "duration"
		public void reset()
		{
			this.startTime = System.currentTimeMillis();
			this.expireTime = this.startTime + this.duration;
			this.running = true;
		}
		
		// change the duration of the Timeout and reset
		public void changeDuration(int delay)
		{
			this.duration = delay;
			this.startTime = System.currentTimeMillis();
			this.expireTime = this.startTime + this.duration;			
			this.running = true;        
			return;  		
		}
		
		// kill the time because it is irrelevant
		public void kill()
		{
			this.expireTime = this.startTime;
			this.running = false;
			return;
		}


   }