package com.br.gabrielsilva.prismamc.commons.core.utils.loaders;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileLoader {

	public static List<String> load(File dataFolder) {
		List<String> lista = new ArrayList<>();
		
		try {
			if (!dataFolder.exists()) {
				dataFolder.createNewFile();
			}
			
			Scanner scanner = new Scanner(new FileReader(dataFolder));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
                if (line.isEmpty()) {
                	continue;
                }
                if (line.equals("")) {
                	continue;
                }
                lista.add(line);
			}
			scanner.close();
			scanner = null;
		} catch (Exception ex) {
			//handle exception
		}
		return lista;
	}
}