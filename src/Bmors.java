import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Bmors {
	 public String[][] datasetMatrix;
	 public int numCols = 0;
	 public int numRows = 0;
	 public String[][] Skyline;
	 public int numSkyline = 0;
	 private String[][][] lists = new String[10][][];
	 
	 public Bmors(int numRows, int numCols){
		 
		this.numCols = numCols;
		this.numRows = numRows;
		this.datasetMatrix = new String[numRows][numCols];
		for(int i=0; i < numCols; i++){
			this.lists[i] = new String[numRows][2];
		}
	 }
	 
	 public void load(String csvFile) {
		 
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ";";
			int cont = 0;
		 
			try {
		 
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
				    // use comma as separator
				    String[] fila  = line.split(cvsSplitBy);			
					this.datasetMatrix[cont] = fila;
					//this.numRows++;
					cont = cont + 1;
					//System.out.println( "Cont "+cont);
		 
				}
		 
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		  }
	
	 private String[][] insertInPlace(String[][] list, int index, String[] element){
		 String[][] result = new String[this.numRows][2];
		 int i = 0;
		 while(i < this.numRows){
			 if(i < index){
				 result[i] = list[i];
			 }else{
				 if(i == index){
					result[i] = element;				
					}else{
					 if(list[i-1][1] != null){
						 result[i] = list[i-1];
					 }else{
						 i = this.numRows;
					 }
				 }
			 }
			 i++;
		 }
		 return result;
	 }
	 
	 private void sortInsert(int col, String[] element){
		 // the element are sorted in an ascending way because we just want to minimize
		 String[][] list = new String[this.numRows][2]; 
		 list = this.lists[col];
		 String strValue = new String();
		 int intValue;
		 int insertValue = Integer.parseInt(element[1]);
		 int i = 0;
		 
		 while(i < this.numRows){ 	 	
			 strValue = list[i][1];
		//	 System.out.println("valor de la lista: "+strValue+" en la pos: "+i+",1");
			 if(strValue == null ){
				 list = this.insertInPlace(list, i, element);
			//	 System.out.println("1:  "+element[0]+","+element[1]+" inserted in pos "+i);
				 i = this.numRows;
			 }else {
				 intValue = Integer.parseInt(list[i][1]);
				 if(intValue > insertValue){
					 list = this.insertInPlace(list, i, element);
				//	 System.out.println("2: "+element[0]+","+element[1]+" inserted in pos "+i);
					 i = this.numRows;
				 }
					 
			 }
			 i++;
		 }
		 this.lists[col] = list;
	 }
	 
	 private void splitDataset(){
		for(int c = 1; c < this.numCols; c++){
			for(int r = 0; r < this.numRows; r++){
				String[] Ielement = new String[2];
				Ielement[0] = this.datasetMatrix[r][0]; // the index is in the first column
				Ielement[1] = this.datasetMatrix[r][c]; // the value in the column
				this.sortInsert(c-1, Ielement);
				}
		
		}
	
	 } 
	 
	private boolean dominates(String[] element1, String[] element2){
		
		/* Chechks pareto dominance of the elements. Minimization over intergers
		 * Returns:
		 * 0 if the elements are incomparable
		 * 1 if element 1 dominates element2
		 * */
		
		boolean allLessOrEqual = true;
		boolean oneStrictLess = false;
		
		for(int i = 1; i < this.numCols;i++){
			int diff = Integer.parseInt(element1[i]) - Integer.parseInt(element2[i]);
			if(diff < 0){
				oneStrictLess = true;
			}
			if(diff > 0){
				allLessOrEqual = false;
			}
		}
		if(allLessOrEqual & oneStrictLess){
			return true;	
		}else{
			return false;
		}
	}
	 
	private String[] getElementValues(String index){
		String[] values = new String[this.numCols];
		boolean stop = false;
		int i = 0;
		
		while(!stop){
			if(index.equals(this.datasetMatrix[i][0])){
				values = this.datasetMatrix[i];
				stop = true;
			}

			i++;
			if(i == this.numRows){
				stop = true;
			}
		}
		
		return values;
	}
	public void getSkyline(){
	/* Method implement the Basic Multi-Object Retrieval Skyline (BMORS) algorithm
	 * presented in the paper "Multi-objective Query Processing for Database Systems"
	 * By Wolf-Tilo Balke and Ulrich Güntzer on the Proceedings of the 30th VLDB Conference, 
	 * Toronto, Canada, 2004 
	 * -------------------------R-------------------------------------------------
	 * Copy of the pseudo code extracted from the paper (copy-paste)
	 * ---------------------------------------------------------------------------
	 * 0 	Given n lists ranking N database objects, each sorted  descending by score and 
	 * 		m monotonic functions f1,…,fm
	 * 1. 	Get an object o by sorted access from any list in a round robin fashion 
	 * 2. 	For new objects perform random accesses on the other lists and calculate the object’s 
	 * 		objective scores F(o)
	 * 3. 	Create a virtual database object p characterized by the minimum score values that have 
	 * 		occurred in each list, as its score values (i.e. si(p) is the current minimum score 
	 * 		in the i-th list) and calculate its objective scores F(p)  
	 * 4.	If some object w has already been seen for which holds 
			F(w) > F(p), i.e. its objective scores are better or equal, 
			but in at least one dimension strictly better than the virtual 
			object’s, discard all unseen objects, else return to step 1 
	 *  5.  Compare all seen objects pairwise and output all non dominated objects as the result set
	 *  	of all non dominated objects.
	 *  ----------------------------------------------------------------------------
	 *  */
		
		//Declare varibles:
		String[] virtualDBobject = new String[this.numCols];
		String[][] seenObjects = new String[this.numRows][this.numCols];
		
		
		// 0: Split and sort the original database in list.
		this.splitDataset();
		boolean stop = false;
		int i = 0;
		int seenCont = 0;
		// inicialice the virtual object with the first elements
		for(int j = 1; j < this.numCols; j++){
			virtualDBobject[j] = this.lists[j-1][0][1];
		}
		
		String[] testElement = new String[this.numCols];
		while (!stop){
			//read the i-th element of all lists
			testElement = this.getElementValues(this.lists[0][i][0]);
			
			System.out.println("-- COMPARE --");
			System.out.println(Arrays.toString(testElement));
			System.out.println(Arrays.toString(virtualDBobject));
			
			if(this.dominates(testElement, virtualDBobject)){
				seenObjects[seenCont] = testElement;
				seenCont++;
				stop = true;
				System.out.println("Dominates and stops");
			}else{
				seenObjects[seenCont] = testElement;
				seenCont++;
				//update virtual object
				for(int j = 1; j < this.numCols; j++){
					virtualDBobject[j] = this.lists[j-1][i][1];
				}
				//System.out.println(Arrays.toString(virtualDBobject));
			}
			
			i++;
			if(i == this.numRows){
				stop = true;
			}
		}// while
		
		// Compare all seen objects pairwise and output all non dominated objects as the result set
		// of all non dominated objects
		this.Skyline = new String[seenCont][this.numCols];
		String[] SLelement = new String[this.numCols];
		boolean isDominated = false;

		for(int t = 0; t < seenCont; t++){
			//System.out.println(Arrays.toString(seenObjects[t]));
			SLelement = seenObjects[t];
			isDominated = false;
			for(int j = 0; j < seenCont;j++){
				if(this.dominates(seenObjects[j], SLelement)){
					isDominated = true;
				}
			}
			if(!isDominated){
				this.Skyline[this.numSkyline] = SLelement;
				this.numSkyline++;
			}
		}
	}
	 
	public static void main(String[] args) {
		Bmors obj = new Bmors(10,5);
		obj.load("/home/pch/Dropbox/manejo-de-preferencias/proyecto1/bmors/datasets/dataset.csv");
		obj.getSkyline();
		System.out.println("-----SKYLINE----");
		for(int i = 0; i < obj.numSkyline; i++){
			System.out.println(Arrays.toString(obj.Skyline[i]));
		}
		
	}
		
	
}
