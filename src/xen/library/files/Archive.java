package xen.library.files;

public class Archive {
	private static Archive instance = null;
	
	public static Archive getInstance(){
		if(instance == null){
			instance = new Archive();
		}
		return instance;
	}
	
	private Archive(){
		
	}

	public boolean inArchive() {
		return false;
	}

}
