package core;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Event implements Comparable<Event> {

	
	private RevCommit commit;
	private String commitLog;
	private Instant commitTime;
	private String commitType;
	public final static String Management = "mangement commit", Switch = "switch", Reverted = "reverted", Executed = "executed", 
						Start = "start", Started = "started", Leaved = "leaved", ReadTip = "readtip", CallHelp = "callHelp",
						CancelCallHelp = "cancel call for help", Idle="idle", Unhandled = "unhandled";
	private String  resultCompil;
	public final static String Success = "success", Failed = "failed", CompilError = "compilation error";
	private String os, plm_version, java_version;
	private String exoSwitchTo;
	private int totalTests, passedTests;
	private String exoName, exoLang, outCome, code, error = null;
	private String IdCommit;
	private Instant idleStart, idleEnd;
	private Boolean usesBeta = false, usesUnhandledBeta = false;
	private static Set<String> validVersions = new HashSet<String>();
	private static Set<String> betaVersions = new HashSet<String>();
	static {
		validVersions.add("2.4 (20140901)");
		validVersions.add("2.4.1 (20140905)");
		validVersions.add("2.4.2 (20140909)");
		validVersions.add("2.4.3 (20140911)");
		validVersions.add("2.4.4 (20140912)");
		validVersions.add("2.4.5 (20140916)");
		validVersions.add("2.4.6 (20140917)");
		validVersions.add("2.4.7 (20130920)");
		validVersions.add("2.4.8 (20140928)");
		validVersions.add("2.4.9 (20140929)");
		validVersions.add("2.4.10 (20140930)");
		validVersions.add("2.4.11 (20141009)");
		validVersions.add("2.5 (20141031)");
		betaVersions.add("2.3beta (20140515)");
		betaVersions.add("2.4alpha (20140821)");
		betaVersions.add("2.4beta1 (20140821)");
		betaVersions.add("2.4beta1 (20140901)");
		betaVersions.add("2.4beta2 (20140901)");
		betaVersions.add("2.4alpha (20140724)");
		betaVersions.add("2.4.8-git (20131001)");
		betaVersions.add("2.5-pre (20141015)");
		betaVersions.add("2.6-pre (20141130)");
		betaVersions.add("2.6-pre (20150202)");		
		betaVersions.add("internal (internal)");
	}
	
	
	public Event(RevCommit commit){
		this.commit = commit;
		this.commitTime = Instant.ofEpochMilli(commit.getCommitTime() * 1000L);
		this.commitLog = commit.getFullMessage();
		this.IdCommit = commit.getId().toString();
	}
	
	public void setGlobalInfo(){
			if (commitLog.equals("Create README.md") ||
				commitLog.equals("Empty initial commit")||
				commitLog.equals("Initial commit")||
				commitLog.equals("manual merge\n")||
				commitLog.equals("Manual merging")||
				commitLog.startsWith("Merge remote-tracking branch 'origin/PLM")||
				(commitLog.startsWith("Merge branch 'PLM") && commitLog.contains("https://github.com/mquinson/PLM-data.git into")) ) {
				commitType = Management;
				return;
			}	
			
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(commitLog);
			String kind = jo.get("kind").getAsString();
			
			if(jo.get("plm") !=  null){
				if (! validVersions.contains(jo.get("plm").getAsString()) ){
					usesBeta = true;
					if (! betaVersions.contains(jo.get("plm").getAsString()) ) 
						usesUnhandledBeta = true;
					//System.out.println("\nUnhandled version name: "+  jo.get("plm").getAsString()) ;
				}
			}			

			
			switch(kind){
			case "switched":
				commitType = Switch;
				exoSwitchTo = jo.get("switchto").getAsString();
				break;
			case "reverted":
				commitType = Reverted;
				break;
			case "executed":
				commitType = Executed;
				this.setExecutedInfo(jo);
				break;
			case "start":
				commitType = Start;
			case "started":
				commitType = Started;
				os = jo.get("os").getAsString();
				plm_version = jo.get("plm").getAsString();
				java_version = jo.get("java").getAsString();
				break;
			case "leaved":
				commitType = Leaved;
				os = jo.get("os").getAsString();
				plm_version = jo.get("plm").getAsString();
				java_version = jo.get("java").getAsString();
				break;
			case "readTip":
				commitType = ReadTip;
				break;
			case "callForHelp":
				commitType = CallHelp;
				break;				
			case "cancelCallForHelp":
				commitType = CancelCallHelp;
				break;
			case "idle":
				commitType = Idle;
				idleStart = Instant.parse(jo.get("start").getAsString());
				idleEnd = Instant.parse(jo.get("end").getAsString());
				break;
			default:
				commitType= Unhandled;
			}			
			
			if(jo.has("exo"))
				exoName = jo.get("exo").getAsString();
			else
				exoName = "";
	}

	private void setExecutedInfo(JsonObject jo){
		if(jo.has("outcome")){// old commit log format does not contain outcome entry
			outCome = jo.get("outcome").getAsString();
			switch(outCome){
			case "pass":
				resultCompil = Success;
				break;
			case "compile":
				resultCompil = CompilError;
				break;
			case "fail" : 
				resultCompil = Failed;
				break;
			}			
		}
		
		exoLang = jo.get("lang").getAsString();

		if (jo.has("totaltests"))
			totalTests = jo.get("totaltests").getAsInt(); // not present if outcome is "compile"
		if (jo.has("passedtests"))
			passedTests = jo.get("passedtests").getAsInt(); // not present if outcome is "compile"

		if (resultCompil==null) { // old commit log format support
			if (totalTests == passedTests) {
				resultCompil = Success;
			} else {
				resultCompil = Failed;
			}
		}
	}
	
	
	static Map<String,String> langExt=null;
	static private void initLangExt() {
		langExt = new HashMap<String, String>();
		langExt.put("Python","py");
		langExt.put("Java","java");
		langExt.put("C","c");
		langExt.put("Scala","scala");
		langExt.put("lightbot","ignored");
		langExt.put("Blockly", "blockly");
	}
	
	public void setCode() throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		if(langExt==null)
			initLangExt();
		String path = this.exoName + "." + langExt.get(this.exoLang) + ".code" ;
		code = LocalRepository.getFileContent(path, this.commit);
	}

	public void setError() throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		if(langExt==null)
			initLangExt();
		String path = this.exoName + "." + langExt.get(this.exoLang) + ".error" ;
		error = LocalRepository.getFileContent(path, this.commit);
	}
	
	
	
	@Override
	public String toString() {
		return "Event [commit=" + "commit" + ", commitLog=" + "commitLog"
				+ ", commitTime=" + commitTime + ", commitType=" + commitType
				+ ", resultCompil=" + resultCompil + ", os=" + os
				+ ", plm_version=" + plm_version + ", java_version="
				+ java_version + ", exoSwitchTo=" + exoSwitchTo
				+ ", totalTests=" + totalTests + ", passedTests=" + passedTests
				+ ", exoName=" + exoName + ", exoLang=" + exoLang
				+ ", outCome=" + "outCome" + ", code=" + "code" + "]";
	}
	
	public void printEvent(){
		Instant cal = this.getCommitTime();
		String date = cal.toString(); //cal.get(Calendar.DATE) + "/" +cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + " " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE);
		if(this.getCommitType().equals(Event.Switch))
			System.out.println("____________________________\n"
					+ "Switch from : " + getExoName() + "\nTO " + getExoSwitchTo() + "   / id : " + getIdCommit().substring(6,16) + " date : " + date);		
		else if(this.getCommitType().equals(Event.Executed))
			System.out.println("Exec, result : " + getResultCompil()  + "   / id : " + this.getIdCommit().substring(6,16)  + " date : " + date );
		else if( this.getCommitType().equals(Event.Start) || getCommitType().equals(Event.Started))
			System.out.println("Start "  + "   / id : " + getIdCommit().substring(6,16)  + " date : " + date );
		else
			System.out.println("erreur : "+ getCommitType()   + "   / id : " + getIdCommit().substring(6,16)  + " date : " + date );
		}

	
	@Override
	public int compareTo(Event e) {
		return this.commitTime.compareTo(e.getCommitTime());
	}

	public Instant getCommitTime() {
		return commitTime;
	}

	public String getCommitType() {
		return commitType;
	}

	public int getTotalTests() {
		return totalTests;
	}

	public int getPassedTests() {
		return passedTests;
	}

	public String getExoName() {
		return exoName;
	}

	public String getExoLang() {
		return exoLang;
	}

	public String getCode() {
		return code;
	}

	public String getExoSwitchTo() {
		return exoSwitchTo;
	}

	public String getResultCompil() {
		return resultCompil;
	}

	public String getIdCommit() {
		return IdCommit;
	}
	
	public String getError() {
		return error;
	}

	public Boolean getUsesBeta() {
		return usesBeta;
	}

	public Boolean getUsesUnhandledBeta() {
		return usesUnhandledBeta;
	}

	public Instant getIdleStart() {
		return idleStart;
	}

	public Instant getIdleEnd() {
		return idleEnd;
	}
	
	

	
}
