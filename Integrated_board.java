package sum01;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import test01.BoardNum;


class Integrated_board extends board {

	public Integrated_board(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id) {
		this.u_msg=m_msg;
		this.u_ip=m_ip;
		this.u_port=m_port;
		this.u_id=m_id;
		this.u_lrecv=m_lrecv;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(){
		System.out.println(u_msg.toString()+u_ip+u_port);
		switch(u_msg.get("MsgName").toString()) {
		case "ShutdownSet":		    
			byte[]	msg = {0x02,0,0,16,1,(byte) 1,0,2,2,0,(byte)u_id,0,0,0,0,1,0x0,0x10,0,(byte) Integer.parseInt(u_msg.get("shutdown").toString()),0x03,0,0,0};	//獄쏅뗄?뵠占쎈뱜獄쏄퀣肉?
	    	recvmsg=sendToBoard(msg, IntegratedBoardIP);
	    	obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			if(recvmsg==null||recvmsg[9]==0) {
	    		obj.put("ResponseValue", 0);
	    		obj.put("Reserved",0);
	    		ErrorBoard.add("Integrate-1");
	    	}else {
	    		obj.put("ResponseValue", 1);		
	    	}
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
	    	sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
		break;
		case "CleanSet":
			byte[] CleanMsg = {0x02,0,0,16,1,(byte) (1),0,2,2,0,(byte)u_id,0,0,0,0,1,0x0,0x20,0,(byte) Integer.parseInt(u_msg.get("CleanDirection").toString()),0x03,0,0,0};	//獄쏅뗄?뵠占쎈뱜獄쏄퀣肉?
			recvmsg=sendToBoard(CleanMsg, IntegratedBoardIP);
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			if(recvmsg==null||recvmsg[9]==0) {
	    		obj.put("ResponseValue", 0);
	    		obj.put("Reserved",0);
	    		ErrorBoard.add("Integrate-1");
	    	}else {
	    		obj.put("ResponseValue", 1);		
	    	}
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
	    	sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
	    break;	
	    
		case "IntBGAngleSet":
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", "0");
			short x = (short) Integer.parseInt(u_msg.get("BGAngle").toString());
			byte[] Angle = new byte[2];
			Angle[0] = (byte) (x >> 8);
			Angle[1] = (byte) x;
			byte[] msg15 = {0x02,0,0,28,1,1,0,2,2,0,(byte)u_id,0,0,0,0,4,
				    			0x00,0x70, Angle[0],Angle[1],
				    			0x00,0x71, Angle[0],Angle[1],
				    			0x00,0x72, Angle[0],Angle[1],
				    			0x00,0x73, Angle[0],Angle[1],
				    			0x03,0,0,0};
				    	recvmsg = sendToBoardLong(msg15, IntegratedBoardIP);
				    	if(recvmsg==null||recvmsg[9]==0) {
				    		ErrorBoard.add("Int-"+Integer.toString(1));
				    	}	
		    if(ErrorBoard.isEmpty()) {
		    	ErrorBoard.add(0);
		    	obj.put("ResponseValue", "1");
		    	obj.put("NumOfErrorBoard", 0);
		    }else {
		    	obj.put("ResponseValue", "0");
		    	obj.put("NumOfErrorBoard", ErrorBoard.size());
		    }
		    obj.put("ErrorBoard", ErrorBoard);
		    array.add(obj);
		    sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
		
		break;
		
		case "IntAlarmGet":
			JSONArray Status = new JSONArray();
			JSONArray total = new JSONArray();
			JSONArray errorboad = new JSONArray();
			u_msg.put("ResponseValue",1);
			u_msg.put("Reserved", null);
				int err[];
				err = new int[] {1,1,1,1,1,1};
				byte[] msg2 = { 0x02, 0, 0, 12, 1, (byte) 1, 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg2, IntegratedBoardIP);
				if(recvmsg == null||recvmsg[9]==0) {
					u_msg.put("ResponseValue",0);
					u_msg.put("AlarmStatus",0);
					errorboad.add(1);
					for(int j=0;j<6;j++) {
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
						case(0x50):
							err[1]=recvmsg[j*4+19];
						break;
						case(0x51):
							err[2]=recvmsg[j*4+19];
						break;
						case(0x52):
							err[3]=recvmsg[j*4+19];
						break;
						case(0x70):
							err[4]=recvmsg[j*4+19];
						break;
						case(0x71):
							err[5]=recvmsg[j*4+19];
						break;
						default:
							System.out.println("�뾾�뒗 �젅吏�媛�");
						break;
						}
					}
					for(int j=0;j<6;j++) {
						System.out.println("j: "+j+" 媛�: "+err[j]);
						Status.add(err[j]);
					}
				}
				else if(recvmsg[11] == 1) {
					u_msg.put("AlarmStatus",1);
				}
				
			
		
			u_msg.put("NumOfBoard",errorboad.size());
			u_msg.put("BoardNum",errorboad);
			u_msg.put("IntAlarmStatus",Status);
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
		break;
		
		case "PRTempGet":
			byte[] TGMsg = {0x02,0,0,16,1,(byte) (1),0,2,1,0,(byte)u_id,0,0,0,0,1,0x10,(byte) 0x81,0, 0,0x03,0,0,0};	//獄쏅뗄?뵠占쎈뱜獄쏄퀣肉?
			recvmsg=sendToBoard(TGMsg, IntegratedBoardIP);
			if(recvmsg==null||recvmsg[9]==0) {
				u_msg.put("ResponseValue", 0);
				u_msg.put("Reserved",0);
	    		ErrorBoard.add("Integrate-1");
	    	}else {
	    		u_msg.put("Reserved",1);
	    		u_msg.put("ResponseValue", 1);	
	    		u_msg.put("PRTemp", recvmsg[19]*0.5);
	    	}
			array.add(u_msg);
	    	sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
		break;
		
		case "HeaterSet":
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			byte[] HSMsg = {0x02,0,0,20,1,(byte) (1),0,2,2,0,(byte)u_id,0,0,0,0,2,0x0,0x30,0,(byte) Integer.parseInt(u_msg.get("HeaterAutoOnOff").toString()),0x0,0x31,0,(byte) Integer.parseInt(u_msg.get("HeaterAutoTemp").toString()),0x03,0,0,0};	//獄쏅뗄?뵠占쎈뱜獄쏄퀣肉?
			recvmsg=sendToBoard(HSMsg, IntegratedBoardIP);
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			if(recvmsg==null||recvmsg[9]==0) {
	    		obj.put("ResponseValue", 0);
	    		obj.put("Reserved",0);
	    		ErrorBoard.add("Integrate-1");
	    	}else {
	    		obj.put("ResponseValue", 1);		
	    	}
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
	    	sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
		break;
		
		case "HeaterGet":
			byte[] HGMsg = {0x02,0,0,16,1,(byte) (1),0,2,1,0,(byte)u_id,0,0,0,0,1,0x10,(byte) 0x82,0,0,0x03,0,0,0};	//獄쏅뗄?뵠占쎈뱜獄쏄퀣肉?
			recvmsg=sendToBoard(HGMsg, IntegratedBoardIP);
			if(recvmsg==null||recvmsg[9]==0) {
				u_msg.put("Reserved",0);
				u_msg.put("ResponseValue", 0);
				u_msg.put("HeaterTemp", "-");
	    	}else {
	    		u_msg.put("ResponseValue", 1);
	    		u_msg.put("Reserved",1);
	    		u_msg.put("HeaterTemp", recvmsg[19]);  		
	    	}
			array.add(u_msg);
	    	sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
		break;
		
		case "WaterValveSet":
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			byte[] WMsg = {0x02,0,0,16,1,(byte) 1,0,2,2,0,(byte)u_id,0,0,0,0,1,0x0,0x40,0,(byte) Integer.parseInt(u_msg.get("WaterValveOnOff").toString()),0x03,0,0,0};	//獄쏅뗄?뵠占쎈뱜獄쏄퀣肉?
			recvmsg=sendToBoard(WMsg, IntegratedBoardIP);
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			if(recvmsg==null||recvmsg[9]==0) {
	    		obj.put("ResponseValue", 0);
	    		obj.put("Reserved",0);
	    		ErrorBoard.add("Integrate-1");
	    	}else {
	    		obj.put("ResponseValue", 1);		
	    	}
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
	    	sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
		break;
		
		case "AlarmLampSet":
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			byte[] ALSMsg = {0x02,0,0,16,1,(byte) (1),0,2,2,0,(byte)u_id,0,0,0,0,1,0x0,0x41,0,(byte) Integer.parseInt(u_msg.get("AlarmLampOnOff").toString()),0x03,0,0,0};	//獄쏅뗄?뵠占쎈뱜獄쏄퀣肉?
			recvmsg=sendToBoard(ALSMsg, IntegratedBoardIP);
			obj.put("Reserved", null);
			obj.put("MsgName", "SetResult");
			if(recvmsg==null||recvmsg[9]==0) {
	    		obj.put("ResponseValue", 0);
	    		obj.put("Reserved",0);
	    		ErrorBoard.add("Integrate-1");
	    	}else {
	    		obj.put("ResponseValue", 1);		
	    	}
			obj.put("ErrorBoard", ErrorBoard);
			obj.put("NumOfErrorBoard", ErrorBoard.size());
			array.add(obj);
	    	sendToLWGM(u_lrecv,u_ip,u_port,array.toString());
		break;
		
		case "IntSwitchInSet":
			obj.put("MsgName", "SetResult");
			obj.put("Reserved", "0"); 
		    	byte[] msg1 = {0x02,0,0,24,1,1,0,2,2,0,(byte)u_id,0,0,0,0,3,0x0,0x50,0,(byte) Integer.parseInt(u_msg.get("DCSW1OnOff").toString()),0x0,0x51,0,(byte) Integer.parseInt(u_msg.get("DCSW2OnOff").toString()),0x0,0x52,0,(byte) Integer.parseInt(u_msg.get("ACSW1OnOff").toString()),0x03,0,0,0};
		    	recvmsg = sendToBoard(msg1, IntegratedBoardIP);
		    	if(recvmsg==null||recvmsg[9]==0) {
		    		ErrorBoard.add("Int-"+Integer.toString(1));
		    	}	    
		    if(ErrorBoard.isEmpty()) {
		    	ErrorBoard.add(0);
		    	obj.put("ResponseValue", "1");
		    	obj.put("NumOfErrorBoard", 0);
		    }else {
		    	obj.put("ResponseValue", "0");
		    	obj.put("NumOfErrorBoard", ErrorBoard.size());
		    }
		    obj.put("ErrorBoard", ErrorBoard);
		    array.add(obj);
		    sendToLWGM(u_lrecv, u_ip, u_port, array.toString());	
		
		break;
		
		case "IntSwitchOutSet":
		
		break;
		
		default:
			System.out.println("占쎈르占쎈땾占쎈씨占쎈뮉 筌롫뗄?뻻筌욑옙"+u_msg.get("MsgName").toString());
		break;
		}
	}
}