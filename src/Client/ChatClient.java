package Client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Statement;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ChatClient extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tf;
	private JButton send;
	private JTextArea textArea;

	private Socket socket;
	private DataOutputStream os;
	private DataInputStream is;
	Connection conn;
	String name;
	java.sql.Statement stm;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClient frame = new ChatClient("Lộc");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws SQLException
	 */
	public ChatClient(String name) throws UnknownHostException, IOException, SQLException {

		String url = "jdbc:sqlserver://localhost:1433;DatabaseName=ChatApp;encrypt=true;trustServerCertificate=true;";
		String userName = "sa";
		String password = "123";
		conn = DriverManager.getConnection(url, userName, password);
		stm = conn.createStatement();

		this.name = name;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 457, 535);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 48, 423, 402);
		contentPane.add(scrollPane);
		
				textArea = new JTextArea();
				scrollPane.setViewportView(textArea);

		tf = new JTextField();
		tf.setBounds(10, 460, 333, 28);
		contentPane.add(tf);
		tf.setColumns(10);

		send = new JButton("send");
		send.setBounds(353, 460, 80, 28);
		contentPane.add(send);

		JLabel namelbl = new JLabel(name);
		namelbl.setBounds(10, 10, 101, 28);
		contentPane.add(namelbl);
		
		JButton exit = new JButton("exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		exit.setBounds(353, 10, 80, 28);
		contentPane.add(exit);

		send.addActionListener(this);
		exit.addActionListener(this);

		socket = new Socket("localhost", 12345);
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());

		getMessages();
		getConn();
		this.setVisible(true);
	}

	private void getMessages() {

		new Thread(() -> {
			String txt;
			while (true) {
				try {
					txt = is.readUTF();
					textArea.append(txt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

	}

	private void getConn() throws SQLException {

		String query = "SELECT SenderUsername, Message FROM ChatHistory";
		java.sql.Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			String sender = rs.getString("SenderUsername");
			String message = rs.getString("Message");
			this.textArea.append(sender + ": " + message + "\n");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("send")) {
			try {
				sendMessages();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		if (e.getActionCommand().equals("exit")) {
			this.dispose();
			new Login();
		}
	}

	private void sendMessages() throws SQLException {
		String message = tf.getText();
		try {
			socket = new Socket("localhost", 12345);
			os = new DataOutputStream(socket.getOutputStream());
			os.writeUTF(name +": " + message);
			os.flush();
			tf.setText("");
			String query = "INSERT INTO ChatHistory (SenderUsername, Message, Timestamp) VALUES(N'" + name + "', '"
					+ message + "',  GETDATE());";
			stm = conn.createStatement();
			stm.execute(query);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
