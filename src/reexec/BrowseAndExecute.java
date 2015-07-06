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
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

	private static HashMap<String,String> oldToNewExo = new HashMap<String,String>();
	static {
		oldToNewExo.put("welcome.lessons.welcome.loopwhile.WhileMoria", "welcome.lessons.welcome.summative.Moria");
		oldToNewExo.put("welcome.lessons.welcome.array.maxvalue.Extrema", "welcome.lessons.welcome.array.search.Extrema");
		oldToNewExo.put("welcome.lessons.welcome.array.extrema.Extrema", "welcome.lessons.welcome.array.search.Extrema");
		oldToNewExo.put("welcome.lessons.welcome.array.indexof.value.IndexOfValue", "welcome.lessons.welcome.array.search.IndexOfValue");
		oldToNewExo.put("welcome.lessons.welcome.array.occurenceofvalue.OccurrenceOfValue", "welcome.lessons.welcome.array.search.OccurrenceOfValue");
		oldToNewExo.put("welcome.lessons.welcome.array.averagevalue.AverageValue", "welcome.lessons.welcome.array.search.AverageValue");
		oldToNewExo.put("welcome.lessons.welcome.array.indexof.value.IndexOfMaxValue", "welcome.lessons.welcome.array.search.IndexOfMaxValue");
		oldToNewExo.put("welcome.lessons.welcome.loopdowhile.Poucet", "welcome.lessons.welcome.loopdowhile.Poucet1");
		oldToNewExo.put("welcome.lessons.welcome.array.maxvalue.MaxValue", "welcome.lessons.welcome.array.search.MaxValue");
		oldToNewExo.put("welcome.lessons.welcome.methods.picture.PictureMono", "welcome.lessons.welcome.methods.picture.PictureMono1");
		oldToNewExo.put("welcome.lessons.welcome.array.averagevalue.AverageValue", "welcome.lessons.welcome.array.search.AverageValue");
		oldToNewExo.put("welcome.lessons.welcome.array.indexof.maxvalue.IndexOfMaxValue", "welcome.lessons.welcome.array.search.IndexOfMaxValue");
	}

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
	 * @throws ExecutionException 
	 */
	public void execCode(Game g, Event commit, String branchName, String code, ProgrammingLanguage lang, String lessonID, String exoID) throws FileNotFoundException, InterruptedException, ExecutionException {
		g.getProgressSpyListeners().clear();
		g.removeSessionKit();
		g.setBatchExecution();

		g.setLocale(new Locale("en"));
		g.setProgrammingLanguage(lang.getLang());
		g.switchLesson(lessonID, true);
		g.getCurrentLesson().setCurrentExercise(exoID);

		Exercise exo = (Exercise) g.getCurrentLesson().getCurrentExercise();

		exo.getSourceFile(lang, 0).setBody(code);
		
		boolean timeout = false;

		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<String> f = exec.submit(new Callable<String>() {
			public String call() throws Exception {
				g.startExerciseExecution();
				Game.waitRunners();
				return "";
			}
		});
		try {
			System.out.println(f.get(15, TimeUnit.SECONDS));
		} catch (TimeoutException e) {
			timeout = true;
		}
		exec.shutdownNow();
		
		Vector<World> currentWorlds = exo.getWorlds(WorldKind.CURRENT);
		g.stopExerciseExecution();

		BufferedWriter bw = null;
		for(int i = 0 ; i < currentWorlds.size() ; i++) {
			World currentWorld = currentWorlds.get(i);
			World answerWorld = exo.getAnswerOfWorld(i);
			if (!currentWorld.winning(answerWorld)) {
				String error = answerWorld.diffTo(currentWorld);
				try {
					File file = new File("logsDir/"+exoID);
					file.mkdirs();
					File dest = new File("logsDir/"+exoID+"/"+branchName+"_"+commit.getIdCommit().split(" ")[1] + ".log");
					dest.createNewFile();
					bw = new BufferedWriter(new FileWriter("logsDir/"+exoID+"/"+branchName+"_"+commit.getIdCommit().split(" ")[1] + ".log", true));
					if(!timeout) {
						if(error!=null) {
							bw.write(error+"\n");
						} else {
							bw.write("Possibly a Python error, that has to be checked out...\n");
						}
						bw.flush();
					} else {
						bw.write("Infinite loop or execution took too long...\n");
						bw.flush();
					}
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	 * Method that is firstly called to obtain and verify every data in the commits and that calls the execution method in certain cases
	 * @param student	Get the Student object in order to obtain all the commits attached
	 * @param nbBranch	Number of branches that has to be checked
	 * @param nbFails	Number of commits that have the type "Failed"
	 * @return the number of Failed-type commits in a branch
	 * @throws ExecutionException 
	 */
	public int getCode(Game g, Student student, int nbBranch, int nbFails) throws ExecutionException {
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));
		int tempFails = nbFails;
		this.listCodes.clear();
		String[] tempBranch = student.getBranchName().split("/");
		String branchName = tempBranch[tempBranch.length-1];
		/*if(nbBranch > 10) { //TODO: Retirer ces huit lignes pour exécuter le code sur toutes les branches !
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

				if(oldToNewExo.containsKey(exoID)) {
					exoID = oldToNewExo.get(exoID);
				}

				String commitID = commit.getIdCommit().split(" ")[1];
				if(lessonsName.contains(lessonID)) {
					tempFails++;
					String code = commit.getCode();
					if(isCodeAlreadyWritten(code)) {
						tempFails--;
						continue;
					}
					if(rescanCache(exoID, branchName, commitID)) {
						continue;
					}
					try {
						execCode(g, commit, branchName, code, language, lessonID, exoID);
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