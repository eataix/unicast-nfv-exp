package Network;
import java.util.HashSet;

import Simulation.Parameters;

public class Request {
	public int delay; //delay bound requirement
	public int [] SC; //each number points to index of nfv in Parameters
	public int bandwidth;
	Server src;
	Server dst;
	
	public Request(int b, Server s, Server t){
		bandwidth = b;
		src = s;
		dst = t;
		generateServiceChain();
	}
	
	public boolean setServiceChain(int [] sc){
		//make sure all elements are valid nfv ids, and there are no repeats. 
		HashSet<Integer> nfvs = new HashSet<Integer>();
		for(int i = 0; i < sc.length; i++){
			if(sc[i]>=Parameters.L || nfvs.contains(sc[i])){
				return false;
			}
			else {
				nfvs.add(sc[i]);
			}
		}
		SC = sc;
		return true;
	}
	
	public Server getSource(){
		return src;
	}
	
	public Server getDest(){
		return dst;
	}
	
	private void generateServiceChain(){
		//create randomly ordered list of NFVs
		int [] nfvlist = new int[Parameters.L];
		for(int i = 0; i < Parameters.L; i++)
			nfvlist[i] = i;
		//fisher-yates shuffle
		for(int i = 0; i < Parameters.L; i++){
			int temp = nfvlist[i];
			int index = (int) Math.floor(Math.random()*(Parameters.L-i)+i);
			nfvlist[i] = nfvlist[index];
			nfvlist[index] = temp;
		}
		
		int l = (int) Math.floor(Math.random()*(Parameters.L-1)+1); //ensure there is at least one service in the service chain
		
		SC = new int [l];
		for(int i = 0; i < l; i++){
			SC[i] = nfvlist[i];
		}
	}
}
