package vazkii.instancesync;

public class Instance {

	public Addon[] installedAddons;
	public Scan[] cachedScans;

	public static class Addon {
		
		public AddonFile installedFile;
		
		@Override
		public String toString() {
			return installedFile == null ? "NO FILE" : installedFile.toString();
		}
		
		public static class AddonFile {
			
			private String fileNameOnDisk;
			private String FileNameOnDisk;
			public String downloadUrl;
			
			@Override
			public String toString() {
				return fileNameOnDisk;
			}
			
			public String getFileName() {
				return fileNameOnDisk != null ? fileNameOnDisk : FileNameOnDisk;
			}
			
			
		}
		
	}
	
	public static class Scan {
		
		public String folderName;
		
	}
	
}
