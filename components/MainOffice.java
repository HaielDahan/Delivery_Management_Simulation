package components;


import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JPanel;


public class MainOffice implements Runnable, Cloneable
{
	private static int clock=0;
	private Hub hub;
	private ArrayList<Package> packages=new ArrayList<Package>();
	private JPanel panel;
	//private int maxPackages;
	private boolean threadSuspend = false;

	
	private static volatile MainOffice theMainOffice; 
	private static final int NUM_OF_CUSTOMERS = 10;
	private static final int NUM_OF_TRUCKS_FOR_BRANCHES = 5, NUM_OF_BRANCHES = 5, NUM_OF_PACKAGES = NUM_OF_CUSTOMERS * 5; // 5 packages per customer
	private ThreadPoolExecutor threadPool =  (ThreadPoolExecutor)Executors.newFixedThreadPool(2);
	private Vector<Customer> customerVec = new Vector<>();
	protected boolean isNotRun = false;
	
	
	private MainOffice(int branches, int trucksForBranch, JPanel panel/*, int maxPack*/) {
		this.panel = panel;
		//this.maxPackages = maxPack;
		addHub(trucksForBranch);
		addBranches(branches, trucksForBranch);
		System.out.println("\n\n========================== START ==========================");
	}
	
	
	private MainOffice()
	{
		addHub(NUM_OF_TRUCKS_FOR_BRANCHES);
		addBranches(NUM_OF_BRANCHES, NUM_OF_TRUCKS_FOR_BRANCHES);
	}
	
	public Object clone()
	{
		MainOffice clone = new MainOffice();
		clone.setPanel(this.panel);
		
		return clone;
	}
	
	public void restore(MainOffice m) { theMainOffice = m; }
	
	public void resetStaticMainOffice()
	{
		Branch.resetStaticBranch();
		Truck.resetStaticTruck();
	}
	
	public void stopAllThreads() 
	{ 
		this.isNotRun = true; 
		
		for (Customer c : this.customerVec) { c.setIsNotRun(); }
		
		for (Branch b : this.hub.getBranches()) 
		{ 
			for (Truck t : b.getTrucks()) { t.setIsNotRun(); }
			b.setIsNotRun(); 
		}
		
		this.hub.setIsNotRun();
	} 
	
	
	public void setPanel(JPanel panel) { this.panel = panel; }
	
	
	public static int getNumOfPackages() { return NUM_OF_PACKAGES; }
	
	
	public static int getNumOfTrucksForBranches() { return NUM_OF_TRUCKS_FOR_BRANCHES; }
	
	
	public static int getNumOfBranches() { return NUM_OF_BRANCHES; }
	
	
	public static MainOffice getInstance(int branches, int trucksForBranch, JPanel panel)
	{
		 if (theMainOffice == null) 
	        {
	            synchronized (MainOffice.class) 
	            {
	                if (theMainOffice == null) 
	                {
	                	theMainOffice = new MainOffice(branches, trucksForBranch, panel);
	                }
	            }
	        }  
		 
	     return theMainOffice;
	}
	
	
    public static MainOffice getInstance() 
    {
        if (theMainOffice == null) 
        {
            synchronized (MainOffice.class) 
            {
                if (theMainOffice == null) 
                {
                	theMainOffice = new MainOffice();
                }
            }
        }
        
        return theMainOffice;
    }
	
	
	public Hub getHub() {
		return hub;
	}


	public static int getClock() {
		return clock;
	}

	@Override
	public void run() 
	{
		for (int i = 0; i < NUM_OF_CUSTOMERS; i++)
		{
			customerVec.add(new Customer());
			//new Thread(customerVec.get(i)).start();
			this.threadPool.execute(customerVec.get(i));
		}
		
		Thread hubThread = new Thread(hub);
		hubThread.start();
	
		
		for (Truck t : hub.listTrucks) 
		{
			Thread trackThread = new Thread(t);
			trackThread.start();
		}
		
		for (Branch b: hub.getBranches()) 
		{
			Thread branch = new Thread(b);
			for (Truck t : b.listTrucks)
			{
				Thread trackThread = new Thread(t);
				trackThread.start();
			}
			branch.start();
		}
		
		while(true) 
		{
		    synchronized(this) 
		    {
		    	if (this.isNotRun == true) { return; }
		    	
                while (threadSuspend)
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    }
			//tick();
		    this.panel.repaint();
		}
	}
	
	
	public void printReport() {
		for (Package p: packages) {
			System.out.println("\nTRACKING " +p);
			for (Tracking t: p.getTracking())
				System.out.println(t);
		}
	}
	
	
	public String clockString() {
		String s="";
		int minutes=clock/60;
		int seconds=clock%60;
		s+=(minutes<10) ? "0" + minutes : minutes;
		s+=":";
		s+=(seconds<10) ? "0" + seconds : seconds;
		return s;
	}
	
	
//	public void tick() {
//		try {
//			Thread.sleep(300);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(clockString());
//		
////		if (clock++%5==0 && maxPackages>0) {
////			addPackage();
////			maxPackages--;
////		}
//		
//		/*branchWork(hub);
//		for (Branch b:hub.getBranches()) {
//			branchWork(b);
//		}*/
//		
//		panel.repaint();
//	}
	
	
	
	public void branchWork(Branch b) {
		for (Truck t : b.listTrucks) {
			t.work();
		}
		b.work();
	}
	
	
	public void addHub(int trucksForBranch) {
		hub=new Hub();
		for (int i=0; i<trucksForBranch; i++) {
			Truck t = new StandardTruck();
			hub.addTruck(t);
		}
		Truck t=new NonStandardTruck();
		hub.addTruck(t);
	}
	
	
	public void addBranches(int branches, int trucks) {
		for (int i=0; i<branches; i++) {
			Branch branch=new Branch();
			for (int j=0; j<trucks; j++) {
				branch.addTruck(new Van());
			}
			hub.add_branch(branch);		
		}
	}
	
	
	public ArrayList<Package> getPackages(){
		return this.packages;
	}
	
//	public void addPackage() {
//		Random r = new Random();
//		Package p;
//		Branch br;
//		Priority priority=Priority.values()[r.nextInt(3)];
//		Address sender = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999)+100000);
//		Address dest = new Address(r.nextInt(hub.getBranches().size()), r.nextInt(999999)+100000);
//
//		switch (r.nextInt(3)){
//		case 0:
//			p = new SmallPackage(priority,  sender, dest, r.nextBoolean() );
//			br = hub.getBranches().get(sender.zip);
//			br.addPackage(p);
//			p.setBranch(br); 
//			break;
//		case 1:
//			p = new StandardPackage(priority,  sender, dest, r.nextFloat()+(r.nextInt(9)+1));
//			br = hub.getBranches().get(sender.zip); 
//			br.addPackage(p);
//			p.setBranch(br); 
//			break;
//		case 2:
//			p=new NonStandardPackage(priority,  sender, dest,  r.nextInt(1000), r.nextInt(500), r.nextInt(400));
//			hub.addPackage(p);
//			break;
//		default:
//			p=null;
//			return;
//		}
//		
//		this.packages.add(p);
//	}
	
	
	public synchronized void setSuspend() 
	{
	   	threadSuspend = true;
	   	
		for(Customer c : this.customerVec)
		{
			c.setSuspend();
		}
		for (Truck t : hub.listTrucks) 
		{
			t.setSuspend();
		}
		for (Branch b: hub.getBranches()) 
		{
			for (Truck t : b.listTrucks) 
			{
				t.setSuspend();
			}
			b.setSuspend();
		}
		
		hub.setSuspend();
		
	}

	
	
	public synchronized void setResume() {
	   	threadSuspend = false;
	   	
		for(Customer c : this.customerVec)
		{
			c.setResume();
		}
		
	   	notify();
	   	
	   	hub.setResume();
		for (Truck t : hub.listTrucks) {
			t.setResume();
		}
		for (Branch b: hub.getBranches()) {
			b.setResume();
			for (Truck t : b.listTrucks) {
				t.setResume();
			}
		}

	}
}
