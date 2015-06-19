package stats;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class StatsPLM {

	private static Map<String,Map<String,Vector<String>>> diffs = new HashMap<String,Map<String,Vector<String>>>();

	
	/**
	 * Method that scans all the files in the "logsDir" directory, in order to put all the data in the HashMap "diffs"
	 */
	public static void scanFiles() {
		File dir = new File("logsDir");
		File[] directories = dir.listFiles();
		File[] files;
		for(int i = 0 ; i<directories.length ; i++) {
			String exoID = directories[i].getName();
			files = directories[i].listFiles();
			for(int j = 0 ; j<files.length ; j++) {
				String id = files[j].getName();
				String filePath = "logsDir/"+exoID+"/"+files[j].getName();
				Scanner scanner = null;
				try {
					scanner = new Scanner(new File(filePath));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				String error = "";
				while (scanner.hasNextLine()) {
					error = error + scanner.nextLine();
				}
				if(diffs.get(exoID) == null) {
					diffs.put(exoID, new HashMap<String,Vector<String>>());
				}
				if(diffs.get(exoID).get(error) == null) {
					diffs.get(exoID).put(error, new Vector<String>());
					diffs.get(exoID).get(error).add(id);
				}
				else {
					if(!diffs.get(exoID).get(error).contains(id)) {
						diffs.get(exoID).get(error).add(id);
					}
				}
			}
		}
	}

	
	/**
	 * Method that creates the worksheet object to create an Excel sheet
	 * @param xls	Boolean that asks if you want a .xls file (true) or a .xlsx file (false) (better)
	 * @throws InterruptedException
	 */
	public static void writeWorkSheet(boolean xls) {
		Workbook wb;
		Object sheet;
		Row row;
		if(!xls) {
			wb = new XSSFWorkbook();
			sheet = (XSSFSheet) wb.createSheet("Result");
		} else {
			wb = new HSSFWorkbook();
			sheet = (HSSFSheet) wb.createSheet("Result");
		}
		int i = 0;
		int j = i;
		for(String exoID : diffs.keySet()) {
			if(!xls) {
				row = ((XSSFSheet) sheet).createRow(i);
			} else {
				row = ((HSSFSheet) sheet).createRow(i);
			}
			i++;
			row.createCell((short)0, XSSFCell.CELL_TYPE_STRING).setCellValue(exoID);
			j = i-1;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(String error : diffs.get(exoID).keySet()) {
				Row subRow;
				if(!xls) {
					subRow = (XSSFRow) row;
				} else {
					subRow = (HSSFRow) row;
				}
				if(i-1!=j) {
					if(!xls) {
						subRow = ((XSSFSheet) sheet).createRow(j);
					} else {
						subRow = ((HSSFSheet) sheet).createRow(j);
					}
				}
				j++;
				if(error!=null) {
					if(!xls) {
					if(error.length()>30000) {
						subRow.createCell((short)1, XSSFCell.CELL_TYPE_STRING).setCellValue(error.substring(0, 32766));
					} else {
						subRow.createCell((short)1, XSSFCell.CELL_TYPE_STRING).setCellValue(error);
					}
					} else {
						if(error.length()>30000) {
							subRow.createCell((short)1, HSSFCell.CELL_TYPE_STRING).setCellValue(error.substring(0,29999));
						} else {
							subRow.createCell((short)1, HSSFCell.CELL_TYPE_STRING).setCellValue(error);
						}
					}
					subRow.createCell((short)2).setCellValue(diffs.get(exoID).get(error).size());
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			i=j;
		}

		FileOutputStream fileOut;
		try {
			if(!xls) {
				fileOut = new FileOutputStream("result.xlsx");
			} else {
				fileOut = new FileOutputStream("result.xls");
			}
			wb.write(fileOut);
			fileOut.close();
			wb.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Starting execution");
		boolean xls = false;
		scanFiles();
		writeWorkSheet(xls);
		System.out.println("Execution finished without errors");
	}

}
