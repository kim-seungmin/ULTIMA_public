package sum01;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//version 바꾸기 리저브 정상 null 문제 0
public class LSIMMain extends Thread{
	
	static String getTime(){
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}

	public void run() {
		DatagramSocket Lrecv = null;
		DatagramPacket receivePacket;
		final int portNumber = 6000;
		int smiID;
		int id=1;
		JSONArray array = new JSONArray();
		JSONParser p = new JSONParser();
		JSONObject obj = new JSONObject();
		BoardNum num = new BoardNum();

		DataBase FeedNum = new DataBase();
		int FeddPortNum=FeedNum.findFeedNum();
		num.FeedBoardNum=FeddPortNum/8+1;
		num.FeedPortNum=FeddPortNum;
		DataBase SortNum = new DataBase();
		num.SortBoardNum = SortNum.findSortNum();
		System.out.println("FeedPortNum: "+num.FeedPortNum+" FeedBoardNum: "+num.FeedBoardNum+" SortBoardNum: "+num.SortBoardNum);

		try {
			Lrecv = new DatagramSocket(portNumber);
			System.out.println("LSIMMain");
			while (true) {
				byte[] buf = new byte[1024];
				receivePacket = new DatagramPacket(buf, buf.length);
				Lrecv.receive(receivePacket);
				String msg ="";
				msg = new String(receivePacket.getData(), 0, receivePacket.getLength());		//UDP서버, 웹에서 JSON문자 받기		
				if(!msg.isEmpty()) {
					array = (JSONArray)p.parse(msg);														//문자 파싱
					for(int i=0; i<array.size(); i++) {												//JSON오브젝트 JSON어레이로 만들기
						obj = (JSONObject)array.get(i);
					}
					System.out.println(getTime()+"Client 에서 보낸 메시지 : "+obj.toString());
					if(obj.containsKey("MsgName")) {
						switch(obj.get("MsgName").toString()) {
						case "AllEjectStartStopSet": 
							id=EjectorBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "AllFeedStartStopSet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "ShutdownSet":
							id= IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "FeedSet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "FeedAdvancedSet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "FeedAdvancedGet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "SensColorSet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "SensAdvancedSet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "CleanSet":
							id= IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "CameraSet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "CameraRGBSet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "EjectSet":
							id=EjectorBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "CamBGSet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "CamGOSet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "IntBGAngleSet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "LampBGSet":
							id=LightingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "CamWhiteDarkSet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "CamBGLineGet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "CamBGImgGet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "EjectorSet":
							id=EjectorBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "PingGet":
							id=SubBThread(Lrecv,obj,receivePacket.getAddress(),receivePacket.getPort(),id, num);
							break;
						case "IntAlarmGet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "FeedAlarmGet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "LightAlarmGet":
							id=LightingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "SortAlarmGet":
							id=SortingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "CamAlarmGet":
							id=CameraBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "EjectAlarmGet":
							id=EjectorBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "EjectDriverGet":
							id=EjectorBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "EjectOverloadGet":
							id=EjectorBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "PowerGet":
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "PRTempGet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "AirPressSet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "AirPressGet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "HeaterSet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "HeaterGet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "WaterValveSet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "AlarmLampSet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "IntSwitchInSet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "FeedSwitchInSet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "IntSwitchOutSet":
							id=IntegratedBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id);
							break;
						case "FeedSwitchOutSet":
							id=FeedingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "AlignmentSet":
							id=SortingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "AlignmentLineGet":
							id=SortingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "AlignmentImgGet":
							id=SortingBoard(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "BoardFWVersionGet":
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "BoardHWVersionGet":
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "SetResult":
							// set 결과값
							break;
						case "ModeChangeSet":
							DataBase FeedNum1 = new DataBase();
							int FeddPortNum1=FeedNum1.findFeedNum();
							num.FeedBoardNum=FeddPortNum1/8+1;
							num.FeedPortNum=FeddPortNum1;
							DataBase SortNum1 = new DataBase();
							num.SortBoardNum = SortNum1.findSortNum();
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "ModeRestoreSet":
							DataBase FeedNum2 = new DataBase();
							int FeddPortNum2=FeedNum2.findFeedNum();
							num.FeedBoardNum=FeddPortNum2/8+1;
							num.FeedPortNum=FeddPortNum2;
							DataBase SortNum2 = new DataBase();
							num.SortBoardNum = SortNum2.findSortNum();
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "GenRegSet":
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "GenRegGet":
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						case "LicenceExpire":
							id=Extensive(Lrecv, obj, receivePacket.getAddress(), receivePacket.getPort(), id, num);
							break;
						default:
							System.out.println("알수없는 메시지"+obj.get("MsgName").toString());
						}
					}else {
						System.out.println("No MsgName");
					}
				}else {
					System.out.println("No Msg");
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("소켓 실패");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO 실패");
		}catch (ParseException e) {
			e.printStackTrace();
		}finally {
			Lrecv.close();
			System.out.println("Server 종료");
		}
	}
	private static int SortingBoard(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id, BoardNum num) {
		Sorting_board SB = new Sorting_board(lrecv, obj, address, port, id, num);	//보드 쓰레드 실행
		SB.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
	private static int FeedingBoard(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id, BoardNum num) {
		Feeding_board FB = new Feeding_board(lrecv, obj, address, port, id, num);	//보드 쓰레드 실행
		FB.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
	private static int IntegratedBoard(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id) {
		Integrated_board IB = new Integrated_board(lrecv, obj, address, port,id);	//보드 쓰레드 실행
		IB.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
	private static int LightingBoard(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id) {
		Lighting_board LB = new Lighting_board(lrecv, obj, address, port,id);	//보드 쓰레드 실행
		LB.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
	private static int Extensive(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id, BoardNum num) {
		Extensive ET = new Extensive(lrecv, obj, address, port, id, num);	//보드 쓰레드 실행
		ET.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
	private static int CameraBoard(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id, BoardNum num) {
		Cam_board CB = new Cam_board(lrecv, obj, address, port, id, num);	//보드 쓰레드 실행
		CB.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
	private static int EjectorBoard(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id, BoardNum num) {
		Eject_board EB = new Eject_board(lrecv, obj, address, port, id, num);	//보드 쓰레드 실행
		EB.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
	private static int SubBThread(DatagramSocket lrecv, JSONObject obj, InetAddress address, int port, int id, BoardNum num) {
		SubBThread ST = new SubBThread(lrecv, obj, address, port, id, num);
		ST.start();
		if(id==120) {
			return 1;
		}
		else {
			id++;
			return id;
		}
	}
}