import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.lang.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.Timer;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author Andrea Biagini
 * 
 */


// Dependencies:
// $ sudo apt-get install librxtx-java

// Compile and run with:
// $ javac -cp /usr/share/java/RXTXcomm.jar:. PVHomeMonitor.java s:[] k:[]
// $ java -Djava.library.path=/usr/lib/jni -cp /usr/share/java/RXTXcomm.jar:. PVHomeMonitor s:[] k:[]



public class PVHomeMonitor
{

	public static GpioController gpio = GpioFactory.getInstance();
	public static GpioPinDigitalOutput Beep;
	public static GpioPinDigitalOutput Led;
	public static Timer timer;
	public static Timer sendClock;
	public static DecodeSolarString dss;
	//public static PvOutSend http;
	public static int ByteToRecive=0;
	public static boolean go=true;
	
	// Costanti
	
	static final int TIME_REQUEST=1000*5*1; //millisecondi*secondi*minuti
    static final int SEND_REQUEST=1000*60*10; //millisecondi*secondi*minuti
	// Defaults
	public static int SOGLIA_ALL=-3000;  //Watt Da parametrizare anche questo

    public static String serialPort = "/dev/ttyUSB0";
    //public static String serialPort = "COM1";
        public static  String sid="";
	public static  String key="";
	public static  String Log_dir="";
	
	
	public static SerialPort sp;
	
	public static int tot;
	public static int index;
//	public static  byte[] byteRecived;
//  public static int bufferlenght;
	static final byte[] testRicevimento1={
			 0x53, 0x30, 0x31, 0x0E, 0x06, 00, 00, 0x32, 0x53, 0x30, 0x32, 00, 00, 00, 00, 0x32,
			 0x53, 0x30, 0x33, 00, 00, 00, 00, 0x32, 0x53, 0x30, 0x34, 00, 00, 00, 00, 0x32,   
			 0x53, 0x30, 0x35, 00, 00, 00, 00, 0x32, 0x53, 0x30, 0x36, 00, 00, 00, 00, 0x32,   
			 0x53, 0x30, 0x37, 00, 00, 00, 00, 0x32, 0x53, 0x30, 0x38, 00, 00, 00, 00, 0x32,   
			};
	static final byte[] testRicevimento2={
			0x53, 0x30, 0x39, 00, 00, 00, 00, 0x32, 0x53, 0x31, 0x30, 00, 00, 00, 00, 0x32,
			0x53, 0x31, 0x31, 00, 00, 00, 00, 0x32, 0x53, 0x31, 0x32, 00, 00, 00, 00, 0x32,   
			 0x53, 0x31, 0x33, 00, 00, 00, 00, 0x32, 0x53, 0x31, 0x34, 0x78, 0x33, 0x00, 0x01, 0x31,   
			 0x53, 0x31, 0x35, (byte) 0x84, 0x33, 0x14, 00, 0x31, 0x53, 0x31, 0x36, (byte) 0xFF, (byte) 0xFF, 00, 00, 0x32,   
			 0x2A };

  void connect( String portName, int baudRate, int bufferSize ) throws Exception 
  				{
	  				CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( portName );
	  				if( portIdentifier.isCurrentlyOwned() ) {
	  						System.out.println( "[ERROR] Port '" + portName + "' is currently in use" );
	  				} 
	  				else {
	  					int timeout = 2000;
	  					CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );

	  					if( commPort instanceof SerialPort ) {
	  						sp = ( SerialPort ) commPort;
	  						System.out.println( "[USAGE] t+Enter: Start/Stop AutoMode, d+Enter: Test/Eco Mode, c+Enter: Manual request X+Enter: Exit" );
	  						sp.setSerialPortParams( baudRate,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE );

	  					//	System.out.println( "[GET] Input stream" );
					    //    System.out.println( "[GET] Output stream" );
					        InputStream in =sp.getInputStream();
					        OutputStream out = sp.getOutputStream();

					     //   System.out.println( "[START] Serial reader" );
					     //   System.out.println( "[START] Serial writer" );
					        ( new Thread( new SerialReader( in, bufferSize) ) ).start();
					        ( new Thread( new SerialWriter( out ) ) ).start();
					        
					    //   final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.LOW);
	
        
	  					} 
	  					else 
	  					{
	  						System.out.println( "[ERROR] Only serial ports are implemented" );
	  					}
	  				}
  				}
  
  // Legge  Seriale

  public  class SerialReader implements Runnable {

    InputStream in;
    int bufferSize;
   
    

    public SerialReader( InputStream in, int bufferSize) {
      this.in = in;
      this.bufferSize = bufferSize;
  
     dss= new DecodeSolarString(); 
      sendClock =new Timer(SEND_REQUEST,clock );

    }
	// allo scadere del timer invia nuovi dati a pvout
	
		 ActionListener clock = new ActionListener() {
			 
			 @Override
			
			 public void actionPerformed(ActionEvent event) {
				 if (dss.ind >1){
			
					 PvOutSend http =new PvOutSend();
					 http = new PvOutSend();
				 try {
					http.sendPost(dss.getStringToSend(Log_dir),sid,key);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
			 }
			}
		};

    public void run() {
      byte[] buffer = new byte[ this.bufferSize ];
      int len = -1;
      int index=0;
      Beep = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyBeep", PinState.LOW);
      Led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "MyLED", PinState.LOW);
      try {
    	  while (go ){
    		if (ByteToRecive!=0){
    			
    		 while ( index!=ByteToRecive){
    			
    		  len=this.in.read(buffer,index,ByteToRecive);
    		  if (len!=-1)index=index+len;
    		 }
    	   
    		 if (index>1){ 
    		  
				dss.saveData(buffer);
				tot=-dss.getIntConsumption()+dss.getIntProduction();
				System.out.println(new String(Integer.toString(tot)+"   "+Integer.toString(dss.ind)));
				if (tot<SOGLIA_ALL){
					Toolkit.getDefaultToolkit().beep();
					Beep.pulse(1500, true);
					Led.pulse(3000, true);
				}
					
				
				ByteToRecive=0;
				index=0;
    		 }
    		 else 
    			 System.out.println("[TEST]='" + new String( buffer, 0, index ) + "'");	
    			 ByteToRecive=0;
    			 index=0;
    			
    			 
    	  }
    	
    	 }
      } catch( IOException e ) {
        e.printStackTrace();
      }
    }
 
  }
  
  // Scrive su seriale 
  
  public  class SerialWriter implements Runnable {

    OutputStream out;
   
    public SerialWriter( OutputStream out ) {
      this.out = out;
      timer = new Timer(TIME_REQUEST, time);
    }
   
    
    public void run() {
      try {
        int  c=-1;

        timer.start();
        System.out.println("[SET]: Auto ON" );
     
        while ( go ) 
        {
        c=System.in.read();
        if (c !=-1) {
        switch (c)
		{
	
		case 99: //c
			ByteToRecive=0x81;
			
			
			SerialWriter.this.out.write(0xAA);
			SerialWriter.this.out.write(0x02);
			SerialWriter.this.out.write(0x00);
			SerialWriter.this.out.write(0xAD);
			
			break;
		case 100: //d
			ByteToRecive=0x81;
			
			this.out.write(testRicevimento1);
			this.out.write(testRicevimento2);
			
			break;
		case 116: //t
			//
			 if (timer.isRunning())
			 { 
				 System.out.println("[SET]: Auto OFF" );
				 timer.stop();
			 }
			 else
			 {
				 timer.start();
				 System.out.println("[SET]: Auto ON" );
			 }
			break;	
		
		case 13:
			break;
		case 10:
			break;
		case  88: // X
			go=false;
			sp.close();
			gpio.shutdown();
			//  chiudi tutti i thread la seriale ed esci 
			System.exit(0);
			
			break;
		default:
			this.out.write(c);
			ByteToRecive=1;
			break;
		}
        }
        }
      } catch( IOException e ) {
        e.printStackTrace();
      }
    }

    // allo scadere del timer invia una richiesta di dati 
    ActionListener time = new ActionListener() {
		 
		 @Override
		
		 public void actionPerformed(ActionEvent event) {
			 try {
				ByteToRecive=0x81;
				SerialWriter.this.out.write(0xAA);
				SerialWriter.this.out.write(0x02);
				SerialWriter.this.out.write(0x00);
				SerialWriter.this.out.write(0xAD);
				//SerialWriter.this.out.write(testRicevimento1);
				//SerialWriter.this.out.write(testRicevimento2);
				
			 
			 } catch( IOException e ) {
			        e.printStackTrace();
			      }		
				
				if(!sendClock.isRunning())
					sendClock.start();
			 }
		 
		
	};

  }

  public static void main( String[] args ) {
   
    int baudRate = 9600;
    int bufferSize = 1024; // TODO: Find an appropriate buffer size
    
// Passare , seriale, sid, key
    for (int i = 0; i < args.length; i++) {
        
    	switch (args[i].substring(0, 2)) {
          case "-s:":
            try {
              sid = args[i].substring(3,args[i].length());
              System.out.println("[ARGUMENT] Pvout SID: " + sid);
            } catch (NumberFormatException e) {
              System.out.println("SID not correct '" + args[i] + "'");
              System.out.println("[SYSTEM EXIT]");
              System.exit(0);
            }
            break;
          case "-k:":
            try {
              key = args[i].substring(3,args[i].length());
              System.out.println("[ARGUMENT] pvout Key: " + key);
            } catch (NumberFormatException e) {
              System.out.println("[ERROR] Key not correct. Was: '" + args[i] + "'");
              System.out.println("[SYSTEM EXIT]");
              System.exit(0);
            }
            break;
          case "-c":
            serialPort = args[i].substring(3,args[i].length());
            System.out.println("[ARGUMENT] port: " + serialPort);
            break;
          case "-l":
              Log_dir =args[i].substring(3,args[i].length());
              System.out.println("[ARGUMENT] Log directroy: " + Log_dir);
              break;
          case "-p":
             SOGLIA_ALL = Integer.parseInt(args[i].substring(3,args[i].length()));
              System.out.println("[ARGUMENT] Power Limit: " + new String(Integer.toString(SOGLIA_ALL)));
             
              break;
          case "-?" :
        	  System.out.println("\n\n******* PVHomeMonitor ********** By Andrea Biagini\n");
        	    System.out.println("Usage:PVHomeMonitor.jar [-c:Serialport] -s:sid -k:key [-l:log_dir] [-p:Power_Limit]\n");
              	System.out.println("\nDefault serial com is ttyUSB0. \nSid and Key must be provided from your pvout setup.\nIf no log_dir log won't be generated.");
              	System.out.println("\nPower_limit is the contractual power on your House(-watt), default is -3000 \nAlarm will be generated if Power Consumption-Generated is more than Power Limit. ");
              	System.out.println("\nExample : PVHomeMonitor.jar -s:1234 -k:oxfjsjdsdskdf -l:Solar-log\n\n");
              	
              	System.exit(0);
              break;	
             
        }
    }

    System.out.println("[USING] port:        " + serialPort);
    System.out.println("[USING] Sid:   " + sid);
    System.out.println("[USING] Key: " + key);


    // TODO: Potentially not necessary to set it explicitly with System.setProperty
    // Set SerialPorts property for gnu.io.rxtx
    System.setProperty("gnu.io.rxtx.SerialPorts", serialPort);
    try {
      System.out.println("\n\n[CONNECT]: serial=" + serialPort + ", baudRate=" + baudRate + ", bufferSize=" + bufferSize);
      ( new PVHomeMonitor()).connect( serialPort, baudRate, bufferSize);
    } catch( Exception e ) {
      e.printStackTrace();
    }
    
  }
}