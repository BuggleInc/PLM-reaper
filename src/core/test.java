package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JFrame;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;	
import org.eclipse.jgit.lib.Ref;

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

		
		//		LocalRepository.clone();	
		//		LocalRepository.fetch();

		RepoIterator ite = new RepoIterator();
		//ite.setCollectCode(true);
		//ite.setCollectError(true);
		ite.addCommitType(Event.Executed);
		ite.setCollectError(true);
		//ite.setCollectCode(true);
		//ite.addValidBranch("refs/remotes/origin/PLM195c1d47108db69f8abac9e56b1fa65a7000171a");
		ite.addValidBranch("refs/remotes/origin/PLMb9c5556003558a5aa5b5a48239a456e8c0171f17");
		//ArrayList<Student> students = new ArrayList<Student>();
		while(ite.hasNext()){
			Student temp = ite.next();
			if(temp != null)
				for(Event commit : temp.getEvents()){
					System.out.println("id : " + commit.getIdCommit() + "exoname : " + commit.getExoName() + "exolang : "+ commit.getExoLang()+ "\n" + commit.getError());
					
				}
			//if(temp != null)
			//students.add(temp);
		}
	}

}