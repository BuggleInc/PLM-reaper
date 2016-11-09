package example;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import core.Event;
import core.LocalRepository;
import core.RepoIterator;
import core.Student;


public class CumulativeLinesExos {

	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		LocalRepository.cloneRepo();	

		//LocalRepository.fetch();


		RepoIterator ite = new RepoIterator();
		
		//option for the iterator
		ite.setCollectCode(true);
		ite.setCollectError(true);

		ite.addCommitType(Event.Executed);

		int[] lines           = new int[4001];
		int[] cumulativeLines = new int[4001];
		int[] passing         = new int[201];
		int[] attempting      = new int[201];

		
		int activeBranch = 0;
		while(ite.hasNext()){// && activeBranch < 5){
			Student student = ite.next();
			if (student != null) {
				int studentTotalLines = 0;
				int studentTotalPassed = 0;
				int studentTotalAttempted = 0;
				
				for (String exoName : student.getTriedExoName()) {
					int linesOnThisExo = Integer.MAX_VALUE;
					studentTotalAttempted++;
					
					boolean passed = false;
					for (Event evt: student.getEventsByExo(exoName)) {						
						if (evt.getResultCompil().equals("success")) {
							linesOnThisExo = Math.min(linesOnThisExo, 1+evt.getCode().split("\n").length);
							passed = true;
						}
					}
					if (linesOnThisExo < Integer.MAX_VALUE)
						studentTotalLines += linesOnThisExo;
					if (passed)
						studentTotalPassed++;
				}
				lines[studentTotalLines] += 1;
				
				for (int i=0;i<lines.length;i++)
					if (studentTotalLines >= i)
						cumulativeLines[i]++;
				
				for (int i=0;i<passing.length;i++) {
					if (studentTotalPassed >=i)
						passing[i] ++;
					if (studentTotalAttempted >=i)
						attempting[i] ++;
				}


				activeBranch ++;
			}
		}
		
		System.out.println("# lines count , cumulative count of students with more passing lines");
		for (int i=cumulativeLines.length-1;i>=0;i--)
//			if ((i%25==0 && i!=0) || i==500 || i==250 || i==50 || i==10 || i==1)
				System.out.format("|  %4d|  %4d|\n",i, cumulativeLines[i]);

		System.out.println("# exo count , cumulative count of attempting students, cumulative count of passing students");
		for (int i=passing.length-1;i>=0;i--) 
//			if ((i%5 == 0 && i!=0) || i==190 || i==10 || i==5 || i==1)
				System.out.format("| %4d |  %4d|  %4d|\n",i, attempting[i], passing[i]);

		System.out.println("\nExplored "+activeBranch+" active branchs.");
	}
}
