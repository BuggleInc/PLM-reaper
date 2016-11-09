package example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import core.Event;
import core.LocalRepository;
import core.RepoIterator;
import core.Student;


public class ScalaCompilationError {

	static void incOrInitialize(Map<String,Integer> map, String key) {
		Integer value = map.get(key); 
		if (value == null) {
			map.put(key,1);
		} else {
			map.put(key, value + 1);
		}
	}

	public static void main(String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		LocalRepository.cloneRepo();	

		//LocalRepository.fetch();


		RepoIterator ite = new RepoIterator();
		
		//option for the iterator
		ite.setCollectCode(true);
		ite.setCollectError(true);
		ite.quiet();

		ite.addCommitType(Event.Executed);

		/*
		Calendar dateMin = Calendar.getInstance();
		dateMin.set(Calendar.YEAR, 2015);dateMin.set(Calendar.MONTH, 8);dateMin.set(Calendar.DATE, 1);
		ite.setDateMin(dateMin);
		*/

		Map<String, Pattern>errors = new HashMap<String, Pattern>();
		errors.put("syntax error: wrong token",    Pattern.compile("error: (identifier|eof|'[^ ']+') expected but ('[^ ']+'|identifier|(integer|symbol|string|double) literal) found."));

		errors.put("syntax error: not a member in <scalar>", Pattern.compile("error: value ([a-zA-Z0-9]*|unary_!) is not a member of (Nothing|Unit|Boolean|String|Int)"));
		errors.put("syntax error: not a member/missing space?", Pattern.compile("error: value ([~:><=!+-|]|&)+ is not a member of (Unit|Boolean|String|Int)"));
		errors.put("syntax error: missing arguments for operator?", Pattern.compile("error: missing arguments for method ([=!+-|]|&)+ in class (Unit|Boolean|String|Int)"));
		errors.put("syntax error: type not a member", Pattern.compile("error: type [a-zA-Z0-9=!+-]+ is not a member of "));
		errors.put("syntax error: invalid literal number", Pattern.compile("error: Invalid literal number"));

		errors.put("syntax error: illegal/expected start of statement/expression/def", Pattern.compile("error: (illegal|expected) start of (statement|simple expression|simple pattern|definition)"));

		errors.put("syntax error: unclosed literal", Pattern.compile("error: unclosed (string|character) literal"));
		errors.put("syntax error: overloaded method with alternatives", Pattern.compile("error: overloaded method value [a-zA-Z0-9]+ with alternatives:"));


		errors.put("recursive variable", Pattern.compile("error: recursive variable [a-zA-Z0-9]+ needs type"));
		errors.put("forward reference extends over definition", Pattern.compile("error: forward reference extends over definition of variable"));

		errors.put("<scalar> does not take parameters", Pattern.compile("error: (Unit|Boolean|Int)(\\([^)]+\\))? does not take parameters"));
		errors.put("ambiguous reference to overloaded definition", Pattern.compile("error: ambiguous reference to overloaded definition,"));
		errors.put("forward reference extends over definition of value", Pattern.compile("error: forward reference extends over definition of value"));
		errors.put("not a legal formal parameter", Pattern.compile("error: not a legal formal parameter"));


		errors.put("type not found", Pattern.compile("error: not found: type [=a-zA-Z0-9]+"));
		errors.put("variable redefinition", Pattern.compile("error: [a-zA-Z0-9]+ is already defined as variable [a-zA-Z0-9]+"));
		errors.put("method redefinition", Pattern.compile("error: method [a-zA-Z0-9]+ is defined twice"));

		errors.put("missing = in front of body", Pattern.compile("error: illegal start of declaration \\(possible cause: missing .=' in front of current method body\\)"));

		errors.put("use: not enough arguments", Pattern.compile("error: not enough arguments for (method|constructor) "));
		errors.put("use: too many arguments", Pattern.compile("error: too many arguments for (method|constructor) "));

		errors.put("use: not a member in object/class", Pattern.compile("error: value ([-%a-zA-Z0-9]*|unary_!) is not a member of (object |class |Object|Double|java.)"));
		errors.put("use: Out of bound exception", Pattern.compile("java.lang.RuntimeException : Out of bounds in"));
		errors.put("use: missing arguments", Pattern.compile("error: missing arguments for method [a-zA-Z]+(;| in (package|object|class))"));

		errors.put("use: protection error", Pattern.compile("error: variable [a-zA-Z]+ in class [a-zA-Z]+ cannot be accessed in "));
		errors.put("useless 'overrides'", Pattern.compile("error: method [a-zA-Z]+ overrides nothing"));

		Map<String,Integer> scalaError = new HashMap<String,Integer>();
		
		int activeBranch = 0;
		int unhandled = 0;
		int totalCompilError = 0;
		while(ite.hasNext() && activeBranch < 50){
			Student student = ite.next();
			if (student != null) {
				
				for (Event evt: student.getEvents()) {						
					if (!evt.getResultCompil().equals("compilation error"))
						continue;
					if (!evt.getExoLang().equals("Scala")) 
						continue;
					
					totalCompilError++;
					String msg = evt.getError();
					
					if (msg == null || msg.length() == 0)
						continue;
					else if (msg.contains("error: value lenght is not a member of Array[")||
							msg.contains("error: value lenth is not a member of Array[")||
							msg.contains("error: value lengtht is not a member of Array[")||
							msg.contains("error: value lengths is not a member of Array[")||
							msg.contains("error: value enght is not a member of Array[")||
							msg.contains("error: value Length is not a member of Array["))
						incOrInitialize(scalaError, "typo in value name 'lenght'");

					else if (msg.contains("java.lang.IllegalArgumentException")||
							msg.contains("java.lang.RuntimeException"))
						incOrInitialize(scalaError, "Runtime error");

					else if (msg.contains("Your entity failed to start. Did you forgot to put your code within a method?")||
							msg.contains("Your entity does not start. Did you forgot to put your code within a method?"))
						incOrInitialize(scalaError, "code out of any method");

					else if (msg.contains("error: reassignment to val")) 
						incOrInitialize(scalaError, "reassignment to val");
					else if (msg.contains("error: only classes can have declared but undefined members"))
						incOrInitialize(scalaError, "only classes can have declared but undefined members");
					else if (msg.contains("error: type mismatch"))
						incOrInitialize(scalaError, "type mismatch");
					else if (msg.contains("error: not found: value "))
						incOrInitialize(scalaError, "value not found");

					else if (msg.contains("error: illegal character '"))
						incOrInitialize(scalaError, "illegal character");

					else if (msg.contains("error: Unmatched closing brace '}' ignored here"))
						incOrInitialize(scalaError, "Unmatched/Missing closing brace '}'");
					else if (msg.contains("error: Missing closing brace `}' assumed here"))
						incOrInitialize(scalaError, "Unmatched/Missing closing brace '}'");

					else if (msg.contains("error: missing parameter type"))
						incOrInitialize(scalaError, "missing parameter type");
					else if (msg.contains("error: unclosed comment"))
						incOrInitialize(scalaError, "unclosed comment");
					else if (msg.contains("error: expected class or object definition"))
						incOrInitialize(scalaError, "expected class or object definition");

					else {
						Boolean found = false;
						for (String key: errors.keySet()) {
							if (errors.get(key).matcher(msg).find()) {
								found = true; 
								incOrInitialize(scalaError, key);
								break;
							}
						}

						if (!found) {
							unhandled++;
							System.out.println(evt.getExoName()+" -- >>"+msg+"<<");
						}
					}
				}
				activeBranch ++;
			}
		}

		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXX\n");
		System.out.println("\nExplored "+activeBranch+" active branchs. Handled "+(totalCompilError-unhandled)+" compilation errors out of "+totalCompilError);
		for (String key:scalaError.keySet()) 
			if (scalaError.get(key) > 0)
				System.out.println(scalaError.get(key)+": "+key);

	}
}
