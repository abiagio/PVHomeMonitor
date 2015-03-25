import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * @author Andrea Biagini
 * 
 */



public class DecodeSolarString {
	private static byte[] byteRecived;

	public int ind;
	public final  int[] storeP = new int[1000];
	public final  int[] storeC = new int[1000];	
//	private static WriteLogFile wlf;
	// costruttore
	
	public DecodeSolarString(){
	
	
	
		}
	
	public String getConsumption(){
		int offset= 13;
	    return ( getSensor(offset));
		
	}
	
	public String getProduction(){
		int offset= 14;
		return ( getSensor(offset));
		
	}	
	public int getIntConsumption() {
		int offset= 13;
	    return ( getIntSensor(offset));
		
	}
	public int getIntProduction(){
		int offset= 14;
		return ( getIntSensor(offset));
	}	
	// ritorna il valore di ogni canale ogni canale � composto da 8 byte , 3 per la descrizione + 4 valore + ultimo non so
	// id =0 riporta S01
	public String getSensor(int id ){
		id=id*8;
		byte[] val={byteRecived[id+3], byteRecived[id+4],byteRecived[id+5],byteRecived[id+6]};                                                 
		return ( Integer.toString(byteArrayToInt(val)));
	}		
	
	public int getIntSensor(int id ){
		id=id*8;
		byte[] val={byteRecived[id+3], byteRecived[id+4],byteRecived[id+5],byteRecived[id+6]};                                                 
		return ( byteArrayToInt(val));
	}		
	public int byteArrayToInt(byte[] b) 
	{
	 	   			
		return  b[2] & 0xFF |
	            (b[3] & 0xFF) << 8 ;
	}
	public String getStringToSend(String Log_dir){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd,HH:mm");
		
		Calendar calendar = new GregorianCalendar(); 
		Date newDate = calendar.getTime(); 
		String date = formatter.format(newDate);
		String data ="data="+date+ ",-1,"+getPAvarage()+",-1,"+getCAvarage();
		reset();
	
	 if (Log_dir!=""){
		 WriteLogFile wlf = new WriteLogFile(Log_dir);
		 wlf.writeFile(new String(data)); 
	 }
		
		return (data);
		
	}
	public int saveData(byte[] byteR){
		byteRecived=byteR;
		storeP[ind]=getIntProduction();
		storeC[ind]=getIntConsumption();
		ind++;
		return (ind) ;
	}
	public void  reset(){
		ind=0;
		storeP[0]=0;
		storeC[0]=0;
		}
	
	public String  getPAvarage( ){
		int i=0;
		int somma=0;
		for (i=0; i<ind; i++){
			somma=somma+storeP[i];
		}
		somma=somma/i-1;
		// coorregge di un 6 % di differenza rilevato rispetto le indicazioni dell'inverter
		somma=somma+somma*5/100;
		return (Integer.toString(somma)) ;
	}
	public String  getCAvarage( ){
		int i=0;
		int somma=0;
		for (i=0; i<ind; i++){
			somma=somma+storeC[i];
		}
		somma=somma/i-1;
		return (Integer.toString(somma)) ;
	}
}

