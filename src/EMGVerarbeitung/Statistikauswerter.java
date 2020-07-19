package EMGVerarbeitung;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/*
	Evaluate statistics
 */
public class Statistikauswerter {
	
	public static Statistiken fasseZusammen(ArrayList<Statistiken> stats) {
		double confmatrix[][]=new double[stats.get(0).getKonfusionsMatrix().length][stats.get(0).getKonfusionsMatrix()[0].length];
		
		for(Statistiken s: stats) {
			double tmp[][]=s.getKonfusionsMatrix();
			for(int i=0;i<tmp.length;i++) {
				for(int x=0;x<tmp[0].length;x++) {
					confmatrix[i][x]+=tmp[i][x];
				}
			}	
		}
		
		Statistiken res=new Statistiken(confmatrix);
		res.setWeitereInfos(stats.get(0).getWeitereInfos());
		return res;
	}
	
	
	
	
	public static String statsToString(ArrayList<Statistiken> stats) {
		PrintStream oldout = System.out;
		ByteArrayOutputStream prs=new ByteArrayOutputStream();
		PrintStream newStream = new PrintStream(prs);
		System.setOut(newStream);
		printStats(stats);
		System.out.flush();
		System.setOut(oldout);
		return prs.toString();
	}
	
	
	
	
	public static void printStats(ArrayList<Statistiken> stats) {
		Statistiken result=new Statistiken();
		String infoString=stats.get(0).getWeitereInfos();
		double sum[][]=new double[stats.get(0).getKonfusionsMatrix().length][stats.get(0).getKonfusionsMatrix().length];
		for(Statistiken s: stats) {
			sum=addMatrix(sum,s.getKonfusionsMatrix());
			//if(!s.getAdditionalInfos().isEmpty())infoString+=s.getAdditionalInfos()+"\n";
		}
		result.setKonfusionsMatrix(sum);
		result.setWeitereInfos(infoString);
		printStats(result);
	}
	
	
	
	private static double[][] addMatrix(double[][] sum, double[][] s) {
		for(int i=0;i<sum.length;i++) {
			for(int j=0;j<sum.length;j++) {
				sum[i][j]+=s[i][j];
			}
		}
		return sum;
	}



	public static void printStats(Statistiken stats) {
		double mat[][] = stats.getKonfusionsMatrix();
		String infos= stats.getWeitereInfos();
		
		
		
		printInfos(infos);
		printGeneralStats(mat);
		printStatsMatrix(mat);
		
		printConfusionMatrix(mat);
		
		
		
	}



	private static void printStatsMatrix(double[][] mat) {
		double statsmatrix[][]=new double[mat.length+1][4];
		for(int x=0;x<mat.length;x++) {
			int tp=(int)mat[x][x];
			int fp=0,fn=0,tn=0;
			
			//Calc fp
			for(int i=0;i<mat.length;i++) {
				if(i!=x) {
					fp+=(int)mat[i][x];
				}
			}
			
			//Calc fn
			for(int i=0;i<mat.length;i++) {
				if(i!=x) {
					fn+=mat[x][i];
				}
			}
			
			//Calc tn
			for(int i=0;i<mat.length;i++) {
				for(int j=0;j<mat.length;j++) {
					if(i!=x&&j!=x) {
						tn+=mat[i][j];
					}
				}
			}
			
			//1. TP Rate
			statsmatrix[x][0]=(double)tp/(tp+fn);
			
			//2. FP Rate
			statsmatrix[x][1]=(double)fp/(fp+tn);
			
			//3. Presiciosn Rate
			statsmatrix[x][2]=(double)tp/(tp+fp);
			
			//4. Klasse
			statsmatrix[x][3]=x;
			
		}
		
		double sum0=0;
		double sum1=0;
		double sum2=0;

		for(int i=0;i<statsmatrix.length-1;i++) {
			sum0+=statsmatrix[i][0];
			sum1+=statsmatrix[i][1];
			sum2+=statsmatrix[i][2];
		}
		sum0/=(double)mat.length;
		sum1/=(double)mat.length;
		sum2/=(double)mat.length;
		
		
		System.out.println("=== Detailed Information per Class  ===");
		System.out.printf("%10s  %10s  %10s  %10s", "TP-Rate","FP-Rate","Precision","Class");
		System.out.println("");
		for(int i=0;i<statsmatrix.length-1;i++) {
			System.out.printf("%10.3f  %10.3f  %10.3f  %10d\n",statsmatrix[i][0],statsmatrix[i][1],statsmatrix[i][2],(int)statsmatrix[i][3]);
		}
		System.out.printf("AVG: %5.3f  %10.3f  %10.3f\n",sum0,sum1,sum2);
		
		
	}



	private static void printGeneralStats(double[][] mat) {
		
		int sumInstances=0;
		int correctClassified=0;
		int falseClassified=0;
		double correctp=0;
		double falsep=0;
		for(int x=0;x<mat.length;x++) {
			for(int y=0;y<mat.length;y++) {
				sumInstances+=mat[x][y];
				if(x==y)correctClassified+=mat[x][y];
				else falseClassified+=mat[x][y];
			}
		}
		assert sumInstances==correctClassified+falseClassified;
		correctp=(correctClassified/(double)sumInstances)*(double)100;
		falsep=(falseClassified/(double)sumInstances)*(double)100;
		
		System.out.println("=== Summary ===");
		System.out.printf("%40s  %10d    %.3f%%  \n","Correctly Classified Instances", correctClassified, correctp);
		System.out.printf("%40s  %10d    %.3f%%  \n","Incorrectly Classified Instances", falseClassified, falsep);
		System.out.printf("%40s  %10d  \n","Total Number of Instances", sumInstances);
		System.out.println("");

	}



	private static void printInfos(String infos) {
		if(infos==null)return;
		if(!infos.isEmpty()) {
			System.out.println("=== Additional Infos ===");
			System.out.println(infos);
		}
		System.out.println("");
	}



	private static void printConfusionMatrix(double[][] mat) {
		System.out.println("=== Confusion Matrix ===");
		System.out.println("Klassifiziert als:");
		for(int j=0;j<mat.length;j++) {
			System.out.printf(" %4d ", j);
		}
		
		System.out.println("");
		System.out.println("-------------------------------------------------------------Klasse:");
		for(int i=0;i<mat.length;i++) {
			for(int j=0;j<mat.length;j++) {
				System.out.printf(" %4d ", (int)mat[i][j]);
			}
			System.out.println("  | "+i);
		}
		System.out.println("");
	}
	
	
/*
 * TP Rate=   TP/TP+FN
 * 	
 */
	
	
	
	
}



/*


** Decision Tress Evaluation with Datasets **

Correctly Classified Instances         669               83.625  %
Incorrectly Classified Instances       131               16.375  %
Kappa statistic                          0.8181
Mean absolute error                      0.1152
Root mean squared error                  0.2112
Relative absolute error                 64.0157 %
Root relative squared error             70.4039 %
Total Number of Instances              800     

 the expression for the input data as per alogorithm is RandomForest

Bagging with 1000 iterations and base learner

weka.classifiers.trees.RandomTree -K 0 -M 1.0 -V 0.001 -S 1 -do-not-check-capabilities
=== Confusion Matrix ===

  a  b  c  d  e  f  g  h  i  j   <-- classified as
 77  1  1  0  0  0  0  0  1  0 |  a = 0
  1 72  0  0  6  0  0  1  0  0 |  b = 1
  3  0 69  2  0  0  2  3  0  1 |  c = 2
  2  0  8 55  0  0  1  6  1  7 |  d = 3
  0  3  4  6 64  0  1  0  1  1 |  e = 4
  0  0  2  3  0 62  1  8  2  2 |  f = 5
  0  1  2  0  6  0 70  0  1  0 |  g = 6
  0  0  4  2  0  0  0 71  0  3 |  h = 7
  3  0  2  0  0  0  2  0 72  1 |  i = 8
  0  0  4  6  0  6  1  0  6 57 |  j = 9

=== Detailed Accuracy By Class ===

                 TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0,963    0,013    0,895      0,963    0,928      0,920    0,996     0,984     0
                 0,900    0,007    0,935      0,900    0,917      0,908    0,985     0,965     1
                 0,863    0,038    0,719      0,863    0,784      0,762    0,976     0,887     2
                 0,688    0,026    0,743      0,688    0,714      0,685    0,944     0,773     3
                 0,800    0,017    0,842      0,800    0,821      0,801    0,935     0,786     4
                 0,775    0,008    0,912      0,775    0,838      0,825    0,991     0,937     5
                 0,875    0,011    0,897      0,875    0,886      0,874    0,990     0,929     6
                 0,888    0,025    0,798      0,888    0,840      0,823    0,965     0,886     7
                 0,900    0,017    0,857      0,900    0,878      0,864    0,994     0,937     8
                 0,713    0,021    0,792      0,713    0,750      0,725    0,953     0,819     9
Weighted Avg.    0,836    0,018    0,839      0,836    0,836      0,819    0,973     0,890 

*/