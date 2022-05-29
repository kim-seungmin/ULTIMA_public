package sum01;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SubBThread extends board {
	int SortNum;
	int FeedNum;
	int PortNum = 5002;
	JSONArray array = new JSONArray();
	JSONArray array1 = new JSONArray();
	JSONArray array2 = new JSONArray();
	JSONArray array3 = new JSONArray();
	JSONArray array4 = new JSONArray();
	byte x = 0;
	byte y = 0;
	byte z = 0;
	int i = 0;
	byte[] UMLI = { 0x02, 0, 0x00, 0x0c, x, y, 0x00, 0x02, 11, 0, z, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };

	public SubBThread(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id,
			BoardNum m_num) {
		this.u_msg = m_msg;
		this.u_ip = m_ip;
		this.u_port = m_port;
		this.u_id = m_id;
		this.u_lrecv = m_lrecv;
		SortNum = m_num.SortBoardNum;
		FeedNum = m_num.FeedBoardNum;
		this.num = m_num;
	}

	@Override
	public void run() {
		switch (u_msg.get("MsgName").toString()) {
		case "PingGet":
			u_msg.put("ResponseValue", 1);
			u_msg.put("Reserved", null);
			ThrowPing();
			if (array1.size() == 0) {
				if (array2.size() == 0) {
					if (array3.size() == 0) {
						if (array4.size() == 0) {
							u_msg.put("AlarmStatus", 1);
						} else
							u_msg.put("AlarmStatus", 0);
					} else
						u_msg.put("AlarmStatus", 0);
				} else
					u_msg.put("AlarmStatus", 0);
			} else
				u_msg.put("AlarmStatus", 0);

			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		default:
			System.out.println("알수없는 메시지" + u_msg.get("MsgName").toString());
			break;
		}
	}

	public void sendSortBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			socket.setSoTimeout(300);
			byte[] bytes = new byte[1024];
			SocketAddress socketAddress = new InetSocketAddress(SortBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, 100);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 4;
			y = (byte) BoardNum;
			z = (byte) u_id;
			UMLI[4] = x;
			UMLI[5] = y;
			UMLI[10] = z;

			os.write(UMLI);
			os.flush();
			is.read(bytes);
			for (int j = 0; j < 20; j++) {
				System.out.print(bytes[j]);

			}
			socket.close();
			for (int k = 1; k <= SortNum; k++) {
				if (bytes[6] == 4 && bytes[7] == k) {
					System.out.println("\n" + getTime() + "10.0.4." + k + " BOARD OK!");
				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= SortNum; k++) {
				if (BoardNum == k) {
					System.out.println("\n" + getTime() + "10.0.4." + k + " BOARD fail");
					array1.add(k);
					u_msg.put("NumOfSortBoard", array1);
				}
			}
		}
	}

	public void sendLightingBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			socket.setSoTimeout(300);
			byte[] bytes = new byte[1024];
			SocketAddress socketAddress = new InetSocketAddress(LightingBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, 100);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 3;
			y = (byte) BoardNum;
			z = (byte) u_id;
			UMLI[4] = x;
			UMLI[5] = y;
			UMLI[10] = z;
			os.write(UMLI);
			os.flush();
			is.read(bytes);
			for (int j = 0; j < 20; j++) {
				System.out.print(bytes[j]);
			}
			socket.close();
			for (int k = 1; k <= 2; k++) {
				if (bytes[6] == 3 && bytes[7] == k) {
					System.out.println("\n" + getTime() + "10.0.3." + k + " BOARD OK!");
				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= 2; k++) {
				if (BoardNum == k) {
					System.out.println("\n" + getTime() + "10.0.3." + k + " BOARD fail");
					array2.add(k);
					u_msg.put("NumOfLightBoard", array2);
				}
			}
		}
	}

	public void sendFeedBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			socket.setSoTimeout(300);
			byte[] bytes = new byte[1024];
			SocketAddress socketAddress = new InetSocketAddress(FeedBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, 100);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 2;
			y = (byte) BoardNum;
			z = (byte) u_id;
			UMLI[4] = x;
			UMLI[5] = y;
			UMLI[10] = z;
			os.write(UMLI);
			os.flush();
			is.read(bytes);
			for (int j = 0; j < 20; j++) {
				System.out.print(bytes[j]);
			}
			socket.close();
			for (int k = 1; k <= FeedNum; k++) {
				if (bytes[6] == 2 && bytes[7] == k) {
					System.out.println("\n" + getTime() + "10.0.2." + k + " BOARD OK!");
				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= FeedNum; k++) {
				if (BoardNum == k) {
					System.out.println("\n" + getTime() + "10.0.2." + k + " BOARD fail");
					array3.add(k);
					u_msg.put("NumOfFeedBoard", array3);
				}
			}
		}
	}

	public void sendIntegrateBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			socket.setSoTimeout(300);
			byte[] bytes = new byte[1024];
			SocketAddress socketAddress = new InetSocketAddress(IntegratedBoardIP, PortNum);
			socket.connect(socketAddress, 100);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 1;
			y = 1;
			z = (byte) u_id;
			UMLI[4] = x;
			UMLI[5] = y;
			UMLI[10] = z;
			os.write(UMLI);
			os.flush();
			is.read(bytes);
			for (int j = 0; j < 20; j++) {
				System.out.print(bytes[j]);
			}
			socket.close();
			if (bytes[6] == 1 && bytes[7] == 1) {
				System.out.println("\n" + getTime() + "10.0.1.1 BOARD OK!");
			}
		} catch (Exception e) {
			if (BoardNum == 1) {
				System.out.println("\n" + getTime() + "10.0.1.1 BOARD fail");
				array4.add(1);
				u_msg.put("NumOfIntBoard", array4);
			}
		}
	}

	static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}

	public void ThrowPing() {
		try {
			sendIntegrateBoard(1);
			for (int k = 1; k <= FeedNum; k++) {
				sendFeedBoard(k);
			}
			for (i = 0; i < 2; i++) {
				sendLightingBoard(i + 1);
			}
			for (int k = 1; k <= SortNum; k++) {
				sendSortBoard(k);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
		
	