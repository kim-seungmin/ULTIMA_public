package sum01;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Feeding_board extends board {
	int FeedNum;
	int FeedPortNum;

	public Feeding_board(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id, BoardNum m_num) {
		this.u_msg = m_msg;
		this.u_ip = m_ip;
		this.u_port = m_port;
		this.u_id = m_id;
		this.u_lrecv = m_lrecv;
		FeedNum = m_num.FeedBoardNum;
		FeedPortNum = m_num.FeedPortNum;
		this.num =m_num;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void run() {
		System.out.println(u_msg.toString() + u_ip + u_port+"1124214214");
		switch (u_msg.get("MsgName").toString()) {
		case "AllFeedStartStopSet":			
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			for (int i = 0; i < FeedNum; i++) {
				byte[] msg = { 0x02, 0, 0, 16, 2, (byte) (i + 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x0, 0x10, 0,	(byte) Integer.parseInt(u_msg.get("FeedStartStop").toString()), 0x03, 0, 0, 0 }; // 바이트배열
				recvmsg = sendToBoard(msg, FeedBoardIP[i]);
				if (recvmsg == null || recvmsg[9] == 0) {
					ErrorBoard.add("Feed-" + Integer.toString(i + 1));
				}
			}
			if (ErrorBoard.isEmpty()) {
				ErrorBoard.add(0);
				obj.put("ResponseValue", 1);
				obj.put("NumOfErrorBoard", 0);
			} else {
				obj.put("ResponseValue", 0);
				obj.put("Reserved", null);
				obj.put("NumOfErrorBoard", ErrorBoard.size());
			}
			obj.put("ErrorBoard", ErrorBoard);
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "FeedSet":
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1);
			try {
				Class.forName("org.mariadb.jdbc.Driver");
				DataBase RC = new DataBase();
				conn=RC.returnConn();
				String query = "SELECT COUNT(feeding_PortNum) AS num FROM tb_ultima_feeding WHERE feeding_Order = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, u_msg.get("Order").toString());
				rs = pstmt.executeQuery();				
				rs.next();
				int feed[] = new int[rs.getInt("num")];
				int count=0;
				
						
				query = "SELECT feeding_PortNum FROM tb_ultima_feeding WHERE feeding_Order = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, u_msg.get("Order").toString());
				rs = pstmt.executeQuery();
				
				while (rs.next()) {					
					feed[count]=rs.getInt("feeding_PortNum")-1;
					count++;					
				}
				Arrays.sort(feed);
				
				
				byte[] FA1Smsg = new byte[300];
				byte[] FA2Smsg = new byte[300];
				byte[] FASet = {0x02, 0, 0, 100, 2, 1, 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 100};
				int FA1C=0;
				int FA2C=0;
				
				System.arraycopy(FASet, 0, FA1Smsg, 0, FASet.length);
				System.arraycopy(FASet, 0, FA2Smsg, 0, FASet.length);
				FA2Smsg[5]=2;
				
				for(int feedport : feed) {					
					if(feedport<7) {
						FA1Smsg[FA1C*8+16]=0;
						FA1Smsg[FA1C*8+17]=(byte)(0x20 + (feedport%7));
						FA1Smsg[FA1C*8+18]=0;
						FA1Smsg[FA1C*8+19]=(byte) Integer.parseInt(u_msg.get("FeedOnOff").toString());
						FA1Smsg[FA1C*8+20]=0;
						FA1Smsg[FA1C*8+21]=(byte)(0x30 + (feedport%7));
						FA1Smsg[FA1C*8+22]=0;
						FA1Smsg[FA1C*8+23]=(byte) Integer.parseInt(u_msg.get("FeedIntensity").toString());
						FA1Smsg[FA1C*8+24]=0;
						FA1Smsg[FA1C*8+25]=0x11;
						FA1Smsg[FA1C*8+26]=0;
						FA1Smsg[FA1C*8+27]=(byte) Integer.parseInt(u_msg.get("AutoStatus").toString());
						FA1Smsg[FA1C*8+28]=0x03;
						FA1Smsg[FA1C*8+29]=0;
						FA1Smsg[FA1C*8+30]=0;
						FA1Smsg[FA1C*8+31]=0;
						FA1C++;
					}else {
						FA2Smsg[FA2C*8+16]=0;
						FA2Smsg[FA2C*8+17]=(byte)(0x20 + (feedport%7));
						FA2Smsg[FA2C*8+18]=0;
						FA2Smsg[FA2C*8+19]=(byte) Integer.parseInt(u_msg.get("FeedOnOff").toString());
						FA2Smsg[FA2C*8+20]=0;
						FA2Smsg[FA2C*8+21]=(byte)(0x30 + (feedport%7));
						FA2Smsg[FA2C*8+22]=0;
						FA2Smsg[FA2C*8+23]=(byte) Integer.parseInt(u_msg.get("FeedIntensity").toString());
						FA2Smsg[FA2C*8+24]=0;
						FA2Smsg[FA2C*8+25]=0x11;
						FA2Smsg[FA2C*8+26]=0;
						FA2Smsg[FA2C*8+27]=(byte) Integer.parseInt(u_msg.get("AutoStatus").toString());
						FA2Smsg[FA2C*8+28]=0x03;
						FA2Smsg[FA2C*8+29]=0;
						FA2Smsg[FA2C*8+30]=0;
						FA2Smsg[FA2C*8+31]=0;
						FA2C++;
					}					
				}
				System.out.println("FA1: "+FA1C+" FA2: "+FA2C+ " FL: "+feed.length);
				if(FA1C!=0) {
					byte[] FASend = new byte[24+(FA1C*2*4)];
					System.arraycopy(FA1Smsg, 0, FASend, 0, FASend.length);
					FASend[3]= (byte) (FA1C*2*4+16);
					FASend[15]=(byte) (FA1C*2+1);
					recvmsg = sendToBoard(FASend, FeedBoardIP[0]);
					System.out.println("전송");
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-1");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
				}
				if(FA2C!=0) {
					byte[] FASend = new byte[24+(FA2C*2*4)];
					System.arraycopy(FA1Smsg, 0, FASend, 0, FASend.length);
					FASend[3]= (byte) (FA2C*2*4+16);
					FASend[15]=(byte) (FA2C*2+1);
					recvmsg = sendToBoard(FASend, FeedBoardIP[1]);
					System.out.println("전송");
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-2");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
				}
				Thread.sleep(300);
				if(FA1C==0) {
					byte[] msg = { 0x02, 0, 0, 16, 2, 1, 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x00, 0x11, 0, (byte) Integer.parseInt(u_msg.get("AutoStatus").toString()), 0x03, 0, 0, 0 };
					recvmsg = sendToBoard(msg, FeedBoardIP[0]);
					System.out.println("전송");
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-1");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
				}
				if(FA2C==0&&num.FeedBoardNum==2) {
					byte[] msg = { 0x02, 0, 0, 16, 2, 2, 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x00, 0x11, 0, (byte) Integer.parseInt(u_msg.get("AutoStatus").toString()), 0x03, 0, 0, 0 };
					recvmsg = sendToBoard(msg, FeedBoardIP[1]);
					System.out.println("전송");
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-2");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
				}
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				obj.put("ErrorBoard", ErrorBoard);
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			} catch (Exception e) {
				System.err.println("DB 연결 오류");
				System.err.println(e.getMessage());
			} finally {
				try {
					rs.close();
					pstmt.close();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			break;
			
		case "FeedAdvancedSet":
			if(Integer.parseInt(u_msg.get("FeedPort").toString())>num.FeedPortNum) {
				System.out.println("wrong port");
				obj.put("MsgName", "SetResult");
				obj.put("Reserved", null);
				obj.put("ResponseValue", "0");
				ErrorBoard.add("Feed-"+Integer.toString(Integer.parseInt(u_msg.get("FeedPort").toString())/7+1));
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				obj.put("ErrorBoard", ErrorBoard);
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;
			}
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", "1");
			int AFS=Integer.parseInt(u_msg.get("FeedPort").toString());
			System.out.println("port"+AFS);
			AFS--;
			byte[] FASmsg = { 0x02, 0, 0, 20, 2, (byte) (AFS/7+ 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 2, 0x00,(byte)(0x20+AFS%7),0x00, (byte) Integer.parseInt(u_msg.get("FeedOnOff").toString()),0x00,(byte)(0x40+AFS%7),0x00, (byte) Integer.parseInt(u_msg.get("FeedOffSet").toString()), 0x03, 0, 0, 0 }; // 바이트배열
			recvmsg = sendToBoard(FASmsg, FeedBoardIP[AFS/7]);
			if (recvmsg == null || recvmsg[9] == 0) {
				ErrorBoard.add("Feed-"+Integer.toString(AFS/7+1));
				obj.put("Reserved", 0);
				obj.put("ResponseValue", 0);
			}
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			obj.put("ErrorBoard", ErrorBoard);
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "FeedAdvancedGet": 
			JSONArray FeedVibration = new JSONArray();
			boardPort=5001;
			try {
				Class.forName("org.mariadb.jdbc.Driver");
				DataBase RC = new DataBase();
				conn=RC.returnConn();
				String query = "SELECT COUNT(feeding_PortNum) AS num FROM tb_ultima_feeding where feeding_Order !=0 ";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();				
				rs.next();
				int feed[] = new int[rs.getInt("num")];
				int count=0;			
						
				query = "SELECT feeding_PortNum FROM tb_ultima_feeding where feeding_Order !=0 ";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();				
				while (rs.next()) {					
					feed[count]=rs.getInt("feeding_PortNum")-1;
					count++;					
				}
				Arrays.sort(feed);				
				byte[] FA1Smsg = new byte[300];
				byte[] FA2Smsg = new byte[300];
				byte[] FASet = {0x02, 0, 0, 100, 2, 1, 0, 2, 1, 0, (byte) u_id, 0, 0, 0, 0, 100};
				int FA1C=0;
				int FA2C=0;
				
				System.arraycopy(FASet, 0, FA1Smsg, 0, FASet.length);
				System.arraycopy(FASet, 0, FA2Smsg, 0, FASet.length);
				FA2Smsg[5]=2;
				
				for(int feedport : feed) {					
					if(feedport<7) {
						FA1Smsg[FA1C*4+16]=0x10;
						FA1Smsg[FA1C*4+17]=(byte)(0x90 + (feedport%7));
						FA1Smsg[FA1C*4+18]=0;
						FA1Smsg[FA1C*4+19]=0;
						FA1Smsg[FA1C*4+20]=0x03;
						FA1Smsg[FA1C*4+21]=0;
						FA1Smsg[FA1C*4+22]=0;
						FA1Smsg[FA1C*4+23]=0;
						FA1C++;
					}else {
						FA2Smsg[FA2C*4+16]=0x10;
						FA2Smsg[FA2C*4+17]=(byte)(0x90 + (feedport%7));
						FA2Smsg[FA2C*4+18]=0;
						FA2Smsg[FA2C*4+19]=0;
						FA2Smsg[FA2C*4+20]=0x03;
						FA2Smsg[FA2C*4+21]=0;
						FA2Smsg[FA2C*4+22]=0;
						FA2Smsg[FA2C*4+23]=0;
						FA2C++;
					}					
				}
				System.out.println("FA1: "+FA1C+" FA2: "+FA2C+ " FL: "+feed.length);
				if(FA1C!=0) {
					byte[] FASend = new byte[20+(FA1C*4)];
					System.arraycopy(FA1Smsg, 0, FASend, 0, FASend.length);
					FASend[3]= (byte) (FA1C*4+12);
					FASend[15]=(byte) (FA1C);
					recvmsg = sendToBoard(FASend, FeedBoardIP[0]);
					System.out.println("전송");					
					if (recvmsg == null || recvmsg[9] == 0) {
						for(int i=0;i<FA1C;i++)FeedVibration.add("?");
					} else {
						for(int i=0;i<FA1C;i++) {
							FeedVibration.add(((int) recvmsg[18+i*4]) * 0x100 + (Byte.toUnsignedInt(recvmsg[19+i*4])));
						}
					}
					
				}
				if(FA2C!=0) {
					byte[] FASend = new byte[20+(FA2C*4)];
					System.arraycopy(FA1Smsg, 0, FASend, 0, FASend.length);
					FASend[3]= (byte) (FA2C*4+12);
					FASend[15]=(byte) (FA2C);
					recvmsg = sendToBoard(FASend, FeedBoardIP[1]);
					System.out.println("전송");
					if (recvmsg == null || recvmsg[9] == 0) {
						for(int i=0;i<FA2C;i++)FeedVibration.add("?");
					} else {
						for(int i=0;i<FA2C;i++) {
							FeedVibration.add(((int) recvmsg[18+i*4]) * 0x100 + (Byte.toUnsignedInt(recvmsg[19+i*4])));
						}
					}
				}
				u_msg.put("FeedVibration", FeedVibration);
				array.add(u_msg);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			} catch (Exception e) {
				System.err.println("DB 연결 오류");
				System.err.println(e.getMessage());
			} finally {
				try {
					rs.close();
					pstmt.close();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			break;

		case "FeedAlarmGet":
			JSONArray Status = new JSONArray();
			JSONArray total = new JSONArray();
			JSONArray errorboad = new JSONArray();		
			JSONArray total1 = new JSONArray();
			JSONArray total10 = new JSONArray();
			JSONArray errorport = new JSONArray();
			JSONArray Status6 = new JSONArray();
			u_msg.put("ResponseValue", 1);
			u_msg.put("Reserved", null);

			for(int k=1; k<=FeedNum; k++) {
				Status.clear();
				Status6.clear();
				errorport.clear();
				int err[];
				err = new int[] { 1, 1, 1, 1, 1, 1, 1, 1 };

				byte[] msg = { 0x02, 0, 0, 12, 2, (byte) k, 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg, FeedBoardIP[k-1]);
				if (recvmsg == null || recvmsg[9] == 0) {
					u_msg.put("ResponseValue", 0);
					u_msg.put("AlarmStatus", 0);
					errorboad.add(k);
					for (int i = 0; i < 8; i++) {
						Status.add("?");
					}
				} else if (recvmsg[11] == 0) {
					u_msg.put("AlarmStatus", 0);
					errorboad.add(k);
					for (int i = 0; i < recvmsg[15]; i++) {
						switch (recvmsg[i * 4 + 17]) {
						case (0x10):
							err[0] = recvmsg[i * 4 + 19];

							break;
						case (0x11):
							err[1] = recvmsg[i * 4 + 19];

							break;
						case (0x12):
							err[2] = recvmsg[i * 4 + 19];

							break;
						case (0x20):
							err[3] = recvmsg[i * 4 + 19];

							break;
						case (0x30):
							err[4] = recvmsg[i * 4 + 19];

							break;
						case (0x31):
							err[5] = recvmsg[i * 4 + 19];

							break;
						case (0x32):
							err[6] = recvmsg[i * 4 + 19];

							break;
						case (0x33):
							err[7] = recvmsg[i * 4 + 19];

							break;
						default:
							System.out.println("없는 레지값");
						break;
						}
						

					}
					for (int i = 0; i < 8; i++) {
						System.out.println("i: " + i + " 값: " + err[i]);
						Status.add(err[i]);
						
					}

				}else if (recvmsg[11] == 1) {
					u_msg.put("AlarmStatus", 1);

				}

				total.add(Status);
			

   			int err4[];
			err4 = new int[] { 1, 1 };
			byte[] msg5 = { 0x02, 0, 0, 12, 2, (byte) k, 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
			recvmsg = sendToBoard(msg5, FeedBoardIP[k-1]);
			if (recvmsg == null || recvmsg[9] == 0) {
				for (int i = 0; i < 2; i++) {
					Status6.add("?");
				}
			} else if (recvmsg[11] == 0) {
				for (int j=0; j<recvmsg[15]; j++) {
					switch (recvmsg[j*4+17]) {
					case (0x40):
						err4[0] =  recvmsg[j*4+19];
						errorport.add(1);
						break;
					case (0x41):
						err4[0] = recvmsg[j*4+19];
						errorport.add(2);
						break;
					case (0x42):
						err4[0] =  recvmsg[j*4+19];
						errorport.add(3);

						break;
					case (0x43):
						err4[0] =  recvmsg[j*4+19];
						errorport.add(4);

						break;
					case (0x44):
						err4[0] = recvmsg[j*4+19];
						errorport.add(5);

						break;
					case (0x45):
						err4[0] =  recvmsg[j*4+19];
						errorport.add(6);

						break;
					case (0x46):
						err4[0] =  recvmsg[j*4+19];
						errorport.add(7);

						break;
					case (0x50):
						err4[1] =  recvmsg[j*4+19];
						errorport.add(1);

						break;
					case (0x51):
						err4[1] =  recvmsg[j*4+19];
						errorport.add(2);

						break;
					case (0x52):
						err4[1] =  recvmsg[j*4+19];
						errorport.add(3);

						break;
					case (0x53):
						err4[1] = recvmsg[j*4+19];
						errorport.add(4);

						break;
					case (0x54):
						err4[1] =  recvmsg[j*4+19];
						errorport.add(5);

						break;
					case (0x55):
						err4[1] = recvmsg[j*4+19];
						errorport.add(6);

						break;
					case (0x56):
						err4[1] =  recvmsg[j*4+19];
						errorport.add(7);

						break;
					default:
						System.out.println("없는 레지값");
					break;
					}
				}
					for (int i = 0; i < 2; i++) {
						System.out.println("i: " + i + " 값: " + err4[i]);
						Status6.add(err4[i]);
					}	
			}
			 else if (recvmsg[11] == 1) {
				}
			total1.add(Status6);
			total10.add(errorport);
			}
			
			u_msg.put("NumOfBoard", errorboad.size());
			u_msg.put("BoardNum", errorboad);
			u_msg.put("NumOfPort", errorport.size());
			u_msg.put("PortNum", total10);
			u_msg.put("FeedBAlarmStatus", total);
			u_msg.put("FeedPAlarmStatus", total1);
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "AirPressSet":
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", "1");
			float LValue=Float.parseFloat(u_msg.get("LTAlarmValue").toString());
			float GValue=Float.parseFloat(u_msg.get("GTAlarmValue").toString());
			GValue=GValue*10;
			LValue=LValue*10;
			int AirOnOff=Integer.parseInt(u_msg.get("AirPressAlarmOnOff").toString());
			for(int i=0;i<FeedNum;i++) {
				byte[] ASmsg = { 0x02, 0, 0, 24, 2, (byte) (i+ 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 3, 0x00,0x51,0x00, (byte) LValue,0x00,0x52,0x00, (byte) GValue, 0x00,0x50,0x00, (byte) AirOnOff, 0x03, 0, 0, 0 }; // 바이트배열
				recvmsg = sendToBoard(ASmsg, FeedBoardIP[i]);
				if (recvmsg == null || recvmsg[9] == 0) {
					ErrorBoard.add("Feed-"+Integer.toString(i+1));
					obj.put("Reserved", 0);
					obj.put("ResponseValue", 0);
				}
			}
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			obj.put("ErrorBoard", ErrorBoard);
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "AirPressGet":
			boardPort=5001;
			u_msg.put("ResponseValue", 1);
			u_msg.put("Reserved",1);    		
    		byte[] AGmsg = { 0x02, 0, 0, 16, 2, 1, 0, 2, 1, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x10,(byte) 0xa0,0x00, 0x00, 0x03, 0, 0, 0 }; // 바이트배열
			recvmsg = sendToBoard(AGmsg, FeedBoardIP[0]);
			if (recvmsg == null || recvmsg[9] == 0) {
				u_msg.put("ResponseValue", 0);
				u_msg.put("Reserved",0);
				u_msg.put("AirPressValue", "-");
			}else{
				float tem=Byte.toUnsignedInt(recvmsg[19]);
				tem=(float) (tem/25.5);
				tem=Float.parseFloat(String.format( "%.1f", tem));
				System.out.println(tem);
				u_msg.put("AirPressValue", tem);
			}
			u_msg.put("Reserved", null);			
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
			
		case "FeedSwitchInSet":
			if(Integer.parseInt(u_msg.get("BoardNum").toString())>num.FeedBoardNum) {
				System.out.println("wrong board");
				obj.put("MsgName", "SetResult");
				obj.put("Reserved", null);
				obj.put("ResponseValue", "0");
				ErrorBoard.add("Feed-"+u_msg.get("BoardNum").toString());
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				obj.put("ErrorBoard", ErrorBoard);
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;
			}
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", "0");
				byte[] msg10 = { 0x02, 0, 0, 28, 2, (byte) Integer.parseInt(u_msg.get("BoardNum").toString()), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 4, 0x00, 0x60,
						0, (byte) Integer.parseInt(u_msg.get("DCSW1OnOff").toString()), 0x00, 0x61, 0,
						(byte) Integer.parseInt(u_msg.get("DCSW2OnOff").toString()), 0x00, 0x62, 0,
						(byte) Integer.parseInt(u_msg.get("ACSW1OnOff").toString()), 0x00, 0x63, 0,
						(byte) Integer.parseInt(u_msg.get("ACSW2OnOff").toString()), 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg10, FeedBoardIP[(byte) Integer.parseInt(u_msg.get("BoardNum").toString())-1]);
				if (recvmsg == null || recvmsg[9] == 0) {
					ErrorBoard.add("Feed-" + Integer.toString((byte) Integer.parseInt(u_msg.get("BoardNum").toString())));
				}
			
			if (ErrorBoard.isEmpty()) {
				ErrorBoard.add(0);
				obj.put("ResponseValue", "1");
				obj.put("NumOfErrorBoard", 0);
			} else {
				obj.put("ResponseValue", "0");
				obj.put("NumOfErrorBoard", ErrorBoard.size());
			}
			obj.put("ErrorBoard", ErrorBoard);
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "FeedSwitchOutSet":
			break;
		default:
			System.out.println("알수없는 메시지" + u_msg.get("MsgName").toString());
			break;
		}
	}
}