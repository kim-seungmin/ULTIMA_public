package sum01;

import java.io.FileOutputStream;
import java.io.IOException;

public class BGR_Array {
	
	private byte[] imageArray;
	private byte[] relocateArray;
	
	public BGR_Array(byte[] imageArray) {
		this.imageArray = new byte[387000];
	    this.imageArray = imageArray;
	    this.relocateArray = new byte[387450];
	}
	
	private void addZero() {
		
		int j = 0;
		int cnt = 3;
		
		for (int k = 0; k < 387447; k++) {
			this.relocateArray[k] = this.imageArray[j++];
			if (cnt % 2583 == 0 || cnt % 2583 == 1 || cnt % 2583 == 2) {
				this.relocateArray[k] = (byte) 0;
				j--;
			}
			cnt++;
		}
		
		this.relocateArray[387447] = (byte) 0;
		this.relocateArray[387448] = (byte) 0;
		this.relocateArray[387449] = (byte) 0;
	}
	
	public byte[] addZeroArray() throws IOException {
		addZero();
		
		return relocateArray;
	}

}
