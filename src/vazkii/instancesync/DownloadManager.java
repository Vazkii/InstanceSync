package vazkii.instancesync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import vazkii.instancesync.Instance.Addon;
import vazkii.instancesync.Instance.Addon.AddonFile;
import vazkii.instancesync.Instance.Scan;

public class DownloadManager {

	private final File modsDir;
	
	private List<String> acceptableFilenames = new LinkedList<>();
	private ExecutorService executor; 
	private int downloadCount;
	
	public DownloadManager(File modsDir) {
		this.modsDir = modsDir;
	}

	public void downloadInstance(Instance instance) {
		executor = Executors.newFixedThreadPool(10);

		System.out.println("Downloading any missing mods");
		long time = System.currentTimeMillis();

		for(Addon a : instance.installedAddons)
			downloadAddonIfNeeded(a);

		if(downloadCount == 0) {
			System.out.println("No mods need to be downloaded, yay!");
		} else try {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);

			float secs = (float) (System.currentTimeMillis() - time) / 1000F;
			System.out.printf("Finished downloading %d mods (Took %.2fs)%n%n", downloadCount, secs);
		} catch (InterruptedException e) {
			System.out.println("Downloads were interrupted!");
			e.printStackTrace();
		}
		
		for(Scan s : instance.cachedScans)
			acceptableFilenames.add(s.folderName);

		deleteRemovedMods();
	}

	private void downloadAddonIfNeeded(Addon addon) {
		AddonFile file = addon.installedFile;
		if(file == null)
			return;

		String filenameOnDisk = file.getFileName();
		acceptableFilenames.add(filenameOnDisk);

		File modFile = new File(modsDir, filenameOnDisk);
		if(!modExists(modFile))
			download(modFile, file.downloadUrl);
	}

	private void download(final File target, final String downloadUrl) {
		Runnable run = () -> {
			String name = target.getName();

			try {
				System.out.println("Downloading " + name);
				long time = System.currentTimeMillis(); 

				URL url = new URL(downloadUrl);
				FileOutputStream out = new FileOutputStream(target);

				URLConnection connection = url.openConnection();
				InputStream in = connection.getInputStream();

				byte[] buf = new byte[4096];
				int read = 0;

				while((read = in.read(buf)) > 0)
					out.write(buf, 0, read);

				out.close();
				in.close();

				float secs = (float) (System.currentTimeMillis() - time) / 1000F;
				System.out.printf("Finished downloading %s (Took %.2fs)%n", name, secs);
			} catch(Exception e) {
				System.out.println("Failed to download " + name);
				e.printStackTrace();
			}
		};

		downloadCount++;
		executor.submit(run);
	}

	private void deleteRemovedMods() {
		System.out.println("Deleting any removed mods");
		File[] files = modsDir.listFiles(f -> !f.isDirectory() && !acceptableFilenames.contains(f.getName()));

		if(files.length == 0)
			System.out.println("No mods were removed, woo!");
		else { 
			for(File f : files) {
				System.out.println("Found removed mod " + f.getName());
				f.delete();
			}
			
			System.out.println("Deleted " + files.length + " old mods");
		}
	}

	private boolean modExists(File file) {
		if(file.exists())
			return true;
		
		String name = file.getName();
		
		if(name.endsWith(".disabled"))
			return swapIfExists(file, name.replaceAll("\\.disabled", ""));
		else return swapIfExists(file, name + ".disabled");
	}
	
	private boolean swapIfExists(File target, String searchName) {
		File search = new File(modsDir, searchName);
		if(search.exists()) {
			System.out.println("Found alt file for " + target.getName() + " -> " + searchName + ", switching filename");
			search.renameTo(target);
			return true;
		}
		
		return false;
	}
	
}
