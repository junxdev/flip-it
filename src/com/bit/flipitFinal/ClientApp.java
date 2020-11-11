package com.bit.flipitFinal;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import java.util.Date;
import java.util.Random;

import javax.swing.JButton;

class Card extends JButton {
	int index;
	String value;
	boolean paired;
	Card(int index, String value) {
		super("back");
		this.index = index;
		this.value = value;
	}
}

public class ClientApp extends Frame implements ActionListener {
	
	Socket socket = null;
	String packet = null;
	boolean closeSocket = false;

	String userID = null;
	String userName = null;
	String oppositeName = null;
	int userScore = 0;
	int oppositeScore = 0;
	
	int cardPanelRows = 0;
	int cardPanelColumns = 0;
	String timeCount = null;
	
	Card[] cardDeck = null;
	Card flippedCard = null;
	Card[] flippedCards = new Card[2];
	
	Panel titleScreen = null;
	Panel inGameScreen = null;
	
	// titleScreen Components
	Button startButton = null;
	TextField insertName = null; 
	
	// inGameScreen Components
	Label userBoard = null;
	Label timer = null;
	Label oppositeBoard = null;
	Panel cardPanel = null;
	Panel[] cardBoard = null;
	TextArea console = new TextArea();
	Button restartButton = null;
	Button exitButton = null;
	
	public ClientApp(Socket socket) {
		this.socket = socket;
		new Receiver(socket).start();
		
		setTitle("FlipIt!");
		setSize(1000, 600);
		setLocation((1920 - this.getWidth()) / 2, (1080 - this.getHeight()) / 2);
		setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
		titleScreen = new Panel();
		add(titleScreen);
		
		titleScreen.add(new Label("FlipIt!"));

		insertName = new TextField("Fill in your name");
		insertName.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				insertName.setText("");
				insertName.removeKeyListener(insertName.getKeyListeners()[0]);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
		});
		titleScreen.add(insertName);
		
		startButton = new Button("Start");
		startButton.addActionListener(this);
		titleScreen.add(startButton);
		
		inGameScreen = new Panel();
		inGameScreen.setLayout(new BorderLayout());
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				sendToServer("exitButton : " + userID + "@" + userName);
//				closeSocket = true;
				dispose();
			}
		});
		
		cardPanelRows = 4;
		cardPanelColumns = 4;
		
		Panel statusPanel = new Panel(new GridLayout(1, 3));
		statusPanel.setPreferredSize(new Dimension(1000, 50));
		inGameScreen.add(BorderLayout.PAGE_START, statusPanel);
		
		userBoard = new Label();
		userBoard.setAlignment(Label.CENTER);
		statusPanel.add(userBoard);
		timer = new Label("5");
		timer.setAlignment(Label.CENTER);
		statusPanel.add(timer);
		oppositeBoard = new Label("Looking for new user...");
		oppositeBoard.setAlignment(Label.CENTER);
		statusPanel.add(oppositeBoard);
		
		cardPanel = new Panel(new GridLayout(cardPanelRows, cardPanelColumns));
		inGameScreen.add(BorderLayout.CENTER, cardPanel);
		
		cardBoard = new Panel[cardPanelRows * cardPanelColumns];
		cardDeck = new Card[cardPanelRows * cardPanelColumns];
		
		for(int i = 0; i < cardDeck.length; i++) {
			Card card = new Card(i, "" + ((i / 2) + 1));
			card.setFont(new Font(Font.SERIF, Font.BOLD, 30));
			
			card.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					flippedCard = (Card)e.getSource();
					cardBoard[flippedCard.index].setEnabled(false);
					flippedCard.setText("" + flippedCard.value);
					sendToServer("flippedCard : " + flippedCard.index + " : " + flippedCard.value);
				}
			});
			cardBoard[i] = new Panel();
			cardBoard[i].setLayout(new GridLayout());
			cardPanel.add(cardBoard[i]);
			cardDeck[i] = card;
		}
		
		Panel controlPanel = new Panel(new GridLayout(3, 1));
		controlPanel.setPreferredSize(new Dimension(250, 500));
		
		inGameScreen.add(BorderLayout.LINE_END, controlPanel);
		
		console.setEditable(false);
		controlPanel.add(console);

		restartButton = new Button("Play Again");
		restartButton.setEnabled(false);
		restartButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		restartButton.addActionListener(this);
		controlPanel.add(restartButton);

		exitButton = new Button("Leave");
		exitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		exitButton.addActionListener(this);
		controlPanel.add(exitButton);

		setVisible(true);
	}
	
	@Override
	// 시작, 재시작, 종료버튼을 누를 때 이벤트
	public void actionPerformed(ActionEvent e) {
		if(startButton.equals(e.getSource())) {
			startButton.setEnabled(false);
			userName = insertName.getText();
			userID = new Random().nextInt(1000) + "";
			setTitle("FlipIt! by " + userID + "@" + userName);
			sendToServer("startButton : " + userName + " : " + userID);
		}
		if(restartButton.equals(e.getSource())) {
			restartButton.setEnabled(false);
			userScore = 0;
			oppositeScore = 0;
			userBoard.setBackground(Color.WHITE);
			oppositeBoard.setBackground(Color.WHITE);
			userBoard.setText(userName + " : " + userScore); 
			oppositeBoard.setText("Looking for new user..."); 
			sendToServer("restartButton : " + userName + " : " + userID);
		}
		if(exitButton.equals(e.getSource())) {	
			sendToServer("exitButton : " + userID + "@" + userName);
			dispose();
		}
	}
	
	// 메시지를 시간과 함께 콘솔에 올리는 메서드
	public void printLog(String string) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		console.append("[" + dateFormat.format(new Date()) + "] " + string + "\n");
	}
	
	// 서버에 메세지를 보내는 메서드
	public void sendToServer(String string) {
		System.out.println("send action");
		OutputStream os = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		
		try {
			os = socket.getOutputStream();
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			
			bw.write(string);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// 맞추지 못한 카드를 뒷면으로 두는 메서드
	public void flipBack() {
		for(int i = 0; i < cardDeck.length; i++) { // 모든 카드에 대해서 실행
			if(cardDeck[i].paired == false) { // 짝을 맞추지 못한 카드는
				cardDeck[i].setText("back"); // 뒷면으로 두며
				cardBoard[i].setEnabled(true); // 선택 가능한 상태로 설정하고
			}
			cardBoard[i].add(cardDeck[i]); // 카드판에 올려 놓는다
		}
		revalidate();
	}
	
	// 메시지를 받는 메서드
	public void receive() {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try {
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			
			try {
				// 게임을 종료할 때까지 반복
				while(true) {
					packet = br.readLine();
					String[] data = packet.split(" : ");
					String message = null;

					// 서버가 임의로 메시지를 보낸 경우
					if("Server".equals(data[0])) { 
						message = "Server : " + data[1];

					// 게임에 처음 접속
					} else if("inGame".equals(data[0])) { 
						remove(titleScreen);
						add(inGameScreen);
						userBoard.setText(userName + " : " + userScore);
						message = data[1];

					// 게임 종료
					} else if("Terminate".equals(data[0])) { 
						sendToServer("Gone : ");
						timer.setText("Tip : Focus on cards with high number!");
						userBoard.setBackground(Color.WHITE);
						oppositeBoard.setBackground(Color.WHITE);
						message = data[1] + " has left";

					// 상대방을 기다리는 중인 경우
					} else if("waitOpposite()".equals(data[0])) { 
						if(cardPanel.isEnabled()) {
							cardPanel.setEnabled(false);
						}

						timer.setText("Tip : Each card has a different score!");

						// 몇 초 동안 기다렸는지 채팅창에 표시
						message = "Looking for user : " + data[1] + "s"; 
						
						// 카드 순서를 100번 섞는다
						Random random = new Random();
						for(int i = 0; i < 100; i++) {
							int randomIndex = random.nextInt
									(cardPanelRows * cardPanelColumns - 1) + 1;
							String tempValue = cardDeck[0].value;
							cardDeck[0].value = cardDeck[randomIndex].value;
							cardDeck[randomIndex].value = tempValue;
						}

						// 카드를 패널에 올린다
						for(int i = 0; i < cardDeck.length; i++) {
							cardDeck[i].setText(cardDeck[i].value);
							cardBoard[i].add(cardDeck[i]);
							cardBoard[i].setEnabled(true);
						}

					// 게임을 위해 섞은 카드의 순서를 메시지로 받음
					} else if("shuffle()".equals(data[0])) { 
						String tempCard = null;
						cardPanel.setEnabled(false);

						// 카드를 메시지의 순서대로 패널에 놓음
						for(int i = 0; i < cardDeck.length; i++) { 
							tempCard = "" + data[1].charAt(i);
							cardDeck[i].value = tempCard;
							cardDeck[i].paired = false; 
							cardBoard[i].add(cardDeck[i]);
							cardBoard[i].setEnabled(true);
						}

						continue;

					// 상대방의 이름을 설정하도록 메시지를 받음
					} else if("setOppositeName()".equals(data[0])) { 
						// 상대방의 이름인 경우 상대편 정보판에 설정
						if(!data[1].contains(userID)){
							oppositeName = data[1].substring(data[1].indexOf('@') + 1);
							oppositeBoard.setText(oppositeName + " : " + oppositeScore);
						// 본인의 이름인 경우 생략
						} else {
							continue;
						}
						message = ("Welcome! " + oppositeName);

					// 게임 시작 전 카운트다운 중 몇 초인지 데이터를 받음
					} else if("countdown()".equals(data[0])) { 
						timeCount = data[1];
						timer.setText(timeCount);
						message = "Countdown " + timeCount;

					// 카운트다운이 종료되면 잠깐 답안을 보여줌
					} else if("ShowAnswer".equals(data[0])) { 
						timer.setText("Game Start!");
						message = "Game Start!";
						sendToServer("countdownBreak : ");

						for(int i = 0; i < cardDeck.length; i++) {
							cardDeck[i].setText(cardDeck[i].value);
							cardBoard[i].add(cardDeck[i]);
							cardBoard[i].setEnabled(false);
						}

					// 답안을 보여준 후 모든 카드를 뒤집음
					} else if("FlipBack".equals(data[0])) { 
						flipBack();

						continue;

					// 뒤집을 차례가 바뀐 경우
					} else if("Turn".equals(data[0])) { 
						flipBack();

						// 사용자의 차례인 경우
						if(userID.equals(data[1])) {
							userBoard.setBackground(Color.green);
							oppositeBoard.setBackground(Color.white);
							cardPanel.setEnabled(true);
						// 상대방의 차례인 경우
						} else {
							userBoard.setBackground(Color.white);
							oppositeBoard.setBackground(Color.green);
							cardPanel.setEnabled(false);
						}

						continue;

					// 사용자의 턴 중 카운트다운 중 몇 초인지 데이터를 받음
					} else if("InGameCountdown".equals(data[0])) {
						if(userID.equals(data[1])) {
							timer.setText("Your turn : " + data[2]);
						} else {
							timer.setText(oppositeName + "'s turn : " + data[2]);
						}

						continue;

					// 뒤집은 카드 1장이 무엇인지 알려줌
					} else if("flippedCardIndex".equals(data[0])) {
						int flippedCardIndex = Integer.parseInt(data[1]);
						Card flippedCard = cardDeck[flippedCardIndex];
						flippedCard.setText(flippedCard.value);
						cardBoard[flippedCardIndex].setEnabled(false);
						continue;

					// 카드 2장이 짝이 맞은 경우
					} else if("pairCheck()".equals(data[0])) {
						// 카드 2장을 앞으로 놓음
						for(int i = 0; i < cardDeck.length; i++) {
							if(cardDeck[i].value.equals(data[1])) {
								cardDeck[i].setText(cardDeck[i].value);
								cardDeck[i].paired = true;						
								cardDeck[i].setEnabled(false);					
							}
						}

						// 맞춘 사람의 정보판의 점수를 더하고 각자의 채팅창에 메시지를 올림
						if(userID.equals(data[2])) {
							userBoard.setText(userName + " : " + data[3] + " points");
							message = userName + " got " + data[1] + " points!";
						} else {
							oppositeBoard.setText(oppositeName + " : " + data[3] + " points");
							message = oppositeName + " got " + data[1] + " points!";
						}

					// 게임 종료를 알리고 상호간에 확인하는 메시지
					} else if("Interrupt".equals(data[0])) {
						if(!userID.equals(data[1])) {
							sendToServer("Interrupt : ");
						}
						continue;

					// 서버가 차례를 바꾸도록 알려주는 메시지
					} else if("threadTurn".equals(data[0])) {
						if(!userID.equals(data[1])) {
							sendToServer("threadTurn : ");
						}
						continue;
					
					// 사용자가 승리한 경우
					} else if("Win".equals(data[0])) {
						timer.setText("You win!");
						userBoard.setBackground(Color.RED);
						oppositeBoard.setBackground(Color.WHITE);
						continue;

					// 사용자가 패배한 경우
					} else if("Lose".equals(data[0])) {
						timer.setText("You lose!");
						userBoard.setBackground(Color.WHITE);
						oppositeBoard.setBackground(Color.RED);
						continue;

					// 무승부인 경우
					} else if("Draw".equals(data[0])) {
						timer.setText("Draw!");
						userBoard.setBackground(Color.WHITE);
						oppositeBoard.setBackground(Color.WHITE);
						continue;
					
					// 다시 게임할 것인지 물어봄
					} else if("PlayAgain".equals(data[0])) {
						restartButton.setEnabled(true);
						message = "Wanna play again?";

//					} else if("Gone".equals(data[0])) {
//						userBoard.setBackground(Color.WHITE);
//						oppositeBoard.setBackground(Color.WHITE);
//						restartButton.setEnabled(true);
//						continue;
					}

					// 콘솔창에 메시지를 올림
					console.append(message + "\n");
					revalidate();
				}
			} catch (NullPointerException e) {
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
	
	// 메시지를 받는 메서드를 실행할 리시버 쓰레드
	class Receiver extends Thread {
		
		Socket socket; 
		
		public Receiver(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			receive();
		}
	}
}