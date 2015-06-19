package core;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class RepoIterator {
	
	private String partialExoName; // collect event only with exoName or with exoName starting with partial exoName
	private ArrayList<String> exoName, exoNameExact; // list of valid exoname for event , list of valid exoname that must have been tried by the student to be valid 
	private ArrayList<String> validBranch; //List of valid branch, collect only those branchs
	private ArrayList<String> commitType; //collect event only with a type in commitType
	private Calendar dateMin, dateMax; // collect event only after and/or before dateMin/DateMax
	private boolean collectCode = false, collectError = false; //get the code/error of the commit ?
	
	//	private Collection<Ref> branchs ;
	private Iterator<Ref> iterator;
	private int i, size;
	

	public RepoIterator(){
		Collection<Ref> temp = LocalRepository.getBranchs();
		size = temp.size();
		iterator = temp.iterator();
		i = 0;
		System.out.println("Parse "+size+" branchs. This may take a few seconds.");

	}

	public boolean hasNext(){
		return iterator.hasNext();
	}
	
	/**
	 * @param branchs Branchs of the repository to browse
	 * @return Collection of students
	 * @throws IOException
	 * Option must be set before (type of commit to gather, gather code or not,...)
	 */
	public Student next() throws IOException{
		Ref branch = iterator.next();
		i++;
		if (i % 150 == 0)
			System.out.println(". "+i+"/"+size);
		else
			System.out.print(".");

		
		if(validBranch != null && !validBranch.contains(branch.getName()))
			return null;
		
		Student student = new Student(branch.getName());
		Collection<RevCommit> commits = LocalRepository.getCommits(branch.getName());
		Boolean unhandledBeta = false ;
		for(RevCommit commit : commits){ //browse all commit of the student			
			Event temp = new Event(commit);
			if(testConstruct(temp))
				continue;

			temp.setGlobalInfo();
			if(temp.getUsesUnhandledBeta()){ // no need to parse this student any further
				unhandledBeta = true;
				break;
			}

			if(testGlobalInfo(temp))
				continue;				

			if(collectCode && temp.getCommitType().equals(Event.Executed))
				temp.setCode();
			if(collectError && temp.getCommitType().equals(Event.Executed) && 
					(temp.getResultCompil().equals(Event.CompilError) || temp.getResultCompil().equals((Event.Failed))))
				temp.setError();

			student.addEvent(temp);			
		}
		if(!unhandledBeta)
			if(student.getEvents().size()>0)
				if(!testStudent(student))
					return student;
		
		return null;
	}
		
		
	
	private boolean testStudent(Student student) {
		if(exoNameExact != null && !student.getTriedExoName().containsAll(exoNameExact))
			return true;	
		
		return false;
	}

	private Boolean testConstruct(Event e){ // test if the commit is conform to the option after the construction of the event
		if(dateMin != null && dateMin.after(e.getCommitTime()))
			return true;
		if(dateMax != null && dateMax.before(e.getCommitTime()))
			return true;		
		
		return false;
	}
	
	private Boolean testGlobalInfo(Event e){// test if the commit is conform to the option after setting general data
		if(exoName != null && e.getExoName() != null && !exoName.contains(e.getExoName()))			
			return true;
		if(partialExoName != null && e.getExoName() != null
				&&( e.getExoName().length()<=partialExoName.length() 
				|| !partialExoName.equals(e.getExoName().substring(0, partialExoName.length()))))
				return true;
		if( commitType != null && !commitType.contains(e.getCommitType()))
			return true;
		
			
		return false;
	}

	
	
	/**
	 * @param exoName
	 * Set to collect only event of exercise "exoName"
	 */
	public void setExoName(ArrayList<String> exoName) {
		this.exoName = exoName;
	}

	public void addExoName(String exo){
		if(this.exoName == null)
			this.exoName = new ArrayList<>();
		exoName.add(exo);			
	}

	public void setExoNameExact(ArrayList<String> exoNameExact) {
		this.exoNameExact = exoNameExact;
	}

	public void addExoNameExact(String exo){
		if(this.exoNameExact== null)
			this.exoNameExact = new ArrayList<>();
		exoNameExact.add(exo);			
	}
	
	
	public ArrayList<String> getCommitType() {
		return commitType;
	}

	/**
	 * @param commitType
	 * Set to collect only event with a type in commitType
	 */
	public void setCommitType(ArrayList<String> commitType) {
		this.commitType = commitType;
	}
	
	public void addCommitType(String commitType) {
		if(this.commitType == null)
			this.commitType = new ArrayList<String>();
		if(!this.commitType.contains(commitType))
			this.commitType.add(commitType);
	}

	public Calendar getDateMin() {
		return dateMin;
	}

	/**
	 * @param dateMin
	 * Set to collect only event after dateMin 
	 */
	public void setDateMin(Calendar dateMin) {
		this.dateMin = dateMin;
	}

	public Calendar getDateMax() {
		return dateMax;
	}

	/**
	 * @param dateMax
	 * Set to collect only event before dateMax
	 */
	public void setDateMax(Calendar dateMax) {
		this.dateMax = dateMax;
	}

	public String getPartialExoName() {
		return partialExoName;
	}

	/**
	 * @param partialExoName
	 * Set to collect only event of exercise with a name starting by partialExoname
	 */
	public void setPartialExoName(String partialExoName) {
		this.partialExoName = partialExoName;
	}

	public boolean isCollectCode() {
		return collectCode;
	}

	
	/**
	 * @param collectCode
	 * Set to true to set the "code" attribute of event collected
	 */
	public void setCollectCode(boolean collectCode) {
		this.collectCode = collectCode;
	}

	public void setCollectError(boolean collectError) {
		this.collectError = collectError;
	}

	public void setValidBranch(ArrayList<String> validBranch) {
		this.validBranch = validBranch;
	}

	public void addValidBranch(String validBranch) {
		if(this.validBranch == null)
			this.validBranch = new ArrayList<String>();
		if(!this.validBranch.contains(validBranch))
			this.validBranch.add(validBranch);
	}
	
	
}
