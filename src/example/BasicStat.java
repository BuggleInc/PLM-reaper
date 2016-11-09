package example;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import core.Event;
import core.LocalRepository;
import core.RepoIterator;
import core.Student;


public class BasicStat {

	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		LocalRepository.cloneRepo();	

		//LocalRepository.fetch();


		RepoIterator ite = new RepoIterator();
		
		//option for the iterator
		ite.setCollectCode(true);
		ite.setCollectError(true);

		ite.addCommitType(Event.Executed);

		/*
		Calendar dateMin = Calendar.getInstance();
		dateMin.set(Calendar.YEAR, 2015);dateMin.set(Calendar.MONTH, 8);dateMin.set(Calendar.DATE, 1);
		ite.setDateMin(dateMin);
		*/

		int activeBranch = 0;
		double scala= 0;
		double python = 0;
		double C = 0;
		double java = 0;
		double lightbot = 0;
		double blockly = 0;

		int failedAttempts = 0;
		int compilErrors = 0;
		int succeed = 0;

		while(ite.hasNext()){
			Student student = ite.next();
			if (student != null) {

				for (Event evt: student.getEvents()) {
					if (evt.getExoLang().equals("Scala")) 
						scala++;
					else if (evt.getExoLang().equals("Python"))
						python++;
					else if (evt.getExoLang().equals("Java"))
						java++;
					else if (evt.getExoLang().equals("C"))
						C++;
					else if (evt.getExoLang().equals("Blockly"))
						blockly++;
					else if (evt.getExoLang().equals("lightbot"))
						lightbot++;
					else {
						System.out.println("Lang = "+evt.getExoLang());
						System.exit(1);
					}
					
					if (evt.getResultCompil().equals("compilation error"))
						compilErrors++;
					else if (evt.getResultCompil().equals("failed"))
						failedAttempts++;
					else if (evt.getResultCompil().equals("success"))
						succeed++;
					else {
						System.out.println(evt);
						System.exit(1);
					}

				}
				activeBranch ++;
			}
		}
		double total = scala+python+java+C;
		System.out.println("\nScala: "+scala+" ("+((int)(100*scala/total))+"%); "+
				           "Python: "+python+" ("+((int)(100*python/total))+"%); "+
				           "Java: "+java+" ("+((int)(100*java/total))+"%); "+
				           "Blockly: "+blockly+" ("+((int)(100*blockly/total))+"%); "+
				           "lightbot: "+lightbot+"; "+
				           "C:"+C+" ("+((int)(100*C/total))+"%)");
		total = compilErrors+failedAttempts+succeed;
		System.out.println("compilation errors: "+ compilErrors+" ("+(int)(100*compilErrors/total)+"%), "
				+ "logical errors: "+failedAttempts+" ("+(int)(100*failedAttempts/total)+"%), "
				+ "succeeded: "+succeed+" ("+(int)(100*succeed/total)+"%)");

		
		System.out.println("\nExplored "+activeBranch+" active branchs.");

	}
}
