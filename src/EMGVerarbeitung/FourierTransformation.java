package EMGVerarbeitung;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import Diagramme.Diagramm;



public class FourierTransformation {

	/*

	 */
	public static void berechneFrequenzspektrum(EMGContainer e) {
		double[][] res=new double[8][];
		//double resultData[][]=new double[e.getLeange()][e.getAnzahlSensoren()];
		// double result Data
		for(int sensor=0;sensor<e.getAnzahlSensoren();sensor++) {
			double sdata[]=e.getSensordaten(sensor);
			double paddeddata[];
			
			if(!(((sdata.length)&(sdata.length-1))==0)) {
				int newlength=1;
				while(newlength<sdata.length) {
					newlength=newlength<<1;
				}
				paddeddata=new double[newlength];
				for(int i=0;i<sdata.length;i++) {
					paddeddata[i]=sdata[i];
				}	
			
			}else {
				paddeddata=sdata;
			}
			
			FastFourierTransformer ftr=new FastFourierTransformer(DftNormalization.STANDARD);
			Complex result[]=ftr.transform(paddeddata, TransformType.FORWARD);
			
			double real[]=new double[result.length];
			//printComplexArray(result);
			for(int i=0;i<result.length;i++) {
				real[i]=result[i].abs();
			}

			res[sensor]=real;
		}
		
		double sum[]=new double[res[0].length];
		
		for(int i=0;i<res.length;i++) {
			for(int j=0;j<res[i].length/2+1;j++) {
				sum[j]+=res[i][j];
			}
		}
		
		for(int i=0;i<sum.length;i++) {
			
			System.out.println(((i+1)*(200/(double)e.getLeange())+" "+sum[i]));
		
		}
		
		Diagramm.printArray(sum,e.getLeange());
		
	}
	
	/*
		High-pass filter (Hochpass)
	 */
	public static void hochpassFFT(EMGContainer e, int grenze) {
		double resultData[][]=new double[e.getLeange()][e.getAnzahlSensoren()];
		for(int sensor=0;sensor<e.getAnzahlSensoren();sensor++) {
			double sdata[]=e.getSensordaten(sensor);
			double paddeddata[];
			
			if(!(((sdata.length)&(sdata.length-1))==0)) {
				int newlength=1;
				while(newlength<sdata.length) {
					newlength=newlength<<1;
				}
				paddeddata=new double[newlength];
				for(int i=0;i<sdata.length;i++) {
					paddeddata[i]=sdata[i];
				}	
			
			}else {
				paddeddata=sdata;
			}
			
			FastFourierTransformer ftr=new FastFourierTransformer(DftNormalization.STANDARD);
			Complex result[]=ftr.transform(paddeddata, TransformType.FORWARD);
			
			
			//result[49]=result[49].multiply(0);
			//result[50]=result[50].multiply(0);
			
			for(int i=0;i<grenze;i++) {
				
				result[i]=result[i].multiply(0);
				
			}
			
			
			result=ftr.transform(result, TransformType.INVERSE);
			//printComplexArray(result);
			for(int i=0;i<resultData.length;i++) {
				//System.out.println(i+"  Real: "+result[i].getReal());
				resultData[i][sensor]=result[i].getReal();
			}
						
		}
		e.setDaten(resultData);
	}
	
	
	/*
		Low-pass filter (Hochpass)
	 */
	public static void tiefpassFFT(EMGContainer e, int grenze) {
		double resultData[][]=new double[e.getLeange()][e.getAnzahlSensoren()];
		for(int sensor=0;sensor<e.getAnzahlSensoren();sensor++) {
			double sdata[]=e.getSensordaten(sensor);
			double paddeddata[];
			
			if(!(((sdata.length)&(sdata.length-1))==0)) {
				int newlength=1;
				while(newlength<sdata.length) {
					newlength=newlength<<1;
				}
				paddeddata=new double[newlength];
				for(int i=0;i<sdata.length;i++) {
					paddeddata[i]=sdata[i];
				}	
			
			}else {
				paddeddata=sdata;
			}
			
			FastFourierTransformer ftr=new FastFourierTransformer(DftNormalization.STANDARD);
			Complex result[]=ftr.transform(paddeddata, TransformType.FORWARD);
			
			
			//result[49]=result[49].multiply(0);
			//result[50]=result[50].multiply(0);
			
			double freqProIndex=200/(double)e.getLeange();
			double grenzeHz=(double)grenze/freqProIndex;
			//System.out.println("GRHZ INDEX:"+grenzeHz);
			for(int i=(int)Math.round(grenzeHz);i<result.length-grenze;i++) {
				result[i]=result[i].multiply(0);
			}
			
			
			result=ftr.transform(result, TransformType.INVERSE);
			//printComplexArray(result);
			for(int i=0;i<resultData.length;i++) {
				//System.out.println(i+"  Real: "+result[i].getReal());
				resultData[i][sensor]=result[i].getReal();
			}
						
		}
		e.setDaten(resultData);
	}
	
	
	
}
