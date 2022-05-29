package sum01;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

public class BytetoBmp {        
    private static final int BMP_SIZE_HEADER               = 54;                    // total header length, 54 bytes
    private static final int BMP_SIZE_IMAGE_WIDTH          = 4;                     // size of image width field, 4 bytes
    private static final int BMP_SIZE_PAYLOAD_LENGTH       = 4;                     // size of 'horizontal resolution' field, here: payload length, 4 bytes
    private static final int BMP_SIZE_BMPUTIL_MAGIC        = 4;                     // size of 'vertical resolution' field, here: payload length, 4 bytes

    private static final int BMP_OFFSET_FILESIZE_BYTES     = 2;                     // offset of filesize field, 4 bytes
    private static final int BMP_OFFSET_IMAGE_WIDTH        = 18;                    // offset of image width field, 4 bytes
    private static final int BMP_OFFSET_IMAGE_HEIGHT       = 22;                    // offset of image height field, 4 bytes
    private static final int BMP_OFFSET_IMAGE_DATA_BYTES   = 34;                    // 4 bytes
    private static final int BMP_OFFSET_PAYLOAD_LENGTH     = 38;                    // 4 bytes
    private static final int BMP_OFFSET_BMPUTIL_MAGIC      = 42;                    // 4 bytes
    
    private static final byte UDEF                         = 0;                     // undefined value in bitmap header, to be overwritten by methods                              

    private static final byte[] BMP_HEADER = new byte[] {
        /* 00 */ 0x42, 0x4d,                                                        // signature, "BM"
        /* 02 */ UDEF, UDEF, UDEF, UDEF,                                            // size in bytes, filled dynamically
        /* 06 */ 0x00, 0x00,                                                        // reserved, must be zero
        /* 08 */ 0x00, 0x00,                                                        // reserved, must be zero
        /* 10 */ 0x36, 0x00, 0x00, 0x00,                                            // offset to start of image data in bytes
        /* 14 */ 0x28, 0x00, 0x00, 0x00,                                            // size of BITMAPINFOHEADER structure, must be 40 (0x28)
        /* 18 */ UDEF, UDEF, UDEF, UDEF,                                            // image width in pixels, filled dynamically
        /* 22 */ UDEF, UDEF, UDEF, UDEF,                                              // image height in pixels, filled dynamically
        /* 26 */ 0x01, 0x00,                                                        // number of planes, must be 1
        /* 28 */ 0x18, 0x00,                                                        // number of bits per pixel (1, 4, 8, or 24) -> 24 = 0x18
        /* 30 */ 0x00, 0x00, 0x00, 0x00,                                            // compression type (0=none, 1=RLE-8, 2=RLE-4)
        /* 34 */ UDEF, UDEF, UDEF, UDEF,                                            // size of image data in bytes (including padding)
        /* 38 */ UDEF, UDEF, UDEF, UDEF,                                            // normally: horizontal resolution in pixels per meter (unreliable)
                                                                                    //  --> HERE: used to indicate the payload length
        /* 42 */ UDEF, UDEF, UDEF, UDEF,                                            // vertical resolution in pixels per meter (unreliable)
                                                                                    //  --> HERE: used to mark file as encoded bitmap, see BMPUTIL_MAGIC
        /* 46 */ 0x00, 0x00, 0x00, 0x00,                                            // number of colors in image, or zero
        /* 50 */ 0x00, 0x00, 0x00, 0x00,                                            // number of important colors, or zero
    };  

    private static final byte[] BMPUTIL_MAGIC = new byte[] {
    	0x30, 0x32, 0x30, 0x35                                                      // mark used in 'vertical resolution' field to mark bmp file, "0205"
    };
     
    public static void encodeToBitmap(File srcFile, File destFile) throws IOException {
    	encodeToBitmap(new FileInputStream(srcFile), srcFile.length(), new FileOutputStream(destFile));
    }

    public static void encodeToBitmap(byte[] srcBytes, File destFile) throws IOException {
    	encodeToBitmap(new ByteArrayInputStream(srcBytes), srcBytes.length, new FileOutputStream(destFile));
    }

    public static void encodeToBitmap(byte[] srcBytes, OutputStream destStream) throws IOException {
    	encodeToBitmap(new ByteArrayInputStream(srcBytes), srcBytes.length, destStream);
    }

    public static void encodeToBitmap(InputStream srcStream, long srcStreamLength, OutputStream destStream) throws IOException {	
        if (srcStreamLength > Integer.MAX_VALUE) {
            throw new IOException("File too big; max. "+Integer.MAX_VALUE+" bytes supported.");
        }

        int imageWidth = 861;
        int imageHeight = 150;
        
        int rowPadding = 4 - (imageWidth*3 % 4);                
        int filesizeBytes = imageWidth*imageHeight*3 + imageHeight*rowPadding
        		+ BMP_SIZE_HEADER;

        int imageBytesWithPadding = filesizeBytes - BMP_SIZE_HEADER;             
        int payloadPadding = (int) (imageWidth*imageHeight*3 - srcStreamLength);		

        byte[] header = BMP_HEADER.clone();                                         // Clone bitmap header template, and overwrite with fields

        writeIntLE(header, BMP_OFFSET_FILESIZE_BYTES, filesizeBytes);
        writeIntLE(header, BMP_OFFSET_IMAGE_WIDTH, imageWidth);
        writeIntLE(header, BMP_OFFSET_IMAGE_HEIGHT, imageHeight);
        writeIntLE(header, BMP_OFFSET_IMAGE_DATA_BYTES, imageBytesWithPadding);
        writeIntLE(header, BMP_OFFSET_PAYLOAD_LENGTH, (int) srcStreamLength);   
        
        System.arraycopy(BMPUTIL_MAGIC, 0, header,                                  // Copy magic number to header 
        		BMP_OFFSET_BMPUTIL_MAGIC, BMPUTIL_MAGIC.length);

        destStream.write(header, 0, header.length);

        byte[] row = new byte[imageWidth*3];
        int read;

        while ((read = srcStream.read(row)) != -1) {
        	destStream.write(row, 0, read);                                         // Write payload
        	destStream.write(new byte[rowPadding]);                                 // Write padding
        }

        destStream.write(new byte[payloadPadding]);

        srcStream.close();
        destStream.close();
    }   
    
    private static void writeIntLE(byte[] bytes, int startoffset, int value) {
        bytes[startoffset] = (byte)(value);
        bytes[startoffset+1] = (byte)(value >>> 8);
        bytes[startoffset+2] = (byte)(value >>> 16);
        bytes[startoffset+3] = (byte)(value >>> 24);
    }

	public static void byteArrayConvertToImageFile(int location, byte type,byte[] ArrayByte, byte size) throws Exception { 
		BGR_Array bgr;
		BGRtoTxt bgrline;
    	bgr = new BGR_Array(ArrayByte);
    	bgrline = new BGRtoTxt(ArrayByte);
		
  
		switch(type) {
		
			case 0x11:
			  
				switch(location) {
  
					case 0: 
						BytetoBmp.encodeToBitmap(bgr.addZeroArray(), new File("C:\\xampp\\htdocs\\ULTIMA\\public\\assets\\camImage\\f_img"+ServerStartMain.fimgcnt+".bmp")); 
//						ServerStartMain.fimgcnt++; 
//						if (ServerStartMain.fimgcnt == 3) ServerStartMain.fimgcnt = 0; 
						break;
  
					case 1: 
						BytetoBmp.encodeToBitmap(bgr.addZeroArray(), new File("C:\\xampp\\htdocs\\ULTIMA\\public\\assets\\camImage\\r_img"+ServerStartMain.rimgcnt+".bmp")); 
//						ServerStartMain.rimgcnt++; 
//						if (ServerStartMain.rimgcnt == 3) ServerStartMain.rimgcnt = 0; 
						break;
				}
				
				break;
  
			case 0x10: 
				
  
				switch(location) {
  
				case 0: 
						try { 
							FileWriter w = new FileWriter("C:\\xampp\\htdocs\\ULTIMA\\public\\assets\\camImage\\/f_line"+ServerStartMain.flinecnt+".txt");
							for (int lcnt = 0; lcnt < 2580; lcnt++) w.write(bgrline.LineRelocateBGR()[lcnt] + "/r/n"); 
							w.close();
//							ServerStartMain.flinecnt++; 
//						if (ServerStartMain.flinecnt == 3) ServerStartMain.flinecnt = 0;

							} catch (Exception e) { 
								e.getStackTrace(); 
								} break;
  
				case 1: 
						try { 
							FileWriter w = new FileWriter("C:\\xampp\\htdocs\\ULTIMA\\public\\assets\\camImage\\r_line"+ServerStartMain.rlinecnt+".txt");
							for (int lcnt = 0; lcnt < 2580; lcnt++) w.write(bgrline.LineRelocateBGR()[lcnt] + "/r/n"); 
							w.close();
//							ServerStartMain.rlinecnt++; 
//							if (ServerStartMain.rlinecnt == 3) ServerStartMain.rlinecnt = 0;

							} catch (Exception e) { 
								e.getStackTrace(); 
								} break;
				}
				break;
		} 
  
  } 

	
  

    public static void main(String[] args) throws IOException  {
    	//byteArrayConvertToImageFile(String location, String type,byte[] ArrayByte, int size); //format
    	
    	
			Random random = new Random();
		      byte[] hi = new byte[387000];  //img test array -RRRGGGBBB
		      int check = 3;
		      for (int k = 1; k <= 387000; k++) {
		         if (check % 3 == 0) {
		            hi[k - 1] = (byte) (random.nextInt(10) + 10);
		         } else if (check % 3 == 1) {
		            hi[k - 1] = (byte) (random.nextInt(10) + 100);
		         } else {
		            hi[k - 1] = (byte) (random.nextInt(10) + 200);
		         }

		         if (k % 860 == 0) {
		            check++;
		         }
		      }
		      
		      
		      byte[] bye = new byte[387000];  //img test array - BGRBGR
		      int cnt = 3;
		      for (int k = 1; k <= 387000; k++) {
		         if (cnt % 3 == 0) {
		            bye[k-1] = (byte) 250;
		            cnt++;
		         } else if (cnt % 3 == 1) {
		            bye[k-1] = (byte) 150;
		            cnt++;
		         } else {
		            bye[k-1] = (byte) 50;
		            cnt++;
		         }
		      }
		      
		      byte[] bye1 = new byte[2580];  //line test array
		      int cnt1 = 3;
		      for (int k = 1; k <= 2580; k++) {
		         if (cnt1 % 3 == 0) {
		            bye1[k-1] = (byte) 250;
		            cnt1++;
		         } else if (cnt1 % 3 == 1) {
		            bye1[k-1] = (byte)150;
		            cnt1++;
		         } else {
		            bye1[k-1] = (byte) 50;
		            cnt1++;
		         }
		      }
    	
    	
    	RelocationByteArray rba1;
		rba1 = new RelocationByteArray(hi);
    	
		byte[] hi5 = new byte[387000];
    	BytetoBmp.encodeToBitmap(hi5, new File("D:\\RGBTest1.bmp"));  //img test - RRRGGGBBB
    	//BytetoBmp.encodeToBitmap(rba1.StartRelocateByteArray(), new File("D:\\RGBTest1.bmp"));  //img test - RRRGGGBBB
    	
    	
//    	BGR_Array bgr;
//    	bgr = new BGR_Array(bye);
//    	
//    	BytetoBmp.encodeToBitmap(bgr.addZeroArray(), new File("D:\\RGBTest3.bmp")); //img test - BGRBGR
//    	
//    	
//    	BGRtoTxt bgrline;
//    	bgrline = new BGRtoTxt(bye1);
//    		
//    	FileWriter w = new FileWriter("D:\\r-line-0k.txt");  //line test - after
//    	for (int a = 0; a < 2580; a++) w.write((char)bgrline.LineRelocateBGR()[a] + "\r\n");
//		w.close();
//		
//		
//		FileWriter w1 = new FileWriter("D:\\r-line-l.txt");  //line test - before
//		for (int a = 0; a < 2580; a++) w1.write((char)bye1[a] + "\r\n");
//		w1.close();
    }
}