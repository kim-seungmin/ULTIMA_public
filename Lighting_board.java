package sum01;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.DriverManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import test01.BoardNum;

class Lighting_board extends board {
	public Lighting_board(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id) {
		this.u_msg = m_msg;
		this.u_ip = m_ip;
		this.u_port = m_port;
		this.u_id = m_id;
		this.u_lrecv = m_lrecv;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		byte FR; // 앞뒤
		System.out.println(u_msg.toString() + u_ip + u_port);		
		switch (u_msg.get("MsgName").toString()) {
		case "LampBGSet":
			FR=(byte)Integer.parseInt(u_msg.get("Location").toString());
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			JSONArray NumList = ((JSONArray) u_msg.get("LampRGBOnOffValue"));
			JSONParser parser = new JSONParser();
			try {
				NumList = (JSONArray)parser.parse(u_msg.get("LampRGBOnOffValue").toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			switch (u_msg.get("Select").toString()) {
			case "0":
					byte[] msgTop = { 0x02, 0, 0, 36, 3, (byte) (FR+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 6, 
							0x00, 0x10, 0x00, (byte) Integer.parseInt((NumList.get(1).toString())),
							0x00, 0x20, 0x00, (byte) Integer.parseInt((NumList.get(2).toString())),
							0x00, 0x11, 0x00, (byte) Integer.parseInt((NumList.get(3).toString())),
							0x00, 0x21, 0x00, (byte) Integer.parseInt((NumList.get(4).toString())),
							0x00, 0x12, 0x00, (byte) Integer.parseInt((NumList.get(5).toString())),
							0x00, 0x22, 0x00, (byte) Integer.parseInt((NumList.get(6).toString())),
							0x03, 0, 0, 0 };
					recvmsg = sendToBoard(msgTop, LightingBoardIP[FR]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Light-" + Integer.toString(FR + 1));
					}
				
				break;
			case "1":
					byte[] msgMid = { 0x02, 0, 0, 36, 3, (byte) (FR+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 6, 
							0x00, 0x13, 0x00, (byte) Integer.parseInt((NumList.get(1).toString())),
							0x00, 0x23, 0x00, (byte) Integer.parseInt((NumList.get(2).toString())),
							0x00, 0x14, 0x00, (byte) Integer.parseInt((NumList.get(3).toString())),
							0x00, 0x24, 0x00, (byte) Integer.parseInt((NumList.get(4).toString())),
							0x00, 0x15, 0x00, (byte) Integer.parseInt((NumList.get(5).toString())),
							0x00, 0x25, 0x00, (byte) Integer.parseInt((NumList.get(6).toString())),
							0x03, 0, 0, 0 };
					recvmsg = sendToBoard(msgMid, LightingBoardIP[FR]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Light-" + Integer.toString(FR+1));
					}
				
				break;
			case "2":
					byte[] msgBot = { 0x02, 0, 0, 36, 3, (byte) (FR+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 6, 
							0x00, 0x16, 0x00, (byte) Integer.parseInt((NumList.get(1).toString())),
							0x00, 0x26, 0x00, (byte) Integer.parseInt((NumList.get(2).toString())),
							0x00, 0x17, 0x00, (byte) Integer.parseInt((NumList.get(3).toString())),
							0x00, 0x27, 0x00, (byte) Integer.parseInt((NumList.get(4).toString())),
							0x00, 0x18, 0x00, (byte) Integer.parseInt((NumList.get(5).toString())),
							0x00, 0x28, 0x00, (byte) Integer.parseInt((NumList.get(6).toString())),
							0x03, 0, 0, 0 };
					recvmsg = sendToBoard(msgBot, LightingBoardIP[FR]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Light-" + Integer.toString(FR+1));
					}
				
				break;
			}
			if (ErrorBoard.isEmpty()) {
				ErrorBoard.add(0);
				obj.put("ResponseValue", 1);
				obj.put("NumOfErrorBoard", 0);
				obj.put("Reserved", 0);
			} else {
				obj.put("ResponseValue", 0);
				obj.put("NumOfErrorBoard", ErrorBoard.size());
			}
			obj.put("ErrorBoard", ErrorBoard);
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "LightAlarmGet":
			JSONArray Status = new JSONArray();
			JSONArray Status2 = new JSONArray();
			JSONArray total = new JSONArray();
			JSONArray errorboad = new JSONArray();
			u_msg.put("Reserved", null);
			u_msg.put("ResponseValue",1);
			int err[];
			err = new int[] {1,1,1,1,1,1,1,1,1,1,1};
				byte[] msg = { 0x02, 0, 0, 12, 3, (byte)  1, 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg, LightingBoardIP[0]);
				if(recvmsg == null||recvmsg[9]==0) {
					u_msg.put("ResponseValue",0);
					u_msg.put("AlarmStatus",0);
					errorboad.add(1);
					for(int i=0; i<11; i++) {
					Status.add("?");
					}
				}
				else if (recvmsg[11] == 0) {
					u_msg.put("AlarmStatus",0);
					errorboad.add(1);
					for(int j=0;j<recvmsg[15];j++) {						
						switch(recvmsg[j*4+17]) {
						case(0x10):
							err[0]=recvmsg[j*4+19];
						break;
						case(0x11):
							err[1]=recvmsg[j*4+19];
						break;
						case(0x20):
							err[2]=recvmsg[j*4+19];
						break;
						case(0x21):
							err[3]=recvmsg[j*4+19];
						break;
						case(0x22):
							err[4]=recvmsg[j*4+19];
						break;
						case(0x23):
							err[5]=recvmsg[j*4+19];
						break;
						case(0x24):
							err[6]=recvmsg[j*4+19];
						break;
						case(0x25):
							err[7]=recvmsg[j*4+19];
						break;
						case(0x26):
							err[8]=recvmsg[j*4+19];
						break;
						case(0x27):
							err[9]=recvmsg[j*4+19];
						break;
						case(0x28):
							err[10]=recvmsg[j*4+19];
						break;
						default:
							System.out.println("없는 레지값");
						break;
						}
						
					}
					for(int k=0;k<11;k++) {
						System.out.println("k: "+k+" 값: "+err[k]);
						Status.add(err[k]);
					}
					
				} 	
				else if (recvmsg[11] == 1) {
					u_msg.put("AlarmStatus",1);

				} 	
				total.add(Status);
				
				int err2[];
				err2 = new int[] {1,1,1,1,1,1,1,1,1,1,1};
				byte[] msg1 = { 0x02, 0, 0, 12, 3, (byte)  2, 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg1, LightingBoardIP[1]);
				if(recvmsg == null||recvmsg[9]==0) {
					u_msg.put("ResponseValue",0);
					u_msg.put("AlarmStatus",0);
					errorboad.add(2);
					for(int i=0; i<11; i++) {
					Status2.add("?");
					}
				}
				else if (recvmsg[11] == 0) {
					u_msg.put("AlarmStatus",0);
					errorboad.add(2);
					for(int j=0;j<recvmsg[15];j++) {						
						switch(recvmsg[j*4+17]) {
						case(0x10):
							err2[0]=recvmsg[j*4+19];
						break;
						case(0x11):
							err2[1]=recvmsg[j*4+19];
						break;
						case(0x20):
							err2[2]=recvmsg[j*4+19];
						break;
						case(0x21):
							err2[3]=recvmsg[j*4+19];
						break;
						case(0x22):
							err2[4]=recvmsg[j*4+19];
						break;
						case(0x23):
							err2[5]=recvmsg[j*4+19];
						break;
						case(0x24):
							err2[6]=recvmsg[j*4+19];
						break;
						case(0x25):
							err2[7]=recvmsg[j*4+19];
						break;
						case(0x26):
							err2[8]=recvmsg[j*4+19];
						break;
						case(0x27):
							err2[9]=recvmsg[j*4+19];
						break;
						case(0x28):
							err2[10]=recvmsg[j*4+19];
						break;
						default:
							System.out.println("없는 레지값");
						break;
						}
						
					}
					for(int j=0;j<11;j++) {
						System.out.println("j: "+j+" 값: "+err2[j]);
						Status2.add(err2[j]);
					}
					
				} 
				else if (recvmsg[11] == 1) {
					u_msg.put("AlarmStatus",1);

					
				} 
				total.add(Status2);
				
			
			
			u_msg.put("BoardNum",errorboad);
			u_msg.put("NumOfBoard",errorboad.size());
			u_msg.put("LightAlarmStatus",total);
			array.add(u_msg);
			
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
		default:
			System.out.println("알수없는 메시지" + u_msg.get("MsgName").toString());
			break;
		}
	}
}