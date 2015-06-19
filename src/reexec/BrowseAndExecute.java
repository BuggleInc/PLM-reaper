package reexec;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JFrame;

import plm.core.lang.ProgrammingLanguage;
import plm.core.model.Game;
import plm.core.model.lesson.Exercise;
import plm.core.model.lesson.Exercise.WorldKind;
import plm.universe.World;
import core.Event;
import core.Student;

/**
 * Class that makes a process in the "repo" directory in order to re-execute all the code that failed in every commit
 * @author Alexandre Carpentier
 * @version 1.0.0
 */
public class BrowseAndExecute {

	private ArrayList<String> lessonsName = new ArrayList<String>(Arrays.asList( // WARNING, keep ChooseLessonDialog.lessons synchronized
			"lessons.welcome", "lessons.turmites", "lessons.maze", "lessons.turtleart",
			"lessons.sort.basic", "lessons.sort.dutchflag", "lessons.sort.baseball", "lessons.sort.pancake", 
			"lessons.recursion.cons", "lessons.recursion.lego", "lessons.recursion.hanoi", 
			"lessons.lightbot", "lessons.bat.string1", "lessons.lander"
			));
	
	private ArrayList<String> listCodes = new ArrayList<String>();
	
	
	/**
	 * Empty constructor
	 */
	public BrowseAndExecute() {}
	
	
	/**
	 * Method that re-execute the code
	 * @param commit					Commit that is analyzed
	 * @param branchName				Name of the branch that the commit is extracted form
	 * @param code						Code that is re-executed
	 * @param lang						Language of the code that is re-executed
	 * @param lessonID					Name of the lesson of the code
	 * @param exoID						Name of the exercise of the code
	 * @param infiniteLoop				Check if the code has or not an infinite loop
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	public void execCode(Event commit, String branchName, String code, ProgrammingLanguage lang, String lessonID, String exoID, boolean infiniteLoop) throws FileNotFoundException, InterruptedException {
		if(!infiniteLoop) {
			MockLogHandler log = new MockLogHandler();
			Game g = new Game(log, new JFrame().getLocale());
			g.getProgressSpyListeners().clear();
			g.removeSessionKit();
			g.setBatchExecution();
	
			g.setProgramingLanguage(lang);
			g.switchLesson(lessonID, true);
//			if(exoID.equals("welcome.lessons.welcome.loopwhile.WhileMoria")) {
			g.getCurrentLesson().setCurrentExercise(exoID);
			g.setLocale(new Locale("en"));
	
			Exercise exo = (Exercise) g.getCurrentLesson().getCurrentExercise();
			exo.getSourceFile(lang, 0).setBody(code);
			g.startExerciseExecution();
			
			//System.out.println("Execution started!");
	
			Vector<World> currentWorlds = exo.getWorlds(WorldKind.CURRENT);
			BufferedWriter bw = null;
			for(int i = 0 ; i < currentWorlds.size() ; i++) {
				World currentWorld = currentWorlds.get(i);
				World answerWorld = exo.getAnswerOfWorld(i);
				//System.out.println("Checking winner...");
				if (!currentWorld.winning(answerWorld)) {
					//System.out.println("Checking for differences...");
					Thread.sleep(15);
					String error = currentWorld.diffTo(answerWorld);
					//System.out.println("Diff done!");
					//System.out.println(error);
					try {
						File f = new File("logsDir/"+exoID);
						f.mkdirs();
						File dest = new File("logsDir/"+exoID+"/"+branchName+"_"+commit.getIdCommit().split(" ")[1] + ".log");
						dest.createNewFile();
						bw = new BufferedWriter(new FileWriter("logsDir/"+exoID+"/"+branchName+"_"+commit.getIdCommit().split(" ")[1] + ".log", true));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					try {
						if(error!=null) {
							bw.write(error);
						} else {
							bw.write("Possibly a Python error, that has to be checked out...");
						}
						bw.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			}
		} else {
			BufferedWriter bw = null;
			String error = "Infinite loop potentially detected";
			try {
				File f = new File("logsDir/"+exoID);
				f.mkdirs();
				File dest = new File("logsDir/"+exoID+"/"+branchName+"_"+commit.getIdCommit().split(" ")[1] + ".log");
				dest.createNewFile();
				bw = new BufferedWriter(new FileWriter("logsDir/"+exoID+"/"+branchName+"_"+commit.getIdCommit().split(" ")[1] + ".log", true));
				bw.write(error);
				bw.flush();
				bw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Method that checks if a branch and a commit is already in the cache
	 * @param exoName		Name of the exercise that is tested in the cache
	 * @param branchName	Name of the branch that is tested in the cache
	 * @param commitName	Name of the commit that is tested in the cache
	 * @return true if the branch and commit is already in the cache's exercise folder and false if the file or the exercise's folder do not exist
	 */
	public boolean rescanCache(String exoName, String branchName, String commitName) {
		File dir = new File("logsDir/"+exoName);
		File[] files = dir.listFiles();
		if(files==null)
			return false;
		for(int i = 0;i<files.length;i++) {
			if(files[i].getName().equals(branchName+"_"+commitName+".log")) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Method that verifies if a code has already been analyzed in the previous iterations in other commits
	 * @param code	Code that is checked in the ArrayList listCodes
	 * @return true if the code has already been analyzed and false otherwise
	 */
	public boolean isCodeAlreadyWritten(String code) {
		String newCode = code.replaceAll("[\r\n]+", "");
		if(this.listCodes.contains(newCode)) {
			return true;
		} else {
			this.listCodes.add(newCode);
			return false;
		}
	}
	
	
	/**
	 * Method that naively verifies if there is an infinite loop in a code
	 * @param error	Error that is returned to the student and that is in the .error file in commits
	 * @return true if the word "infini" is contained in the error message in the .error file and false otherwise
	 */
	public boolean isStopped(String error) {
		if(error != null) {
			if(error.contains("infini")) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Method that is firstly called to obtain and verify every data in the commits and that calls the execution method in certain cases
	 * @param student	Get the Student object in order to obtain all the commits attached
	 * @param nbBranch	Number of branches that has to be checked
	 * @param nbFails	Number of commits that have the type "Failed"
	 * @return the number of Failed-type commits in a branch
	 */
	public int getCode(Student student, int nbBranch, int nbFails) {
		System.setErr(new PrintStream(new OutputStream() {
		    public void write(int b) {
		    }
		}));
		int tempFails = nbFails;
		this.listCodes.clear();
		String[] tempBranch = student.getBranchName().split("/");
		String branchName = tempBranch[tempBranch.length-1];
		/*if(nbBranch > 151) { //TODO: Retirer ces huit lignes pour exécuter le code sur toutes les branches !
			System.out.println("");
			System.out.println("");
			System.out.println("============================================================");
			System.out.println(tempFails+" files created at the end of execution. ");
			System.out.println("============================================================");
			System.exit(0);
		}*/
		System.out.println(" ");
		System.out.println("------------------------------------------------------------");
		System.out.println("Branch No."+nbBranch+" ["+branchName+"]");
		ArrayList<Event> commits = (ArrayList<Event>) student.getEvents();
		int nbCommit = commits.size();
		Collections.sort(commits);
		System.out.println("The branch No."+nbBranch+" contains "+nbCommit+" commits.");
		int commitNumber = 0;
		int space = 0;
		for(Event commit : commits) {
			commitNumber++;
			if (commitNumber % 50 == 0)
				System.out.println(". "+commitNumber+"/"+nbCommit);
			else {
				System.out.print(".");
				space = 50 - commitNumber % 50;
			}
			if(commitNumber == nbCommit) {
				for(int i = 0 ; i < space ; i++) {
					System.out.print(" ");
				}
				System.out.print(" "+commitNumber+"/"+nbCommit);
				System.out.println("");
			}
			if(commit.getCommitType().equals(Event.Executed) && commit.getResultCompil().equals(Event.Failed)) {
				//System.out.println("Erreur : "+commit.getError());
				ProgrammingLanguage language = Game.JAVA;
				switch(commit.getExoLang().toLowerCase()) {
				case "scala":
					language = Game.SCALA;
					break;
				case "python":
					language = Game.PYTHON;
					break;
				default:
					continue;
				}
				String lessonID = "lessons."+commit.getExoName().split(".lessons.")[0];
				if(lessonID.equalsIgnoreCase("lessons.recursion")) {
					lessonID = lessonID+".lego";
				}
				String exoID = commit.getExoName();
				String commitID = commit.getIdCommit().split(" ")[1];
				//System.out.println("Lesson: "+lessonID+" / Exo: "+exoID);
				if(lessonID.equals("lessons.recursion")) {
				}
				//String error = commit.getError();
				if(lessonsName.contains(lessonID)) {
					tempFails++;
					String code = commit.getCode();
					/*if(code.toLowerCase().contains("system.exit") || code.toLowerCase().contains("sys.exit")) {
						continue;
					}*/
					if(isStopped(commit.getError())) {
						try {
							//System.out.println("Stopped !!!");
							execCode(commit, branchName, code, language, lessonID, exoID, true);
						} catch (FileNotFoundException | InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					}
					if(isCodeAlreadyWritten(code)) {
						tempFails--;
						continue;
					}
					if(rescanCache(exoID, branchName, commitID)) {
						continue;
					}
					//System.out.println("branchName : "+branchName+" (n°"+(nbBranch)+")"+" / lessonID : "+lessonID+" / exoID : "+exoID+" / commitID : "+commitID);
					try {
						//System.out.println("Execution !");
						//System.out.println("CommitID (n°"+commitNumber+") : "+commitID);
						execCode(commit, branchName, code, language, lessonID, exoID, false);
					} catch (FileNotFoundException | InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Lesson: "+lessonID+" / Exercise: "+exoID+" / Commit: "+commitID);
				}
			}
		}
		return tempFails;
	}
}