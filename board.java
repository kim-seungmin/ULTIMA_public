package sum01;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class board  extends Thread{								//전체 보드 부모 클래스					
	JSONObject u_msg;
	InetAddress u_ip;
	DatagramSocket u_lrecv;
	int ch = 0;
	int u_port;
	int u_id;
	int TimeOut = 1000;
	int waitTime = 200;
	int boardPort = 5000;
	JSONArray array = new JSONArray();
	JSONArray ErrorBoard = new JSONArray();
	JSONObject obj = new JSONObject();
	byte[] recvmsg;
	int[] err1 = null;
	int flag = 0;

	

	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;

	BoardNum num = new BoardNum();
	String IntegratedBoardIP = num.IntegratedBoardIP;				//전체보드 ip
	String[] FeedBoardIP = num.FeedBoardIP;
	String[] LightingBoardIP = num.LightingBoardIP;
	String[] SortBoardIP = num.SortBoardIP;
	public byte[] sendToBoard(byte[] mes, String ip) {         //보드 전송부분
		Socket socket = null;
		byte[] readbyte = new byte[1024];
		try {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			socket = new Socket();
			socket.setSoTimeout(TimeOut);
			SocketAddress socketAddress = new InetSocketAddress(ip,boardPort);
			socket.connect(socketAddress ,TimeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			
			os.write(mes);
			os.flush();
			System.out.println("send to "+socket.toString());
			
//			try {
//				Thread.sleep(80);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			int blen=is.read(readbyte);
//			for(int i=0;i<readbyte.length;i++) {
//				System.out.print(" ("+i+"): "+readbyte[i]);
//			}
			System.out.println("recived from: "+readbyte[6]+"."+readbyte[7]+" success flag: "+readbyte[9]);
			
		}catch(IOException e) {
			e.printStackTrace();System.out.println("Error: "+e);
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	 
			return null;
		}finally {
			try {
				socket.close();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 			
		}
		return readbyte;
	}
	
	public byte[] sendToLine(byte[] mes, String ip) { // 보드 전송부분
		Socket socket = null;

		try {
			socket = new Socket();
			// socket.setSoTimeout(TimeOut);
			SocketAddress socketAddress = new InetSocketAddress(ip, 5000);
			socket.connect(socketAddress, 100);
			byte[] bytes = new byte[2580];
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			System.out.println(socket.toString());
			os.write(mes);
			os.flush();
			System.out.println("보드보냄");
			is.read(bytes);
			
			System.arraycopy(bytes, 16, bytes, 0, 2580);
 
			socket.close();
			return bytes;
		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error: " + e);
			return null;
		}
 
	}
	
	public byte[] sendToBoardLong(byte[] mes, String ip) { // 보드 전송부분
		Socket socket = null;

		try {
			socket = new Socket();
			socket.setSoTimeout(10000);
			SocketAddress socketAddress = new InetSocketAddress(ip, 5000);
			socket.connect(socketAddress, 100);
			byte[] bytes = new byte[2600];
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			System.out.println(socket.toString());
			os.write(mes);
			os.flush();
			System.out.println("보드보냄");
			is.read(bytes);
 
			socket.close();
			return bytes;
		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error: " + e);
			return null;
		}
 
	}
	
	static String getTime(){
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss:ms]");
		return f.format(new Date());
	}


	public void sendToLWGM(DatagramSocket lrecv, InetAddress ip, int port, String msg) {					//웹서버로 웅답 보네는 함수
		DatagramPacket sendPacket = null;
		try {
			sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ip, port);
			lrecv.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] sendToImage(byte[] mes, String ip) {
		Socket socket = null;
		byte[] bytes = new byte[387000];
		byte[] zero = new byte[1310];
		try { 
			socket = new Socket();
			socket.setSoTimeout(4000);
			SocketAddress socketAddress = new InetSocketAddress(ip, 5001);
			socket.connect(socketAddress, 2000);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			byte[] hi = new byte[1310];
			os.write(mes);
			os.flush();
			for (int i = 1; i <=300; i++) {	
		
			//	System.out.println("/");
			//	System.out.println(i);
			//	System.out.println("/");
				
			//	System.out.println(socket.toString());
				
				is.read(hi);
		//		System.out.println((byte)i);
			//	System.out.println(hi[13]);
					
		//		System.out.println("보드보냄");

//				

//						
//			
//			
				if (hi[13] != (byte)i) {
					System.arraycopy(zero, 16, bytes, 0 + (1290 * (i - 1)), 1290);
					System.out.println("1번실패");
					if(i==300) {
						System.out.println(i + " " + i);
						socket.close();
						return bytes;
						
					}
					

					if (hi[13] != (byte)++i) {
						System.arraycopy(zero, 16, bytes, 0 + (1290 * (i - 1)), 1290);
						System.out.println("2번실패");
						if(i==300) {
							System.out.println(i + " " + i);
							socket.close();
							return bytes;
							
						}
						
						
						if (hi[13] != (byte)++i) {
							System.out.println("3번실패");
							socket.close();
							return null;

						} else {

							System.arraycopy(hi, 16, bytes, 0 + (1290 * (i -1)), 1290);
							System.out.println("2번실패후성공");

						}
					} else {

						System.arraycopy(hi, 16, bytes, 0 + (1290 * (i -1 )), 1290);
						System.out.println("1번실패후성공");

					}

				} else {

					System.arraycopy(hi, 16, bytes, 0 + (1290 * (i -1)), 1290);
			//		System.out.println("바로성공");
					

				}

			}
			
		//	System.out.println("\n"+getTime());
			socket.close();
			
		} catch (Exception e) {
			ServerStartMain.Busyflag[0] = false;
			ServerStartMain.Busyflag[1] = false;
		e.printStackTrace();
			return null;
		}
		// socket.close();
		return bytes;

		}
	
}

