package components;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


public class ReadWriteFile 
{
	private static int numOfLine = 1;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final String pathOfTxtFile = "tracking.txt";
    private ArrayList <String> dataFile;
    
    /**
     * A function that checks if all the customer's packages have reached their destination
     * @param sireal
     * @return
     */
    public boolean isPackagesInDestination(String sireal)
    {
    	readFromFile();
    	dataFile.removeIf(filtered -> !filtered.contains(sireal));
    	
    	if (dataFile != null)
    	{
    		dataFile.removeIf(filtered -> !filtered.contains(Status.DELIVERED.toString()));
    		
    		if (dataFile.size() == 5) { return true; }
    	}
    	
    	return false;
    }
    
    
    /**
     * A function that writes into the file 
     * @param p
     */
    public void writeToFile(Package p)
    {
        writeLock.lock();
        try
        {
        	boolean flag = false;
        	String emptyLine = "";
        	
        	if (p != null)
        	{
        		flag = true;
        		emptyLine = numOfLine++ + ". " + p.getStringOfLastTracking() + "\r\n";
        	}
        	
        	FileWriter fileWriter = new FileWriter(pathOfTxtFile, flag);
        	fileWriter.write(emptyLine);
        	fileWriter.close();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally
        {
            writeLock.unlock();
        }
    }
     
    /**
     * A function that reads from the file
     */
    public void readFromFile()
    {
    	readLock.lock();
        try
        {
        	dataFile = new ArrayList<>(Files.lines(Paths.get(pathOfTxtFile)).collect(Collectors.toList()));   
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally
        {
        	readLock.unlock();
        }
    }
    
    public void setNumOfLine() { numOfLine = 1; }
}
