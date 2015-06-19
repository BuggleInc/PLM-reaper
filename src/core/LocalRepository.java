package core;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;


public class LocalRepository {
	private static final String path = "repo/";
	private static final String REMOTE_URL = "https://github.com/mquinson/PLM-data.git";
	private static Repository repo = null;
	
	public static void cloneRepo() throws InvalidRemoteException, TransportException, GitAPIException{		
		File repo = new File(path);
		if(!repo.exists())
			repo.mkdir();
		
		System.err.println("Cloning from " + REMOTE_URL + " to " + path);
		Git.cloneRepository().setURI(REMOTE_URL).setDirectory(repo).call();
	}

	public static void fetch(){
		try {
			Repository repo = FileRepositoryBuilder.create(new File(path + "/.git"));
			System.err.println("Fetch all remote events");
			new Git(repo).fetch().setCheckFetchedObjects(true).call();
			repo.close();
		} catch (IOException | GitAPIException e1) {
			// TODO Bloc catch généré automatiquement
			e1.printStackTrace();
		}
	}
	
	public static Collection<RevCommit> getCommits(String branchName) throws IOException{
		Repository repo =  FileRepositoryBuilder.create(new File(path + "/.git"));
		
		Ref ref = repo.getRef(branchName);
		RevWalk walk = new RevWalk(repo);
		RevCommit startCommit = walk.parseCommit(ref.getObjectId());
		walk.markStart(startCommit);
		ArrayList<RevCommit> commits = new ArrayList<>();

		for (RevCommit rev : walk) 
			commits.add(rev);

		walk.dispose();
		repo.close();
		return commits;
	}
	
	public static Collection<Ref> getBranchs(){
		try {
			Git git = new Git(FileRepositoryBuilder.create(new File(path + "/.git")));
			return git.branchList().setListMode(ListMode.REMOTE).call();
		} catch (IOException | GitAPIException e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
			return null;
		}
	}

	public static String getFileContent(String pathFile, RevCommit commit){
		try {			
			if(repo == null)
				repo = FileRepositoryBuilder.create(new File(path + "/.git"));
				
			// Using commit's tree, find the path
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repo);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);
			treeWalk.setFilter(PathFilter.create(pathFile));
			if (!treeWalk.next())
				return null;
				
			// Get a stream loader onto that object
			ObjectId objectId = treeWalk.getObjectId(0);
			ObjectLoader loader = repo.open(objectId);
			InputStream is = loader.openStream();		

			// And convert the stream into a String object -- damn java
			final char[] buffer = new char[2048];
			final StringBuilder out = new StringBuilder();

			final Reader in = new InputStreamReader(is, "UTF-8");
			try {
				for (;;) {
					int rsz = in.read(buffer, 0, buffer.length);
					if (rsz < 0)
						break;
					out.append(buffer, 0, rsz);
				}
			}
			finally {
				in.close();
			}
			return out.toString();
		} 
		catch (IOException e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
			return null;
		}




	}

}
