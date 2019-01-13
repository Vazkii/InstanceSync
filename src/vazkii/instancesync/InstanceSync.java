package vazkii.instancesync;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

public final class InstanceSync {
	
	private static final String VERSION = "1.0.2";

	public static void main(String[] args) {
		System.out.println("InstanceSync " + VERSION);
		
		long time = System.currentTimeMillis();
		File dir = new File(".");
		System.out.println("Running in " + dir.getAbsolutePath());
		
		File instanceFile = new File(dir, "minecraftinstance.json");
		if(!instanceFile.exists()) {
			System.out.println("No minecraftinstance.json file exists in this directory, aborting");
			System.exit(1);
		}
		
		System.out.println("Found minecraftinstance.json file");
		
		File mods = new File(dir, "mods");
		if(mods.exists() && !mods.isDirectory()) {
			System.out.println("/mods exists but is a file, aborting");
			System.exit(1);
		}
		
		if(!mods.exists()) {
			System.out.println("/mods does not exist, creating");
			mods.mkdir();
		}
		
		Gson gson = new Gson();
		try {
			System.out.println("Reading minecraftinstance.json");
			Instance instance = gson.fromJson(new FileReader(instanceFile), Instance.class);
			
			if(instance == null) {
				System.out.println("Couldn't load minecraftinstance.json, aborting");
				System.exit(1);
			}
			
			System.out.println("Instance loaded, has " + instance.installedAddons.length + " mods\n");

			DownloadManager manager = new DownloadManager(mods);
			manager.downloadInstance(instance);
			
			float secs = (float) (System.currentTimeMillis() - time) / 1000F;
			System.out.printf("%nDone! Took %.2fs%n", secs);
		} catch (IOException e) {
			System.out.println("ERROR: Something bad happened!");
			e.printStackTrace();
		}
		
	}

}
