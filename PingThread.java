package sum01;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingThread {

	BoardNum Bd = new BoardNum();
	byte x = 0;
	byte y = 0;
	byte z = 0;
	int mnum = 1;
	byte q = 0;
	int fIntCount = 0;
	int fFeedCount = 0;
	int fLightCount = 0;
	int fSortCount = 0;
	int timeOut = 1000;
	int PortNum = 5002;
	int i = 0;
	int sleepCount = 0;
	byte[] UMLI = { 0x02, 0, 0x00, 0x0c, x, y, 0x00, 0x02, 11, 0, z, 0, 0, 0, 0, 0, 0x03, 0, 0, 0 };
	byte[] fmsg = { 0x02, 0, 0x00, 0x0c, 0x00, 0x02, 0x00, 0x02, 0x21, 0, 0, 0, 0x01, q, 0, 0, 0x03, 0, 0, 0 };
	byte[] smsg = { 0x02, 0, 0x00, 0x0c, 0x00, 0x02, 0x00, 0x02, 0x21, 0, 0, 1, 0x01, q, 0, 0, 0x03, 0, 0, 0 };
	boolean flag[] = { true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
			true };
	boolean reflag[] = { true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
			true };

	public void sendSortBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.SortBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			x = 4;
			y = (byte) BoardNum;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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

			for (int k = 1; k <= 12; k++) {
				if (bytes[6] == 4 && bytes[7] == k) {
					System.out.println("\n" + getTime() + "10.0.4." + k + " BOARD OK!");
					flag[k + 4] = true;
					if (reflag[k + 4] == false) {
						if (k == 1) {
							q = 0x41;
						} else if (k == 2) {
							q = 0x42;
						} else if (k == 3) {
							q = 0x43;
						} else if (k == 4) {
							q = 0x44;
						} else if (k == 5) {
							q = 0x45;
						} else if (k == 6) {
							q = 0x46;
						} else if (k == 7) {
							q = 0x47;
						} else if (k == 8) {
							q = 0x48;
						} else if (k == 9) {
							q = 0x49;
						} else if (k == 10) {
							q = 0x4a;
						} else if (k == 11) {
							q = 0x4b;
						} else if (k == 12) {
							q = 0x4c;
						}
						smsg[13] = q;
						sendHandler(smsg);
						System.out.println("오류해제메시지전송");
						reflag[k + 4] = true;
					}
				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= 12; k++) {
				if (BoardNum == k) {
					System.out.println("10.0.4." + k + " 처음실패");
					flag[k + 4] = false;
					sendSortBoardfail(k);
					if (flag[k + 4] == false) {
						fSortCount++;
					} else
						System.out.println("1");
					sendSortBoardfail(k);
					if (flag[k + 4] == false) {
						fSortCount++;
					} else
						System.out.println("2");
					sendSortBoardfail(k);
					if (flag[k + 4] == false) {
						fSortCount++;
					} else
						System.out.println("3");
					if (fSortCount == 3) {
						fSortCount = 0;
						if (reflag[k + 4] == true) {
							if (k == 1) {
								q = 0x41;
							} else if (k == 2) {
								q = 0x42;
							} else if (k == 3) {
								q = 0x43;
							} else if (k == 4) {
								q = 0x44;
							} else if (k == 5) {
								q = 0x45;
							} else if (k == 6) {
								q = 0x46;
							} else if (k == 7) {
								q = 0x47;
							} else if (k == 8) {
								q = 0x48;
							} else if (k == 9) {
								q = 0x49;
							} else if (k == 10) {
								q = 0x4a;
							} else if (k == 11) {
								q = 0x4b;
							} else if (k == 12) {
								q = 0x4c;
							}
							fmsg[13] = q;
							sendHandler(fmsg);
							System.out.println("오류 메시지전송");
							reflag[k + 4] = false;
						}
					} else
						fSortCount = 0;
				}
			}
		}

	}

	public void sendSortBoardfail(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.SortBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			x = 4;
			y = (byte) BoardNum;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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
			for (int k = 1; k <= 12; k++) {
				if (bytes[6] == 4 && bytes[7] == k) {
					System.out.println("\n" + getTime() + "10.0.4." + k + " BOARD OK!");
					flag[k + 4] = true;
					if (reflag[k + 4] == false) {
						if (k == 1) {
							q = 0x41;
						} else if (k == 2) {
							q = 0x42;
						} else if (k == 3) {
							q = 0x43;
						} else if (k == 4) {
							q = 0x44;
						} else if (k == 5) {
							q = 0x45;
						} else if (k == 6) {
							q = 0x46;
						} else if (k == 7) {
							q = 0x47;
						} else if (k == 8) {
							q = 0x48;
						} else if (k == 9) {
							q = 0x49;
						} else if (k == 10) {
							q = 0x4a;
						} else if (k == 11) {
							q = 0x4b;
						} else if (k == 12) {
							q = 0x4c;
						}
						smsg[13] = q;
						sendHandler(smsg);
						System.out.println("오류해제메시지전송");
						reflag[k + 4] = true;
					}

				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= 12; k++) {
				if (BoardNum == k) {
					if (flag[k + 4] == false) {
						System.out.println("\n" + getTime() + "10.0.4." + k + " BOARD fail");
					}
				}
			}
		}
	}

	public void sendLightingBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.LightingBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			x = 3;
			y = (byte) BoardNum;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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
					flag[k + 2] = true;
					if (reflag[k + 2] == false) {
						if (k == 1) {
							q = 0x31;
						} else if (k == 2) {
							q = 0x32;
						}
						smsg[13] = q;
						sendHandler(smsg);
						System.out.println("오류해제메시지전송");
						reflag[k + 2] = true;
					}

				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= 2; k++) {
				if (BoardNum == k) {
					System.out.println("10.0.3." + k + " 처음실패");
					flag[k + 2] = false;
					sendLightingBoardfail(k);
					if (flag[k + 2] == false) {
						fLightCount++; // 1
					} else
						System.out.println("1");
					sendLightingBoardfail(k);
					if (flag[k + 2] == false) {
						fLightCount++; // 1
					} else
						System.out.println("2");
					sendLightingBoardfail(k);
					if (flag[k + 2] == false) {
						fLightCount++; // 1
					} else
						System.out.println("3");

					if (fLightCount == 3) {
						fLightCount = 0;
						if (reflag[k + 2] == true) {
							if (k == 1) {
								q = 0x31;
							} else if (k == 2) {
								q = 0x32;
							}
							fmsg[13] = q;
							sendHandler(fmsg);
							System.out.println("오류 메시지전송");
							reflag[k + 2] = false;
						}
					} else
						fLightCount = 0;
				}
			}
		}
	}

	public void sendLightingBoardfail(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.LightingBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			x = 3;
			y = (byte) BoardNum;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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
					flag[k + 2] = true;
					if (reflag[k + 2] == false) {
						if (k == 1) {
							q = 0x31;
						} else if (k == 2) {
							q = 0x32;
						}
						smsg[13] = q;
						sendHandler(smsg);
						System.out.println("오류해제메시지전송");
						reflag[k + 2] = true;
					}
				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= 2; k++) {
				if (BoardNum == k) {
					if (flag[k + 2] == false) {
						System.out.println("\n" + getTime() + "10.0.3." + k + " BOARD fail");
					}
				}
			}
		}
	}

	public void sendFeedBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.FeedBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 2;
			y = (byte) BoardNum;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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
				if (bytes[6] == 2 && bytes[7] == k) {
					System.out.println("\n" + getTime() + "10.0.2." + k + " BOARD OK!");
					flag[k] = true;
					if (reflag[k] == false) {
						if (k == 1) {
							q = 0x21;
						} else if (k == 2) {
							q = 0x22;
						}
						smsg[13] = q;
						sendHandler(smsg);
						System.out.println("오류해제메시지전송");
						reflag[k] = true;
					}
				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= 2; k++) {
				if (BoardNum == k) {
					System.out.println("10.0.2." + k + " 처음실패");
					flag[k] = false;
					sendFeedBoardfail(k);
					if (flag[k] == false) {
						fFeedCount++; // 1
					} else
						System.out.println("1");
					sendFeedBoardfail(k);
					if (flag[k] == false) {
						fFeedCount++; // 1
					} else
						System.out.println("2");
					sendFeedBoardfail(k);
					if (flag[k] == false) {
						fFeedCount++; // 1
					} else
						System.out.println("3");

					if (fFeedCount == 3) {
						fFeedCount = 0;
						if (reflag[k] == true) {
							if (k == 1) {
								q = 0x21;
							} else if (k == 2) {
								q = 0x22;
							}
							fmsg[13] = q;
							sendHandler(fmsg);
							System.out.println("오류 메시지전송");
							reflag[k] = false;
						}
					} else
						fFeedCount = 0;
				}
			}
		}
	}

	public void sendFeedBoardfail(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.FeedBoardIP[BoardNum - 1], PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 2;
			y = (byte) BoardNum;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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
				if (bytes[6] == 2 && bytes[7] == k) {
					System.out.println("\n" + getTime() + "10.0.2." + k + " BOARD OK!");
					flag[k] = true;
					if (reflag[k] == false) {
						if (k == 1) {
							q = 0x21;
						} else if (k == 2) {
							q = 0x22;
						}
						smsg[13] = q;
						sendHandler(smsg);
						System.out.println("오류해제메시지전송");
						reflag[k] = true;
					}

				}
			}
		} catch (Exception e) {
			for (int k = 1; k <= 2; k++) {
				if (BoardNum == k) {
					if (flag[k] == false) {
						System.out.println("\n" + getTime() + "10.0.2." + k + " BOARD fail");
					}
				}
			}
		}
	}

	public void sendIntegrateBoard(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.IntegratedBoardIP, PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 1;
			y = 1;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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
				flag[0] = true;
				if (reflag[0] == false) {
					q = 0x11;
					smsg[13] = q;
					sendHandler(smsg);
					System.out.println("오류해제메시지전송");
					reflag[0] = true;
				}
			}
		} catch (Exception e) {
			if (BoardNum == 1) {
				System.out.println("10.0.1.1 처음실패");
				flag[0] = false;
				sendIntegrateBoardfail(1);
				if (flag[0] == false) {
					fIntCount++; // 1
				} else
					System.out.println("1");
				sendIntegrateBoardfail(1);
				if (flag[0] == false) {
					fIntCount++; // 2
				} else
					System.out.println("2");
				sendIntegrateBoardfail(1);
				if (flag[0] == false) {
					fIntCount++; // 3
				} else
					System.out.println("3");

				if (fIntCount == 3) {
					fIntCount = 0;
					if (reflag[0] == true) {
						q = 0x11;
						fmsg[13] = q;
						sendHandler(fmsg);
						System.out.println("오류 메시지전송");
						reflag[0] = false;
					}
				} else
					fIntCount = 0;
			}
		}
	}

	public void sendIntegrateBoardfail(int BoardNum) {
		Socket socket = null;
		try {
			socket = new Socket();
			byte[] bytes = new byte[1024];
			socket.setSoTimeout(timeOut);
			SocketAddress socketAddress = new InetSocketAddress(Bd.IntegratedBoardIP, PortNum);
			socket.connect(socketAddress, timeOut);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			x = 1;
			y = 1;
			z = (byte) mnum;
			if (mnum == 120) {
				mnum = 1;
			} else {
				mnum++;
			}
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
				flag[0] = true;
				if (reflag[0] == false) {
					q = 0x11;
					smsg[13] = q;
					sendHandler(smsg);
					System.out.println("오류해제메시지전송");
					reflag[0] = true;
				}
			}

		} catch (Exception e) {
			if (BoardNum == 1) {
				if (flag[0] == false) {
					System.out.println("\n" + getTime() + "10.0.1.1 BOARD fail");
				}
			}
		}
	}

	public void sendHandler(byte[] msg) {
		try {
			InetAddress ia = InetAddress.getByName("10.0.0.2");
			// InetAddress ia = InetAddress.getByName("220.69.240.95");
			DatagramSocket ds = new DatagramSocket();
			DatagramPacket dp = new DatagramPacket(msg, msg.length, ia, 6001);
			ds.send(dp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}

	public void PingLoop() {
		DataBase FeedNum = new DataBase();
		int FeddPortNum = FeedNum.findFeedNum();
		Bd.FeedBoardNum = FeddPortNum / 8 + 1;
		Bd.FeedPortNum = FeddPortNum;
		DataBase SortNum = new DataBase();
		Bd.SortBoardNum = SortNum.findSortNum();
		Runnable runnable = new Runnable() {
			public void run() {

				try {
					sendIntegrateBoard(1);
					for (i = 1; i <= Bd.FeedBoardNum; i++) {
						sendFeedBoard(i);
					}
					for (i = 1; i <= 2; i++) {
						sendLightingBoard(i);
					}
					for (int k = 1; k <= Bd.SortBoardNum; k++) {
						sleepCount = 0;
						if (ServerStartMain.Busyflag[k - 1] == true) {
							System.out.println("Sorting board using-" + k);
							while (sleepCount < 30) {
								Thread.sleep(200);
								sleepCount++;
								System.out.println(ServerStartMain.Busyflag[k - 1]);
								if (ServerStartMain.Busyflag[k - 1] == false) {
									sleepCount = 30;
									sendSortBoard(k);
								}
							}
							if (ServerStartMain.Busyflag[k - 1] == true) {
								sendSortBoard(k);
							}

						} else
							sendSortBoard(k);
					}
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		};
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(runnable, 0, 20000, TimeUnit.MILLISECONDS);

	}

}
