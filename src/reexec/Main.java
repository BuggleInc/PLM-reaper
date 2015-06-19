package reexec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import core.LocalRepository;
import core.RepoIterator;
import core.Student;

public class Main {
	public static void main(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		if(args.length == 1) {
			if(args[0].equals("-c") || args[0].equals("-clone")) {
				Path path = Paths.get("repo");
				if(!Files.exists(path)) {
					LocalRepository.cloneRepo();
				} else {
					System.out.println("If you want to update your repo directory, please use the option -f or -fetch instead of -c or -clone");
					System.exit(1);
				}
			}
			if(args[0].equals("-f") || args[0].equals("-fetch")) {
				LocalRepository.fetch();
			} /*else if(args.length > 1) {
				System.out.println(args[0]+args[1]);
				System.out.println("Please specify only a maximum of one argument to run this program");
				System.exit(1);
			}*/
		}
		RepoIterator ite = new RepoIterator();
		/*ite.addValidBranch("refs/remotes/origin/PLM135133026701cb4c442ee940a8cd465c3997e148");
		ite.addValidBranch("refs/remotes/origin/PLM0266985b7338b528ca1a1cd97ab0d032ee5af2f4");
		ite.addValidBranch("refs/remotes/origin/PLM065351a5aa002f465d5dd23648dcdfa1ec46b000");
		ite.addValidBranch("refs/remotes/origin/PLM02d0f5013b796ce1f9245ebe995451edea2447d2");*/
		ite.setCollectCode(true);
		ite.setCollectError(true);
		int nbBranch = 0;
		int nbFails = 0;
		while(ite.hasNext()) {
			Student student = ite.next();
			if(student != null) {
				BrowseAndExecute bae = new BrowseAndExecute();
				nbFails = bae.getCode(student, nbBranch, nbFails);
				nbBranch++;
			}
		}
		System.out.println("========== Execution finished!!! ==========");
	}
}
