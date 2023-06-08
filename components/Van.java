package components;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Van extends Truck
{
	public Van() {
		super();
		System.out.println("Creating " + this);
	}
	
	
	public Van(String licensePlate,String truckModel) {
		super(licensePlate,truckModel);
	}
	

	@Override
	public String toString() {
		return "Van ["+ super.toString() + "]";
	}
	
	
	@Override
	public synchronized void deliverPackage(Package p) {
		this.getPackages().add(p);
		setAvailable(false);
		int time=(p.getDestinationAddress().street%10+1)*10;
		this.setTimeLeft(time);
		this.initTime = time;
		p.setStatus(Status.DISTRIBUTION);
		p.addTracking(new Tracking(MainOffice.getClock(), this, p.getStatus(), p.getPackageID()));
		System.out.println("Van "+ this.getTruckID() + " is delivering package " + p.getPackageID() + ", time left: "+ this.getTimeLeft()  );
	}
	
	
	@Override
	public void run() {
		while(true) 
		{
			if (this.isNotRun == true) { return; }
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    synchronized(this) {
                while (threadSuspend)
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                
                if (this.isNotRun == true) { return; }
		    }
		    
			Branch branch=null;
			
			if (!this.isAvailable()) {
				this.setTimeLeft(this.getTimeLeft()-1);
				if (this.getTimeLeft()==0){
					for (Package p : this.getPackages()) {
						if (p.getStatus()==Status.COLLECTION) {
							branch=MainOffice.getInstance().getHub().getBranches().get(p.getSenderAddress().zip);
							synchronized(branch) {
								p.setStatus(Status.BRANCH_STORAGE);
								System.out.println("Van " + this.getTruckID() + " has collected package " +p.getPackageID()+" and arrived back to branch " + branch.getBranchId());
								branch.addPackage(p);
							}
						}
						else 
						{
							p.setStatus(Status.DELIVERED);
							branch=MainOffice.getInstance().getHub().getBranches().get(p.getDestinationAddress().zip);
							synchronized(branch) {
								branch.listPackages.remove(p);
								branch=null;
								System.out.println("Van " + this.getTruckID() + " has delivered package "+p.getPackageID() + " to the destination");
								if (p instanceof SmallPackage && ((SmallPackage)p).isAcknowledge()) {
									System.out.println("Acknowledge sent for package "+p.getPackageID());
								}
							}
						}
						p.addTracking(new Tracking(MainOffice.getClock(), branch, p.getStatus(), p.getPackageID()));
	
					}
					this.getPackages().removeAll(getPackages());
					this.setAvailable(true);
				}
			}
			else 				
				synchronized(this) 
				{
					if (this.isNotRun == true) { return; }
					
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
		
	}
	
	@Override
	public void work() {

	}


	@Override
	public void paintComponent(Graphics g) {
		if (isAvailable()) return;
		Package p = this.getPackages().get(getPackages().size()-1);	
		Point start=null;
		Point end=null;
		if (p.getStatus()==Status.COLLECTION) {
			start = p.getSendPoint();
			end = p.getBInPoint();
		}
		else if (p.getStatus()==Status.DISTRIBUTION) {
			start = p.getBOutPoint();
			end = p.getDestPoint();
		}
		
		if (start!=null) {
			int x2 = start.getX();
			int y2 = start.getY();
			int x1 = end.getX();
			int y1 = end.getY();
				
			double ratio = (double) this.getTimeLeft()/this.initTime;
			double length = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
			int dX = (int) (ratio*(x2-x1));
			int dY = (int) (ratio*(y2-y1));
				
			g.setColor(Color.BLUE);
			g.fillRect(dX+x1-8, dY+y1-8, 16, 16); 
			g.setColor(Color.BLACK);
			g.fillOval(dX+x1-12, dY+y1-12, 10, 10);
			g.fillOval(dX+x1, dY+y1, 10, 10);
			g.fillOval(dX+x1, dY+y1-12, 10, 10);
			g.fillOval(dX+x1-12, dY+y1, 10, 10);
		}

	}
	


	@Override
	public Object clone() 
	{
		Random r = new Random();
		Van cloneVan = new Van((r.nextInt(900)+100)+"-"+(r.nextInt(90)+10)+"-"+(r.nextInt(900)+100), this.getTruckModel());
		
		return cloneVan;
	}
}
