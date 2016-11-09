package example;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import core.Event;
import core.LocalRepository;
import core.RepoIterator;
import core.Student;


public class AttemptsPerExercises {

	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		LocalRepository.cloneRepo();	

		RepoIterator ite = new RepoIterator();
		
		//option for the iterator
		ite.setCollectCode(true);
		ite.setCollectError(true);
		ite.quiet();

		ite.addCommitType(Event.Executed);

		int activeBranch = 0;
		int nbreExec = 0;
		while(ite.hasNext() && activeBranch < 5){
			Student student = ite.next();
			if (student != null) {
				System.out.println(activeBranch+": "+student.getBranchName());
				for (String exoName : student.getTriedExoName()) {
					int failedAttempts = 0;
					int compilErrors = 0;
					int succeed = 0;
					for (Event evt: student.getEventsByExo(exoName)) {						
						if (evt.getResultCompil().equals("compilation error"))
							compilErrors++;
						else if (evt.getResultCompil().equals("failed"))
							failedAttempts++;
						else if (evt.getResultCompil().equals("success"))
							succeed++;
						else {
							System.out.println(exoName+": "+evt);
							System.exit(1);
						}
					}
					System.out.println(exoName+": "+ compilErrors+","+failedAttempts+","+succeed);
				}
				nbreExec += student.getEvents().size();
				activeBranch ++;
			}
		}

		System.out.println("\n branche active : " + activeBranch + " exec : " + nbreExec);

	}
}
