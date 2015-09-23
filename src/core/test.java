package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JFrame;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;	
import org.eclipse.jgit.lib.Ref;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion.User;

import core.Event;
import core.RepoIterator;
import core.Student;
import plm.core.lang.ProgrammingLanguage;
import plm.core.model.Game;
import plm.core.model.lesson.Exercise;
import plm.core.model.lesson.Exercise.WorldKind;
import plm.universe.World;




public class test {


	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		//LocalRepository.cloneRepo();	
		//LocalRepository.fetch();

		RepoIterator ite = new RepoIterator();
		//ite.setCollectCode(true);
		//ite.setCollectError(true);
		ite.addCommitType(Event.Executed);
		Calendar dateMin = Calendar.getInstance();
		dateMin.set(Calendar.YEAR, 2015);dateMin.set(Calendar.MONTH, 8);dateMin.set(Calendar.DATE, 1);
		//dateMin.set(Calendar.HOUR_OF_DAY, 14); dateMin.set(Calendar.MINUTE, 00); dateMin.set(Calendar.SECOND, 00);
		//dateMin.add(Calendar.MINUTE, -5);
		//ite.setDateMin(dateMin);
		//ite.setDateMax(dateMin);
		/*Calendar dateMax = (Calendar)  dateMin.clone();
		dateMax.add(Calendar.MINUTE, 5);
		ite.setDateMax(dateMax);
		System.out.println(dateMin.getTime().toString() + "      " + dateMax.getTime().toString());
		 */
		//System.out.println(ite.getDateMin().getTime().toString());
		 int count = 0;
		 int countIdle = 0;
		 int exec = 0;
		 int nbreExec = 0;
		 int error = 0, error2 = 0;
		 ArrayList<Instant> result = new ArrayList<Instant>();
		 while(ite.hasNext()){
			 Student student = ite.next();
			 if(student != null){
				 nbreExec += student.getEvents().size();
				 //System.out.println(student.getBranchName());
				 count ++;
			 }
			 /*for(Event e : student.getEvents()){
					//				exec++;
					//			result.add(e.getCommitTime());

					if(e.getCommitType() != null && e.getCommitType().equals(Event.Executed) && 
							(e.getResultCompil().equals(Event.CompilError) || e.getResultCompil().equals((Event.Failed)))){
						if(e.getError()!=null)
							error ++;
						else
							error2++;
					}
				}*/
			 /*
				if(!ok){
					//System.out.println(student.getBranchName());
					countIdle++;
				}*/


		 }
		 //	Collections.sort(result);

		 System.out.println("branche active : " + count + " exec : " + nbreExec + " fichier error : " + error + " fichier error manquant : " + error2);
		 /*
		//log nbre branche active
		String fileAdr = System.getProperty("user.home") + "/Documents/testBrancheActive";
		try
		{	FileWriter fw = new FileWriter(fileAdr, true);
		BufferedWriter output = new BufferedWriter(fw);
		output.write("Date début : " + dateMin.getTime().toString() + " ; branche active :  " + count + "; \n");
		output.flush();
		output.close();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}


		/*
		String fileAdr = System.getProperty("user.home") + "/Documents/test1";
		System.out.println(fileAdr);
		try
		{	FileWriter fw = new FileWriter(fileAdr, false);
		BufferedWriter output = new BufferedWriter(fw);
		//for(Instant i : result)
		//output.write(i.toString() + "\n");

		Instant currentDate = result.get(0);
		int commitNbre = 0;
		int incr = 10;
		for(Instant i : result){
			//System.out.println(currentDate.toString() + "      " + i.toString());
			if(currentDate.until(i, ChronoUnit.SECONDS)<=incr){
				commitNbre++;
			}
			else{
				output.write(currentDate.toString() + ";" + commitNbre + "\n");
				commitNbre = 0;
				currentDate = currentDate.plusSeconds(incr);
			}
		}
		output.flush();
		output.close();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}


		  */






		 //		System.out.println("\n count : " + count + "    branche sans exec : " + countIdle);


		 /*
		ArrayList<core.Exercise> listExo = new ArrayList<>();
		while(ite.hasNext()){
			if(listExo.size() > 10)
				break;
			Student temp = ite.next();
			if(temp != null){
				ArrayList<String> listExoName = new ArrayList<String>();
				for(Event e : temp.getEvents()){
					if(!listExoName.contains(e.getExoName())){
						listExoName.add(e.getExoName());
						core.Exercise exo = new core.Exercise(temp, e.getExoName());
						if(exo.sizeWithouIlde() > 4 && exo.firstCommitSuccessful() > 1 && exo.getSize() < 20)
							listExo.add(exo);
					}
				}
			}
		}

		DataSet trainingSet = new DataSet(2, 1);

		for(core.Exercise exo : listExo){
			System.out.println("frist commit succcess " + exo.firstCommitSuccessful());
			if(exo.getEvents().size()>1 && exo.firstCommitSuccessful() != -1){
				for(int i = 0 ; i < exo.firstCommitSuccessful(); i ++){
					double EQ =	EQmean(exo.getEvents().subList(0, i));
					System.out.println("eq " +EQ);
					double time = 0;
					if(i>0)
						time = (double) exo.getEvent(i).getCommitTime().until(exo.getEvent(i-1).getCommitTime(), ChronoUnit.MINUTES);
					time = Math.min(time/60, 1);
					System.out.println(time);
					double succesful = 0;
					if(exo.firstCommitSuccessful() - i > 3)
						succesful = 1;
					trainingSet.addRow(new DataSetRow(new double[]{EQ, time}, new double[]{succesful}));
				}	
			}
		}
		  *//*
		// create multi layer perceptron
		MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, 2, 3, 1);
		// learn the training set
		System.out.println("start learning, set size : " + trainingSet.size());
		myMlPerceptron.learn(trainingSet);
		System.out.println("hey");

		for(core.Exercise exo : listExo){
			if(exo.getEvents().size()>1 && exo.firstCommitSuccessful() != -1){
				for(int i = 0 ; i < exo.firstCommitSuccessful(); i ++){
					double EQ =	EQmean(exo.getEvents().subList(0, i));
					double time = 0;
					if(i>0)
						time = (double) exo.getEvent(i-1).getCommitTime().until(exo.getEvent(i).getCommitTime(), ChronoUnit.HOURS);
					double succesful = 0;
					if(exo.firstCommitSuccessful() - i > 3)
						succesful = 1;
					myMlPerceptron.setInput(new double[] {EQ,time});
					myMlPerceptron.calculate();
					System.out.println("Result : " + myMlPerceptron.getOutput()[0] + "expected + " + succesful);
				}	
			}
		}
		   */


	}

	private static double EQmean(List<Event> events){

		String prevResult = null, prevError = null;
		int nbreEQ = 0, currentEQ;
		double sumEQ = 0;

		for(int i = 0 ; i < events.size() ; i++){
			Event e = events.get(i);
			if(e.getCommitType().equals(Event.Executed)){
				if(prevResult == null){
					prevResult = e.getResultCompil();
					prevError = e.getError();
					continue;
				}

				currentEQ = 0;
				if(prevResult.equals(Event.CompilError) && e.getResultCompil().equals(Event.CompilError)){
					currentEQ += 8;
					if(prevError.equals(e.getError()))
						currentEQ += 3;
				}
				sumEQ += currentEQ/11;
				nbreEQ ++;
				prevResult = e.getResultCompil();
				prevError = e.getError();
			}
		}

		if(nbreEQ > 0)
			return sumEQ/nbreEQ;
		else
			return 0;
	}





}
