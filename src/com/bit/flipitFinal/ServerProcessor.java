package com.bit.flipitFinal;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

class User {
	OutputStream os = null;
	String userID = null;
	String userName = null;
	int userScore = 0;
	String[] flippedCards = null;
	String token = null;
	
	User(Socket socket, String userName, String userID) {
		try {
			this.os = socket.getOutputStream();
			this.userName = userName;
			this.userID = userID;
			flippedCards = new String[2];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ServerProcessor extends Frame implements ActionListener  {
	
	static List<User> userList = new ArrayList<User>(); 
	static List<String> pairList = new ArrayList<String>();
	static List<String> tokenList = new ArrayList<String>();
	
	Socket socket = null;

	Receiver receiver = null;
	Operator operator = null;
	Countdown countdown = null;
	
	User user = null;
	
	int cardPanelRows = 4;
	int cardPanelColumns = 4;
	String[] cardDeck = null;
	String cardData = null;
	int timeCount = 0;
	
	static boolean gameOver = false;
	boolean gameSet = false;
	boolean countdownBreak = false;
	boolean threadTurn = false;
	
	TextArea log = null;
	TextField textField = null;
	Button sendButton = null;
	
	public ServerProcessor(Socket socket) {
		this.socket = socket;
		
		setSize(600, 600);
		setLocation(200, 200);
		setTitle("Server");
		setLayout(new BorderLayout());
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		
		log = new TextArea();
		add(BorderLayout.CENTER, log);
		
		textField = new TextField();
		add(BorderLayout.PAGE_END, textField);
		sendButton = new Button("send");
		sendButton.addActionListener(this);
		add(BorderLayout.LINE_END, sendButton);
		setVisible(true);
		
		receiver = new Receiver(socket);
		receiver.start();
	}
	
	// 보내기 버튼을 클릭하면 사용자들에게 임의로 메시지를 보내는 메서드
	@Override
	public void actionPerformed(ActionEvent e) {
		if(sendButton.equals(e.getSource())) {
			String msg = textField.getText();
			sendToUser("Server : " + msg);
			textField.setText("");
		}
	}
	
	// 사용자가 접속하면 사용자 리스트에 추가하는 메서드
	public void addUser(Socket socket, String userName, String userID) {
		user = new User(socket, userName, userID);
		userList.add(user);
	}
	
	// 사용자가 이탈하면 사용자 리스트에서 제거하는 메서드
	public void removeUser() {
		userList.remove(user);
	}
	
	// 메시지 로그를 메시지창에 출력하는 메서드
	public void printLog(String string) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			log.append("[" + dateFormat.format(new Date()) + "] " + string + "!\n");
		revalidate();
	}
	
	// 모든 사용자에게 문자열을 전송하는 메서드
	public void sendToUser(String string) {
		printLog("Sent / " + string);
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		try {
			for(int i = 0; i < userList.size(); i++) {
				os = userList.get(i).os;
				osw = new OutputStreamWriter(os);
				bw = new BufferedWriter(osw);
				
				bw.write(string);
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// 특정 사용자에게 문자열을 전송하는 메서드
	public void sendToUser(User user, String string) {
		printLog("Sent / " + string);
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		try {
			for(int i = 0; i < userList.size(); i++) {
				if(user == userList.get(i)) {
					os = userList.get(i).os;
					osw = new OutputStreamWriter(os);
					bw = new BufferedWriter(osw);
					
					bw.write(string);
					bw.newLine();
					bw.flush();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// 특정 사용자에게 문자열을 전송하는 메서드
	public void sendToUser(int i, String string) {
		printLog("Sent / " + string);
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		try {
			os = userList.get(i).os;
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			
			bw.write(string);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// 사용자가 이름을 입력하면 토큰을 생성하는 메서드
	public void insertToken() {
		user.token = user.userID + "@" + user.userName;
		tokenList.add(user.token);
	}
	
	// 사용자가 정한 크기에 따라 카드 덱을 생성하는 메서드
	public void createDeck(int a, int b) {
		cardDeck = new String[a * b];
		for(int i = 0; i < cardDeck.length; i++) {
			cardDeck[i] = "" + ((i / 2) + 1); 
		}
	}
	
	// 다른 사용자를 몇 초 동안 기다렸는지 사용자에게 전송하는 메서드
	public void waitOpposite(int count) {
		printLog("Waiting for game for " + (count + 1) + "s");
		sendToUser(user, "waitOpposite() : " + (count + 1));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// 카드 순서를 섞고 순서를 사용자 어플리케이션에 보내는 메서드
	public void shuffle() {		
		Random random = new Random();
		for(int i = 0; i < 100; i++) {
			int randomIndex = random.nextInt
					(cardPanelRows * cardPanelColumns - 1) + 1;
			String randomCard = cardDeck[0];
			cardDeck[0] = cardDeck[randomIndex];
			cardDeck[randomIndex] = randomCard;
		}
		cardData = "";
		for(int i = 0; i < cardDeck.length; i++) {
			cardData += cardDeck[i];
		}
		printLog(cardData);
		sendToUser("shuffle() : " + cardData);
	}
	
	// 상대방의 이름을 어플리케이션에 보내는 메서드
	public void setOppositeName() {
		sendToUser("setOppositeName() : " + user.userID + "@" + user.userName);
	}
	
	// 짝을 맞춘 경우 실행되는 메서드
	public void pairCheck() {
		try {
			if(user.flippedCards[0].equals(user.flippedCards[1])) {	
				// 맞춘 사용자의 점수를 높임							
				user.userScore += Integer.parseInt(cardData);
				// 맞춘 카드 리스트에 해당 카드를 등록
				pairList.add(cardData);
				// 사용자에게 카드의 번호와 맞춘 사용자를 알림
				sendToUser("pairCheck() : " + cardData + " : " + user.userID + " : "  + user.userScore);
			}
		} catch(NullPointerException e) {
		}
		// 사용자가 본인 차례에 뒤집은 카드 목록을 비움
		user.flippedCards = new String[2];			
	}

	// 게임 진행 쓰레드
	class Operator extends Thread {
		
		@Override
		public synchronized void run() {
			createDeck(cardPanelRows, cardPanelColumns);

			timeCount = 0;

			// 게임 대기 중 쓰레드를 실행
			countdown = new Countdown();
			countdown.start();
			try {
				countdown.join();
			} catch (InterruptedException e1) {
			}
			printLog("countdown.getState() : " + countdown.getState());

			// 두 명이 모두 게임을 시작 시
			setOppositeName();
			timeCount = 0;

			// 게임 시작 전 카운트 다운 시작
			countdownBreak = false;
			while(!countdownBreak) {
				try {
					// 1번 사용자의 프로세서가 진행
					if((user.userID + "@" + user.userName).equals(tokenList.get(0))) {
						if(timeCount == 0) {
							shuffle();
							sendToUser("FlipBack : ");
						}
						sendToUser("countdown() : " + (5 - timeCount));
						Thread.sleep(1000);
						timeCount++;
						if(timeCount == 5) {
							sendToUser("ShowAnswer : ");
							Thread.sleep(200);
							sendToUser("FlipBack : ");
						}
					}
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			printLog("Start");

			threadTurn = false;

			// 1번 사용자의 차례 먼저
			if(userList.get(0) == user) {
				threadTurn = true;
			}

			// 게임 초기화
			pairList.clear();
			user.flippedCards = new String[2];
			gameOver = false;

			// 게임이 종료될 때까지 실행
			while(true) {
				timeCount = 0;

				// 차례가 아닌 경우 5초간 슬립
				if(!threadTurn) {
					try {
						sleep(10000);
					} catch (InterruptedException e) {
					}
				}

				// 차례가 돌아온 사용자에게 알림
				for(int i = 0; i < userList.size(); i++) {
					if(userList.get(i) == user) {
						sendToUser("Turn : " + userList.get(i).userID);
					}
				}

				// 5초 동안 반복 실행
				while(timeCount < 5) {
					// 게임 종료 시 탈출
					if(gameOver) break;

					// 사용자에게 시간 전송
					sendToUser("InGameCountdown : " + user.userID + " : " + (5 - timeCount));
					
					// 2장을 뒤집으면 탈출
					try {
						if(user.flippedCards[1] != null) {
							sleep(100);
							break;
						}
						sleep(1000);
					} catch (InterruptedException e) {
					}

					timeCount++;
				}

				// 2장 확인
				pairCheck();

				// 차례 전환
				sendToUser("threadTurn : " + user.userID);

				// 게임 종료 시 다른 프로세서도 종료되도록 인터럽트 및 종료 메시지 전파
				if(pairList.size() == (cardPanelRows * cardPanelColumns) / 2) {
					sendToUser("Interrupt : " + user.userID);
					gameOver = true;
				}
				if(gameOver) break;
				threadTurn = !threadTurn;
			}

			// 사용자의 토큰 제거
			tokenList.remove(user.token);
			printLog("gameover");

			// 1번 사용자의 프로세서만 결과 메시지 전송을 실행
			if(userList.get(0) == user) {
				if(userList.get(0).userScore > userList.get(1).userScore) {
					sendToUser(0, "Win : ");
					sendToUser(1, "Lose : ");
				} else if(userList.get(0).userScore < userList.get(1).userScore) {
					sendToUser(0, "Lose : ");
					sendToUser(1, "Win : ");
				} else {
					sendToUser("Draw : ");
				}
				sendToUser("PlayAgain : ");
			}
			printLog("at the end");
			
		}
	}
	
	// 어플리케이션으로부터 메시지를 받는 메서드
	public void receive() {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try {
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			while(true) {
				String packet = br.readLine();
				String[] data = packet.split(" : ");

				// 시작 버튼을 누른 경우
				if("startButton".equals(data[0])) {
					printLog("startButton clicked");
					addUser(socket, data[1], data[2]);
					insertToken();
					operator = new Operator();
					operator.start();
					setTitle("Server by " + user.userID + "@" + user.userName);
					sendToUser(user, "inGame : Welcome! " + user.userName);
					printLog(socket.getRemoteSocketAddress() + "@" + user.userName + " has joined. "
							+ "Current user number is " + userList.size());

				// 다시하기 버튼을 누른 경우
				} else if("restartButton".equals(data[0])) {
					user.userScore = 0;
					insertToken();
					operator = new Operator();
					operator.start();

				// 종료 버튼을 누른 경우
				} else if("exitButton".equals(data[0])) {
					printLog("exitButton clicked");
					if(user != null) {
						tokenList.remove(user.token);
						sendToUser("Terminate : " + user.userName);
						removeUser();
						operator.stop();
					}
					printLog(socket.getInetAddress().getHostAddress() + " : " + data[1] + " has left");
					dispose();
					break;

				// 카운트다운이 끝난 경우
				} else if("countdownBreak".equals(data[0])) {
					countdownBreak = true;

				// 카드를 뒤집은 경우
				} else if("flippedCard".equals(data[0])) {

					// 다른 사용자에게 해당 카드를 알려줌
					for(int i = 0; i < userList.size(); i++) {
						if(userList.get(i) == user) {
							continue;
						}
						sendToUser(i, "flippedCardIndex : " + data[1]);
					}

					// 뒤집은 카드를 배열에 추가하고 2장 추가 시 오퍼레이터 인터럽트
					if(user.flippedCards[0] == null) {
						user.flippedCards[0] = data[2];
					} else {
						user.flippedCards[1] = data[2];
						cardData = data[2];
						operator.interrupt();
					}

				// 차례가 바뀐 경우 오퍼레이터 인터럽트
				} else if("threadTurn".equals(data[0])) {
					threadTurn = !threadTurn;
					operator.interrupt();

				// 필요한 경우 인터럽트 메시지를 보내 오퍼레이터 인터럽트
				} else if("Interrupt".equals(data[0])) {
					operator.interrupt();

				// 사용자가 이탈한 경우
				} else if("Gone".equals(data[0])) {
					// 게임 중인 경우 처리
					if(countdown.getState() == Thread.State.TERMINATED) {
						printLog("Game stopped : Player has left");
						tokenList.remove(user.token);
						sendToUser("PlayAgain : ");
						operator.stop();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(br != null) br.close();
				if(isr != null) isr.close();
				if(is != null) is.close();
				if(socket != null) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 게임 시작 전 대기 중 쓰레드
	class Countdown extends Thread {
		
		public void run() {
			synchronized (tokenList) {
				printLog("run 2");
				// 두 명의 사용자가 토큰을 넣기 전까지 반복
				while(tokenList.size() < 2) {
					waitOpposite(timeCount);
					timeCount++;
				}
			}
		}
	}
	
	// 사용자로부터 메시지를 받는 쓰레드 클래스
	class Receiver extends Thread {
		
		Socket socket; 
		
		public Receiver(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			printLog("New user just has connected");
			receive();
		}
	}

}

