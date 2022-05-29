package sum01;

public class BGRtoTxt {
	
	private byte[] imageArray;
	private byte[] r;
	private byte[] g;
	private byte[] b;
	private byte[] relocateArray;

	public BGRtoTxt(byte[] imageArray) {
	      this.imageArray = new byte[2580];
	      this.imageArray = imageArray;
	      this.r = new byte[860];
	      this.g = new byte[860];
	      this.b = new byte[860];
	      this.relocateArray = new byte[2580];
	   }
	
	private void SplitBGR() {
		int flag = 3;
	    int r = 0;
	    int g = 0;
	    int b = 0;
	    for (int k = 0; k < 2580; k++) {
	       if (flag % 3 == 0) {
	          this.b[b++] = this.imageArray[k];
	          flag++;
	       } else if (flag % 3 == 1) {
	          this.g[g++] = this.imageArray[k];
	          flag++;
	       } else {
	          this.r[r++] = this.imageArray[k];
	          flag++;
	       }

	    }
	}
	
	private void PutArray() {
		for (int k = 0; k < 2580; k++) {
			if (k < 860) this.relocateArray[k] = this.r[k];
			else if (k < 1720) this.relocateArray[k] = this.g[k - 860];
			else this.relocateArray[k] = this.b[k - 1720];
		}
	}
	
	public byte[] LineRelocateBGR() {
		SplitBGR();
		PutArray();
		
		return relocateArray;
	}

}
