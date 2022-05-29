package sum01;

public class ServerStartMain {
	static boolean Busyflag[] = {false,false,false,false,false,false,false,false,false,false,false,false};
	static int fimgcnt = 0; 
	static int rimgcnt = 0; 
	static int flinecnt = 0; 
	static int rlinecnt = 0;
	public static void main(String[] args) {
		try {
			new LSIMMain().start();
			Thread.sleep(10000); 
			//new PingThread().PingLoop(); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
