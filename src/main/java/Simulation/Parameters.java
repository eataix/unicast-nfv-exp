package Simulation;
import java.io.FileReader;
import java.io.BufferedReader;
import java.net.URL;

public class Parameters { //singleton class
	private static String filepath = "src/resources/Parameters/"; 
	public static int [] NFVreq; //nfv vm resource requirements
	public static int [] NFVrate; //nfv vm service rate
	public static int [] NFVOpCost; //operating cost of providing an vnf service 
	public static int [] NFVInitCost; //initialization cost of vnf service 
	public static int a; //alpha and beta are multipliers to |V|
	public static int b;
	public static double threshold = 1;
	public static double serverRatio = 0.2;
	public static int L; //maximum chain length
	
	public static void importParameters(String filename){
		try{
			FileReader fr = new FileReader(filepath+filename);
			BufferedReader br = new BufferedReader(fr);
			String [] nfvreq = br.readLine().split(" ");
			String [] nfvrate = br.readLine().split(" ");
			String [] nfvopcost = br.readLine().split(" ");
			String [] nfvinitcost = br.readLine().split(" ");			
			a = Integer.parseInt(br.readLine());
			b = Integer.parseInt(br.readLine());
			
			L = nfvreq.length;
			NFVreq = new int [L];
			NFVrate = new int [L];
			NFVOpCost = new int [L];
			NFVInitCost = new int [L];
			for(int i = 0; i < L; i++){
				NFVreq[i]	   = Integer.parseInt(nfvreq[i]);
				NFVrate[i] 	   = Integer.parseInt(nfvrate[i]);
				NFVOpCost[i]   = Integer.parseInt(nfvopcost[i]);
				NFVInitCost[i] = Integer.parseInt(nfvinitcost[i]);
			}	

		}
		catch(Exception e){
			System.out.println("Parameter import has failed");
			e.printStackTrace();
			return;
		}
	}
}
