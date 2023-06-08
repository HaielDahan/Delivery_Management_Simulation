package components;


import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Customer implements Runnable
{
	private static final int MAX_NUM_OF_PACKAGES = 5, TIME_TO_SLEEP = 10000;
	private static int packageCounter = 0;
	private int packageCounterPerCustomer = 0;
	private String serial;
	private Address customerAddress; 
	private ReadWriteFile readWriteFile = new ReadWriteFile();
	MainOffice mainOffice = MainOffice.getInstance();
	private boolean threadSuspend = false;
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private boolean isNotRun = false;
	
	/**
	 * Default constructor
	 */
	public Customer() 
	{
		String abc = "XYZW";
		this.serial = String.valueOf(abc.charAt((new Random()).nextInt(abc.length()))) + packageCounter++;
		this.customerAddress = new Address((new Random()).nextInt(mainOffice.getHub().getBranches().size()), (new Random()).nextInt(999999) + 100000);
	}
	
	/**
	 * A function that adds packages
	 */
	public void addPackage() 
	{
		lock.writeLock().lock();
		
		Random r = new Random();
		Package p;
		Branch br;
		Priority priority=Priority.values()[r.nextInt(3)];
		//Address sender = new Address(r.nextInt(MainOffice.getHub().getBranches().size()), r.nextInt(999999)+100000);
		Address dest = new Address(r.nextInt(mainOffice.getHub().getBranches().size()), r.nextInt(999999)+100000);

		switch (r.nextInt(3))
		{
		case 0:
			p = new SmallPackage(priority,  this.customerAddress, dest, r.nextBoolean(), this.serial);
			br = mainOffice.getHub().getBranches().get(this.customerAddress.zip);
			br.addPackage(p);
			p.setBranch(br); 
			break;
		case 1:
			p = new StandardPackage(priority,  this.customerAddress, dest, r.nextFloat()+(r.nextInt(9)+1), this.serial);
			br = mainOffice.getHub().getBranches().get(this.customerAddress.zip); 
			br.addPackage(p);
			p.setBranch(br); 
			break;
		case 2:
			p=new NonStandardPackage(priority,  this.customerAddress, dest,  r.nextInt(1000), r.nextInt(500), r.nextInt(400), this.serial);
			mainOffice.getHub().addPackage(p);
			break;
		default:
			p=null;
			return;
		}
		
		mainOffice.getPackages().add(p);
		lock.writeLock().unlock();
	}
	
	@Override
	public void run() 
	{
		while (this.packageCounterPerCustomer < MAX_NUM_OF_PACKAGES)
		{	
			if (this.isNotRun == true) { return; }
			
			try {
				Thread.sleep(((new Random()).nextInt(6) + 2) * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			synchronized(this) 
			{
	            while (threadSuspend)
	            {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	            
	            if (this.isNotRun == true) { return; }
			}
			
			addPackage();
			this.packageCounterPerCustomer++;
		}
		
		while (!readWriteFile.isPackagesInDestination(this.serial))
		{
			if (this.isNotRun == true) { return; }
			
			try {
				Thread.sleep(TIME_TO_SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setIsNotRun() { this.isNotRun = true; }
	
	public synchronized void setSuspend() {
	   	threadSuspend = true;
	}

	public synchronized void setResume() {
	   	threadSuspend = false;
	   	notify();
	}
}
