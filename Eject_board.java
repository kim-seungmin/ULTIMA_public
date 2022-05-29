package sum01;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Eject_board extends board{
	int SortNum;
	String[] eventOccuredArray = new String[8];


	
	public Eject_board(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id, BoardNum m_num) {
		this.u_msg=m_msg;
		this.u_ip=m_ip;
		this.u_port=m_port;
		this.u_id=m_id;
		this.u_lrecv=m_lrecv;
		SortNum = m_num.SortBoardNum;
		this.num=m_num;
	}
	public int LogicNumtoPyshicNum(String LN) {
		int PN=Integer.parseInt(LN.substring(0,LN.length()-1))*2;
		char[] first = LN.substring(LN.length()-1).toCharArray();
		if(first[0] == 'a')PN--;
		return PN;
	}
	
	public String[] registerValueParser(byte value) { 
		int k = 0; 
		String binaryString = Integer.toBinaryString(Byte.toUnsignedInt(value)); 
		while(binaryString.length() % 4 != 0) { 
			binaryString = "0" + binaryString; 
			}
		String[] str = binaryString.split("");
		
		System.out.println("Source : " + binaryString);
		for(int i = 0; i<8; i++) {
		 eventOccuredArray[i] = str[i];
		}

	return eventOccuredArray;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void run(){
		System.out.println(u_msg.toString()+u_ip+u_port);
		switch(u_msg.get("MsgName").toString()) {
		case("AllEjectStartStopSet"):
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1);
			for(int i=0;i<SortNum;i++) {	
				byte[] msg = {0x02,0,0,16,4,(byte) (i+1),0,2,2,0,(byte)u_id,0,0,0,0,1,0x20,0x40,0,(byte) Integer.parseInt(u_msg.get("EjectStartStop").toString()),0x03,0,0,0};	//바이트배열
				if(ServerStartMain.Busyflag[0]==true) {
					System.out.println("sorting board using");
					while(true) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println(i+"Th sort board busy flag: "+ServerStartMain.Busyflag[i]);
						if(ServerStartMain.Busyflag[i]==false) {
							System.out.println(i+"Th sort board busy flag: "+ServerStartMain.Busyflag[i]);
							recvmsg = sendToBoard(msg, num.SortBoardIP[i]);
					    	if(recvmsg==null||recvmsg[9]==0) {
					    		ErrorBoard.add("Sort-"+Integer.toString(i+1));
					    		obj.put("NumOfErrorBoard", 0);
					    		obj.put("ResponseValue", 0);
					    	}
							break;
						}
					}
				}
		    }
		    obj.put("NumOfErrorBoard", ErrorBoard.size());
		    obj.put("ErrorBoard", ErrorBoard);
		    array.add(obj);
		    sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
		   break;	
		case "EjectSet":
			DataBase Con = new DataBase();
			conn=Con.returnConn();
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1);
			try {
				String query = "SELECT COUNT(sorting_Chute) AS num FROM tb_ultima_sorting WHERE sorting_Order = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, Integer.parseInt(u_msg.get("Order").toString()));	
				rs = pstmt.executeQuery();
				byte[] ejectnum=null;
				while(rs.next()) {
					ejectnum= new byte[rs.getInt("num")];															
				}
				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, Integer.parseInt(u_msg.get("Order").toString()));	
				rs = pstmt.executeQuery();
				int cou=0;
				while(rs.next()) {
					ejectnum[cou]= (byte) LogicNumtoPyshicNum(rs.getString("sorting_Chute"));
					cou++;
				}			
				Arrays.sort(ejectnum);
				for(int i=0;i<ejectnum.length;i++) {
					int num = ejectnum[i];
					num++;
					if(i+1!=ejectnum.length && (ejectnum[i]+1)/2==(ejectnum[i+1]+1)/2) {
						byte[] ESmsg = { 0x02, 0, 0, 36, 4, (byte) ((num)/2), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 6, 0x00,(byte) 0x80,0x00,(byte) Integer.parseInt(u_msg.get("Delay").toString()), 0x00,(byte)0x90,0x00,(byte) Integer.parseInt(u_msg.get("Head").toString()), 0x00,(byte)0x91,0x00, (byte) Integer.parseInt(u_msg.get("Hold").toString()),(byte) 0x01,(byte) 0x80,0x00, (byte) Integer.parseInt(u_msg.get("Delay").toString()), 0x01,(byte)0x90,0x00, (byte) Integer.parseInt(u_msg.get("Head").toString()), 0x01,(byte)0x91,0x00, (byte) Integer.parseInt(u_msg.get("Hold").toString()), 0x03, 0, 0, 0 }; // 바이트배열
						recvmsg=sendToBoard(ESmsg, SortBoardIP[(num)/2-1]);					
						if (recvmsg == null || recvmsg[9] == 0) {
							ErrorBoard.add("Sort-"+(num/2));
							obj.put("ResponseValue",0);
							obj.put("Reserved",0);
						}
						i++;
					}else{
						byte[] ESmsg = { 0x02, 0, 0, 24, 4, (byte) ((num)/2), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 3, (byte) ((num)%2),(byte) 0x80,0x00, (byte) Integer.parseInt(u_msg.get("Delay").toString()), (byte) ((num)%2),(byte)0x90,0x00, (byte) Integer.parseInt(u_msg.get("Head").toString()), (byte) ((num)%2),(byte)0x91,0x00, (byte) Integer.parseInt(u_msg.get("Hold").toString()), 0x03, 0, 0, 0 }; // 바이트배열
						recvmsg=sendToBoard(ESmsg, SortBoardIP[(num)/2-1]);					
						if (recvmsg == null || recvmsg[9] == 0) {
							ErrorBoard.add("Sort-"+(num/2));
							obj.put("ResponseValue",0);
							obj.put("Reserved",0);
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	finally {
				try {
					rs.close();
					pstmt.close();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			obj.put("ErrorBoard", ErrorBoard);
	    	obj.put("NumOfErrorBoard", ErrorBoard.size());
		    array.add(obj);
		    sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
		
		case "EjectorSet":
			if(Integer.parseInt(u_msg.get("ChuteNum").toString())>num.SortBoardNum){
				System.out.println("wrong board");
				obj.put("MsgName", "SetResult");
				obj.put("Reserved", null);
				obj.put("ResponseValue", "0");
				ErrorBoard.add("Sort-"+u_msg.get("ChuteNum").toString());
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				obj.put("ErrorBoard", ErrorBoard);
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;
			}
			int chute = Integer.parseInt(u_msg.get("ChuteNum").toString());
			byte[] msg = {0x02,0,0,24,4,(byte) (chute),0,2,2,0,(byte)u_id,0,0,0,0,3,0x20,0x31,0,(byte) Integer.parseInt(u_msg.get("EjectNum").toString()),0x20,0x30,0,(byte) Integer.parseInt(u_msg.get("EjectStartStop").toString()),0x20,0x40,0,(byte) Integer.parseInt(u_msg.get("EjectStartStop").toString()),0x03,0,0,0};	//바이트배열
	    	recvmsg=sendToBoard(msg, SortBoardIP[chute-1]);
	    	obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1);
	    	if(recvmsg==null||recvmsg[9]==0) {
	    		obj.put("Reserved", 0);
	    		obj.put("ResponseValue", 0);
	    		ErrorBoard.add("Sort-"+Integer.toString(chute));
	    	}
	    	obj.put("ErrorBoard", ErrorBoard);
	    	obj.put("NumOfErrorBoard", ErrorBoard.size());
		    array.add(obj);
		    sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
		case "EjectAlarmGet":
			JSONArray total = new JSONArray();
			JSONArray total1 = new JSONArray();
			JSONArray Status = new JSONArray();
			JSONArray errorboad = new JSONArray();
			u_msg.put("ResponseValue",1);
			u_msg.put("Reserved", null);

			int err[];
			err = new int[] {1,1,1,1};
			for(int i=0;i<SortNum;i++) {
				total.clear();
				byte[] msg2 = { 0x02, 0, 0, 12, 4, (byte) (i + 1), 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg2, SortBoardIP[i]);
				if(recvmsg == null||recvmsg[9]==0) {
					u_msg.put("ResponseValue",0);
					u_msg.put("AlarmStatus",0);
					errorboad.add(i+1);
					for(int u=0;u<4;u++) {
						total.add("?");
					}
				}
				else if (recvmsg[11] == 0) {
					u_msg.put("AlarmStatus",0);
					errorboad.add(i+1);
					for(int j=0;j<recvmsg[15];j++) {
						switch(recvmsg[j*4+17]) {
						case(0x10):
							err[0]=recvmsg[j*4+19];
						break;
						case(0x11):
							err[1]=recvmsg[j*4+19];
						break;
						
						case (0x20):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							    err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x21):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x22):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x23):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x24):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x25):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x26):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x27):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x28):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
							 err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x29):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
							break;
						case (0x2a):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
							  err[2] =0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							break;
						

						case (0x30):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
							break;
						case (0x31):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							break;

						case (0x32):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								err[3]=0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								err[3]=0;
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								err[3]=0;
							} else
								System.out.println("정상");
							break;

						case (0x33):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
							err[3]=0;
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							break;
						default:
							System.out.println("없는 레지값");
						break;
						}
					}
					for(int k=0;k<4;k++) {
						System.out.println("k: "+k+" 값: "+err[k]);
						total.add(err[k]);
					}
								
				}
				else if (recvmsg[11] == 1) {
					u_msg.put("AlarmStatus",1);
					
				}
					
				
				Status.add(total);
				
				
			}
			
			u_msg.put("NumOfBoard",errorboad.size());
			u_msg.put("BoardNum",errorboad);
			u_msg.put("EjectAlarmStatus",Status);
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			
			break;
		case "EjectDriverGet":
			JSONArray errorboad2 = new JSONArray();
			JSONArray arr = new JSONArray();
			JSONArray arr2 = new JSONArray();
			u_msg.put("ResponseValue",1);
			u_msg.put("Reserved", null);
			for(int i=0;i<SortNum;i++) {	
				arr.clear();
				byte[] msg3 = { 0x02, 0, 0, 12, 4, (byte) (i + 1), 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg3, SortBoardIP[i]);
				if(recvmsg == null||recvmsg[9]==0) {
					u_msg.put("ResponseValue",0);
					errorboad2.add(i+1);
				} else if (recvmsg[11] == 0) {
					errorboad2.add(i + 1);
					for (int j = 0; j < recvmsg[15]; j++) {

						switch (recvmsg[j * 4 + 17]) {
						case (0x20):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(1);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(2);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(3);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(4);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(5);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(6);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(7);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(8);
							} else
								System.out.println("정상");
							break;
						case (0x21):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(9);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(10);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(11);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(12);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(13);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(14);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(15);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(16);
							} else
								System.out.println("정상");
							break;
						case (0x22):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(17);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(18);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(19);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(20);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(21);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(22);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(23);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(24);
							} else
								System.out.println("정상");
							break;
						case (0x23):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(25);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(26);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(27);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(28);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(29);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(30);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(31);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(32);
							} else
								System.out.println("정상");
							break;
						case (0x24):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(33);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(34);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(35);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(36);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(37);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(38);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(39);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(40);
							} else
								System.out.println("정상");
							break;
						case (0x25):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
							if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(41);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(42);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(43);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(44);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(45);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(46);
							} else
								System.out.println("정상");
							break;
						case (0x26):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(47);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(48);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(49);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(50);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(51);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(52);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(53);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(54);
							} else
								System.out.println("정상");
							break;
						case (0x27):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(55);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(56);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(57);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(58);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(59);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(60);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(61);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(62);
							} else
								System.out.println("정상");
							break;
						case (0x28):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(63);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(64);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(65);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(66);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(67);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(68);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(69);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(70);
							} else
								System.out.println("정상");
							break;
						case (0x29):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(71);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(72);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(73);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(74);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(75);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(76);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr.add(77);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr.add(78);
							} else
								System.out.println("정상");
							break;
						case (0x2a):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr.add(79);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr.add(80);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr.add(81);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr.add(82);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr.add(83);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr.add(84);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							break;
						default:
							System.out.println("없는 레지값");
							break;

						}

					}

				}
				arr2.add(arr);
			}
		    u_msg.put("NumOfBoard",errorboad2.size());
			u_msg.put("BoardNum",errorboad2);
			u_msg.put("NumOfFalutDriver",arr.size());
			u_msg.put("FalutDriverList",arr2);
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());		
			break;

		case "EjectOverloadGet":
			JSONArray errorboad4 = new JSONArray();
			JSONArray arr1 = new JSONArray();
			JSONArray arr3 = new JSONArray();
			u_msg.put("ResponseValue", 1);
			u_msg.put("Reserved", null);
			for (int i = 0; i < SortNum; i++) {
				arr1.clear();
				byte[] msg3 = { 0x02, 0, 0, 12, 4, (byte) (i + 1), 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0,
						0 };
				recvmsg = sendToBoard(msg3, SortBoardIP[i]);
				if (recvmsg == null || recvmsg[9] == 0) {
					u_msg.put("ResponseValue", 0);
					errorboad4.add(i + 1);
				} else if (recvmsg[11] == 0) {
					errorboad4.add(i + 1);
					for (int j = 0; j < recvmsg[15]; j++) {
						switch (recvmsg[j * 4 + 17]) {
						case (0x30):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr1.add(1);
								arr1.add(2);
								arr1.add(3);
								arr1.add(4);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr1.add(5);
								arr1.add(6);
								arr1.add(7);
								arr1.add(8);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr1.add(9);
								arr1.add(10);
								arr1.add(11);
								arr1.add(12);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr1.add(13);
								arr1.add(14);
								arr1.add(15);
								arr1.add(16);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr1.add(17);
								arr1.add(18);
								arr1.add(19);
								arr1.add(20);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr1.add(21);
								arr1.add(22);
								arr1.add(23);
								arr1.add(24);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr1.add(25);
								arr1.add(26);
								arr1.add(27);
								arr1.add(28);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr1.add(29);
								arr1.add(30);
								arr1.add(31);
								arr1.add(32);
							} else
								System.out.println("정상");
							break;
						case (0x31):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr1.add(33);
								arr1.add(34);
								arr1.add(35);
								arr1.add(36);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr1.add(37);
								arr1.add(38);
								arr1.add(39);
								arr1.add(40);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr1.add(41);
								arr1.add(42);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							break;

						case (0x32):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr1.add(43);
								arr1.add(44);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr1.add(45);
								arr1.add(46);
								arr1.add(47);
								arr1.add(48);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr1.add(49);
								arr1.add(50);
								arr1.add(51);
								arr1.add(52);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								arr1.add(53);
								arr1.add(54);
								arr1.add(55);
								arr1.add(56);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								arr1.add(57);
								arr1.add(58);
								arr1.add(59);
								arr1.add(60);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								arr1.add(61);
								arr1.add(62);
								arr1.add(63);
								arr1.add(64);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								arr1.add(65);
								arr1.add(66);
								arr1.add(67);
								arr1.add(68);
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								arr1.add(69);
								arr1.add(70);
								arr1.add(71);
								arr1.add(72);
							} else
								System.out.println("정상");
							break;

						case (0x33):
							registerValueParser((byte)recvmsg[j * 4 + 19]);
						if (Integer.parseInt(eventOccuredArray[7]) == 0) {
								arr1.add(73);
								arr1.add(74);
								arr1.add(75);
								arr1.add(76);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[6]) == 0) {
								arr1.add(77);
								arr1.add(78);
								arr1.add(79);
								arr1.add(80);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[5]) == 0) {
								arr1.add(81);
								arr1.add(82);
								arr1.add(83);
								arr1.add(84);
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[4]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[3]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
						if (Integer.parseInt(eventOccuredArray[2]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[1]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							if (Integer.parseInt(eventOccuredArray[0]) == 0) {
								System.out.println("정상");
							} else
								System.out.println("정상");
							break;
						default:
							System.out.println("없는 레지값");
							break;
						}
					}
					arr3.add(arr1);
				}
			}

			u_msg.put("NumOfBoard", errorboad4.size());
			u_msg.put("BoardNum", errorboad4);
			u_msg.put("NumOfFalutOverload", arr1.size());
			u_msg.put("FalutOverload", arr3);
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
		}
	}
}
