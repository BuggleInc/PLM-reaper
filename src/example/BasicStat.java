package example;

import java.io.IOException;
import java.util.Calendar;

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
import core.LocalRepository;
import core.RepoIterator;
import core.Student;
import plm.core.lang.ProgrammingLanguage;
import plm.core.model.Game;
import plm.core.model.lesson.Exercise;
import plm.core.model.lesson.Exercise.WorldKind;
import plm.universe.World;


public class BasicStat {

	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		LocalRepository.cloneRepo();	
		
		LocalRepository.fetch();

		
		RepoIterator ite = new RepoIterator();
		//option for the iterator
		
		//ite.setCollectCode(true);
		//ite.setCollectError(true);
		
		ite.addCommitType(Event.Executed);
		
		Calendar dateMin = Calendar.getInstance();
		dateMin.set(Calendar.YEAR, 2015);dateMin.set(Calendar.MONTH, 8);dateMin.set(Calendar.DATE, 1);
		ite.setDateMin(dateMin);

		int count = 0;
		 int nbreExec = 0;
		 while(ite.hasNext()){
			 Student student = ite.next();
			 if(student != null){
				 nbreExec += student.getEvents().size();
				 count ++;
			 }
		 }

		 System.out.println("\n branche active : " + count + " exec : " + nbreExec);

	}
}
