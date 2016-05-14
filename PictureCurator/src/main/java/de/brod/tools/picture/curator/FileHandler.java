package de.brod.tools.picture.curator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHandler {

	static File importFolder = new File("c:\\Daten\\Bilder\\DCIM");
	static File outputFolder = new File("c:\\Daten\\Bilder\\Alben");
	static File deletedFolder = new File("c:\\Daten\\Bilder\\AlbenDelete");
	static File newFolder = new File("c:\\Daten\\Bilder\\AlbenNew");

	static String HDR_KEY = "(hdr)";

	private Map<String, String> mapOfMonths = new HashMap<String, String>();

	private Map<File, Map<String, File>> mapOfImages = new HashMap<>();

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

	File deleteFolder(String subFolderName) {
		return new File(FileHandler.deletedFolder, subFolderName);
	}

	List<String> getFolderNames() {
		Pattern compile = Pattern.compile("(.*) \\d{4}");
		return getFolderNames(compile);
	}

	private List<String> getFolderNames(Pattern compile) {
		List<String> lstFolderNames = new ArrayList<>();
		addFolderNamesWithPattern(lstFolderNames, compile, outputFolder.list());
		for (File subFolder : newFolder.listFiles()) {
			if (subFolder.isDirectory()) {
				addFolderNamesWithPattern(lstFolderNames, compile, subFolder.list());
			}
		}
		Collections.sort(lstFolderNames);
		return lstFolderNames;
	}

	File getImageFile(String sFileName) {
		for (Map<String, File> innerMap : mapOfImages.values()) {
			File file = innerMap.get(sFileName);
			if (file != null) {
				return file;
			}
		}
		return null;
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

	String[] getSetOfImages(File imageFolder) {
		Map<String, File> baseMap = mapOfImages.get(imageFolder);
		if (baseMap == null) {
			return new String[0];
		}
		String[] array = baseMap.keySet().toArray(new String[0]);
		Comparator<String> comp = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return baseMap.get(o1).compareTo(baseMap.get(o2));
			}
		};
		Arrays.sort(array, comp);
		return array;
	}

	public String[] getYears(String folderName) {
		if (folderName == null) {
			return new String[0];
		}
		Pattern compile = Pattern.compile(Pattern.quote(folderName) + " (\\d{4})");
		List<String> folderNames = getFolderNames(compile);
		return folderNames.toArray(new String[0]);
	}

	void initImages(File... imageFolders) {
		mapOfImages.clear();
		Set<String> imageNames = new HashSet<>();
		for (File imageFolder : imageFolders) {
			if (imageFolder.exists() && imageFolder.isDirectory()) {
				Map<String, File> mapOfFolderImages = new HashMap<>();
				mapOfImages.put(imageFolder, mapOfFolderImages);
				Pattern compile = Pattern.compile("(\\d{4})-?(\\d{2})-?(\\d{2})[_](\\d{2})-?(\\d{2})-?(\\d{2}).*");
				for (File imageFile : imageFolder.listFiles()) {
					String sName = imageFile.getName();
					if (!sName.toLowerCase().endsWith("jpg")) {
						continue;
					}
					Matcher matcher = compile.matcher(sName);
					if (matcher.find()) {
						String sNameNew = matcher.group(3) + "." + getMonth(matcher.group(2)) + " " + matcher.group(1)
								+ " " + matcher.group(4) + ":" + matcher.group(5) + ":" + matcher.group(6);
						if (sName.contains("HDR")) {
							sNameNew += " " + HDR_KEY;
						}
						if (mapOfFolderImages.containsKey(sNameNew)) {
							int iCount = 2;
							while (mapOfFolderImages.containsKey(sNameNew + iCount)) {
								iCount++;
							}
							sNameNew += iCount;
						}
						sName = sNameNew;
					}
					if (imageNames.add(sName)) {
						mapOfFolderImages.put(sName, imageFile);
					}
				}
			}
		}
	}

	File newFolder() {
		String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		return new File(FileHandler.newFolder, today);
	}

	boolean renameFile(File baseFolder, String moveToFolder, String fileName) {
		File file = getImageFile(fileName);
		if (file == null || !file.exists()) {
			return false;
		}
		File toFolder;
		if (moveToFolder.length() > 0) {
			String year = file.getName().substring(0, 4);
			toFolder = new File(baseFolder, moveToFolder + " " + year);
		} else {
			toFolder = baseFolder;
		}
		if (!toFolder.exists()) {
			toFolder.mkdirs();
		}
		File dest = new File(toFolder, file.getName());
		System.out.println("... rename " + file.getName() + " to " + dest.getAbsolutePath());
		boolean renameTo = file.renameTo(dest);
		return renameTo;
	}

}
