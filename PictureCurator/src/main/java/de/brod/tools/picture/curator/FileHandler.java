package de.brod.tools.picture.curator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHandler {

	static File importFolder = new File("c:\\Daten\\Bilder\\DCIM");
	static File outputFolder = new File("c:\\Daten\\Bilder\\Alben");
	static File newFolder = new File("c:\\Daten\\Bilder\\AlbenNew");

	private Map<String, String> mapOfMonths = new HashMap<String, String>();

	private Map<String, File> mapOfImages = new HashMap<String, File>();

	List<String> getFolderNames() {
		Pattern compile = Pattern.compile("(.*) \\d{4}");
		return getFolderNames(compile);
	}

	private List<String> getFolderNames(Pattern compile) {
		List<String> lstFolderNames = new ArrayList<>();
		addFolderNamesWithPattern(lstFolderNames, compile, outputFolder.list());
		for (File subFolder : newFolder.listFiles()) {
			if (subFolder.isDirectory())
				addFolderNamesWithPattern(lstFolderNames, compile, subFolder.list());
		}
		Collections.sort(lstFolderNames);
		return lstFolderNames;
	}

	void addFolderNamesWithPattern(List<String> lst, Pattern compile, String[] list) {
		for (String sFileName : list) {
			Matcher matcher = compile.matcher(sFileName);
			if (matcher.find()) {
				String group = matcher.group(1);
				if (!lst.contains(group)) {
					lst.add(group);
				}
			}
		}
	}

	String[] getImages(File imageFolder) {
		mapOfImages.clear();
		Pattern compile = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})[_](\\d{2})(\\d{2})(\\d{2}).*");
		for (File sFileName : imageFolder.listFiles()) {
			String sName = sFileName.getName();
			Matcher matcher = compile.matcher(sName);
			if (matcher.find()) {
				sName = matcher.group(3) + "." + getMonth(matcher.group(2)) + " " + matcher.group(1) + " "
						+ matcher.group(4) + ":" + matcher.group(5) + ":" + matcher.group(6);
				if (mapOfImages.containsKey(sName)) {
					int iCount = 2;
					while (mapOfImages.containsKey(sName + iCount)) {
						iCount++;
					}
					sName += iCount;
				}
			}
			mapOfImages.put(sName, sFileName);
		}
		String[] array = mapOfImages.keySet().toArray(new String[0]);
		Comparator<String> comp = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return mapOfImages.get(o1).compareTo(mapOfImages.get(o2));
			}
		};
		Arrays.sort(array, comp);
		return array;
	}

	private String getMonth(String group) {
		String month = mapOfMonths.get(group);
		if (month == null) {
			try {
				month = new SimpleDateFormat("MMMM").format(new SimpleDateFormat("yyyyddMM").parse("200001" + group));
			} catch (Exception e) {
				month = group + ".";
			}
			mapOfMonths.put(group, month);
		}
		return month;
	}

	File getImageFile(String sFileName) {
		return mapOfImages.get(sFileName);
	}

	boolean renameFile(File baseFolder, String moveToFolder, String string) {
		File file = getImageFile(string);
		if (file == null || !file.exists())
			return false;
		String year = file.getName().substring(0, 4);
		File toFolder = new File(baseFolder, moveToFolder + " " + year);
		if (!toFolder.exists()) {
			toFolder.mkdirs();
		}
		File dest = new File(toFolder, file.getName());
		System.out.println("... rename " + file.getName() + " to " + dest.getAbsolutePath());
		boolean renameTo = file.renameTo(dest);
		return renameTo;
	}

	File newFolder() {
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return new File(FileHandler.newFolder, today);
	}

	public String[] getYears(String folderName) {
		Pattern compile = Pattern.compile(Pattern.quote(folderName) + " (\\d{4})");
		List<String> folderNames = getFolderNames(compile);
		return folderNames.toArray(new String[0]);
	}

}
