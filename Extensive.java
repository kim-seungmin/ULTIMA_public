package sum01;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Extensive extends board {
	BoardNum u_num;
	public Extensive(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id, BoardNum num) {
		this.u_msg=m_msg;
		this.u_ip=m_ip;
		this.u_port=m_port;
		this.u_id=m_id;
		this.u_lrecv=m_lrecv;
		u_num=num;
		this.num =num;
	}
	public float map(int i, int in_min, int in_max, float out_min, float out_max) {
		return Float.parseFloat(String.format( "%.1f",(i - in_min) * (out_max - out_min) / (in_max - in_min) + out_min));
	}
	public float byteToFloat(byte bnum) {
		int num= Byte.toUnsignedInt(bnum);
		String bits = Integer.toBinaryString(num);
		int bitsInt;
		float bitsDecimal;
		if(bits.length()<5) {
			bitsInt = 0;
			bitsDecimal=Integer.parseInt(bits, 2);
		}else {
			bitsInt= Integer.parseInt(bits.substring(0, bits.length()-4), 2);
			bitsDecimal = Integer.parseInt(bits.substring(bits.length()-4), 2);
		}		
		while(bitsDecimal>1)bitsDecimal=bitsDecimal/10;
		float total=bitsInt+bitsDecimal;
		return total;
	}
	public int LogicNumtoPyshicNum(String LN) {
		int PN=Integer.parseInt(LN.substring(0,LN.length()-1))*2;
		String first = LN.substring(LN.length()-1);
		if(first.equals("a"))PN--; //real L Num
		return PN;
	}
	public int colornum(String col) {
		switch(col) {
		case "RD1":
			return 1;
		case "RD2":
			return 2;
		case "RL1":
			return 3;
		case "RL2":
			return 4;
		case "GD1":
			return 5;
		case "GD2":
			return 6;
		case "GL1":
			return 7;
		case "GL2":
			return 8;
		case "BD1":
			return 9;
		case "BD2":
			return 10;
		case "BL1":
			return 11;
		case "BL2":
			return 12;
		case "DF1":
			return 13;
		case "DF2":
			return 14;
		case "RD":
			return 1;
		case "RL":
			return 2;
		case "GD":
			return 3;
		case "GL":
			return 4;
		case "BD":
			return 5;
		case "BL":
			return 6;
		default: 
			System.out.println("Unkown color block "+col);
			return (Integer) null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(){
		String driver = "org.mariadb.jdbc.Driver";
		Connection conn;
		PreparedStatement pstmt;
		ResultSet rs;
		int ModeNum = 0;
		DataBase ModeNumber = new DataBase();
		switch(u_msg.get("MsgName").toString()) {
		case "ModeChangeSet":
			ModeNum = ModeNumber.modeNum(u_msg.get("Mode").toString());
			break;
		case "ModeRestoreSet":
			DataBase LM = new DataBase();
			ModeNum = ModeNumber.modeNum(LM.lastMode());
			break;
		}
		int ST = 1000;
		System.out.println(u_msg.toString()+u_ip+u_port);

		switch(u_msg.get("MsgName").toString()) {		
		case "ModeChangeSet":			
			System.out.println("Mode ID: "+ModeNum);
			obj.put("MsgName","SetResult");
			obj.put("ResponseValue",1);
			obj.put("Reserved",null);
			try {
				Class.forName(driver);
				DataBase RC = new DataBase();
				conn = RC.returnConn();
				if (conn != null) {
					System.out.println("DB 연결 성공");
				}
				String query;

				//Sort 차수
				System.out.println("Sort차수");
				String[] FirstSort = null;
				String[] SecondSort = null;
				String[] ThirdSort = null;
				int[] sortOrder = new int[u_num.SortBoardNum*2];

				query = "SELECT COUNT(*) AS num FROM tb_ultima_sorting WHERE sorting_Order = '1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				FirstSort= new String[rs.getInt("num")];

				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = '1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				int Count=0;
				while(rs.next()) {
					FirstSort[Count]=rs.getString("sorting_Chute");
					sortOrder[LogicNumtoPyshicNum(rs.getString("sorting_Chute"))-1]=0;
					Count++;
				}

				query = "SELECT COUNT(*) AS num FROM tb_ultima_sorting WHERE sorting_Order = '2'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				SecondSort= new String[rs.getInt("num")];

				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = '2'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				Count=0;
				while(rs.next()) {
					SecondSort[Count]=rs.getString("sorting_Chute");
					sortOrder[LogicNumtoPyshicNum(rs.getString("sorting_Chute"))-1]=1;
					Count++;
				}

				query = "SELECT COUNT(*) AS num FROM tb_ultima_sorting WHERE sorting_Order = '3'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				ThirdSort= new String[rs.getInt("num")];

				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = '3'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				Count=0;
				while(rs.next()) {
					ThirdSort[Count]=rs.getString("sorting_Chute");
					sortOrder[LogicNumtoPyshicNum(rs.getString("sorting_Chute"))-1]=2;
					Count++;
				}

				String[][] sortNumOrder= new String[3][];
				sortNumOrder[0] = new String[FirstSort.length];
				sortNumOrder[1] = new String[SecondSort.length];
				sortNumOrder[2] = new String[ThirdSort.length];
				System.arraycopy(FirstSort, 0, sortNumOrder[0], 0, sortNumOrder[0].length);
				System.arraycopy(SecondSort, 0, sortNumOrder[1], 0, sortNumOrder[1].length);
				System.arraycopy(ThirdSort, 0, sortNumOrder[2], 0, sortNumOrder[2].length);

				// LampBGSet
				System.out.println("LampBGSet");
				TimeOut=1000;
				int BGC = 0;
				byte[] UseStatus = new byte[18];
				byte[] Brightness = new byte[18];
				for(int i=0; i<18;i++) {
					query = "SELECT led_UseStatus, led_Brightness FROM tb_led WHERE led_mode_ID = ? AND led_FrontRear=? AND led_Location = ? AND led_Color=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, ModeNum);
					pstmt.setInt(2, i/9);
					pstmt.setInt(3, i/3%3);
					pstmt.setInt(4, i%3);
					rs = pstmt.executeQuery();
					while(rs.next()) {
						UseStatus[BGC]=(byte) rs.getInt("led_UseStatus");
						Brightness[BGC]=(byte) rs.getInt("led_Brightness");
						BGC++;
					}
				}
				byte[] onOffSet1 = {0x02,0,0,84,3,1,0,2,2,0,(byte)u_id,0,0,0,0,18,
						0,0x10, 0,UseStatus[0], 0,0x20, 0,Brightness[0],
						0,0x11, 0,UseStatus[1], 0,0x21, 0,Brightness[1],
						0,0x12, 0,UseStatus[2], 0,0x22, 0,Brightness[2],
						0,0x13, 0,UseStatus[3], 0,0x23, 0,Brightness[3],
						0,0x14, 0,UseStatus[4], 0,0x24, 0,Brightness[4],
						0,0x15, 0,UseStatus[5], 0,0x25, 0,Brightness[5],
						0,0x16, 0,UseStatus[6], 0,0x26, 0,Brightness[6],
						0,0x17, 0,UseStatus[7], 0,0x27, 0,Brightness[7],
						0,0x18, 0,UseStatus[8], 0,0x28, 0,Brightness[8],
						0x03,0,0,0};
				onOffSet1[3]=(byte) (onOffSet1.length-8);
				onOffSet1[15]=(byte) ((onOffSet1.length-20)/4);
				recvmsg=sendToBoard( onOffSet1, u_num.LightingBoardIP[0]);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Light-"+1);
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				byte[] onOffSet2 = {0x02,0,0,84,3,2,0,2,2,0,(byte)u_id,0,0,0,0,18,
						0,0x10, 0,UseStatus[9], 0,0x20, 0,Brightness[9],
						0,0x11, 0,UseStatus[10], 0,0x21, 0,Brightness[10],
						0,0x12, 0,UseStatus[11], 0,0x22, 0,Brightness[11],
						0,0x13, 0,UseStatus[12], 0,0x23, 0,Brightness[12],
						0,0x14, 0,UseStatus[13], 0,0x24, 0,Brightness[13],
						0,0x15, 0,UseStatus[14], 0,0x25, 0,Brightness[14],
						0,0x16, 0,UseStatus[15], 0,0x26, 0,Brightness[15],
						0,0x17, 0,UseStatus[16], 0,0x27, 0,Brightness[16],
						0,0x18, 0,UseStatus[17], 0,0x28, 0,Brightness[17],
						0x03,0,0,0};
				onOffSet2[3]=(byte) (onOffSet2.length-8);
				onOffSet2[15]=(byte) ((onOffSet2.length-20)/4);
				recvmsg=sendToBoard( onOffSet2, u_num.LightingBoardIP[1]);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Light-"+2);
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				TimeOut=1000;
				Thread.sleep(ST);
				// IntBGAngleSet
				System.out.println("IntBGAngleSet");
				query = "SELECT mode_RearCamAngle, mode_FrontCamAngle FROM tb_mode WHERE mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				rs.next();

				String FCamAngle= new String();
				String RCamAngle= new String();
				int[] Angle= new int[4];
				FCamAngle=Integer.toHexString(rs.getInt("mode_RearCamAngle"));
				RCamAngle=Integer.toHexString(rs.getInt("mode_FrontCamAngle"));
				TimeOut=10000;
				if(FCamAngle.length()<=2) {
					Angle[1]=0;
					Angle[0]=Integer.parseInt(FCamAngle,16);
				}else {
					if(FCamAngle.length()==3) {
						Angle[1]=Integer.parseInt(FCamAngle.substring(0,1),16);;
						Angle[0]=Integer.parseInt(FCamAngle.substring(1),16);
					}else {
						Angle[1]=Integer.parseInt(FCamAngle.substring(0,2),16);;
						Angle[0]=Integer.parseInt(FCamAngle.substring(2),16);
					}
				}
				if(RCamAngle.length()<=2) {
					Angle[3]=0;
					Angle[2]=Integer.parseInt(RCamAngle,16);
				}else {
					if(RCamAngle.length()==3) {
						Angle[3]=Integer.parseInt(RCamAngle.substring(0,1),16);;
						Angle[2]=Integer.parseInt(RCamAngle.substring(1),16);
					}else {
						Angle[3]=Integer.parseInt(RCamAngle.substring(0,2),16);;
						Angle[2]=Integer.parseInt(RCamAngle.substring(2),16);
					}
				}
				byte[] BGA = {0x02,0,0,28, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,4,
						0x0, 0x73, 0, (byte) Angle[3],
						0x0, 0x72, 0, (byte) Angle[2],
						0x0, 0x71, 0, (byte) Angle[1],
						0x0, 0x70, 0, (byte) Angle[0], 0x03,0,0,0};
				recvmsg=sendToBoard( BGA, u_num.IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				TimeOut = 1000;
				Thread.sleep(ST);
				// HeaterSet
				System.out.println("HeaterSet");
				query = "SELECT maintenance_HeaterStatus FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				byte HS = (byte) rs.getInt("maintenance_HeaterStatus");

				query = "SELECT maintenance_HeaterTemp FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				byte[] heaterTempSet = {0x02,0,0,20, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,2, 0x0, 0x30, 0,HS ,0x0, 0x31, 0, (byte) rs.getInt("maintenance_HeaterTemp"),0x03,0,0,0};
				heaterTempSet[3]=(byte) (heaterTempSet.length-8);
				heaterTempSet[15]=(byte) ((heaterTempSet.length-20)/4);
				recvmsg=sendToBoard( heaterTempSet, u_num.IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				Thread.sleep(ST);
				//WaterValveSet
				System.out.println("WaterValveSet");
				query = "SELECT maintenance_WaterValve FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				byte[] WaterValueSet = {0x02,0,0,16, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,1,0x0, 0x40, 0, (byte) rs.getInt("maintenance_WaterValve"),0x03,0,0,0};
				recvmsg=sendToBoard( WaterValueSet, u_num.IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				Thread.sleep(ST);


				// Intigreated_INPUT설정
				System.out.println("Intigreated_INPUT");
				query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Integrated' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int DCSW1 = rs.getInt("switch_SwitchStatus");

				query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Integrated' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW2'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int DCSW2 = rs.getInt("switch_SwitchStatus");

				query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Integrated' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'AC SW1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int ACSW1 = rs.getInt("switch_SwitchStatus");

				byte[] InputSet = {0x02,0,0,24, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,3,0x0, 0x50, 0, (byte) DCSW1,0x0, 0x51, 0, (byte) DCSW2,0x0, 0x52, 0, (byte) ACSW1,0x03,0,0,0};
				recvmsg = sendToBoard(InputSet, IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				Thread.sleep(ST);

				// FeedStartStop
				System.out.println("FeedStartStop");
				for (int i = 0; i < num.FeedBoardNum; i++) {
					byte[] FSmsg = { 0x02, 0, 0, 16, 2, (byte) (i + 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x0, 0x10, 0,	0, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(FSmsg, FeedBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {
						ErrorBoard.add("Feed-"+(i+1));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}
				if(num.FeedBoardNum==1) {
					Thread.sleep(ST);
				}
				//FeedSet+ADV
				System.out.println("FeedSet+ADV");
				query = "SELECT feeder_Intensity, feeder_AutoUseStatus FROM tb_feeder WHERE feeder_mode_ID=? ORDER BY feeder_order_Num ASC";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				int[] Intensity = new int[3];
				int[] AutoStatus = new int[3];
				int order_num=0;
				while(rs.next()) {
					Intensity[order_num]=rs.getInt("feeder_Intensity");
					AutoStatus[order_num]=rs.getInt("feeder_AutoUseStatus");
					order_num++;
				}
				
				//FeedAdSet
				System.out.println("FeedAdSet");	
				int[] FON = sortOrder;

				query = "SELECT advanced_FeederNum, advanced_UseStatus, advanced_Offset FROM tb_feeder_advanced WHERE advanced_mode_ID=?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();	

				byte[] FUS = new byte[u_num.FeedPortNum];
				byte[] FOS = new byte[u_num.FeedPortNum];
				
				for(int i=0;i<u_num.FeedPortNum;i++) {
					rs.next();
					FUS[rs.getInt("advanced_FeederNum")-1] = (byte) rs.getInt("advanced_UseStatus");
					FOS[rs.getInt("advanced_FeederNum")-1] = (byte) rs.getInt("advanced_Offset");				
				}	
				byte[] FASmsg = new byte[300];
				byte[] FASet = {0x02, 0, 0, 100, 2, 1, 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 100};
				System.arraycopy(FASet, 0, FASmsg, 0, FASet.length);
				if(u_num.FeedPortNum<=7) {
					for(int i=0;i<u_num.FeedPortNum;i++) {
						FASmsg[i*12+16]=0;
						FASmsg[i*12+17]=(byte) (0x20+i);
						FASmsg[i*12+18]=0;
						FASmsg[i*12+19]=FUS[i];
						FASmsg[i*12+20]=0;
						FASmsg[i*12+21]=(byte) (0x30+i);
						FASmsg[i*12+22]=0;
						FASmsg[i*12+23]=(byte) Intensity[FON[i]];
						FASmsg[i*12+24]=0;
						FASmsg[i*12+25]=(byte) (0x40+i);
						FASmsg[i*12+26]=0;
						FASmsg[i*12+27]=FOS[i];
						FASmsg[i*12+28]=0;
						FASmsg[i*12+29]=0x11;
						FASmsg[i*12+30]=0;
						FASmsg[i*12+31]=(byte) AutoStatus[FON[i]];
						FASmsg[i*12+32]=0x03;
						FASmsg[i*12+33]=0;
						FASmsg[i*12+34]=0;
						FASmsg[i*12+35]=0;
					}
					byte[] FASend = new byte[20+u_num.FeedPortNum*3*4+4/*Auto 수*/];
					System.arraycopy(FASmsg, 0, FASend, 0, FASend.length);
					FASend[3]=(byte) (FASend.length-8);
					FASend[15]=(byte) ((FASend.length-20)/4);
					recvmsg = sendToBoard(FASend, FeedBoardIP[0]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-1");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
				}else {
					int FAC=0;
					for(int j=0;j<7;j++) {
						FASmsg[j*12+16]=0;
						FASmsg[j*12+17]=(byte) (0x20+j);
						FASmsg[j*12+18]=0;
						FASmsg[j*12+19]=FUS[j];
						FASmsg[j*12+20]=0;
						FASmsg[j*12+21]=(byte) (0x30+j);
						FASmsg[j*12+22]=0;
						FASmsg[j*12+23]=(byte) Intensity[FON[j]];
						FASmsg[j*12+24]=0;
						FASmsg[j*12+25]=(byte) (0x40+j);
						FASmsg[j*12+26]=0;
						FASmsg[j*12+27]=FOS[j];
						FASmsg[j*12+28]=0;
						FASmsg[j*12+29]=0x11;
						FASmsg[j*12+30]=0;
						FASmsg[j*12+31]=(byte) AutoStatus[FON[j]];
						FASmsg[j*12+32]=0x03;
						FASmsg[j*12+33]=0;
						FASmsg[j*12+34]=0;
						FASmsg[j*12+35]=0;
						FAC++;
					}
					byte[] FA1Send = new byte[20+FAC*3*4+4/*Auto 수*/];
					System.arraycopy(FASmsg, 0, FA1Send, 0, FA1Send.length);
					FA1Send[3]=(byte) (FA1Send.length-8);
					FA1Send[15]=(byte) ((FA1Send.length-20)/4);
					recvmsg = sendToBoard(FA1Send, FeedBoardIP[0]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-1");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
					for(int j=0;j<u_num.FeedPortNum-7;j++) {
						FASmsg[j*12+16]=0;
						FASmsg[j*12+17]=(byte) (0x20+j);
						FASmsg[j*12+18]=0;
						FASmsg[j*12+19]=FUS[j];
						FASmsg[j*12+20]=0;
						FASmsg[j*12+21]=(byte) (0x30+j);
						FASmsg[j*12+22]=0;
						FASmsg[j*12+23]=(byte) Intensity[FON[j]];
						FASmsg[j*12+24]=0;
						FASmsg[j*12+25]=(byte) (0x40+j);
						FASmsg[j*12+26]=0;
						FASmsg[j*12+27]=FOS[j];		
						FASmsg[j*12+28]=0;
						FASmsg[j*12+29]=0x11;
						FASmsg[j*12+30]=0;
						FASmsg[j*12+31]=(byte) AutoStatus[FON[j]];
						FASmsg[j*12+32]=0x03;
						FASmsg[j*12+33]=0;
						FASmsg[j*12+34]=0;
						FASmsg[j*12+35]=0;
					}
					byte[] FA2Send = new byte[20+(u_num.FeedPortNum-FAC)*3*4+4/*Auto 수*/];
					System.arraycopy(FASmsg, 0, FA2Send, 0, FA2Send.length);						
					FA2Send[3]=(byte) (FA2Send.length-8);
					FA2Send[5]=2;
					FA2Send[15]=(byte) ((FA2Send.length-20)/4);
					recvmsg = sendToBoard(FA2Send, FeedBoardIP[1]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-2");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}				
				}
				if(num.FeedBoardNum==1) {
					Thread.sleep(ST);
				}
				//AirPressSet
				System.out.println("AirPressSet");
				query = "SELECT maintenance_AirCheckLT, maintenance_AirCheckGT, maintenance_AirCheckAlarm FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int LValue=(int)(rs.getFloat("maintenance_AirCheckLT")*10);
				int GValue=(int)(rs.getFloat("maintenance_AirCheckGT")*10);
				int AirOnOff=rs.getInt("maintenance_AirCheckAlarm");
				for(int i=0;i<u_num.FeedBoardNum;i++) {
					byte[] ASmsg = { 0x02, 0, 0, 24, 2, (byte) (i+ 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 3, 0x00,0x51,0x00, (byte) LValue,0x00,0x52,0x00, (byte) GValue, 0x00,0x50,0x00, (byte) AirOnOff, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg = sendToBoard(ASmsg, FeedBoardIP[i]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(i+1));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}
				if(num.FeedBoardNum==1) {
					Thread.sleep(ST);
				}
				// FeedSwitchInSet
				System.out.println("FeedSwitchInSet");
				for(int i=0;i<num.FeedBoardNum;i++) {
					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW1' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FDCSW1=rs.getInt("switch_SwitchStatus");

					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW2' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FDCSW2=rs.getInt("switch_SwitchStatus");

					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'AC SW1' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FACSW1=rs.getInt("switch_SwitchStatus");

					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'AC SW2' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FACSW2=rs.getInt("switch_SwitchStatus");

					byte[] FSmsg = { 0x02, 0, 0, 28, 2, (byte) (i+ 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 4, 0x00,0x60,0x00, (byte) FDCSW1,0x00,0x61,0x00, (byte) FDCSW2, 0x00,0x62,0x00, (byte) FACSW1,  0x00,0x63,0x00, (byte) FACSW2, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(FSmsg, FeedBoardIP[i]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(i+1));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}			
				
				//AllEjectStartStopSet
				System.out.println("AllEjectStartStopSet");
				Thread.sleep(2000);
				for(int i=0;i<num.SortBoardNum;i++) {
					byte[] msg = {0x02,0,0,16,4,(byte) (i+1),0,2,2,0,(byte)u_id,0,0,0,0,1,0x20,0x40,0, 0,0x03,0,0,0};	//바이트배열
					recvmsg = sendToBoard(msg, num.SortBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {
						ErrorBoard.add("Sort-"+Integer.toString(i+1));
						obj.put("NumOfErrorBoard", 0);
						obj.put("ResponseValue", 0);
					}
				}
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				TimeOut=3000;
				//CameraSet
				System.out.println("CameraSet"); 
				query = "SELECT * FROM tb_camera WHERE camera_mode_ID = ? AND camera_CameraNum IN(select sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order !=0)"; 
				pstmt = conn.prepareStatement(query); 
				pstmt.setInt(1, ModeNum); 
				rs = pstmt.executeQuery();
				int[] camerFR=new int[num.SortBoardNum*4];
				String[] camerNo=new String[num.SortBoardNum*4];
				int[] camerPo=new int[num.SortBoardNum*4];
				int count=0;
				for(int i=0;i<num.SortBoardNum*4;i++) {
					rs.next();
					camerFR[count]=rs.getInt("camera_CameraFrontRear");
					camerNo[count]=rs.getString("camera_CameraNum");
					camerPo[count]=rs.getInt("camera_CameraPower");

					int FR=rs.getInt("camera_CameraFrontRear");
					int CamNum=	LogicNumtoPyshicNum(rs.getString("camera_CameraNum"));
					count++;

				}
				for(int i=0;i<num.SortBoardNum*4;i++) {
					if(i%4==0) {
						byte[] msg ={0x02,0,0,28,4,(byte) ((LogicNumtoPyshicNum(camerNo[i])+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,4,
								(byte)((0x10*camerFR[i])+(LogicNumtoPyshicNum(camerNo[i])+1)%2),0x00,0,(byte)camerPo[i], 
								(byte)((0x10*camerFR[i+1])+(LogicNumtoPyshicNum(camerNo[i+1])+1)%2),0x00,0,(byte)camerPo[i+1], 
								(byte)((0x10*camerFR[i+2])+(LogicNumtoPyshicNum(camerNo[i+2])+1)%2),0x00,0,(byte)camerPo[i+2], 
								(byte)((0x10*camerFR[i+3])+(LogicNumtoPyshicNum(camerNo[i+3])+1)%2),0x00,0,(byte)camerPo[i+3],
								0x03,0,0,0}; //바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[((LogicNumtoPyshicNum(camerNo[i])+1)/2)-1]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString(((LogicNumtoPyshicNum(camerNo[i])+1)/2))); 
						}
					}
				}
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				//CameraRGBSet
				System.out.println("CameraRGBSet");
				byte RD;
				byte RL;
				byte BD;
				byte BL;
				byte GD;
				byte GL;

				byte[] dif = new byte[6];
				byte[] difsub = new byte[6];
				byte[] RGB= new byte[6];

				query = "SELECT *  FROM tb_camera_rgb WHERE camera_rgb_mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				count=0;
				while(rs.next()) {			
					String FC = rs.getString("camera_rgb_FirstCombination");
					String SC = rs.getString("camera_rgb_SecondCombination");
					String combi = FC+"/"+SC;
					if (combi=="BL/RD")dif[count]=1;
					else if(combi=="BD/RL")dif[count]=2;
					else if(combi=="GL/BD")dif[count]=4;
					else if(combi=="GD/BL")dif[count]=8;
					else if(combi=="RL/GD")dif[count]=16;
					else if(combi=="RD/GL")dif[count]=31;

					String DL = rs.getString("camera_rgb_DarkLight");
					if(DL=="With Dark")difsub[count]=1;
					else if(DL=="Without Dark")difsub[count]=2;
					else if(DL=="With Light")difsub[count]=4;
					else if(DL=="Without Light")difsub[count]=8;


					RD=(byte) rs.getInt("camera_rgb_RDUseStatus");						
					RL=(byte) rs.getInt("camera_rgb_RLUseStatus");						
					GD=(byte) rs.getInt("camera_rgb_GDUseStatus");						
					GL=(byte) rs.getInt("camera_rgb_GLUseStatus");						
					BD=(byte) rs.getInt("camera_rgb_BDUseStatus");						
					BL=(byte) rs.getInt("camera_rgb_BLUseStatus");

					if(RD==1)RGB[count]=(byte) (RGB[count]+1);
					if(RL==1)RGB[count]=(byte) (RGB[count]+2);
					if(GD==1)RGB[count]=(byte) (RGB[count]+4);
					if(GL==1)RGB[count]=(byte) (RGB[count]+8);
					if(BD==1)RGB[count]=(byte) (RGB[count]+16);
					if(BL==1)RGB[count]=(byte) (RGB[count]+31);					
					count++;
				}			
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					if(i%2==0) {
						int lNum=(i+1)/2;
						byte[] msg = { 0x02, 0, 0, 60, 4, (byte) (lNum+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 12,
								0x00,0x10,0x00,(byte) RGB[sortOrder[i]],
								0x00,0x11,0x00,(byte)dif[sortOrder[i]],
								0x00,0x12,0x00,(byte) difsub[sortOrder[i]],

								0x01,0x10,0x00,(byte) RGB[sortOrder[i+1]],
								0x01,0x11,0x00,(byte)dif[sortOrder[i+1]],
								0x01,0x12,0x00,(byte) difsub[sortOrder[i+1]],

								0x10,0x10,0x00,(byte) RGB[sortOrder[i]],
								0x10,0x11,0x00,(byte)dif[sortOrder[i]],
								0x10,0x12,0x00,(byte) difsub[sortOrder[i]],

								0x11,0x10,0x00,(byte) RGB[sortOrder[i+1]],
								0x11,0x11,0x00,(byte)dif[sortOrder[i+1]],
								0x11,0x12,0x00,(byte) difsub[sortOrder[i+1]],
								0x03, 0, 0, 0 }; // 바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[lNum]);
						if (recvmsg == null || recvmsg[9] == 0) {
							ErrorBoard.add("Sort-"+(lNum/2));
							obj.put("ResponseValue",0);
							obj.put("Reserved",0);
						}
					}
				}				
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				// SensColorSet
				System.out.println("SensColorSet");

				int[][][] use = new int [3][2][12];
				int[][][] val = new int [3][2][12];
				int[][][] size = new int [3][2][12];	
				int[][][] dfUse = new int [3][2][2];
				int[][][] dfVal = new int [3][2][2];
				int[][] dfSize = new int [3][2];

				query = "SELECT * FROM tb_color_combination WHERE combination_mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();	
				while(rs.next()) {
					dfUse[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][0]=rs.getInt("combination_FirstBlockOnOff");
					dfUse[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][1]=rs.getInt("combination_SecondBlockOnOff");
					dfVal[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][0]=rs.getInt("combination_ColorValue");
					dfVal[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][1]=rs.getInt("combination_DarkLightValue");
					dfSize[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")]=rs.getInt("combination_PixelSize");
				}
				
				query = "SELECT * FROM tb_color WHERE color_mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();	
				while(rs.next()) {
					use[rs.getInt("color_order_Num")-1][rs.getInt("color_CameraFrontRear")][colornum(rs.getString("color_BlockName"))-1]=rs.getInt("color_BlockStatus");
					val[rs.getInt("color_order_Num")-1][rs.getInt("color_CameraFrontRear")][colornum(rs.getString("color_BlockName"))-1]=rs.getInt("color_Value");
					size[rs.getInt("color_order_Num")-1][rs.getInt("color_CameraFrontRear")][colornum(rs.getString("color_BlockName"))-1]=rs.getInt("color_PixelSize");
				}				
				TimeOut=10000;
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					//길이 664바이트 ==>  724 10.8,
					if(i%2==0) {
						//byte[] msg = {0x02,0,0x0010,(byte) 152,4,(byte) (i/2+1),0,2,2,0,(byte)u_id,0,0,0,0,(byte) 164,
						
						byte[] msg = {0x02,0,0x02,(byte) 0x9c,4,(byte) (i/2+1),0,2,2,0,(byte)u_id,0,0,0,0,(byte) 164,
						//byte[] msg = {0x02,0,0,(byte) 66,4,(byte) (i/2+1),0,2,2,0,(byte)u_id,0,0,0,0,(byte) 14,
								0x00,0x20,0x00,(byte) use[sortOrder[i]][0][0],
								0x00,0x21,0x00,(byte) use[sortOrder[i]][0][1],
								0x00,0x22,0x00,(byte) use[sortOrder[i]][0][2],
								0x00,0x23,0x00,(byte) use[sortOrder[i]][0][3],
								0x00,0x24,0x00,(byte) use[sortOrder[i]][0][4],
								0x00,0x25,0x00,(byte) use[sortOrder[i]][0][5],
								0x00,0x26,0x00,(byte) use[sortOrder[i]][0][6],
								0x00,0x27,0x00,(byte) use[sortOrder[i]][0][7],
								0x00,0x28,0x00,(byte) use[sortOrder[i]][0][8],
								0x00,0x29,0x00,(byte) use[sortOrder[i]][0][9],
								0x00,0x2a,0x00,(byte) use[sortOrder[i]][0][10],
								0x00,0x2b,0x00,(byte) use[sortOrder[i]][0][11],
								0x00,0x2c,0x00,(byte) dfUse[sortOrder[i]][0][0],
								0x00,0x2d,0x00,(byte) dfUse[sortOrder[i]][0][1],

								
								0x01,0x20,0x00,(byte) use[sortOrder[i+1]][0][0],
								0x01,0x21,0x00,(byte) use[sortOrder[i+1]][0][1],
								0x01,0x22,0x00,(byte) use[sortOrder[i+1]][0][2],
								0x01,0x23,0x00,(byte) use[sortOrder[i+1]][0][3],
								0x01,0x24,0x00,(byte) use[sortOrder[i+1]][0][4],
								0x01,0x25,0x00,(byte) use[sortOrder[i+1]][0][5],
								0x01,0x26,0x00,(byte) use[sortOrder[i+1]][0][6],
								0x01,0x27,0x00,(byte) use[sortOrder[i+1]][0][7],
								0x01,0x28,0x00,(byte) use[sortOrder[i+1]][0][8],
								0x01,0x29,0x00,(byte) use[sortOrder[i+1]][0][9],
								0x01,0x2a,0x00,(byte) use[sortOrder[i+1]][0][10],
								0x01,0x2b,0x00,(byte) use[sortOrder[i+1]][0][11],
								0x01,0x2c,0x00,(byte) dfUse[sortOrder[i+1]][0][0],
								0x01,0x2d,0x00,(byte) dfUse[sortOrder[i+1]][0][1],

								0x10,0x20,0x00,(byte) use[sortOrder[i]][1][0],
								0x10,0x21,0x00,(byte) use[sortOrder[i]][1][1],
								0x10,0x22,0x00,(byte) use[sortOrder[i]][1][2],
								0x10,0x23,0x00,(byte) use[sortOrder[i]][1][3],
								0x10,0x24,0x00,(byte) use[sortOrder[i]][1][4],
								0x10,0x25,0x00,(byte) use[sortOrder[i]][1][5],
								0x10,0x26,0x00,(byte) use[sortOrder[i]][1][6],
								0x10,0x27,0x00,(byte) use[sortOrder[i]][1][7],
								0x10,0x28,0x00,(byte) use[sortOrder[i]][1][8],
								0x10,0x29,0x00,(byte) use[sortOrder[i]][1][9],
								0x10,0x2a,0x00,(byte) use[sortOrder[i]][1][10],
								0x10,0x2b,0x00,(byte) use[sortOrder[i]][1][11],
								0x10,0x2c,0x00,(byte) dfUse[sortOrder[i]][1][0],
								0x10,0x2d,0x00,(byte) dfUse[sortOrder[i]][1][1],

								0x11,0x20,0x00,(byte) use[sortOrder[i+1]][1][0],
								0x11,0x21,0x00,(byte) use[sortOrder[i+1]][1][1],
								0x11,0x22,0x00,(byte) use[sortOrder[i+1]][1][2],
								0x11,0x23,0x00,(byte) use[sortOrder[i+1]][1][3],
								0x11,0x24,0x00,(byte) use[sortOrder[i+1]][1][4],
								0x11,0x25,0x00,(byte) use[sortOrder[i+1]][1][5],
								0x11,0x26,0x00,(byte) use[sortOrder[i+1]][1][6],
								0x11,0x27,0x00,(byte) use[sortOrder[i+1]][1][7],
								0x11,0x28,0x00,(byte) use[sortOrder[i+1]][1][8],
								0x11,0x29,0x00,(byte) use[sortOrder[i+1]][1][9],
								0x11,0x2a,0x00,(byte) use[sortOrder[i+1]][1][10],
								0x11,0x2b,0x00,(byte) use[sortOrder[i+1]][1][11],
								0x11,0x2c,0x00,(byte) dfUse[sortOrder[i+1]][0][0],
								0x11,0x2d,0x00,(byte) dfUse[sortOrder[i+1]][0][1],

								0x00,0x30,0x00,(byte) val[sortOrder[i]][0][0],
								0x00,0x31,0x00,(byte) val[sortOrder[i]][0][1],
								0x00,0x32,0x00,(byte) val[sortOrder[i]][0][2],
								0x00,0x33,0x00,(byte) val[sortOrder[i]][0][3],
								0x00,0x34,0x00,(byte) val[sortOrder[i]][0][4],
								0x00,0x35,0x00,(byte) val[sortOrder[i]][0][5],
								0x00,0x36,0x00,(byte) val[sortOrder[i]][0][6],
								0x00,0x37,0x00,(byte) val[sortOrder[i]][0][7],
								0x00,0x38,0x00,(byte) val[sortOrder[i]][0][8],
								0x00,0x39,0x00,(byte) val[sortOrder[i]][0][9],
								0x00,0x3a,0x00,(byte) val[sortOrder[i]][0][10],
								0x00,0x3b,0x00,(byte) val[sortOrder[i]][0][11],
								0x00,0x3c,0x00,(byte) dfVal[sortOrder[i]][0][0],
								0x00,0x3d,0x00,(byte) dfVal[sortOrder[i]][0][1],								


								0x01,0x30,0x00,(byte) val[sortOrder[i+1]][0][0],
								0x01,0x31,0x00,(byte) val[sortOrder[i+1]][0][1],
								0x01,0x32,0x00,(byte) val[sortOrder[i+1]][0][2],
								0x01,0x33,0x00,(byte) val[sortOrder[i+1]][0][3],
								0x01,0x34,0x00,(byte) val[sortOrder[i+1]][0][4],
								0x01,0x35,0x00,(byte) val[sortOrder[i+1]][0][5],
								0x01,0x36,0x00,(byte) val[sortOrder[i+1]][0][6],
								0x01,0x37,0x00,(byte) val[sortOrder[i+1]][0][7],
								0x01,0x38,0x00,(byte) val[sortOrder[i+1]][0][8],
								0x01,0x39,0x00,(byte) val[sortOrder[i+1]][0][9],
								0x01,0x3a,0x00,(byte) val[sortOrder[i+1]][0][10],
								0x01,0x3b,0x00,(byte) val[sortOrder[i+1]][0][11],
								0x01,0x3c,0x00,(byte) dfVal[sortOrder[i+1]][0][0],
								0x01,0x3d,0x00,(byte) dfVal[sortOrder[i+1]][0][1],


								0x10,0x30,0x00,(byte) val[sortOrder[i]][1][0],
								0x10,0x31,0x00,(byte) val[sortOrder[i]][1][1],
								0x10,0x32,0x00,(byte) val[sortOrder[i]][1][2],
								0x10,0x33,0x00,(byte) val[sortOrder[i]][1][3],
								0x10,0x34,0x00,(byte) val[sortOrder[i]][1][4],
								0x10,0x35,0x00,(byte) val[sortOrder[i]][1][5],
								0x10,0x36,0x00,(byte) val[sortOrder[i]][1][6],
								0x10,0x37,0x00,(byte) val[sortOrder[i]][1][7],
								0x10,0x38,0x00,(byte) val[sortOrder[i]][1][8],
								0x10,0x39,0x00,(byte) val[sortOrder[i]][1][9],
								0x10,0x3a,0x00,(byte) val[sortOrder[i]][1][10],
								0x10,0x3b,0x00,(byte) val[sortOrder[i]][1][11],
								0x10,0x3c,0x00,(byte) dfVal[sortOrder[i]][1][0],
								0x10,0x3d,0x00,(byte) dfVal[sortOrder[i]][1][1],

								0x11,0x30,0x00,(byte) val[sortOrder[i+1]][1][0],
								0x11,0x31,0x00,(byte) val[sortOrder[i+1]][1][1],
								0x11,0x32,0x00,(byte) val[sortOrder[i+1]][1][2],
								0x11,0x33,0x00,(byte) val[sortOrder[i+1]][1][3],
								0x11,0x34,0x00,(byte) val[sortOrder[i+1]][1][4],
								0x11,0x35,0x00,(byte) val[sortOrder[i+1]][1][5],
								0x11,0x36,0x00,(byte) val[sortOrder[i+1]][1][6],
								0x11,0x37,0x00,(byte) val[sortOrder[i+1]][1][7],
								0x11,0x38,0x00,(byte) val[sortOrder[i+1]][1][8],
								0x11,0x39,0x00,(byte) val[sortOrder[i+1]][1][9],
								0x11,0x3a,0x00,(byte) val[sortOrder[i+1]][1][10],
								0x11,0x3b,0x00,(byte) val[sortOrder[i+1]][1][11],
								0x11,0x3c,0x00,(byte) dfVal[sortOrder[i+1]][1][0],
								0x11,0x3d,0x00,(byte) dfVal[sortOrder[i+1]][1][1],

								0x00,0x40,0x00,(byte) size[sortOrder[i]][0][0],
								0x00,0x41,0x00,(byte) size[sortOrder[i]][0][1],
								0x00,0x42,0x00,(byte) size[sortOrder[i]][0][2],
								0x00,0x43,0x00,(byte) size[sortOrder[i]][0][3],
								0x00,0x44,0x00,(byte) size[sortOrder[i]][0][4],
								0x00,0x45,0x00,(byte) size[sortOrder[i]][0][5],
								0x00,0x46,0x00,(byte) size[sortOrder[i]][0][6],
								0x00,0x47,0x00,(byte) size[sortOrder[i]][0][7],
								0x00,0x48,0x00,(byte) size[sortOrder[i]][0][8],
								0x00,0x49,0x00,(byte) size[sortOrder[i]][0][9],
								0x00,0x4a,0x00,(byte) size[sortOrder[i]][0][10],
								0x00,0x4b,0x00,(byte) size[sortOrder[i]][0][11],
								0x00,0x4c,0x00,(byte) dfSize[sortOrder[i]][0],

								0x01,0x40,0x00,(byte) size[sortOrder[i+1]][0][0],
								0x01,0x41,0x00,(byte) size[sortOrder[i+1]][0][1],
								0x01,0x42,0x00,(byte) size[sortOrder[i+1]][0][2],
								0x01,0x43,0x00,(byte) size[sortOrder[i+1]][0][3],
								0x01,0x44,0x00,(byte) size[sortOrder[i+1]][0][4],
								0x01,0x45,0x00,(byte) size[sortOrder[i+1]][0][5],
								0x01,0x46,0x00,(byte) size[sortOrder[i+1]][0][6],
								0x01,0x47,0x00,(byte) size[sortOrder[i+1]][0][7],
								0x01,0x48,0x00,(byte) size[sortOrder[i+1]][0][8],
								0x01,0x49,0x00,(byte) size[sortOrder[i+1]][0][9],
								0x01,0x4a,0x00,(byte) size[sortOrder[i+1]][0][10],
								0x01,0x4b,0x00,(byte) size[sortOrder[i+1]][0][11],
								0x01,0x4c,0x00,(byte) dfSize[sortOrder[i+1]][0],

								0x10,0x40,0x00,(byte) size[sortOrder[i]][1][0],
								0x10,0x41,0x00,(byte) size[sortOrder[i]][1][1],
								0x10,0x42,0x00,(byte) size[sortOrder[i]][1][2],
								0x10,0x43,0x00,(byte) size[sortOrder[i]][1][3],
								0x10,0x44,0x00,(byte) size[sortOrder[i]][1][4],
								0x10,0x45,0x00,(byte) size[sortOrder[i]][1][5],
								0x10,0x46,0x00,(byte) size[sortOrder[i]][1][6],
								0x10,0x47,0x00,(byte) size[sortOrder[i]][1][7],
								0x10,0x48,0x00,(byte) size[sortOrder[i]][1][8],
								0x10,0x49,0x00,(byte) size[sortOrder[i]][1][9],
								0x10,0x4a,0x00,(byte) size[sortOrder[i]][1][10],
								0x10,0x4b,0x00,(byte) size[sortOrder[i]][1][11],
								0x10,0x4c,0x00,(byte) dfSize[sortOrder[i]][1],

								0x11,0x40,0x00,(byte) size[sortOrder[i+1]][1][0],
								0x11,0x41,0x00,(byte) size[sortOrder[i+1]][1][1],
								0x11,0x42,0x00,(byte) size[sortOrder[i+1]][1][2],
								0x11,0x43,0x00,(byte) size[sortOrder[i+1]][1][3],
								0x11,0x44,0x00,(byte) size[sortOrder[i+1]][1][4],
								0x11,0x45,0x00,(byte) size[sortOrder[i+1]][1][5],
								0x11,0x46,0x00,(byte) size[sortOrder[i+1]][1][6],
								0x11,0x47,0x00,(byte) size[sortOrder[i+1]][1][7],
								0x11,0x48,0x00,(byte) size[sortOrder[i+1]][1][8],
								0x11,0x49,0x00,(byte) size[sortOrder[i+1]][1][9],
								0x11,0x4a,0x00,(byte) size[sortOrder[i+1]][1][10],
								0x11,0x4b,0x00,(byte) size[sortOrder[i+1]][1][11],
								0x11,0x4c,0x00,(byte) dfSize[sortOrder[i+1]][1], 

								0x03,0,0,0};	//바이트배열
						
//						String ad = Integer.toBinaryString(msg.length-8);						
//						byte a=(byte) (msg.length-8);
//						byte b=0;
//						if(ad.length()>8) {
//							ad=ad.substring(0,ad.length()-8);							
//							b=(byte) Integer.parseInt(ad,2);
//						}						
//						msg[2]=b;						
//						msg[3]=a;
//						msg[15]=(byte) ((msg.length-20)/4);
						ServerStartMain.Busyflag[i/2]=true;
						recvmsg=sendToBoard(msg, SortBoardIP[i/2]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString(i/2)); 
						}
						ServerStartMain.Busyflag[i/2]=false;
					}
				}
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				TimeOut=3000;
				// SensAdvancedSet
				System.out.println("SensAdvancedSet"); 
				query = "SELECT * FROM tb_color_detail WHERE detail_mode_ID = ? AND detail_CameraNum IN(select sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order !=0)";
				int[][][] factor = new int[num.SortBoardNum*2][2][14]; 
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				while(rs.next()) {
					factor[LogicNumtoPyshicNum(rs.getString("detail_CameraNum"))-1][rs.getInt("detail_CameraFrontRear")][colornum(rs.getString("detail_BlockName"))-1]=rs.getInt("detail_Factor");
				}
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					if(i%2==0) {
						byte[] msg = {0x02,0,0,(byte) 236,4,(byte)(i/2+1),0,2,2,0,(byte)u_id,0,0,0,0,56,

								(byte) 0x00,0x50,0x00,(byte) factor[i][0][0],
								(byte) 0x01,0x50,0x00,(byte) factor[i+1][0][0],								
								(byte) 0x10,0x50,0x00,(byte) factor[i][1][0],
								(byte) 0x11,0x50,0x00,(byte) factor[i+1][1][0],

								(byte) 0x00,0x51,0x00,(byte) factor[i][0][1],
								(byte) 0x01,0x51,0x00,(byte) factor[i+1][0][1],								
								(byte) 0x10,0x51,0x00,(byte) factor[i][1][1],
								(byte) 0x11,0x51,0x00,(byte) factor[i+1][1][1],

								(byte) 0x00,0x52,0x00,(byte) factor[i][0][2],
								(byte) 0x01,0x52,0x00,(byte) factor[i+1][0][2],								
								(byte) 0x10,0x52,0x00,(byte) factor[i][1][2],
								(byte) 0x11,0x52,0x00,(byte) factor[i+1][1][2],

								(byte) 0x00,0x53,0x00,(byte) factor[i][0][3],
								(byte) 0x01,0x53,0x00,(byte) factor[i+1][0][3],								
								(byte) 0x10,0x53,0x00,(byte) factor[i][1][3],
								(byte) 0x11,0x53,0x00,(byte) factor[i+1][1][3],

								(byte) 0x00,0x54,0x00,(byte) factor[i][0][4],
								(byte) 0x01,0x54,0x00,(byte) factor[i+1][0][4],								
								(byte) 0x10,0x54,0x00,(byte) factor[i][1][4],
								(byte) 0x11,0x54,0x00,(byte) factor[i+1][1][4],

								(byte) 0x00,0x52,0x00,(byte) factor[i][0][5],
								(byte) 0x01,0x52,0x00,(byte) factor[i+1][0][5],								
								(byte) 0x10,0x52,0x00,(byte) factor[i][1][5],
								(byte) 0x11,0x52,0x00,(byte) factor[i+1][1][5],

								(byte) 0x00,0x56,0x00,(byte) factor[i][0][6],
								(byte) 0x01,0x56,0x00,(byte) factor[i+1][0][6],								
								(byte) 0x10,0x56,0x00,(byte) factor[i][1][6],
								(byte) 0x11,0x56,0x00,(byte) factor[i+1][1][6],

								(byte) 0x00,0x57,0x00,(byte) factor[i][0][7],
								(byte) 0x01,0x57,0x00,(byte) factor[i+1][0][7],								
								(byte) 0x10,0x57,0x00,(byte) factor[i][1][7],
								(byte) 0x11,0x57,0x00,(byte) factor[i+1][1][7],

								(byte) 0x00,0x58,0x00,(byte) factor[i][0][8],
								(byte) 0x01,0x58,0x00,(byte) factor[i+1][0][8],								
								(byte) 0x10,0x58,0x00,(byte) factor[i][1][8],
								(byte) 0x11,0x58,0x00,(byte) factor[i+1][1][8],

								(byte) 0x00,0x59,0x00,(byte) factor[i][0][9],
								(byte) 0x01,0x59,0x00,(byte) factor[i+1][0][9],								
								(byte) 0x10,0x59,0x00,(byte) factor[i][1][9],
								(byte) 0x11,0x59,0x00,(byte) factor[i+1][1][9],

								(byte) 0x00,0x5a,0x00,(byte) factor[i][0][10],
								(byte) 0x01,0x5a,0x00,(byte) factor[i+1][0][10],								
								(byte) 0x10,0x5a,0x00,(byte) factor[i][1][10],
								(byte) 0x11,0x5a,0x00,(byte) factor[i+1][1][10],

								(byte) 0x00,0x5b,0x00,(byte) factor[i][0][11],
								(byte) 0x01,0x5b,0x00,(byte) factor[i+1][0][11],								
								(byte) 0x10,0x5b,0x00,(byte) factor[i][1][11],
								(byte) 0x11,0x5b,0x00,(byte) factor[i+1][1][11],

								(byte) 0x00,0x5c,0x00,(byte) factor[i][0][12],
								(byte) 0x01,0x5c,0x00,(byte) factor[i+1][0][12],								
								(byte) 0x10,0x5c,0x00,(byte) factor[i][1][12],
								(byte) 0x11,0x5c,0x00,(byte) factor[i+1][1][12],

								(byte) 0x00,0x5d,0x00,(byte) factor[i][0][13],
								(byte) 0x01,0x5d,0x00,(byte) factor[i+1][0][13],								
								(byte) 0x10,0x5d,0x00,(byte) factor[i][1][13],
								(byte) 0x11,0x5d,0x00,(byte) factor[i+1][1][13],								

								0x03,0,0,0}; //바이트배열
						ServerStartMain.Busyflag[i/2]=true;
						recvmsg=sendToBoard(msg, SortBoardIP[i/2]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((i)/2+1)); 
						}
						ServerStartMain.Busyflag[i/2]=false;

					}				
				}				
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				TimeOut=3000;
				//EjectSet 
				System.out.println("EjectSet");
				query = "SELECT ejecting_DelayValue, ejecting_HeadValue, ejecting_HoldValue, ejecting_Order FROM tb_ejecting WHERE ejecting_mode_ID = ? ORDER BY ejecting_Order ASC";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				int[] ejecting_DelayValue=new int[3];
				int[] ejecting_HeadValue=new int[3];
				int[] ejecting_HoldValue=new int[3];
				while(rs.next()) {
					ejecting_DelayValue[rs.getInt("ejecting_Order")-1]=rs.getInt("ejecting_DelayValue");
					ejecting_HeadValue[rs.getInt("ejecting_Order")-1]=rs.getInt("ejecting_HeadValue");
					ejecting_HoldValue[rs.getInt("ejecting_Order")-1]=rs.getInt("ejecting_HoldValue");
				}
				
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					if(i%2==0) {
						byte[] ESmsg = { 0x02, 0, 0, 36, 4, (byte) (i/2+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 6, 
								0x00,(byte)0x80,0x00, (byte) ejecting_DelayValue[sortOrder[i]],
								0x00,(byte)0x90,0x00, (byte) ejecting_HeadValue[sortOrder[i]], 
								0x00,(byte)0x91,0x00, (byte) ejecting_HoldValue[sortOrder[i]], 

								0x01,(byte)0x80,0x00, (byte) ejecting_DelayValue[sortOrder[i+1]],
								0x01,(byte)0x90,0x00, (byte) ejecting_HeadValue[sortOrder[i+1]], 
								0x01,(byte)0x91,0x00, (byte) ejecting_HoldValue[sortOrder[i+1]], 

								0x03, 0, 0, 0 }; // 바이트배열
						ESmsg[3]=(byte) (ESmsg.length-8);
						ESmsg[15]=(byte) ((ESmsg.length-20)/4);
						recvmsg=sendToBoard(ESmsg, SortBoardIP[i/2]);
						if (recvmsg == null || recvmsg[9] == 0) {
							ErrorBoard.add("Sort-"+(i/2+1));
							obj.put("ResponseValue",0);
							obj.put("Reserved",0);
						}			
					}
				}

				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				//CamGOSet 
				//CameraOrder = new String[2][3][max]; // FR/order/camnum
				System.out.println("CamGOSet");
				query = "SELECT * FROM tb_gainoffset WHERE gainoffset_modeID = ? AND gainoffset_CameraNum <= (select maintenance_Channel FROM group_ultima_maintenance)"; 
				pstmt = conn.prepareStatement(query); 
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery(); 
				int[][][] gain = new int [num.SortBoardNum][2][3];
				int[][][] offset = new int [num.SortBoardNum][2][3];
				while(rs.next()) { 
					gain[rs.getInt("gainoffset_CameraNum")-1][rs.getInt("gainoffset_FrontRear")][rs.getInt("gainoffset_Color")]=rs.getInt("gainoffset_Gain");
					offset[rs.getInt("gainoffset_CameraNum")-1][rs.getInt("gainoffset_FrontRear")][rs.getInt("gainoffset_Color")]=rs.getInt("gainoffset_Offset");
				}
				for(int i=0;i<u_num.SortBoardNum;i++) {
					byte[] CGmsg = { 0x02, 0, 0, 60, 4, (byte) (i+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 12, 
							0x02,0x10,0x00,(byte) gain[i][0][0],
							0x02,0x13,0x00,(byte) offset[i][0][0],

							0x02,0x11,0x00,(byte) gain[i][0][1],
							0x02,0x14,0x00,(byte) offset[i][0][1],

							0x02,0x12,0x00,(byte) gain[i][0][2],
							0x02,0x15,0x00,(byte) offset[i][0][2],

							0x12,0x10,0x00,(byte) gain[i][1][0],
							0x12,0x13,0x00,(byte) offset[i][1][0],

							0x12,0x11,0x00,(byte) gain[i][1][1],
							0x12,0x14,0x00,(byte) offset[i][1][1],

							0x12,0x12,0x00,(byte) gain[i][1][2],
							0x12,0x15,0x00,(byte) offset[i][1][2],

							0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(CGmsg, SortBoardIP[i]); 
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(i+1)); 
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}				
				
				obj.put("ErrorBoard", ErrorBoard);
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				rs.close();
				pstmt.close();
				conn.close();

			} catch (SQLException e) {
				System.err.println("DB 연결 오류");
				System.err.println(e.getMessage());
				System.err.println(e);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			break;


		case "ModeRestoreSet":		// *******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************
			System.out.println("Mode ID: "+ModeNum);
			obj.put("MsgName","SetResult");
			obj.put("ResponseValue",1);
			obj.put("Reserved",null);
			try {
				Class.forName(driver);
				DataBase RC = new DataBase();
				conn = RC.returnConn();
				if (conn != null) {
					System.out.println("DB 연결 성공");
				}
				String query;

				//Sort 차수
				System.out.println("Sort차수");
				String[] FirstSort = null;
				String[] SecondSort = null;
				String[] ThirdSort = null;
				int[] sortOrder = new int[u_num.SortBoardNum*2];

				query = "SELECT COUNT(*) AS num FROM tb_ultima_sorting WHERE sorting_Order = '1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				FirstSort= new String[rs.getInt("num")];

				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = '1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				int Count=0;
				while(rs.next()) {
					FirstSort[Count]=rs.getString("sorting_Chute");
					sortOrder[LogicNumtoPyshicNum(rs.getString("sorting_Chute"))-1]=0;
					Count++;
				}

				query = "SELECT COUNT(*) AS num FROM tb_ultima_sorting WHERE sorting_Order = '2'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				SecondSort= new String[rs.getInt("num")];

				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = '2'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				Count=0;
				while(rs.next()) {
					SecondSort[Count]=rs.getString("sorting_Chute");
					sortOrder[LogicNumtoPyshicNum(rs.getString("sorting_Chute"))-1]=1;
					Count++;
				}

				query = "SELECT COUNT(*) AS num FROM tb_ultima_sorting WHERE sorting_Order = '3'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				ThirdSort= new String[rs.getInt("num")];

				query = "SELECT sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order = '3'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				Count=0;
				while(rs.next()) {
					ThirdSort[Count]=rs.getString("sorting_Chute");
					sortOrder[LogicNumtoPyshicNum(rs.getString("sorting_Chute"))-1]=2;
					Count++;
				}

				String[][] sortNumOrder= new String[3][];
				sortNumOrder[0] = new String[FirstSort.length];
				sortNumOrder[1] = new String[SecondSort.length];
				sortNumOrder[2] = new String[ThirdSort.length];
				System.arraycopy(FirstSort, 0, sortNumOrder[0], 0, sortNumOrder[0].length);
				System.arraycopy(SecondSort, 0, sortNumOrder[1], 0, sortNumOrder[1].length);
				System.arraycopy(ThirdSort, 0, sortNumOrder[2], 0, sortNumOrder[2].length);

				// LampBGSet
				System.out.println("LampBGSet");
				TimeOut=1000;
				int BGC = 0;
				byte[] UseStatus = new byte[18];
				byte[] Brightness = new byte[18];
				for(int i=0; i<18;i++) {
					query = "SELECT led_UseStatus, led_Brightness FROM tb_led WHERE led_mode_ID = ? AND led_FrontRear=? AND led_Location = ? AND led_Color=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, ModeNum);
					pstmt.setInt(2, i/9);
					pstmt.setInt(3, i/3%3);
					pstmt.setInt(4, i%3);
					rs = pstmt.executeQuery();
					while(rs.next()) {
						UseStatus[BGC]=(byte) rs.getInt("led_UseStatus");
						Brightness[BGC]=(byte) rs.getInt("led_Brightness");
						BGC++;
					}
				}
				byte[] onOffSet1 = {0x02,0,0,84,3,1,0,2,2,0,(byte)u_id,0,0,0,0,18,
						0,0x10, 0,UseStatus[0], 0,0x20, 0,Brightness[0],
						0,0x11, 0,UseStatus[1], 0,0x21, 0,Brightness[1],
						0,0x12, 0,UseStatus[2], 0,0x22, 0,Brightness[2],
						0,0x13, 0,UseStatus[3], 0,0x23, 0,Brightness[3],
						0,0x14, 0,UseStatus[4], 0,0x24, 0,Brightness[4],
						0,0x15, 0,UseStatus[5], 0,0x25, 0,Brightness[5],
						0,0x16, 0,UseStatus[6], 0,0x26, 0,Brightness[6],
						0,0x17, 0,UseStatus[7], 0,0x27, 0,Brightness[7],
						0,0x18, 0,UseStatus[8], 0,0x28, 0,Brightness[8],
						0x03,0,0,0};
				onOffSet1[3]=(byte) (onOffSet1.length-8);
				onOffSet1[15]=(byte) ((onOffSet1.length-20)/4);
				recvmsg=sendToBoard( onOffSet1, u_num.LightingBoardIP[0]);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Light-"+1);
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				byte[] onOffSet2 = {0x02,0,0,84,3,2,0,2,2,0,(byte)u_id,0,0,0,0,18,
						0,0x10, 0,UseStatus[9], 0,0x20, 0,Brightness[9],
						0,0x11, 0,UseStatus[10], 0,0x21, 0,Brightness[10],
						0,0x12, 0,UseStatus[11], 0,0x22, 0,Brightness[11],
						0,0x13, 0,UseStatus[12], 0,0x23, 0,Brightness[12],
						0,0x14, 0,UseStatus[13], 0,0x24, 0,Brightness[13],
						0,0x15, 0,UseStatus[14], 0,0x25, 0,Brightness[14],
						0,0x16, 0,UseStatus[15], 0,0x26, 0,Brightness[15],
						0,0x17, 0,UseStatus[16], 0,0x27, 0,Brightness[16],
						0,0x18, 0,UseStatus[17], 0,0x28, 0,Brightness[17],
						0x03,0,0,0};
				onOffSet2[3]=(byte) (onOffSet2.length-8);
				onOffSet2[15]=(byte) ((onOffSet2.length-20)/4);
				recvmsg=sendToBoard( onOffSet2, u_num.LightingBoardIP[1]);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Light-"+2);
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				TimeOut=1000;
				Thread.sleep(ST);
				// IntBGAngleSet
				System.out.println("IntBGAngleSet");
				query = "SELECT mode_RearCamAngle, mode_FrontCamAngle FROM tb_mode WHERE mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				rs.next();

				String FCamAngle= new String();
				String RCamAngle= new String();
				int[] Angle= new int[4];
				FCamAngle=Integer.toHexString(rs.getInt("mode_RearCamAngle"));
				RCamAngle=Integer.toHexString(rs.getInt("mode_FrontCamAngle"));
				TimeOut=10000;
				if(FCamAngle.length()<=2) {
					Angle[1]=0;
					Angle[0]=Integer.parseInt(FCamAngle,16);
				}else {
					if(FCamAngle.length()==3) {
						Angle[1]=Integer.parseInt(FCamAngle.substring(0,1),16);;
						Angle[0]=Integer.parseInt(FCamAngle.substring(1),16);
					}else {
						Angle[1]=Integer.parseInt(FCamAngle.substring(0,2),16);;
						Angle[0]=Integer.parseInt(FCamAngle.substring(2),16);
					}
				}
				if(RCamAngle.length()<=2) {
					Angle[3]=0;
					Angle[2]=Integer.parseInt(RCamAngle,16);
				}else {
					if(RCamAngle.length()==3) {
						Angle[3]=Integer.parseInt(RCamAngle.substring(0,1),16);;
						Angle[2]=Integer.parseInt(RCamAngle.substring(1),16);
					}else {
						Angle[3]=Integer.parseInt(RCamAngle.substring(0,2),16);;
						Angle[2]=Integer.parseInt(RCamAngle.substring(2),16);
					}
				}
				byte[] BGA = {0x02,0,0,28, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,4,
						0x0, 0x73, 0, (byte) Angle[3],
						0x0, 0x72, 0, (byte) Angle[2],
						0x0, 0x71, 0, (byte) Angle[1],
						0x0, 0x70, 0, (byte) Angle[0], 0x03,0,0,0};
				recvmsg=sendToBoard( BGA, u_num.IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				TimeOut = 1000;
				Thread.sleep(ST);
				// HeaterSet
				System.out.println("HeaterSet");
				query = "SELECT maintenance_HeaterStatus FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				byte HS = (byte) rs.getInt("maintenance_HeaterStatus");

				query = "SELECT maintenance_HeaterTemp FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				byte[] heaterTempSet = {0x02,0,0,20, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,2, 0x0, 0x30, 0,HS ,0x0, 0x31, 0, (byte) rs.getInt("maintenance_HeaterTemp"),0x03,0,0,0};
				heaterTempSet[3]=(byte) (heaterTempSet.length-8);
				heaterTempSet[15]=(byte) ((heaterTempSet.length-20)/4);
				recvmsg=sendToBoard( heaterTempSet, u_num.IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				Thread.sleep(ST);
				//WaterValveSet
				System.out.println("WaterValveSet");
				query = "SELECT maintenance_WaterValve FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				byte[] WaterValueSet = {0x02,0,0,16, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,1,0x0, 0x40, 0, (byte) rs.getInt("maintenance_WaterValve"),0x03,0,0,0};
				recvmsg=sendToBoard( WaterValueSet, u_num.IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				Thread.sleep(ST);


				// Intigreated_INPUT설정
				System.out.println("Intigreated_INPUT");
				query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Integrated' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int DCSW1 = rs.getInt("switch_SwitchStatus");

				query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Integrated' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW2'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int DCSW2 = rs.getInt("switch_SwitchStatus");

				query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Integrated' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'AC SW1'";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int ACSW1 = rs.getInt("switch_SwitchStatus");

				byte[] InputSet = {0x02,0,0,24, 1, 1,0,2,2,0,(byte)u_id,0,0,0,0,3,0x0, 0x50, 0, (byte) DCSW1,0x0, 0x51, 0, (byte) DCSW2,0x0, 0x52, 0, (byte) ACSW1,0x03,0,0,0};
				recvmsg = sendToBoard(InputSet, IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					ErrorBoard.add("Integrate-1");
					obj.put("ResponseValue",0);
					obj.put("Reserved",0);
				}
				Thread.sleep(ST);

				// FeedStartStop
				System.out.println("FeedStartStop");
				for (int i = 0; i < num.FeedBoardNum; i++) {
					byte[] FSmsg = { 0x02, 0, 0, 16, 2, (byte) (i + 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x0, 0x10, 0,	0, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(FSmsg, FeedBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {
						ErrorBoard.add("Feed-"+(i+1));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}
				if(num.FeedBoardNum==1) {
					Thread.sleep(ST);
				}
				//FeedSet+ADV
				System.out.println("FeedSet+ADV");
				query = "SELECT feeder_Intensity, feeder_AutoUseStatus FROM tb_feeder WHERE feeder_mode_ID=? ORDER BY feeder_order_Num ASC";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				int[] Intensity = new int[3];
				int[] AutoStatus = new int[3];
				int order_num=0;
				while(rs.next()) {
					Intensity[order_num]=rs.getInt("feeder_Intensity");
					AutoStatus[order_num]=rs.getInt("feeder_AutoUseStatus");
					order_num++;
				}
				
				//FeedAdSet
				System.out.println("FeedAdSet");
				TimeOut=2000;
				int[] FON = sortOrder;

				query = "SELECT advanced_FeederNum, advanced_UseStatus, advanced_Offset FROM tb_feeder_advanced WHERE advanced_mode_ID=?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();	

				byte[] FUS = new byte[u_num.FeedPortNum];
				byte[] FOS = new byte[u_num.FeedPortNum];
				
				for(int i=0;i<u_num.FeedPortNum;i++) {
					rs.next();
					FUS[rs.getInt("advanced_FeederNum")-1] = (byte) rs.getInt("advanced_UseStatus");
					FOS[rs.getInt("advanced_FeederNum")-1] = (byte) rs.getInt("advanced_Offset");				
				}	
				byte[] FASmsg = new byte[300];
				byte[] FASet = {0x02, 0, 0, 100, 2, 1, 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 100};
				System.arraycopy(FASet, 0, FASmsg, 0, FASet.length);
				if(u_num.FeedPortNum<=7) {
					for(int i=0;i<u_num.FeedPortNum;i++) {
						FASmsg[i*12+16]=0;
						FASmsg[i*12+17]=(byte) (0x20+i);
						FASmsg[i*12+18]=0;
						FASmsg[i*12+19]=FUS[i];
						FASmsg[i*12+20]=0;
						FASmsg[i*12+21]=(byte) (0x30+i);
						FASmsg[i*12+22]=0;
						FASmsg[i*12+23]=(byte) Intensity[FON[i]];
						FASmsg[i*12+24]=0;
						FASmsg[i*12+25]=(byte) (0x40+i);
						FASmsg[i*12+26]=0;
						FASmsg[i*12+27]=FOS[i];
						FASmsg[i*12+28]=0;
						FASmsg[i*12+29]=0x11;
						FASmsg[i*12+30]=0;
						FASmsg[i*12+31]=(byte) AutoStatus[FON[i]];
						FASmsg[i*12+32]=0x03;
						FASmsg[i*12+33]=0;
						FASmsg[i*12+34]=0;
						FASmsg[i*12+35]=0;
					}
					byte[] FASend = new byte[20+u_num.FeedPortNum*3*4+4/*Auto 수*/];
					System.arraycopy(FASmsg, 0, FASend, 0, FASend.length);
					FASend[3]=(byte) (FASend.length-8);
					FASend[15]=(byte) ((FASend.length-20)/4);
					recvmsg = sendToBoard(FASend, FeedBoardIP[0]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-1");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
				}else {
					int FAC=0;
					for(int j=0;j<7;j++) {
						FASmsg[j*12+16]=0;
						FASmsg[j*12+17]=(byte) (0x20+j);
						FASmsg[j*12+18]=0;
						FASmsg[j*12+19]=FUS[j];
						FASmsg[j*12+20]=0;
						FASmsg[j*12+21]=(byte) (0x30+j);
						FASmsg[j*12+22]=0;
						FASmsg[j*12+23]=(byte) Intensity[FON[j]];
						FASmsg[j*12+24]=0;
						FASmsg[j*12+25]=(byte) (0x40+j);
						FASmsg[j*12+26]=0;
						FASmsg[j*12+27]=FOS[j];
						FASmsg[j*12+28]=0;
						FASmsg[j*12+29]=0x11;
						FASmsg[j*12+30]=0;
						FASmsg[j*12+31]=(byte) AutoStatus[FON[j]];
						FASmsg[j*12+32]=0x03;
						FASmsg[j*12+33]=0;
						FASmsg[j*12+34]=0;
						FASmsg[j*12+35]=0;
						FAC++;
					}
					byte[] FA1Send = new byte[20+FAC*3*4+4/*Auto 수*/];
					System.arraycopy(FASmsg, 0, FA1Send, 0, FA1Send.length);
					FA1Send[3]=(byte) (FA1Send.length-8);
					FA1Send[15]=(byte) ((FA1Send.length-20)/4);
					recvmsg = sendToBoard(FA1Send, FeedBoardIP[0]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-1");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}
					for(int j=0;j<u_num.FeedPortNum-7;j++) {
						FASmsg[j*12+16]=0;
						FASmsg[j*12+17]=(byte) (0x20+j);
						FASmsg[j*12+18]=0;
						FASmsg[j*12+19]=FUS[j];
						FASmsg[j*12+20]=0;
						FASmsg[j*12+21]=(byte) (0x30+j);
						FASmsg[j*12+22]=0;
						FASmsg[j*12+23]=(byte) Intensity[FON[j]];
						FASmsg[j*12+24]=0;
						FASmsg[j*12+25]=(byte) (0x40+j);
						FASmsg[j*12+26]=0;
						FASmsg[j*12+27]=FOS[j];		
						FASmsg[j*12+28]=0;
						FASmsg[j*12+29]=0x11;
						FASmsg[j*12+30]=0;
						FASmsg[j*12+31]=(byte) AutoStatus[FON[j]];
						FASmsg[j*12+32]=0x03;
						FASmsg[j*12+33]=0;
						FASmsg[j*12+34]=0;
						FASmsg[j*12+35]=0;
					}
					byte[] FA2Send = new byte[20+(u_num.FeedPortNum-FAC)*3*4+4/*Auto 수*/];
					System.arraycopy(FASmsg, 0, FA2Send, 0, FA2Send.length);						
					FA2Send[3]=(byte) (FA2Send.length-8);
					FA2Send[5]=2;
					FA2Send[15]=(byte) ((FA2Send.length-20)/4);
					recvmsg = sendToBoard(FA2Send, FeedBoardIP[1]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Feed-2");
						obj.put("Reserved", 0);
						obj.put("ResponseValue", 0);
					}				
				}
				if(num.FeedBoardNum==1) {
					Thread.sleep(ST);
				}
				TimeOut=1000;
				//AirPressSet
				System.out.println("AirPressSet");
				query = "SELECT maintenance_AirCheckLT, maintenance_AirCheckGT, maintenance_AirCheckAlarm FROM group_ultima_maintenance";
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				rs.next();
				int LValue=(int)(rs.getFloat("maintenance_AirCheckLT")*10);
				int GValue=(int)(rs.getFloat("maintenance_AirCheckGT")*10);
				int AirOnOff=rs.getInt("maintenance_AirCheckAlarm");
				for(int i=0;i<u_num.FeedBoardNum;i++) {
					byte[] ASmsg = { 0x02, 0, 0, 24, 2, (byte) (i+ 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 3, 0x00,0x51,0x00, (byte) LValue,0x00,0x52,0x00, (byte) GValue, 0x00,0x50,0x00, (byte) AirOnOff, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg = sendToBoard(ASmsg, FeedBoardIP[i]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(i+1));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}
				if(num.FeedBoardNum==1) {
					Thread.sleep(ST);
				}
				// FeedSwitchInSet
				System.out.println("FeedSwitchInSet");
				for(int i=0;i<num.FeedBoardNum;i++) {
					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW1' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FDCSW1=rs.getInt("switch_SwitchStatus");

					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'DC SW2' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FDCSW2=rs.getInt("switch_SwitchStatus");

					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'AC SW1' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FACSW1=rs.getInt("switch_SwitchStatus");

					query = "SELECT switch_SwitchStatus FROM tb_ultima_switch WHERE switch_BoardName = 'Feeding' AND switch_BoardInputOutput = 'Input' AND switch_BoardSwitchName = 'AC SW2' AND switch_BoardNum=?";
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, i+1);
					rs = pstmt.executeQuery();
					rs.next();
					int FACSW2=rs.getInt("switch_SwitchStatus");

					byte[] FSmsg = { 0x02, 0, 0, 28, 2, (byte) (i+ 1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 4, 0x00,0x60,0x00, (byte) FDCSW1,0x00,0x61,0x00, (byte) FDCSW2, 0x00,0x62,0x00, (byte) FACSW1,  0x00,0x63,0x00, (byte) FACSW2, 0x03, 0, 0, 0 }; // 바이트배열
					recvmsg=sendToBoard(FSmsg, FeedBoardIP[i]);
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(i+1));
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}			
				
				//AllEjectStartStopSet
				System.out.println("AllEjectStartStopSet");
				Thread.sleep(2000);
				for(int i=0;i<num.SortBoardNum;i++) {
					byte[] msg = {0x02,0,0,16,4,(byte) (i+1),0,2,2,0,(byte)u_id,0,0,0,0,1,0x20,0x40,0, 0,0x03,0,0,0};	//바이트배열
					recvmsg = sendToBoard(msg, num.SortBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {
						ErrorBoard.add("Sort-"+Integer.toString(i+1));
						obj.put("NumOfErrorBoard", 0);
						obj.put("ResponseValue", 0);
					}
				}
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				//CameraSet
				System.out.println("CameraSet"); 
				query = "SELECT * FROM tb_camera WHERE camera_mode_ID = ? AND camera_CameraNum IN(select sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order !=0)"; 
				pstmt = conn.prepareStatement(query); 
				pstmt.setInt(1, ModeNum); 
				rs = pstmt.executeQuery();
				int[] camerFR=new int[num.SortBoardNum*4];
				String[] camerNo=new String[num.SortBoardNum*4];
				int[] camerPo=new int[num.SortBoardNum*4];
				int count=0;
				for(int i=0;i<num.SortBoardNum*4;i++) {	
					rs.next();
					camerFR[count]=rs.getInt("camera_CameraFrontRear");
					camerNo[count]=rs.getString("camera_CameraNum");
					camerPo[count]=rs.getInt("camera_CameraPower");

					int FR=rs.getInt("camera_CameraFrontRear");
					int CamNum=	LogicNumtoPyshicNum(rs.getString("camera_CameraNum"));
					count++;

				}
				for(int i=0;i<num.SortBoardNum*4;i++) {
					if(i%4==0) {
						byte[] msg ={0x02,0,0,28,4,(byte) ((LogicNumtoPyshicNum(camerNo[i])+1)/2),0,2,2,0,(byte)u_id,0,0,0,0,4,
								(byte)((0x10*camerFR[i])+(LogicNumtoPyshicNum(camerNo[i])+1)%2),0x00,0,(byte)camerPo[i], 
								(byte)((0x10*camerFR[i+1])+(LogicNumtoPyshicNum(camerNo[i+1])+1)%2),0x00,0,(byte)camerPo[i+1], 
								(byte)((0x10*camerFR[i+2])+(LogicNumtoPyshicNum(camerNo[i+2])+1)%2),0x00,0,(byte)camerPo[i+2], 
								(byte)((0x10*camerFR[i+3])+(LogicNumtoPyshicNum(camerNo[i+3])+1)%2),0x00,0,(byte)camerPo[i+3],
								0x03,0,0,0}; //바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[((LogicNumtoPyshicNum(camerNo[i])+1)/2)-1]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString(((LogicNumtoPyshicNum(camerNo[i])+1)/2))); 
						}
					}
				}
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				//CameraRGBSet
				TimeOut=1000;
				System.out.println("CameraRGBSet");
				byte RD;
				byte RL;
				byte BD;
				byte BL;
				byte GD;
				byte GL;

				byte[] dif = new byte[6];
				byte[] difsub = new byte[6];
				byte[] RGB= new byte[6];

				query = "SELECT *  FROM tb_camera_rgb WHERE camera_rgb_mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				count=0;
				while(rs.next()) {			
					String FC = rs.getString("camera_rgb_FirstCombination");
					String SC = rs.getString("camera_rgb_SecondCombination");
					String combi = FC+"/"+SC;
					if (combi=="BL/RD")dif[count]=1;
					else if(combi=="BD/RL")dif[count]=2;
					else if(combi=="GL/BD")dif[count]=4;
					else if(combi=="GD/BL")dif[count]=8;
					else if(combi=="RL/GD")dif[count]=16;
					else if(combi=="RD/GL")dif[count]=31;

					String DL = rs.getString("camera_rgb_DarkLight");
					if(DL=="With Dark")difsub[count]=1;
					else if(DL=="Without Dark")difsub[count]=2;
					else if(DL=="With Light")difsub[count]=4;
					else if(DL=="Without Light")difsub[count]=8;


					RD=(byte) rs.getInt("camera_rgb_RDUseStatus");						
					RL=(byte) rs.getInt("camera_rgb_RLUseStatus");						
					GD=(byte) rs.getInt("camera_rgb_GDUseStatus");						
					GL=(byte) rs.getInt("camera_rgb_GLUseStatus");						
					BD=(byte) rs.getInt("camera_rgb_BDUseStatus");						
					BL=(byte) rs.getInt("camera_rgb_BLUseStatus");

					if(RD==1)RGB[count]=(byte) (RGB[count]+1);
					if(RL==1)RGB[count]=(byte) (RGB[count]+2);
					if(GD==1)RGB[count]=(byte) (RGB[count]+4);
					if(GL==1)RGB[count]=(byte) (RGB[count]+8);
					if(BD==1)RGB[count]=(byte) (RGB[count]+16);
					if(BL==1)RGB[count]=(byte) (RGB[count]+31);					
					count++;
				}			
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					if(i%2==0) {
						int lNum=(i+1)/2;
						byte[] msg = { 0x02, 0, 0, 60, 4, (byte) (lNum+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 12,
								0x00,0x10,0x00,(byte) RGB[sortOrder[i]],
								0x00,0x11,0x00,(byte)dif[sortOrder[i]],
								0x00,0x12,0x00,(byte) difsub[sortOrder[i]],

								0x01,0x10,0x00,(byte) RGB[sortOrder[i+1]],
								0x01,0x11,0x00,(byte)dif[sortOrder[i+1]],
								0x01,0x12,0x00,(byte) difsub[sortOrder[i+1]],

								0x10,0x10,0x00,(byte) RGB[sortOrder[i]],
								0x10,0x11,0x00,(byte)dif[sortOrder[i]],
								0x10,0x12,0x00,(byte) difsub[sortOrder[i]],

								0x11,0x10,0x00,(byte) RGB[sortOrder[i+1]],
								0x11,0x11,0x00,(byte)dif[sortOrder[i+1]],
								0x11,0x12,0x00,(byte) difsub[sortOrder[i+1]],
								0x03, 0, 0, 0 }; // 바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[lNum]);
						if (recvmsg == null || recvmsg[9] == 0) {
							ErrorBoard.add("Sort-"+(lNum/2));
							obj.put("ResponseValue",0);
							obj.put("Reserved",0);
						}
					}
				}				
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				// SensColorSet
				System.out.println("SensColorSet");

				int[][][] use = new int [3][2][12];
				int[][][] val = new int [3][2][12];
				int[][][] size = new int [3][2][12];	
				int[][][] dfUse = new int [3][2][2];
				int[][][] dfVal = new int [3][2][2];
				int[][] dfSize = new int [3][2];

				query = "SELECT * FROM tb_color_combination WHERE combination_mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();	
				while(rs.next()) {
					dfUse[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][0]=rs.getInt("combination_FirstBlockOnOff");
					dfUse[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][1]=rs.getInt("combination_SecondBlockOnOff");
					dfVal[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][0]=rs.getInt("combination_ColorValue");
					dfVal[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")][1]=rs.getInt("combination_DarkLightValue");
					dfSize[rs.getInt("combination_order_Num")-1][rs.getInt("combination_CameraFrontRear")]=rs.getInt("combination_PixelSize");
				}
				
				query = "SELECT * FROM tb_color WHERE color_mode_ID = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();	
				while(rs.next()) {
					use[rs.getInt("color_order_Num")-1][rs.getInt("color_CameraFrontRear")][colornum(rs.getString("color_BlockName"))-1]=rs.getInt("color_BlockStatus");
					val[rs.getInt("color_order_Num")-1][rs.getInt("color_CameraFrontRear")][colornum(rs.getString("color_BlockName"))-1]=rs.getInt("color_Value");
					size[rs.getInt("color_order_Num")-1][rs.getInt("color_CameraFrontRear")][colornum(rs.getString("color_BlockName"))-1]=rs.getInt("color_PixelSize");
				}				
				TimeOut=10000;
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					//길이 664바이트
					if(i%2==0) {
						byte[] msg = {0x02,0,0x0010,(byte) 152,4,(byte) (i/2+1),0,2,2,0,(byte)u_id,0,0,0,0,(byte) 164,
								0x00,0x20,0x00,(byte) use[sortOrder[i]][0][0],
								0x00,0x21,0x00,(byte) use[sortOrder[i]][0][1],
								0x00,0x22,0x00,(byte) use[sortOrder[i]][0][2],
								0x00,0x23,0x00,(byte) use[sortOrder[i]][0][3],
								0x00,0x24,0x00,(byte) use[sortOrder[i]][0][4],
								0x00,0x25,0x00,(byte) use[sortOrder[i]][0][5],
								0x00,0x26,0x00,(byte) use[sortOrder[i]][0][6],
								0x00,0x27,0x00,(byte) use[sortOrder[i]][0][7],
								0x00,0x28,0x00,(byte) use[sortOrder[i]][0][8],
								0x00,0x29,0x00,(byte) use[sortOrder[i]][0][9],
								0x00,0x2a,0x00,(byte) use[sortOrder[i]][0][10],
								0x00,0x2b,0x00,(byte) use[sortOrder[i]][0][11],
								0x00,0x2c,0x00,(byte) dfUse[sortOrder[i]][0][0],
								0x00,0x2d,0x00,(byte) dfUse[sortOrder[i]][0][1],

								0x01,0x20,0x00,(byte) use[sortOrder[i+1]][0][0],
								0x01,0x21,0x00,(byte) use[sortOrder[i+1]][0][1],
								0x01,0x22,0x00,(byte) use[sortOrder[i+1]][0][2],
								0x01,0x23,0x00,(byte) use[sortOrder[i+1]][0][3],
								0x01,0x24,0x00,(byte) use[sortOrder[i+1]][0][4],
								0x01,0x25,0x00,(byte) use[sortOrder[i+1]][0][5],
								0x01,0x26,0x00,(byte) use[sortOrder[i+1]][0][6],
								0x01,0x27,0x00,(byte) use[sortOrder[i+1]][0][7],
								0x01,0x28,0x00,(byte) use[sortOrder[i+1]][0][8],
								0x01,0x29,0x00,(byte) use[sortOrder[i+1]][0][9],
								0x01,0x2a,0x00,(byte) use[sortOrder[i+1]][0][10],
								0x01,0x2b,0x00,(byte) use[sortOrder[i+1]][0][11],
								0x01,0x2c,0x00,(byte) dfUse[sortOrder[i+1]][0][0],
								0x01,0x2d,0x00,(byte) dfUse[sortOrder[i+1]][0][1],

								0x10,0x20,0x00,(byte) use[sortOrder[i]][1][0],
								0x10,0x21,0x00,(byte) use[sortOrder[i]][1][1],
								0x10,0x22,0x00,(byte) use[sortOrder[i]][1][2],
								0x10,0x23,0x00,(byte) use[sortOrder[i]][1][3],
								0x10,0x24,0x00,(byte) use[sortOrder[i]][1][4],
								0x10,0x25,0x00,(byte) use[sortOrder[i]][1][5],
								0x10,0x26,0x00,(byte) use[sortOrder[i]][1][6],
								0x10,0x27,0x00,(byte) use[sortOrder[i]][1][7],
								0x10,0x28,0x00,(byte) use[sortOrder[i]][1][8],
								0x10,0x29,0x00,(byte) use[sortOrder[i]][1][9],
								0x10,0x2a,0x00,(byte) use[sortOrder[i]][1][10],
								0x10,0x2b,0x00,(byte) use[sortOrder[i]][1][11],
								0x10,0x2c,0x00,(byte) dfUse[sortOrder[i]][1][0],
								0x10,0x2d,0x00,(byte) dfUse[sortOrder[i]][1][1],

								0x11,0x20,0x00,(byte) use[sortOrder[i+1]][1][0],
								0x11,0x21,0x00,(byte) use[sortOrder[i+1]][1][1],
								0x11,0x22,0x00,(byte) use[sortOrder[i+1]][1][2],
								0x11,0x23,0x00,(byte) use[sortOrder[i+1]][1][3],
								0x11,0x24,0x00,(byte) use[sortOrder[i+1]][1][4],
								0x11,0x25,0x00,(byte) use[sortOrder[i+1]][1][5],
								0x11,0x26,0x00,(byte) use[sortOrder[i+1]][1][6],
								0x11,0x27,0x00,(byte) use[sortOrder[i+1]][1][7],
								0x11,0x28,0x00,(byte) use[sortOrder[i+1]][1][8],
								0x11,0x29,0x00,(byte) use[sortOrder[i+1]][1][9],
								0x11,0x2a,0x00,(byte) use[sortOrder[i+1]][1][10],
								0x11,0x2b,0x00,(byte) use[sortOrder[i+1]][1][11],
								0x11,0x2c,0x00,(byte) dfUse[sortOrder[i+1]][0][0],
								0x11,0x2d,0x00,(byte) dfUse[sortOrder[i+1]][0][1],

								0x00,0x30,0x00,(byte) val[sortOrder[i]][0][0],
								0x00,0x31,0x00,(byte) val[sortOrder[i]][0][1],
								0x00,0x32,0x00,(byte) val[sortOrder[i]][0][2],
								0x00,0x33,0x00,(byte) val[sortOrder[i]][0][3],
								0x00,0x34,0x00,(byte) val[sortOrder[i]][0][4],
								0x00,0x35,0x00,(byte) val[sortOrder[i]][0][5],
								0x00,0x36,0x00,(byte) val[sortOrder[i]][0][6],
								0x00,0x37,0x00,(byte) val[sortOrder[i]][0][7],
								0x00,0x38,0x00,(byte) val[sortOrder[i]][0][8],
								0x00,0x39,0x00,(byte) val[sortOrder[i]][0][9],
								0x00,0x3a,0x00,(byte) val[sortOrder[i]][0][10],
								0x00,0x3b,0x00,(byte) val[sortOrder[i]][0][11],
								0x00,0x3c,0x00,(byte) dfVal[sortOrder[i]][0][0],
								0x00,0x3d,0x00,(byte) dfVal[sortOrder[i]][0][1],								

								0x01,0x30,0x00,(byte) val[sortOrder[i+1]][0][0],
								0x01,0x31,0x00,(byte) val[sortOrder[i+1]][0][1],
								0x01,0x32,0x00,(byte) val[sortOrder[i+1]][0][2],
								0x01,0x33,0x00,(byte) val[sortOrder[i+1]][0][3],
								0x01,0x34,0x00,(byte) val[sortOrder[i+1]][0][4],
								0x01,0x35,0x00,(byte) val[sortOrder[i+1]][0][5],
								0x01,0x36,0x00,(byte) val[sortOrder[i+1]][0][6],
								0x01,0x37,0x00,(byte) val[sortOrder[i+1]][0][7],
								0x01,0x38,0x00,(byte) val[sortOrder[i+1]][0][8],
								0x01,0x39,0x00,(byte) val[sortOrder[i+1]][0][9],
								0x01,0x3a,0x00,(byte) val[sortOrder[i+1]][0][10],
								0x01,0x3b,0x00,(byte) val[sortOrder[i+1]][0][11],
								0x01,0x3c,0x00,(byte) dfVal[sortOrder[i+1]][0][0],
								0x01,0x3d,0x00,(byte) dfVal[sortOrder[i+1]][0][1],


								0x10,0x30,0x00,(byte) val[sortOrder[i]][1][0],
								0x10,0x31,0x00,(byte) val[sortOrder[i]][1][1],
								0x10,0x32,0x00,(byte) val[sortOrder[i]][1][2],
								0x10,0x33,0x00,(byte) val[sortOrder[i]][1][3],
								0x10,0x34,0x00,(byte) val[sortOrder[i]][1][4],
								0x10,0x35,0x00,(byte) val[sortOrder[i]][1][5],
								0x10,0x36,0x00,(byte) val[sortOrder[i]][1][6],
								0x10,0x37,0x00,(byte) val[sortOrder[i]][1][7],
								0x10,0x38,0x00,(byte) val[sortOrder[i]][1][8],
								0x10,0x39,0x00,(byte) val[sortOrder[i]][1][9],
								0x10,0x3a,0x00,(byte) val[sortOrder[i]][1][10],
								0x10,0x3b,0x00,(byte) val[sortOrder[i]][1][11],
								0x10,0x3c,0x00,(byte) dfVal[sortOrder[i]][1][0],
								0x10,0x3d,0x00,(byte) dfVal[sortOrder[i]][1][1],

								0x11,0x30,0x00,(byte) val[sortOrder[i+1]][1][0],
								0x11,0x31,0x00,(byte) val[sortOrder[i+1]][1][1],
								0x11,0x32,0x00,(byte) val[sortOrder[i+1]][1][2],
								0x11,0x33,0x00,(byte) val[sortOrder[i+1]][1][3],
								0x11,0x34,0x00,(byte) val[sortOrder[i+1]][1][4],
								0x11,0x35,0x00,(byte) val[sortOrder[i+1]][1][5],
								0x11,0x36,0x00,(byte) val[sortOrder[i+1]][1][6],
								0x11,0x37,0x00,(byte) val[sortOrder[i+1]][1][7],
								0x11,0x38,0x00,(byte) val[sortOrder[i+1]][1][8],
								0x11,0x39,0x00,(byte) val[sortOrder[i+1]][1][9],
								0x11,0x3a,0x00,(byte) val[sortOrder[i+1]][1][10],
								0x11,0x3b,0x00,(byte) val[sortOrder[i+1]][1][11],
								0x11,0x3c,0x00,(byte) dfVal[sortOrder[i+1]][1][0],
								0x11,0x3d,0x00,(byte) dfVal[sortOrder[i+1]][1][1],

								0x00,0x40,0x00,(byte) size[sortOrder[i]][0][0],
								0x00,0x41,0x00,(byte) size[sortOrder[i]][0][1],
								0x00,0x42,0x00,(byte) size[sortOrder[i]][0][2],
								0x00,0x43,0x00,(byte) size[sortOrder[i]][0][3],
								0x00,0x44,0x00,(byte) size[sortOrder[i]][0][4],
								0x00,0x45,0x00,(byte) size[sortOrder[i]][0][5],
								0x00,0x46,0x00,(byte) size[sortOrder[i]][0][6],
								0x00,0x47,0x00,(byte) size[sortOrder[i]][0][7],
								0x00,0x48,0x00,(byte) size[sortOrder[i]][0][8],
								0x00,0x49,0x00,(byte) size[sortOrder[i]][0][9],
								0x00,0x4a,0x00,(byte) size[sortOrder[i]][0][10],
								0x00,0x4b,0x00,(byte) size[sortOrder[i]][0][11],
								0x00,0x4c,0x00,(byte) dfSize[sortOrder[i]][0],

								0x01,0x40,0x00,(byte) size[sortOrder[i+1]][0][0],
								0x01,0x41,0x00,(byte) size[sortOrder[i+1]][0][1],
								0x01,0x42,0x00,(byte) size[sortOrder[i+1]][0][2],
								0x01,0x43,0x00,(byte) size[sortOrder[i+1]][0][3],
								0x01,0x44,0x00,(byte) size[sortOrder[i+1]][0][4],
								0x01,0x45,0x00,(byte) size[sortOrder[i+1]][0][5],
								0x01,0x46,0x00,(byte) size[sortOrder[i+1]][0][6],
								0x01,0x47,0x00,(byte) size[sortOrder[i+1]][0][7],
								0x01,0x48,0x00,(byte) size[sortOrder[i+1]][0][8],
								0x01,0x49,0x00,(byte) size[sortOrder[i+1]][0][9],
								0x01,0x4a,0x00,(byte) size[sortOrder[i+1]][0][10],
								0x01,0x4b,0x00,(byte) size[sortOrder[i+1]][0][11],
								0x01,0x4c,0x00,(byte) dfSize[sortOrder[i+1]][0],

								0x10,0x40,0x00,(byte) size[sortOrder[i]][1][0],
								0x10,0x41,0x00,(byte) size[sortOrder[i]][1][1],
								0x10,0x42,0x00,(byte) size[sortOrder[i]][1][2],
								0x10,0x43,0x00,(byte) size[sortOrder[i]][1][3],
								0x10,0x44,0x00,(byte) size[sortOrder[i]][1][4],
								0x10,0x45,0x00,(byte) size[sortOrder[i]][1][5],
								0x10,0x46,0x00,(byte) size[sortOrder[i]][1][6],
								0x10,0x47,0x00,(byte) size[sortOrder[i]][1][7],
								0x10,0x48,0x00,(byte) size[sortOrder[i]][1][8],
								0x10,0x49,0x00,(byte) size[sortOrder[i]][1][9],
								0x10,0x4a,0x00,(byte) size[sortOrder[i]][1][10],
								0x10,0x4b,0x00,(byte) size[sortOrder[i]][1][11],
								0x10,0x4c,0x00,(byte) dfSize[sortOrder[i]][1],

								0x11,0x40,0x00,(byte) size[sortOrder[i+1]][1][0],
								0x11,0x41,0x00,(byte) size[sortOrder[i+1]][1][1],
								0x11,0x42,0x00,(byte) size[sortOrder[i+1]][1][2],
								0x11,0x43,0x00,(byte) size[sortOrder[i+1]][1][3],
								0x11,0x44,0x00,(byte) size[sortOrder[i+1]][1][4],
								0x11,0x45,0x00,(byte) size[sortOrder[i+1]][1][5],
								0x11,0x46,0x00,(byte) size[sortOrder[i+1]][1][6],
								0x11,0x47,0x00,(byte) size[sortOrder[i+1]][1][7],
								0x11,0x48,0x00,(byte) size[sortOrder[i+1]][1][8],
								0x11,0x49,0x00,(byte) size[sortOrder[i+1]][1][9],
								0x11,0x4a,0x00,(byte) size[sortOrder[i+1]][1][10],
								0x11,0x4b,0x00,(byte) size[sortOrder[i+1]][1][11],
								0x11,0x4c,0x00,(byte) dfSize[sortOrder[i+1]][1],

								0x03,0,0,0};	//바이트배열
						
						String ad = Integer.toBinaryString(msg.length-8);						
						byte a=(byte) (msg.length-8);
						byte b=0;
						if(ad.length()>8) {
							ad=ad.substring(0,ad.length()-8);							
							b=(byte) Integer.parseInt(ad,2);
						}						
						msg[2]=b;						
						msg[3]=a;
						msg[15]=(byte) ((msg.length-20)/4);
						ServerStartMain.Busyflag[i/2]=true;
						recvmsg=sendToBoard(msg, SortBoardIP[i/2]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString(i/2)); 
						}
						ServerStartMain.Busyflag[i/2]=false;
					}					
				}
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				TimeOut=2000;
				// SensAdvancedSet
				System.out.println("SensAdvancedSet"); 
				query = "SELECT * FROM tb_color_detail WHERE detail_mode_ID = ? AND detail_CameraNum IN(select sorting_Chute FROM tb_ultima_sorting WHERE sorting_Order !=0)";
				int[][][] factor = new int[num.SortBoardNum*2][2][14]; 
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				while(rs.next()) {
					factor[LogicNumtoPyshicNum(rs.getString("detail_CameraNum"))-1][rs.getInt("detail_CameraFrontRear")][colornum(rs.getString("detail_BlockName"))-1]=rs.getInt("detail_Factor");
				}
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					if(i%2==0) {
						byte[] msg = {0x02,0,0,(byte) 236,4,(byte)(i/2+1),0,2,2,0,(byte)u_id,0,0,0,0,56,

								(byte) 0x00,0x50,0x00,(byte) factor[i][0][0],
								(byte) 0x01,0x50,0x00,(byte) factor[i+1][0][0],								
								(byte) 0x10,0x50,0x00,(byte) factor[i][1][0],
								(byte) 0x11,0x50,0x00,(byte) factor[i+1][1][0],

								(byte) 0x00,0x51,0x00,(byte) factor[i][0][1],
								(byte) 0x01,0x51,0x00,(byte) factor[i+1][0][1],								
								(byte) 0x10,0x51,0x00,(byte) factor[i][1][1],
								(byte) 0x11,0x51,0x00,(byte) factor[i+1][1][1],

								(byte) 0x00,0x52,0x00,(byte) factor[i][0][2],
								(byte) 0x01,0x52,0x00,(byte) factor[i+1][0][2],								
								(byte) 0x10,0x52,0x00,(byte) factor[i][1][2],
								(byte) 0x11,0x52,0x00,(byte) factor[i+1][1][2],

								(byte) 0x00,0x53,0x00,(byte) factor[i][0][3],
								(byte) 0x01,0x53,0x00,(byte) factor[i+1][0][3],								
								(byte) 0x10,0x53,0x00,(byte) factor[i][1][3],
								(byte) 0x11,0x53,0x00,(byte) factor[i+1][1][3],

								(byte) 0x00,0x54,0x00,(byte) factor[i][0][4],
								(byte) 0x01,0x54,0x00,(byte) factor[i+1][0][4],								
								(byte) 0x10,0x54,0x00,(byte) factor[i][1][4],
								(byte) 0x11,0x54,0x00,(byte) factor[i+1][1][4],

								(byte) 0x00,0x52,0x00,(byte) factor[i][0][5],
								(byte) 0x01,0x52,0x00,(byte) factor[i+1][0][5],								
								(byte) 0x10,0x52,0x00,(byte) factor[i][1][5],
								(byte) 0x11,0x52,0x00,(byte) factor[i+1][1][5],

								(byte) 0x00,0x56,0x00,(byte) factor[i][0][6],
								(byte) 0x01,0x56,0x00,(byte) factor[i+1][0][6],								
								(byte) 0x10,0x56,0x00,(byte) factor[i][1][6],
								(byte) 0x11,0x56,0x00,(byte) factor[i+1][1][6],

								(byte) 0x00,0x57,0x00,(byte) factor[i][0][7],
								(byte) 0x01,0x57,0x00,(byte) factor[i+1][0][7],								
								(byte) 0x10,0x57,0x00,(byte) factor[i][1][7],
								(byte) 0x11,0x57,0x00,(byte) factor[i+1][1][7],

								(byte) 0x00,0x58,0x00,(byte) factor[i][0][8],
								(byte) 0x01,0x58,0x00,(byte) factor[i+1][0][8],								
								(byte) 0x10,0x58,0x00,(byte) factor[i][1][8],
								(byte) 0x11,0x58,0x00,(byte) factor[i+1][1][8],

								(byte) 0x00,0x59,0x00,(byte) factor[i][0][9],
								(byte) 0x01,0x59,0x00,(byte) factor[i+1][0][9],								
								(byte) 0x10,0x59,0x00,(byte) factor[i][1][9],
								(byte) 0x11,0x59,0x00,(byte) factor[i+1][1][9],

								(byte) 0x00,0x5a,0x00,(byte) factor[i][0][10],
								(byte) 0x01,0x5a,0x00,(byte) factor[i+1][0][10],								
								(byte) 0x10,0x5a,0x00,(byte) factor[i][1][10],
								(byte) 0x11,0x5a,0x00,(byte) factor[i+1][1][10],

								(byte) 0x00,0x5b,0x00,(byte) factor[i][0][11],
								(byte) 0x01,0x5b,0x00,(byte) factor[i+1][0][11],								
								(byte) 0x10,0x5b,0x00,(byte) factor[i][1][11],
								(byte) 0x11,0x5b,0x00,(byte) factor[i+1][1][11],

								(byte) 0x00,0x5c,0x00,(byte) factor[i][0][12],
								(byte) 0x01,0x5c,0x00,(byte) factor[i+1][0][12],								
								(byte) 0x10,0x5c,0x00,(byte) factor[i][1][12],
								(byte) 0x11,0x5c,0x00,(byte) factor[i+1][1][12],

								(byte) 0x00,0x5d,0x00,(byte) factor[i][0][13],
								(byte) 0x01,0x5d,0x00,(byte) factor[i+1][0][13],								
								(byte) 0x10,0x5d,0x00,(byte) factor[i][1][13],
								(byte) 0x11,0x5d,0x00,(byte) factor[i+1][1][13],								

								0x03,0,0,0}; //바이트배열
						recvmsg=sendToBoard(msg, SortBoardIP[i/2]);
						if(recvmsg==null||recvmsg[9]==0) { 
							obj.put("ResponseValue", 0);
							obj.put("Reserved", 0);
							ErrorBoard.add("Sort-"+Integer.toString((i)/2+1)); 
						}

					}				
				}				
				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				//EjectSet 
				System.out.println("EjectSet");
				query = "SELECT ejecting_DelayValue, ejecting_HeadValue, ejecting_HoldValue, ejecting_Order FROM tb_ejecting WHERE ejecting_mode_ID = ? ORDER BY ejecting_Order ASC";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery();
				int[] ejecting_DelayValue=new int[3];
				int[] ejecting_HeadValue=new int[3];
				int[] ejecting_HoldValue=new int[3];
				while(rs.next()) {
					ejecting_DelayValue[rs.getInt("ejecting_Order")-1]=rs.getInt("ejecting_DelayValue");
					ejecting_HeadValue[rs.getInt("ejecting_Order")-1]=rs.getInt("ejecting_HeadValue");
					ejecting_HoldValue[rs.getInt("ejecting_Order")-1]=rs.getInt("ejecting_HoldValue");
				}
				
				for(int i=0;i<u_num.SortBoardNum*2;i++) {
					if(i%2==0) {
						byte[] ESmsg = { 0x02, 0, 0, 36, 4, (byte) (i/2+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 6, 
								0x00,(byte)0x80,0x00, (byte) ejecting_DelayValue[sortOrder[i]],
								0x00,(byte)0x90,0x00, (byte) ejecting_HeadValue[sortOrder[i]], 
								0x00,(byte)0x91,0x00, (byte) ejecting_HoldValue[sortOrder[i]], 

								0x01,(byte)0x80,0x00, (byte) ejecting_DelayValue[sortOrder[i+1]],
								0x01,(byte)0x90,0x00, (byte) ejecting_HeadValue[sortOrder[i+1]], 
								0x01,(byte)0x91,0x00, (byte) ejecting_HoldValue[sortOrder[i+1]], 

								0x03, 0, 0, 0 }; // 바이트배열
						ESmsg[3]=(byte) (ESmsg.length-8);
						ESmsg[15]=(byte) ((ESmsg.length-20)/4);
						recvmsg=sendToBoard(ESmsg, SortBoardIP[i/2]);
						ServerStartMain.Busyflag[i/2]=true;
						if (recvmsg == null || recvmsg[9] == 0) {
							ErrorBoard.add("Sort-"+(i/2+1));
							obj.put("ResponseValue",0);
							obj.put("Reserved",0);
						}
						ServerStartMain.Busyflag[i/2]=false;
					}
				}

				if(num.SortBoardNum==1) {
					Thread.sleep(ST);
				}
				//CamGOSet 
				//CameraOrder = new String[2][3][max]; // FR/order/camnum
				System.out.println("CamGOSet");
				query = "SELECT * FROM tb_gainoffset WHERE gainoffset_modeID = ? AND gainoffset_CameraNum <= (select maintenance_Channel FROM group_ultima_maintenance)"; 
				pstmt = conn.prepareStatement(query); 
				pstmt.setInt(1, ModeNum);
				rs = pstmt.executeQuery(); 
				int[][][] gain = new int [num.SortBoardNum][2][3];
				int[][][] offset = new int [num.SortBoardNum][2][3];
				while(rs.next()) { 
					gain[rs.getInt("gainoffset_CameraNum")-1][rs.getInt("gainoffset_FrontRear")][rs.getInt("gainoffset_Color")]=rs.getInt("gainoffset_Gain");
					offset[rs.getInt("gainoffset_CameraNum")-1][rs.getInt("gainoffset_FrontRear")][rs.getInt("gainoffset_Color")]=rs.getInt("gainoffset_Offset");
				}
				for(int i=0;i<u_num.SortBoardNum;i++) {
					byte[] CGmsg = { 0x02, 0, 0, 60, 4, (byte) (i+1), 0, 2, 2, 0, (byte) u_id, 0, 0, 0, 0, 12, 
							0x02,0x10,0x00,(byte) gain[i][0][0],
							0x02,0x13,0x00,(byte) offset[i][0][0],

							0x02,0x11,0x00,(byte) gain[i][0][1],
							0x02,0x14,0x00,(byte) offset[i][0][1],

							0x02,0x12,0x00,(byte) gain[i][0][2],
							0x02,0x15,0x00,(byte) offset[i][0][2],

							0x12,0x10,0x00,(byte) gain[i][1][0],
							0x12,0x13,0x00,(byte) offset[i][1][0],

							0x12,0x11,0x00,(byte) gain[i][1][1],
							0x12,0x14,0x00,(byte) offset[i][1][1],

							0x12,0x12,0x00,(byte) gain[i][1][2],
							0x12,0x15,0x00,(byte) offset[i][1][2],

							0x03, 0, 0, 0 }; // 바이트배열
 
					if (recvmsg == null || recvmsg[9] == 0) {
						ErrorBoard.add("Sort-"+(i+1)); 
						obj.put("ResponseValue",0);
						obj.put("Reserved",0);
					}
				}				
				
				obj.put("ErrorBoard", ErrorBoard);
				obj.put("NumOfErrorBoard", ErrorBoard.size());
				array.add(obj);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				rs.close();
				pstmt.close();
				conn.close();

			} catch (SQLException e) {
				System.err.println("DB 연결 오류");
				System.err.println(e.getMessage());
				System.err.println(e);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			break;

		case "PowerGet":
			JSONArray BValue = new JSONArray();
			JSONArray TValue = new JSONArray();
			int max;
			int err=0;
			switch(u_msg.get("BoardType").toString()) {
			case "1": //intigrate
				u_msg.put("ResponseValue", 1);
				byte[] IntPowerGetMsg = {0x02,0,0,16,1,1,0,2,1,0,(byte)u_id,0,0,0,0,1,0x10,(byte) 0x80,0, 0,0x03,0,0,0};	//바이트배열
				recvmsg=sendToBoard(IntPowerGetMsg, IntegratedBoardIP);
				if(recvmsg==null||recvmsg[9]==0) {
					u_msg.put("ResponseValue", 0);
					TValue.add("-");
				}else {
					TValue.add(map(Byte.toUnsignedInt(recvmsg[19]),0,180,0,12));
				}
				u_msg.put("InteValue", TValue);
				array.add(u_msg);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;

			case "2": //feed
				u_msg.put("ResponseValue", 1);
				max=num.FeedBoardNum;
				for(int i=0; i<u_num.FeedBoardNum;i++) {
					BValue.clear();
					byte[] FeedPowerGetMsg = {0x02,0,0,24,2,(byte) (i+1),0,2,1,0,(byte)u_id,0,0,0,0,3,0x10,(byte) 0x80,0, 0,0x10,(byte) 0x81,0, 0,0x10,(byte) 0x82,0, 0,0x03,0,0,0};	//바이트배열
					recvmsg=sendToBoard(FeedPowerGetMsg, FeedBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {						
						BValue.add("-");
						BValue.add("-");
						BValue.add("-");
						err++;
					}else {			    		
						BValue.add(map(Byte.toUnsignedInt(recvmsg[19]),0,180,0,12));
						BValue.add(map(Byte.toUnsignedInt(recvmsg[23]),0,210,0,24.7f));
						BValue.add(map(Byte.toUnsignedInt(recvmsg[27]),0,245,0,404));
					}
					TValue.add(BValue);
				}
				if(max==err) {
					u_msg.put("ResponseValue", 0);
				}
				u_msg.put("FeedingValue", TValue);
				array.add(u_msg);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;

			case "3": //lighting
				u_msg.put("ResponseValue", 1);
				max=2;
				for(int i=0; i<2;i++) {
					BValue.clear();
					byte[] LightPowerGetMsg = {0x02,0,0,20,3,(byte) (i+1),0,2,1,0,(byte)u_id,0,0,0,0,2,0x10,(byte) 0x80,0, 0,0x10,(byte) 0x81,0, 0,0x03,0,0,0};	//바이트배열
					recvmsg=sendToBoard(LightPowerGetMsg, LightingBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {						
						BValue.add("-");
						BValue.add("-");
						err++;
					}else {
						BValue.add(map(Byte.toUnsignedInt(recvmsg[19]),0,180,0,12));
						BValue.add(map(Byte.toUnsignedInt(recvmsg[23]),0,210,0,24.7f));
					}
					TValue.add(BValue);
				}
				if(max==err) {
					u_msg.put("ResponseValue", 0);
				}
				u_msg.put("LightingValue", TValue);
				array.add(u_msg);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;

			case "4": //sorting
				u_msg.put("ResponseValue", 1);
				max=u_num.SortBoardNum;
				for(int i=0; i<u_num.SortBoardNum;i++) {
					BValue.clear();
					byte[] SortPowerGetMsg = {0x02,0,0,16,4,(byte) (i+1),0,2,1,0,(byte)u_id,0,0,0,0,1,0x28,(byte) 0x80,0, 0, 0x03,0,0,0};	//바이트배열
					recvmsg=sendToBoard(SortPowerGetMsg, SortBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {						
						BValue.add("-");
						err++;
					}else {
						BValue.add(map(Byte.toUnsignedInt(recvmsg[19]),0,180,0,12));
					}
					TValue.add(BValue);
				}
				if(max==err) {
					u_msg.put("ResponseValue", 0);
				}
				u_msg.put("SortingValue", TValue);
				array.add(u_msg);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;

			case "5": //cam
				u_msg.put("ResponseValue", 1);
				max=u_num.SortBoardNum;
				for(int i=0; i<u_num.SortBoardNum;i++) {
					BValue.clear();
					byte[] CamPowerGetMsg = {0x02,0,0,28,4,(byte) (i+1),0,2,1,0,(byte)u_id,0,0,0,0,4,
							0x08,(byte) 0x80,0, 0,
							0x08,(byte) 0x81,0, 0,
							0x18,(byte) 0x80,0, 0,						
							0x18,(byte) 0x81,0, 0, 0x03,0,0,0};	//바이트배열
					recvmsg=sendToBoard(CamPowerGetMsg, SortBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {
						err++;
						for(int j=0;j<4;j++)BValue.add("-");
					}else {
						BValue.add(map(Byte.toUnsignedInt(recvmsg[19]),0,180,0,12));
						BValue.add(map(Byte.toUnsignedInt(recvmsg[23]),0,180,0,12));
						BValue.add(map(Byte.toUnsignedInt(recvmsg[27]),0,180,0,12));
						BValue.add(map(Byte.toUnsignedInt(recvmsg[31]),0,180,0,12));
					}
					TValue.add(BValue);
				}
				if(max==err) {
					u_msg.put("ResponseValue", 0);
				}
				u_msg.put("CameraValue", TValue);
				array.add(u_msg);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;

			case "6": //eject
				u_msg.put("ResponseValue", 1);
				max=u_num.SortBoardNum;
				for(int i=0; i<u_num.SortBoardNum;i++) {					
					BValue.clear();
					byte[] EjectPowerGetMsg = {0x02,0,0,20,4,(byte) (i+1),0,2,1,0,(byte)u_id,0,0,0,0,2, 0x29,(byte) 0x80,0, 0,0x29,(byte) 0x81,0, 0,0x03,0,0,0};	//바이트배열
					recvmsg=sendToBoard(EjectPowerGetMsg, SortBoardIP[i]);
					if(recvmsg==null||recvmsg[9]==0) {
						err++;
						for(int j=0;j<2;j++)BValue.add("-");
					}else {
						BValue.add(map(Byte.toUnsignedInt(recvmsg[19]),0,210,0,12));
						BValue.add(map(Byte.toUnsignedInt(recvmsg[23]),0,210,0,24.7f));
					}
					TValue.add(BValue);
				}
				if(max==err) {
					u_msg.put("ResponseValue", 0);
				}
				u_msg.put("EjectingValue", TValue);
				array.add(u_msg);
				sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
				break;

			}
			break;
		case "GenRegSet":

			break;

		case "GenRegGet":

			break;

		case "BoardFWVersionGet":
			u_msg.put("ResponseValue", 1);
    		u_msg.put("Reserved",1);
    		u_msg.put("Feed2Version", null);
    		u_msg.put("Feed2VHDLVersion", null);
			byte[] IFWGMsg = {0x02,0,0,16,1,1,0,2,1,0,(byte)u_id,0,0,0,0,1,0x10,0x01,0,0,0x03,0,0,0};	//바이트배열
			recvmsg=sendToBoard(IFWGMsg, IntegratedBoardIP);
			if(recvmsg==null||recvmsg[9]==0) {
				u_msg.put("ResponseValue", 0);
				u_msg.put("Reserved",0);
				u_msg.put("IntVersion", "-");
			}else {
				u_msg.put("IntVersion", byteToFloat(recvmsg[19]));
			}
			u_msg.put("NumOfFeedingBoard", u_num.FeedBoardNum);
			try {
				Thread.sleep(ST);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i=0;i<u_num.FeedBoardNum;i++) {
				byte[] FFGmsg = { 0x02, 0, 0, 20, 2, (byte) (i+ 1), 0, 2, 1, 0, (byte) u_id, 0, 0, 0, 0, 2, 0x10, 0x01,0x00, 0x00, 0x10, 0x02, 0x00, 0x00, 0x03, 0, 0, 0 }; // 바이트배열
				recvmsg = sendToBoard(FFGmsg, FeedBoardIP[i]);
				if(recvmsg==null||recvmsg[9]==0) {
					u_msg.put("ResponseValue", 0);
					u_msg.put("Reserved",0);
					u_msg.put("Feed"+(i+1)+"Version", "-");
					u_msg.put("Feed"+(i+1)+"VHDLVersion", "-");
				}else {
					u_msg.put("Feed"+(i+1)+"Version", byteToFloat(recvmsg[19]));
					u_msg.put("Feed"+(i+1)+"VHDLVersion", byteToFloat(recvmsg[23]));
				}

			}
			u_msg.put("NumOfLightingBoard",2);
			try {
				Thread.sleep(ST);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i=0;i<2;i++) {
				byte[] LFGmsg = { 0x02, 0, 0, 20, 3, (byte) (i+ 1), 0, 2, 1, 0, (byte) u_id, 0, 0, 0, 0, 2, 0x10, 0x01,0x00, 0x00, 0x10, 0x02, 0x00, 0x00, 0x03, 0, 0, 0 }; // 바이트배열
				recvmsg = sendToBoard(LFGmsg, LightingBoardIP[i]);
				if(recvmsg==null||recvmsg[9]==0) {
					u_msg.put("ResponseValue", 0);
					u_msg.put("Reserved",0);
					u_msg.put("Light"+(i+1)+"Version", "-");
					u_msg.put("Light"+(i+1)+"VHDLVersion", "-");
				}else {
					u_msg.put("Light"+(i+1)+"Version", byteToFloat(recvmsg[19]));
					u_msg.put("Light"+(i+1)+"VHDLVersion", byteToFloat(recvmsg[23]));
				}
			}
			try {
				Thread.sleep(ST);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			u_msg.put("NumOfSortingBoard",u_num.SortBoardNum);
			JSONArray SortingFWVersion  = new JSONArray();
			JSONArray SortingVHDLVersion = new JSONArray();
			JSONArray CFFW= new JSONArray();
			JSONArray CRFW= new JSONArray();
			JSONArray EFW= new JSONArray();
			JSONArray CFVHDL = new JSONArray();
			JSONArray CRVHDL = new JSONArray();
			JSONArray EVHDL= new JSONArray();
			for(int i=0;i<u_num.SortBoardNum;i++) {
				byte[] SFGmsg = { 0x02, 0, 0, 44, 4, (byte) (i+ 1), 0, 2, 1, 0, (byte)u_id,0,0,0,0,8, 0x28,0x01, 0,0, 0x28,0x02, 0,0, 0x08,0x01, 0,0, 0x08,0x02, 0,0, 0x18,0x01, 0,0, 0x18,0x02, 0,0, 0x29,0x01, 0,0, 0x29,0x02, 0,0, 0x03,0,0,0}; // 바이트배열
				recvmsg = sendToBoard(SFGmsg, SortBoardIP[i]);
				if(recvmsg==null||recvmsg[9]==0) {
					u_msg.put("ResponseValue", 0);
					u_msg.put("Reserved",0);
					SortingFWVersion.add("-");
					SortingVHDLVersion.add("-");
					CFFW.add("-");
					CFVHDL.add("-");
					CRFW.add("-");
					CRVHDL.add("-");
					EFW.add("-");
					EVHDL.add("-");
				}else {
					SortingFWVersion.add(byteToFloat(recvmsg[19]));
					SortingVHDLVersion.add(byteToFloat(recvmsg[23]));
					CFFW.add(byteToFloat(recvmsg[27]));
					CFVHDL.add(byteToFloat(recvmsg[31]));
					CRFW.add(byteToFloat(recvmsg[35]));
					CRVHDL.add(byteToFloat(recvmsg[39]));
					EFW.add(byteToFloat(recvmsg[43]));
					EVHDL.add(byteToFloat(recvmsg[47]));
				}
			}
			u_msg.put("SortingFWVersion", SortingFWVersion);
			u_msg.put("FrontFWVersion", CFFW);
			u_msg.put("RearFWVersion", CRFW);
			u_msg.put("EjectingFWVersion", EFW);
			u_msg.put("SortingVHDLVersion", SortingVHDLVersion);
			u_msg.put("FrontVHDLVersion", CFVHDL);
			u_msg.put("RearVHDLVersion", CRVHDL);
			u_msg.put("EjectingVHDLVersion", EVHDL);
			array.add(u_msg);
			sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
			break;
		case "BoardHWVersionGet":
			u_msg.put("ResponseValue", 1);
			u_msg.put("Reserved",1);
			u_msg.put("Feed2Version", null);
			byte[] IHWGMsg = {0x02,0,0,16,1,(byte) (1),0,2,1,0,(byte)u_id,0,0,0,0,1,0x10,0x00,0,(byte) 0,0x03,0,0,0};	//바이트배열
			recvmsg=sendToBoard(IHWGMsg, IntegratedBoardIP);
			if(recvmsg==null||recvmsg[9]==0) {
				u_msg.put("ResponseValue", 0);
				u_msg.put("Reserved",0);
				u_msg.put("IntVersion", "-");
			}else {
				u_msg.put("IntVersion", byteToFloat(recvmsg[19]));
			}
			try {
				Thread.sleep(ST);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			u_msg.put("NumOfFeedingBoard", u_num.FeedBoardNum);
			for(int i=0;i<u_num.FeedBoardNum;i++) {
				byte[] FHGmsg = { 0x02, 0, 0, 16, 2, (byte) (i+ 1), 0, 2, 1, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x10, 0x00,0x00, 0x00, 0x03, 0, 0, 0 }; // 바이트배열
				recvmsg = sendToBoard(FHGmsg, FeedBoardIP[i]);
				if(recvmsg==null||recvmsg[9]==0) {
					u_msg.put("ResponseValue", 0);
					u_msg.put("Reserved",0);
					u_msg.put("Feed"+(i+1)+"Version", "-");
				}else {
					u_msg.put("Feed"+(i+1)+"Version", byteToFloat(recvmsg[19]));
				}

			}
			try {
				Thread.sleep(ST);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			u_msg.put("NumOfLightingBoard",2);
			for(int i=0;i<2;i++) {
				byte[] LHGmsg = { 0x02, 0, 0, 16, 3, (byte) (i+ 1), 0, 2, 1, 0, (byte) u_id, 0, 0, 0, 0, 1, 0x10, 0x00,0x00, 0x00, 0x03, 0, 0, 0 }; // 바이트배열
				recvmsg = sendToBoard(LHGmsg, LightingBoardIP[i]);
				if(recvmsg==null||recvmsg[9]==0) {
					u_msg.put("ResponseValue", 0);
					u_msg.put("Reserved",0);
					u_msg.put("Light"+(i+1)+"Version", "-");
				}else {
					u_msg.put("Light"+(i+1)+"Version", byteToFloat(recvmsg[19]));
				}
			}
			try {
				Thread.sleep(ST);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			u_msg.put("NumOfSortingBoard",u_num.SortBoardNum);
			JSONArray SortingHWVersion  = new JSONArray();
			JSONArray FHW  = new JSONArray();
			JSONArray RHW  = new JSONArray();
			JSONArray EHW  = new JSONArray();
			for(int i=0;i<u_num.SortBoardNum;i++) {
				byte[] SFGmsg = { 0x02, 0, 0, 28, 4, (byte) (i+ 1), 0, 2, 1, 0, (byte)u_id,0,0,0,0,4, 0x28,0x00, 0,0, 0x08,0x00, 0,0, 0x18,0x00, 0,0, 0x29,0x00, 0,0, 0x03,0,0,0}; // 바이트배열
				recvmsg = sendToBoard(SFGmsg, SortBoardIP[i]);
				if(recvmsg==null||recvmsg[9]==0) {
					u_msg.put("ResponseValue", 0);
					u_msg.put("Reserved",0);
					SortingHWVersion.add("-");
					FHW.add("-");
					RHW.add("-");
					EHW.add("-");
				}else {
					SortingHWVersion.add(byteToFloat(recvmsg[19]));
					FHW.add(byteToFloat(recvmsg[23]));
					RHW.add(byteToFloat(recvmsg[27]));
					EHW.add(byteToFloat(recvmsg[31]));
				}
			}
			u_msg.put("SortingHWVersion", SortingHWVersion);
			u_msg.put("FrontHWVersion", FHW);
			u_msg.put("RearHWVersion", RHW);
			u_msg.put("EjectingHWVersion", EHW);
			array.add(u_msg);
			sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
			break;
		}
	}

}
