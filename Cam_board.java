package sum01;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Cam_board extends board{
	int SortNum;
	BytetoBmp BB = new BytetoBmp();

	public Cam_board(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id, BoardNum m_num) {
		this.u_msg=m_msg;
		this.u_ip=m_ip;
		this.u_port=m_port;
		this.u_id=m_id;
		this.u_lrecv=m_lrecv;
		SortNum = m_num.SortBoardNum;
		this.num=m_num;
	}

	//	static String getTime(){
	//	SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss:ms]");
	//	return f.format(new Date());
	//}

	public int LogicNumtoPyshicNum(String LN) {
		int PN=Integer.parseInt(LN.substring(0,LN.length()-1))*2;
		String first = LN.substring(LN.length()-1);
		if(first.equals("a"))PN--; //real L Num
		return PN;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void run(){
		System.out.println(u_msg.toString()+u_ip+u_port);
		switch(u_msg.get("MsgName").toString()) {
		case "SensColorSet":
			DataBase Rc = new DataBase();
			conn=Rc.returnConn();	
			int FR=Integer.parseInt(u_msg.get("CamLocation").toString());
			int addr1=(0x1f+Integer.parseInt(u_msg.get("SensColor").toString())*2+Integer.parseInt(u_msg.get("SensColorNumber").toString()));
			int addr2=(0x2f+Integer.parseInt(u_msg.get("SensColor").toString())*2+Integer.parseInt(u_msg.get("SensColorNumber").toString()));
			int addr3=(0x3f+Integer.parseInt(u_msg.get("SensColor").toString())*2+Integer.parseInt(u_msg.get("SensColorNumber").toString()));
			if(addr3==0x4d)addr3--;		
			int SortChutNum[]=null;
			try {
				String query = "SELECT COUNT(sorting_Chute) AS num FROM tb_ultima_sorting WHERE sorting_Order=?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, u_msg.get("Order").toString());
				rs = pstmt.executeQuery();
				rs.next();
				SortChutNum=new int[rs.getInt("num")];				
				int count=0;

				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order=?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, u_msg.get("Order").toString());
				rs = pstmt.executeQuery();				
				while(rs.next()) {
					SortChutNum[count]=LogicNumtoPyshicNum(rs.getString("sorting_Chute"));
					count++;
				}
				Arrays.sort(SortChutNum);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					rs.close();
					pstmt.close();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			for(int i=0;i<SortChutNum.length;i++) {
				int sen = SortChutNum[i];
				if(i+1!=SortChutNum.length && (SortChutNum[i]+1)/2==(SortChutNum[i+1]+1)/2) {
					if(FR==0) {
						byte[] msg = {0x02,0,0,60,4,(byte) ((sen+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,12,
								0x00 ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),
								0x00 ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), 
								0x00 ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()), 
								0x01 ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),
								0x01 ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), 
								0x01 ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()),

								0x10 ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),
								0x10 ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), 
								0x10 ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()), 
								0x11 ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),
								0x11 ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), 
								0x11 ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()), 
								0x03,0,0,0};	//바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[(sen+1)/2-1]); //실제 출력으로 확인 GUI에서 마커로 체크, 0.3초마다 내려옴
						if(recvmsg==null||recvmsg[9]==0) {
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((sen+1)/2));
						}
					}else {
						FR--;
						byte[] msg = {0x02,0,0,36,4,(byte) ((sen+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,6,
								(byte) (0x10*FR) ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),
								(byte) (0x10*FR) ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), 
								(byte) (0x10*FR) ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()),

								(byte) (0x10*FR+0x01) ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),
								(byte) (0x10*FR+0x01) ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), 
								(byte) (0x10*FR+0x01) ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()),
								0x03,0,0,0};	//바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[(sen+1)/2-1]); //실제 출력으로 확인 GUI에서 마커로 체크, 0.3초마다 내려옴
						if(recvmsg==null||recvmsg[9]==0) {
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((sen+1)/2));
						}
					}
					i++;
				}else {
					if(FR==0) {
						byte[] msg = {0x02,0,0,36,4,(byte) ((sen+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,6,(byte) ((sen+1)%2) ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),(byte) ((sen+1)%2) ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), (byte) ((sen+1)%2) ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()), (byte) (0x10+(sen+1)%2) ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),(byte) (0x10+(sen+1)%2) ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), (byte) (0x10+(sen+1)%2) ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()), 0x03,0,0,0};	//바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[(sen+1)/2-1]); //실제 출력으로 확인 GUI에서 마커로 체크, 0.3초마다 내려옴
						if(recvmsg==null||recvmsg[9]==0) {
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((sen+1)/2));
						}
					}else {
						FR--;
						byte[] msg = {0x02,0,0,24,4,(byte) ((sen+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,3,(byte) (0x10*FR+(sen+1)%2) ,(byte) addr1,0,(byte) Integer.parseInt(u_msg.get("SensOnOff").toString()),(byte) (0x10*FR+(sen+1)%2) ,(byte) addr2,0,(byte) Integer.parseInt(u_msg.get("SensSensitivity").toString()), (byte) (0x10*FR+(sen+1)%2) ,(byte) addr3,0,(byte) Integer.parseInt(u_msg.get("SensSize").toString()), 0x03,0,0,0};	//바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[(sen+1)/2-1]); //실제 출력으로 확인 GUI에서 마커로 체크, 0.3초마다 내려옴
						if(recvmsg==null||recvmsg[9]==0) {
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((sen+1)/2));
						}
					}
				}
			}

			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1); 	    	
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "SensAdvancedSet":
			if((Integer.parseInt(u_msg.get("CamLogicalNum").toString())+1)/2>num.SortBoardNum){
				System.out.println("wrong board");
				obj.put("MsgName", "SetResult");
				obj.put("Reserved", null);
				obj.put("ResponseValue", "0");
				ErrorBoard.add("Sort-"+Integer.toString((Integer.parseInt(u_msg.get("CamLogicalNum").toString())+1)/2));
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				obj.put("ErrorBoard", ErrorBoard);
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;
			}
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1); 
			int camnum=Integer.parseInt(u_msg.get("CamLogicalNum").toString());
			int SFR=Integer.parseInt(u_msg.get("CamLocation").toString());
			if(SFR == 0) {
				byte[] msg = {0x02,0,0,20,4,(byte) ((camnum+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,2,(byte) (0x10+(camnum+1)%2) ,(byte) (0x50+Integer.parseInt(u_msg.get("SelectedColor").toString())),0,(byte) Integer.parseInt(u_msg.get("CamFactor").toString()),(byte) ((camnum+1)%2) ,(byte) (0x50+Integer.parseInt(u_msg.get("SelectedColor").toString())),0,(byte) Integer.parseInt(u_msg.get("CamFactor").toString()), 0x03,0,0,0};	//바이트배열
				recvmsg=sendToBoard(msg, SortBoardIP[(camnum+1)/2-1]);			
				if(recvmsg==null||recvmsg[9]==0) {
					obj.put("ResponseValue", 0);
					obj.put("Reserved", 0);
					ErrorBoard.add("Sort-"+Integer.toString((camnum+1)/2));
				}
			}
			else {
				SFR--;
				byte[] msg = {0x02,0,0,16,4,(byte) ((camnum+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,1,(byte) (0x10*SFR+(camnum+1)%2) ,(byte) (0x50+Integer.parseInt(u_msg.get("SelectedColor").toString())),0,(byte) Integer.parseInt(u_msg.get("CamFactor").toString()), 0x03,0,0,0};	//바이트배열
				recvmsg=sendToBoard(msg, SortBoardIP[(camnum+1)/2-1]);			
				if(recvmsg==null||recvmsg[9]==0) {
					obj.put("ResponseValue", 0);
					obj.put("Reserved", 0);
					ErrorBoard.add("Sort-"+Integer.toString((camnum+1)/2));
				}
			}			
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;

		case "CameraSet": 
			JSONArray camlist = new JSONArray();
			JSONParser parser = new JSONParser();
			try {
				camlist = (JSONArray)parser.parse(u_msg.get("CamOnOff1~12AB").toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DataBase CRC = new DataBase();
			conn=CRC.returnConn();
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1);
			int CSFR = Integer.parseInt(u_msg.get("CamLocation").toString());
			int[] CSAD = new int[100];
			int CSC = 0;
			try {
				String query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, Integer.parseInt(u_msg.get("Order").toString()));
				rs = pstmt.executeQuery();
				while(rs.next()) {
					CSAD[CSC]=LogicNumtoPyshicNum(rs.getString("sorting_Chute"));
					CSC++;
					System.out.println(rs.getString("sorting_Chute")+" "+CSC);
				}			
				int[] AD = new int[CSC];
				System.arraycopy(CSAD, 0, AD, 0, AD.length);
				Arrays.sort(AD);
				if(CSFR==0) {
					for(int i=0;i<AD.length;i++) {	
						int CamNum = AD[i];	
						CamNum++;
						System.out.println(AD[i]);
						byte[] camSetMsg ={0x02,0,0,20,4,(byte) ((CamNum)/2),0,2,2,0,(byte)u_id,0,0,0,0,2,(byte) ((0x10+CamNum%2)) , 0x00, 0,(byte) (Integer.parseInt(camlist.get(i).toString())),(byte) ((CamNum%2)) , 0x00, 0,(byte) (Integer.parseInt(camlist.get(i).toString())), 0x03,0,0,0}; //바이트배열
						recvmsg=sendToBoard(camSetMsg, SortBoardIP[(CamNum)/2-1]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((CamNum)/2)); 
						}

					}
				}
				else {
					for(int i=0;i<AD.length;i++) {					
						int CamNum = AD[i];
						CamNum++;
						System.out.println(AD[i]);
						byte[] camSetMsg ={0x02,0,0,16,4,(byte) ((CamNum)/2),0,2,2,0,(byte)u_id,0,0,0,0,1,(byte) ((0x10*Integer.parseInt(u_msg.get("CamLocation").toString())+CamNum%2)) , 0x00, 0,(byte) (Integer.parseInt(camlist.get(i).toString())), 0x03,0,0,0}; //바이트배열
						recvmsg=sendToBoard(camSetMsg, SortBoardIP[(CamNum)/2-1]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((CamNum)/2)); 
						}						
					}
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}finally {
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

		case "CameraRGBSet":
			DataBase RGBRC = new DataBase();
			conn=RGBRC.returnConn();
			String[] Order = null;
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", null);
			obj.put("ResponseValue", 1);
			int Count=0;
			try {
				String query = "SELECT COUNT(*) AS num FROM tb_ultima_sorting WHERE sorting_Order = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, Integer.parseInt(u_msg.get("Order").toString()));
				rs = pstmt.executeQuery();
				rs.next();				
				Order= new String[rs.getInt("num")];
				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, Integer.parseInt(u_msg.get("Order").toString()));
				rs = pstmt.executeQuery();
				while(rs.next()) {
					Order[Count]=rs.getString("sorting_Chute");
					Count++;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					rs.close();
					pstmt.close();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			int RGB=0;
			int dif=0;
			int difsub=0;
			int[] place = {1,2,4,8,16,32,64};
			JSONArray list = new JSONArray();
			JSONParser RGBParser = new JSONParser();
			try {
				list = (JSONArray)RGBParser.parse(u_msg.get("RGBColorSelect").toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for(int i=list.size()-1;i>=0;i--) {
				if(Integer.parseInt(list.get(i).toString())==1) {
					RGB=RGB+place[i];
				}
			}
			try {
				list = (JSONArray)RGBParser.parse(u_msg.get("DiffColorSelect").toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i=list.size()-1;i>=0;i--) {
				if(Integer.parseInt(list.get(i).toString())==1) {
					dif=dif+place[i];
				}
			}
			try {
				list = (JSONArray)RGBParser.parse(u_msg.get("SubColorSelect").toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i=list.size()-1;i>=0;i--) {
				if(Integer.parseInt(list.get(i).toString())==1) {
					switch(i) {
					case 0: difsub= 8;
					break;
					case 1: difsub= 2;
					break;
					case 2: difsub= 4;
					break;
					case 3: difsub=1;
					break;
					}
				}
			}
			int lNum;
			if(Integer.parseInt(u_msg.get("CamApplyMethod").toString())==0){
				for(int i=0;i<Order.length;i++) {
					lNum=LogicNumtoPyshicNum(Order[i]);
					lNum++;
					byte[] RGBFmsg = { 0x02, 0, 0, 36, 4, (byte) ((lNum)/2), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 6, (byte) ((lNum)%2),(byte) 0x10,0x00, (byte) RGB, (byte) ((lNum)%2),(byte)0x11,0x00, (byte) dif, (byte) ((lNum)%2),(byte)0x12,0x00, (byte) difsub, (byte) (0x10+((lNum)%2)),(byte) 0x00,0x00, (byte) RGB, (byte) (0x10+((lNum)%2)),(byte)0x11,0x00, (byte) dif, (byte) (0x10+((lNum)%2)),(byte)0x12,0x00, (byte) difsub, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(RGBFmsg, SortBoardIP[(lNum)/2-1]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(lNum/2));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}
				obj.put("ErrorBoard", ErrorBoard);
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;
			}else if(Integer.parseInt(u_msg.get("CamApplyMethod").toString())==1){
				for(int i=0;i<Order.length;i++) {
					lNum=LogicNumtoPyshicNum(Order[i]);
					lNum++;
					byte[] RGBFmsg = { 0x02, 0, 0, 24, 4, (byte) ((lNum)/2), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 3, (byte) ((lNum)%2),(byte) 0x10,0x00, (byte) RGB, (byte) ((lNum)%2),(byte)0x11,0x00, (byte) dif, (byte) ((lNum)%2),(byte)0x12,0x00, (byte) difsub, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(RGBFmsg, SortBoardIP[(lNum)/2-1]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(lNum/2));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}
			}else if(Integer.parseInt(u_msg.get("CamApplyMethod").toString())==2){
				for(int i=0;i<Order.length;i++) {
					lNum=LogicNumtoPyshicNum(Order[i]);
					lNum++;
					byte[] RGBRmsg = { 0x02, 0, 0, 24, 4, (byte) ((lNum)/2), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 3, (byte) (0x10+((lNum)%2)),(byte) 0x00,0x00, (byte) RGB, (byte) (0x10+((lNum)%2)),(byte)0x11,0x00, (byte) dif, (byte) (0x10+((lNum)%2)),(byte)0x12,0x00, (byte) difsub, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(RGBRmsg, SortBoardIP[(lNum)/2-1]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(lNum/2));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}
			}
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
		case "CamBGSet":
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
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", 0);
			try {
				byte[] msg1 = {0x02,0,0,20,4,(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()),0,2,2,0,(byte)u_id,0,0,0,0,2,
						0x20,0x21,0,(byte) Integer.parseInt(u_msg.get("CamLocation").toString()),
						0x20,0x20,0,(byte) Integer.parseInt(u_msg.get("CalibratedSelect").toString()),
						0x03,0,0,0};

				recvmsg = sendToBoard(msg1, SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString())-1]);


				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Sort-"+Integer.toString((byte) Integer.parseInt(u_msg.get("ChuteNum").toString())));
				}



				if(ErrorBoard.isEmpty()) {
					ErrorBoard.add(0);
					obj.put("ResponseValue", 1);
					obj.put("NumOfErrorBoard", 0);
				}else {
					obj.put("ResponseValue", 0);
					obj.put("NumOfErrorBoard", ErrorBoard.size());
				}
				obj.put("ErrorBoard", ErrorBoard);
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			}catch(Exception e) {
				System.out.println("레지스트 오류");
			}



			break;
		case "CamGOSet":
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
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", 0);

			switch(u_msg.get("GainAllOnOff").toString()) {
			case "1":
				for(int i=1; i<=SortNum; i++) {
					if(ServerStartMain.Busyflag[i-1]==true) {
						int sleepCount=0;
						while(sleepCount<20) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}							
							System.out.println(ServerStartMain.Busyflag[0]);
							if(ServerStartMain.Busyflag[i-1]==false) {
								byte[] msg13 = {0x02,0,0,60,4,(byte) i, 0,2,2,0,(byte)u_id,0,0,0,0,12,
										0x02,0x10,0,(byte) Integer.parseInt(u_msg.get("CamRedGain").toString()),
										0x02,0x11,0,(byte) Integer.parseInt(u_msg.get("CamGreenGain").toString()),
										0x02,0x12,0,(byte) Integer.parseInt(u_msg.get("CamBlueGain").toString()),
										0x02,0x13,0,(byte) Integer.parseInt(u_msg.get("CamRedOffset").toString()),
										0x02,0x14,0,(byte) Integer.parseInt(u_msg.get("CamGreenOffset").toString()),
										0x02,0x15,0,(byte) Integer.parseInt(u_msg.get("CamBlueOffset").toString()),
										0x12,0x10,0,(byte) Integer.parseInt(u_msg.get("CamRedGain").toString()),
										0x12,0x11,0,(byte) Integer.parseInt(u_msg.get("CamGreenGain").toString()),
										0x12,0x12,0,(byte) Integer.parseInt(u_msg.get("CamBlueGain").toString()),
										0x12,0x13,0,(byte) Integer.parseInt(u_msg.get("CamRedOffset").toString()),
										0x12,0x14,0,(byte) Integer.parseInt(u_msg.get("CamGreenOffset").toString()),
										0x12,0x15,0,(byte) Integer.parseInt(u_msg.get("CamBlueOffset").toString()),
										0x03,0,0,0};
								recvmsg = sendToBoard(msg13, SortBoardIP[i-1]);
								if(recvmsg==null||recvmsg[9]==0) {
									ErrorBoard.add("Sort-"+Integer.toString(i));
								}
								break;					
							}
							sleepCount++;
						}
						if(sleepCount==20) {
							ErrorBoard.add("Sort-"+Integer.toString(i));
						}
					}else {
						byte[] msg13 = {0x02,0,0,60,4,(byte) i, 0,2,2,0,(byte)u_id,0,0,0,0,12,
								0x02,0x10,0,(byte) Integer.parseInt(u_msg.get("CamRedGain").toString()),
								0x02,0x11,0,(byte) Integer.parseInt(u_msg.get("CamGreenGain").toString()),
								0x02,0x12,0,(byte) Integer.parseInt(u_msg.get("CamBlueGain").toString()),
								0x02,0x13,0,(byte) Integer.parseInt(u_msg.get("CamRedOffset").toString()),
								0x02,0x14,0,(byte) Integer.parseInt(u_msg.get("CamGreenOffset").toString()),
								0x02,0x15,0,(byte) Integer.parseInt(u_msg.get("CamBlueOffset").toString()),
								0x12,0x10,0,(byte) Integer.parseInt(u_msg.get("CamRedGain").toString()),
								0x12,0x11,0,(byte) Integer.parseInt(u_msg.get("CamGreenGain").toString()),
								0x12,0x12,0,(byte) Integer.parseInt(u_msg.get("CamBlueGain").toString()),
								0x12,0x13,0,(byte) Integer.parseInt(u_msg.get("CamRedOffset").toString()),
								0x12,0x14,0,(byte) Integer.parseInt(u_msg.get("CamGreenOffset").toString()),
								0x12,0x15,0,(byte) Integer.parseInt(u_msg.get("CamBlueOffset").toString()),
								0x03,0,0,0};
						recvmsg = sendToBoard(msg13, SortBoardIP[i-1]);
						if(recvmsg==null||recvmsg[9]==0) {
							ErrorBoard.add("Sort-"+Integer.toString(i));
						}
					}
				}
				break;

			case "0":
				if(ServerStartMain.Busyflag[Integer.parseInt(u_msg.get("ChuteNum").toString())-1]==true) {
					int sleepCount=0;
					while(sleepCount<20) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
						System.out.println(ServerStartMain.Busyflag[0]);
						if(ServerStartMain.Busyflag[Integer.parseInt(u_msg.get("ChuteNum").toString())-1]==false) {
							break;
						}
						sleepCount++;
					}
					if(sleepCount==20) {
						ErrorBoard.add("Sort-"+Integer.toString((byte) Integer.parseInt(u_msg.get("ChuteNum").toString())));
						obj.put("ResponseValue", 0);
						obj.put("NumOfErrorBoard", ErrorBoard.size());
						obj.put("ErrorBoard", ErrorBoard);
						array.add(obj);
						sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
						System.exit(0);
					}
				}
				switch (u_msg.get("CamLocation").toString()) {
				case "0":
					byte[] msg13 = {0x02,0,0,36,4,(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()),0,2,2,0,(byte)u_id,0,0,0,0,6,
							0x02,0x10,0,(byte) Integer.parseInt(u_msg.get("CamRedGain").toString()),
							0x02,0x11,0,(byte) Integer.parseInt(u_msg.get("CamGreenGain").toString()),
							0x02,0x12,0,(byte) Integer.parseInt(u_msg.get("CamBlueGain").toString()),
							0x02,0x13,0,(byte) Integer.parseInt(u_msg.get("CamRedOffset").toString()),
							0x02,0x14,0,(byte) Integer.parseInt(u_msg.get("CamGreenOffset").toString()),
							0x02,0x15,0,(byte) Integer.parseInt(u_msg.get("CamBlueOffset").toString()),
							0x03,0,0,0};
					recvmsg = sendToBoard(msg13, SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString())-1]);
					if(recvmsg==null||recvmsg[9]==0) {
						ErrorBoard.add("Sort-"+Integer.toString((byte) Integer.parseInt(u_msg.get("ChuteNum").toString())));
					}				

					break;
				case "1":
					byte[] msg17 = {0x02,0,0,36,4,(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()),0,2,2,0,(byte)u_id,0,0,0,0,6,
							0x12,0x10,0,(byte) Integer.parseInt(u_msg.get("CamRedGain").toString()),
							0x12,0x11,0,(byte) Integer.parseInt(u_msg.get("CamGreenGain").toString()),
							0x12,0x12,0,(byte) Integer.parseInt(u_msg.get("CamBlueGain").toString()),
							0x12,0x13,0,(byte) Integer.parseInt(u_msg.get("CamRedOffset").toString()),
							0x12,0x14,0,(byte) Integer.parseInt(u_msg.get("CamGreenOffset").toString()),
							0x12,0x15,0,(byte) Integer.parseInt(u_msg.get("CamBlueOffset").toString()),
							0x03,0,0,0};
					recvmsg = sendToBoard(msg17, SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString())-1]);
					if(recvmsg==null||recvmsg[9]==0) {
						ErrorBoard.add("Sort-"+Integer.toString((byte) Integer.parseInt(u_msg.get("ChuteNum").toString())));
					}

					break;

				}
			}
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			if(ErrorBoard.isEmpty()) {
				obj.put("ResponseValue", 1);
			}else {
				obj.put("ResponseValue", 0);
			}
			obj.put("ErrorBoard", ErrorBoard);
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
		case "CamWhiteDarkSet":
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
			// waitTime = 10000;
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", 0);
			switch (u_msg.get("Location").toString()) {
			case "0":
				byte[] msg18 = {0x02,0,0,24,4,(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()),0,2,2,0,(byte)u_id,0,0,0,0,3,
						0x02,0x00,0,(byte) Integer.parseInt(u_msg.get("WhiteBalance").toString()),
						0x02,0x01,0,(byte) Integer.parseInt(u_msg.get("CamDark").toString()),
						0x20,0x12,0,(byte) Integer.parseInt(u_msg.get("BGAngleRenew").toString()),
						0x03,0,0,0};
				recvmsg = sendToBoardLong(msg18, SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString())-1]);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Sort-"+Integer.toString((byte) Integer.parseInt(u_msg.get("ChuteNum").toString())));
				}
				break;
			case "1":
				byte[] msg19 = {0x02,0,0,24,4,(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()),0,2,2,0,(byte)u_id,0,0,0,0,3,
						0x12,0x00,0,(byte) Integer.parseInt(u_msg.get("WhiteBalance").toString()),
						0x12,0x01,0,(byte) Integer.parseInt(u_msg.get("CamDark").toString()),
						0x20,0x12,0,(byte) Integer.parseInt(u_msg.get("BGAngleRenew").toString()),
						0x03,0,0,0};
				recvmsg = sendToBoardLong(msg19, SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString())-1]);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Sort-"+Integer.toString((byte) Integer.parseInt(u_msg.get("ChuteNum").toString())));
				}
				break;
			}
			if(ErrorBoard.isEmpty()) {
				ErrorBoard.add(0);
				obj.put("ResponseValue", 1);
				obj.put("NumOfErrorBoard", 0);
			}else {
				obj.put("ResponseValue", 0);
				obj.put("NumOfErrorBoard", ErrorBoard.size());
			}
			obj.put("ErrorBoard", ErrorBoard);
			array.add(obj);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());

			break;
		case "CamBGLineGet":
			boardPort=5001;
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
			if ((byte) Integer.parseInt(u_msg.get("CamLocation").toString()) == 1) {
				byte[] msg = { 0x02, 0, 0, 16, 4, (byte) Integer.parseInt(u_msg.get("ChuteNum").toString()), 0, 2, 13,
						0, (byte) u_id, 0, 0, 0, 1, 1, 0x20, 0x10, 0, 1, 0x03, 0, 0, 0 };
				recvmsg = sendToLine(msg,
						SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()) - 1]);
				if(recvmsg==null) {
					u_msg.put("ResponseValue",0);	
				}else u_msg.put("ResponseValue",1);
				try {
					BB.byteArrayConvertToImageFile(0, (byte) 0x10, recvmsg, (byte) 2580);
					String c = "/assets/camImage/f_line"+ServerStartMain.flinecnt+".txt";
					u_msg.put("LineDataRr", null);
					u_msg.put("LineDataFr", c);
					ServerStartMain.flinecnt++; 
					if (ServerStartMain.flinecnt == 3) ServerStartMain.flinecnt = 0;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ((byte) Integer.parseInt(u_msg.get("CamLocation").toString()) == 2) {
				byte[] msg = { 0x02, 0, 0, 16, 4, (byte) Integer.parseInt(u_msg.get("ChuteNum").toString()), 0, 2, 13,
						0, (byte) u_id, 0, 0, 0, 2, 1, 0x20, 0x10, 0, 1, 0x03, 0, 0, 0 };
				recvmsg = sendToLine(msg,
						SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()) - 1]);
				if(recvmsg==null) {
					u_msg.put("ResponseValue",0);	
				}else u_msg.put("ResponseValue",1);
				try {
					BB.byteArrayConvertToImageFile(1, (byte) 0x10, recvmsg, (byte) 2580);
					String g = "/assets/camImage/r_line"+ServerStartMain.rlinecnt+".txt";
					u_msg.put("LineDataRr", g);
					u_msg.put("LineDataFr", null);
					ServerStartMain.rlinecnt++; 
					if (ServerStartMain.rlinecnt == 3) ServerStartMain.rlinecnt = 0;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if ((byte) Integer.parseInt(u_msg.get("CamLocation").toString()) == 0) {
				byte[] msg = { 0x02, 0, 0, 16, 4, (byte) Integer.parseInt(u_msg.get("ChuteNum").toString()), 0, 2, 13,
						0, (byte) u_id, 0, 0, 0, 1, 1, 0x20, 0x10, 0, 1, 0x03, 0, 0, 0 };
				recvmsg = sendToLine(msg,
						SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()) - 1]);
				if(recvmsg==null) {
					u_msg.put("ResponseValue",0);	
				}else u_msg.put("ResponseValue",1);
				try {
					BB.byteArrayConvertToImageFile(0, (byte) 0x10, recvmsg, (byte) 2580);
					String c = "/assets/camImage/f_line"+ServerStartMain.flinecnt+".txt";
					u_msg.put("LineDataFr", c);
					ServerStartMain.flinecnt++; 
					if (ServerStartMain.flinecnt == 3) ServerStartMain.flinecnt = 0;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				byte[] msg3 = { 0x02, 0, 0, 16, 4, (byte) Integer.parseInt(u_msg.get("ChuteNum").toString()), 0, 2, 13,
						0, (byte) u_id, 0, 0, 0, 2, 1, 0x20, 0x10, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToLine(msg3,
						SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()) - 1]);
				if(recvmsg==null) {
					u_msg.put("ResponseValue",0);	
				}else u_msg.put("ResponseValue",1);
				try {
					BB.byteArrayConvertToImageFile(1, (byte) 0x10, recvmsg, (byte) 2580);
					String g = "/assets/camImage/r_line"+ServerStartMain.rlinecnt+".txt";
					u_msg.put("LineDataRr", g);
					ServerStartMain.rlinecnt++; 
					if (ServerStartMain.rlinecnt == 3) ServerStartMain.rlinecnt = 0;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			u_msg.put("Reserved", null);

			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());

			break;
		case "CamBGImgGet":
			boardPort=5001;
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
			ServerStartMain.Busyflag[Integer.parseInt(u_msg.get("ChuteNum").toString())-1]=true;
			byte comLo = (byte) Integer.parseInt(u_msg.get("CamLocation").toString());
			byte chNum = (byte) Integer.parseInt(u_msg.get("ChuteNum").toString());
			if (comLo == 1) {
				byte[] msg = { 0x02, 0, 0, 16, 4, chNum, 0, 2, 13,
						0, (byte) u_id, 0, 0, 0, 4, 1, 0x20, 0x11, 0, 1, 0x03, 0, 0, 0 };
				recvmsg = sendToImage(msg, SortBoardIP[chNum - 1]);

				if(recvmsg==null) {
					u_msg.put("ResponseValue",0);	
				}else u_msg.put("ResponseValue",1);

				try {
					BB.byteArrayConvertToImageFile(0, (byte) 0x11, recvmsg, (byte) 387000);
					String a = "/assets/camImage/f_img"+ServerStartMain.fimgcnt+".bmp";
					u_msg.put("ImageLinkFr", a);
					u_msg.put("ImageLinkRr", null);
					ServerStartMain.fimgcnt++; 
					if (ServerStartMain.fimgcnt == 3) ServerStartMain.fimgcnt = 0; 

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (comLo == 2) {
				byte[] msg2 = { 0x02, 0, 0, 16, 4, (byte) Integer.parseInt(u_msg.get("ChuteNum").toString()), 0, 2,
						13, 0, (byte) u_id, 0, 0, 0, 5, 1, 0x20, 0x11, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToImage(msg2,
						SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()) - 1]);
				if(recvmsg==null) {
					u_msg.put("ResponseValue",0);	
				}else u_msg.put("ResponseValue",1);
				try {
					BB.byteArrayConvertToImageFile(1, (byte) 0x11, recvmsg, (byte) 387000);
					String b =  "/assets/camImage/r_img"+ServerStartMain.rimgcnt+".bmp";
					u_msg.put("ImageLinkFr", null);
					u_msg.put("ImageLinkRr", b);
					ServerStartMain.rimgcnt++; 
					if (ServerStartMain.rimgcnt == 3) ServerStartMain.rimgcnt = 0; 

				} catch (Exception e) {

				}
			} else
				if (comLo == 0) {
					byte[] msg = { 0x02, 0, 0, 16, 4, chNum, 0, 2,
							13, 0, (byte) u_id, 0, 0, 0, 4, 1, 0x20, 0x11, 0, 1, 0x03, 0, 0, 0 };
					recvmsg = sendToImage(msg,
							SortBoardIP[chNum - 1]);
					if(recvmsg==null) {
						u_msg.put("ResponseValue",0);	
					}else u_msg.put("ResponseValue",1);
					try {
						BB.byteArrayConvertToImageFile(0, (byte) 0x11, recvmsg, (byte) 387000);
						String a = "/assets/camImage/f_img"+ServerStartMain.fimgcnt+".bmp";
						u_msg.put("ImageLinkFr", a);
						ServerStartMain.fimgcnt++; 
						if (ServerStartMain.fimgcnt == 3) ServerStartMain.fimgcnt = 0; 
						

					} catch (Exception e) {

					}
					byte[] msg2 = { 0x02, 0, 0, 16, 4, (byte) Integer.parseInt(u_msg.get("ChuteNum").toString()), 0, 2,
							13, 0, (byte) u_id, 0, 0, 0, 5, 1, 0x20, 0x11, 0, 0, 0x03, 0, 0, 0 };
					recvmsg = sendToImage(msg2,
							SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()) - 1]);
					if(recvmsg==null) {
						u_msg.put("ResponseValue",0);	
					}else u_msg.put("ResponseValue",1);
					try {
						BB.byteArrayConvertToImageFile(1, (byte) 0x11, recvmsg, (byte) 387000);
						String b =  "/assets/camImage/r_img"+ServerStartMain.rimgcnt+".bmp";
						u_msg.put("ImageLinkRr", b);
						ServerStartMain.rimgcnt++; 
						if (ServerStartMain.rimgcnt == 3) ServerStartMain.rimgcnt = 0; 

					} catch (Exception e) {

					}

				}

			u_msg.put("Reserved", null);
			array.add(u_msg);

			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			ServerStartMain.Busyflag[Integer.parseInt(u_msg.get("ChuteNum").toString())-1]=false;
			break;

		case "CamAlarmGet":
			JSONArray Status = new JSONArray();
			JSONArray Status1 = new JSONArray();
			JSONArray total = new JSONArray();
			JSONArray errorboad = new JSONArray();
			u_msg.put("ResponseValue",1);
			u_msg.put("Reserved", null);

			int err[];
			err = new int[] {1,1,1,1,1,1,1,1};

			for(int i=0;i<SortNum;i++) {
				Status.clear();


				byte[] msg = { 0x02, 0, 0, 12, 4, (byte) (i + 1), 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };

				recvmsg = sendToBoard(msg, SortBoardIP[i]);

				if(recvmsg == null||recvmsg[9]==0) {
					u_msg.put("ResponseValue",0);
					u_msg.put("AlarmStatus",0);
					errorboad.add(i+1);
					for(i=0;i<8;i++) {
						Status.add("?");
					}		
				} 
				else if (recvmsg[11] == 0) {
					u_msg.put("AlarmStatus",0);
					errorboad.add(i+1);
					for(int j=0;j<recvmsg[15];j++) {
						switch(recvmsg[j*4+17]) {
						case(0x10): 
							if(recvmsg[j*4+16]==0x08) {
								err[0]=recvmsg[j*4+19];	
							}
						if(recvmsg[j*4+16]==0x18) {
							err[4]=recvmsg[j*4+19];		
						}		
						break;
						case(0x11):
							if(recvmsg[j*4+16]==0x08) {
								err[1]=recvmsg[j*4+19];		
							}
						if(recvmsg[j*4+16]==0x18) {
							err[5]=recvmsg[j*4+19];		
						}
						break;
						case(0x20):
							if(recvmsg[j*4+16]==0x08) {
								err[2]=recvmsg[j*4+19];		
							}
						if(recvmsg[j*4+16]==0x18) {
							err[6]=recvmsg[j*4+19];		
						}
						break;
						case(0x21):
							if(recvmsg[j*4+16]==0x08) {
								err[3]=recvmsg[j*4+19];		
							}
						if(recvmsg[j*4+16]==0x18) {
							err[7]=recvmsg[j*4+19];		
						}
						break;
						default:
							System.out.println("없는 레지값");
							break;
						}
					}
					for(int k=0;k<8;k++) {
						System.out.println("k: "+k+" 값: "+err[k]);
						Status.add(err[k]);
					}

				}
				else if (recvmsg[11] == 1) {
					u_msg.put("AlarmStatus",1);						
				}
				Status1.add(Status);
			}

			total.add(Status1);




			u_msg.put("NumOfBoard",errorboad.size());
			u_msg.put("CameraNum",errorboad);
			u_msg.put("CamAlarmStatus",total);
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			break;
		}	
	}
}
