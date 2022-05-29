package sum01;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Sorting_board extends board {
	int SortNum;
	JSONArray array = new JSONArray();
	JSONObject obj = new JSONObject();
	BytetoBmp BB = new BytetoBmp();
	int fimgcnt = 0; 
	int rimgcnt = 0; 
	int flinecnt = 0; 
	int rlinecnt = 0;
	
	public Sorting_board(DatagramSocket m_lrecv, JSONObject m_msg, InetAddress m_ip, int m_port, int m_id, BoardNum m_num) {
		this.u_msg=m_msg;
		this.u_ip=m_ip;
		this.u_port=m_port;
		this.u_id=m_id;
		this.u_lrecv=m_lrecv;
		SortNum = m_num.SortBoardNum;
		this.num=m_num;
	}
	@Override
	public void run(){
		System.out.println(u_msg.toString()+u_ip+u_port);
		switch(u_msg.get("MsgName").toString()) {
		
		
		case "SortAlarmGet":
			JSONArray Status = new JSONArray();
			JSONArray total = new JSONArray();
			JSONArray errorboad = new JSONArray();
			u_msg.put("ResponseValue",1);
			u_msg.put("Reserved", null);

			int err[];
			err = new int[] {1,1,1,1,1,1,1,1,1,1,1,1};
			for(int i=0;i<SortNum;i++) {	
				byte[] msg = { 0x02, 0, 0, 12, 4, (byte) (i + 1), 0, 2, 12, 0, (byte) u_id, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
				recvmsg = sendToBoard(msg, SortBoardIP[i]);	
				if(recvmsg == null||recvmsg[9]==0) {
					u_msg.put("ResponseValue",0);
					u_msg.put("AlarmStatus",0);
					errorboad.add(i+1);
					Status.add("?");
							
					
				}
				else if(recvmsg[11] == 0) {
					u_msg.put("AlarmStatus",0);
					errorboad.add(i+1);
					for(int j=0;j<recvmsg[15];j++) {
						switch(recvmsg[j*4+17]) {
						case(0x10):
							err[i]=recvmsg[j*4+19];
						break;
						default:
							System.out.println("없는 레지값");
						break;
						}
					}
						System.out.println("i: "+i+" 값: "+err[i]);
						Status.add(err[i]);
					}
				else if(recvmsg[11] == 1) {
					u_msg.put("AlarmStatus",1);				
				}
			}
					
			
			total.add(Status);
			
			
			
			u_msg.put("NumOfBoard",errorboad.size());
			u_msg.put("BoardNum",errorboad);
			u_msg.put("SortAlarmStatus",total);
			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString()); 

		
			break;
			
		case "AlignmentSet":
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
			obj.put("Reserved", "0");
			byte[] msg1 = {0x02,0,0,20,4,(byte) Integer.parseInt(u_msg.get("ChuteNum").toString()),0,2,2,0,(byte)u_id,0,0,0,0,2,
				    			0x20,0x21,0,(byte) Integer.parseInt(u_msg.get("CamLocation").toString()),
				    			0x20,0x20,0,(byte) Integer.parseInt(u_msg.get("CalibrateSelect").toString()),
				    			0x03,0,0,0};
				    	recvmsg = sendToBoard(msg1, SortBoardIP[(byte) Integer.parseInt(u_msg.get("ChuteNum").toString())-1]);
				    	if(recvmsg==null||recvmsg[9]==0) {
				    		ErrorBoard.add("Sort-"+Integer.toString((byte) Integer.parseInt(u_msg.get("ChuteNum").toString())));
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
			
		case "AlignmentLineGet":
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
					String c = "/assets/camImage/f_line"+flinecnt+".txt";
					flinecnt++;
					if (flinecnt == 2) {
						flinecnt = 0;
					}
					u_msg.put("LineDataRr", null);
					u_msg.put("LineDataFr", c);
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
					String g = "/assets/camImage/r_line"+rlinecnt+".txt";
					rlinecnt++;
					if (rlinecnt == 2) {
						rlinecnt = 0;
					}
					u_msg.put("LineDataRr", g);
					u_msg.put("LineDataFr", null);
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
					String c = "/assets/camImage/f_line"+flinecnt+".txt";
					flinecnt++;
					if (flinecnt == 2) {
						flinecnt = 0;
					}
					u_msg.put("LineDataFr", c);
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
					String g = "/assets/camImage/r_line"+rlinecnt+".txt";
					rlinecnt++;
					if (rlinecnt == 2) {
						rlinecnt = 0;
					}
					u_msg.put("LineDataRr", g);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			u_msg.put("Reserved", null);

			array.add(u_msg);
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
			
			break;
			
		case "AlignmentImgGet":
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
//					System.out.println("\n"+getTime()+"시작");
					BB.byteArrayConvertToImageFile(0, (byte) 0x11, recvmsg, (byte) 387000);
					String a = "/assets/camImage/f_img"+fimgcnt+".bmp";
					//				/assets/camlmage/f_img1.bmp
					fimgcnt++;
					if (fimgcnt == 2) {
						fimgcnt = 0;
					}
					u_msg.put("ImageLinkFr", a);
					u_msg.put("ImageLinkRr", null);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (comLo == 2) {
				byte[] msg = { 0x02, 0, 0, 16, 4, chNum, 0, 2, 13,
						0, (byte) u_id, 0, 0, 0, 5, 1, 0x20, 0x11, 0, 1, 0x03, 0, 0, 0 };
				recvmsg = sendToImage(msg,
						SortBoardIP[chNum - 1]);
				if(recvmsg==null) {
					u_msg.put("ResponseValue",0);	
				}else u_msg.put("ResponseValue",1);
				try {
//					System.out.println("\n"+getTime()+"시작");
					BB.byteArrayConvertToImageFile(1, (byte) 0x11, recvmsg, (byte) 387000);
					String b =  "/assets/camImage/r_img"+rimgcnt+".bmp";
					rimgcnt++; 
					if (rimgcnt == 2) {
						rimgcnt = 0;
					}
					u_msg.put("ImageLinkFr", null);
					u_msg.put("ImageLinkRr", b);
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
//						System.out.println("\n"+getTime()+"시작");
						BB.byteArrayConvertToImageFile(0, (byte) 0x11, recvmsg, (byte) 387000);

						String a = "/assets/camImage/f_img"+fimgcnt+".bmp";
						fimgcnt++;
						if (fimgcnt == 2) {
							fimgcnt = 0;
						}
						u_msg.put("ImageLinkFr", a);
						System.out.println("\n"+getTime()+"1번");
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
//						System.out.println("\n"+getTime()+"시작");
						BB.byteArrayConvertToImageFile(1, (byte) 0x11, recvmsg, (byte) 387000);
						String b =  "/assets/camImage/r_img"+rimgcnt+".bmp";
						rimgcnt++;
						if (rimgcnt == 2) {
							rimgcnt = 0;
						}
						u_msg.put("ImageLinkRr", b);
						System.out.println("\n"+getTime()+"2번");
					} catch (Exception e) {

					}

				}

			u_msg.put("Reserved", null);
			array.add(u_msg);
//			System.out.println("\n"+getTime()+"3번");
			sendToLWGM(u_lrecv, u_ip, u_port, array.toString());
//			System.out.println("\n"+getTime()+"끝");
			
			break;
			
		default:
			System.out.println("알수없는 메시지"+u_msg.get("MsgName").toString());
		break;
		}
	}
}