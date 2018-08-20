package fileFetch;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GetAllFiles {
	
	public GetAllFiles(String dataPath, Collection<File> allFiles){
		addFilesRecursively(new File(dataPath), allFiles);
	}

	
	void addFilesRecursively(File file, Collection<File> allFiles) {
		final File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				if(child.getName().startsWith(".")){ //ignore hidden file
					continue;
				}
				if(child.isDirectory()){
					addFilesRecursively(child, allFiles);
				}else if(child.isFile()){
					allFiles.add(child);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		String dataPath = "./test";
		Collection<File> allFiles = new ArrayList<File>();
		new GetAllFiles(dataPath, allFiles);

		Iterator<File> it = allFiles.iterator();
		while (it.hasNext()) {
			File file = it.next();
			String filePath = file.toString();
			String fileName = file.getName();
			System.out.println(filePath + "\t" + fileName);
		}
	}
}
