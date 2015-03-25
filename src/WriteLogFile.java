/**
 * @author Andrea Biagini
 * 
 */

import java.io.*;
import java.util.*;
import java.text.*;
//import java.lang.*;



public class WriteLogFile {
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd"); 
	Calendar calendar = new GregorianCalendar(); 
	Date newDate = calendar.getTime(); 
	String data = formatter.format(newDate);
	static String folder= System.getProperty("user.home");
	static String path;
	  
	
	/**
	 * Costruttore
	 *
	 */	
	public WriteLogFile(String d ){
		path = folder +"/"+d+"/" +data + "log.txt";
		
	
		
	}
	


/**
 * Crea un nuovo file se non esiste
 */
	public static void newFile() {
		try {
		    File file = new File(path);
		    
		    if (!file.exists())
		    		file.createNewFile();
			  }
		  catch (IOException exc) {
			 System.out.println("Il file" + path + "non pu� essere creato");
		  }
	}
	/**
	 * Scrive la stringa sul file 
	 */
	public static void writeFile(String Value ) {
	    
	    try {
	      File file = new File(path);	 
	      FileWriter fw = new FileWriter(file, true);
	      BufferedWriter bw = new BufferedWriter(fw);
	      if (!file.exists())
	    		file.createNewFile();	    
	      bw.append(Value+ System.getProperty("line.separator") );
	      // era "\r\n"
	      bw.flush();
	      bw.close();
	    }
	    catch(IOException e) {
	      e.printStackTrace();
	    }
	  }
	
}