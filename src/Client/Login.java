package Client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import java.awt.Color;

public class Login extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField username;
	private JPasswordField password;
	private JButton create;
	private JButton login;
	private JLabel feedback;
	private JPasswordField confirmPass;
	private JLabel confirmlbl;
	private JLabel user;
	private JTextField accName;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		this.setTitle("Đăng Nhập");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 397, 384);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		username = new JTextField();
		username.setBounds(156, 22, 215, 40);
		contentPane.add(username);
		username.setColumns(10);

		JLabel lblNewLabel = new JLabel("user name :");
		lblNewLabel.setBounds(10, 22, 76, 40);
		contentPane.add(lblNewLabel);

		JLabel lblPassword = new JLabel("password :");
		lblPassword.setBounds(10, 72, 76, 40);
		contentPane.add(lblPassword);

		password = new JPasswordField();
		password.setBounds(156, 71, 215, 40);
		contentPane.add(password);

		login = new JButton("login");
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		login.setBounds(286, 230, 85, 40);
		contentPane.add(login);

		create = new JButton("create account");
		create.setBounds(154, 230, 122, 40);
		contentPane.add(create);

		feedback = new JLabel("");
		feedback.setForeground(Color.RED);
		feedback.setBounds(10, 285, 363, 52);
		contentPane.add(feedback);

		confirmPass = new JPasswordField();
		confirmPass.setBounds(156, 121, 215, 40);
		contentPane.add(confirmPass);

		confirmlbl = new JLabel("confirm password :");
		confirmlbl.setBounds(10, 122, 114, 40);
		contentPane.add(confirmlbl);

		user = new JLabel("account's name :");
		user.setBounds(10, 171, 114, 40);
		contentPane.add(user);

		accName = new JTextField();
		accName.setColumns(10);
		accName.setBounds(156, 171, 215, 40);
		contentPane.add(accName);

		login.addActionListener(this);
		create.addActionListener(this);
		confirmlbl.setVisible(false);
		confirmPass.setVisible(false);
		this.accName.setVisible(false);
		this.user.setVisible(false);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("login")) {
			try {
				String user = username.getText();
				char[] pass = password.getPassword();
				logination(user, new String(pass));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (e.getActionCommand().equals("create account")) {
			setToCreateForm();
		}
		if (e.getActionCommand().equals("back")) {
			setToLoginForm();
		}

		if (e.getActionCommand().equals("OK")) {
			String username = this.username.getText();
			char[] password = this.password.getPassword();
			String accName = this.accName.getText();
			try {
				createAccount(accName, username, password);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private void createAccount(String accName, String username, char[] password) throws Exception {
		String url = "jdbc:sqlserver://localhost:1433;DatabaseName=ChatApp;encrypt=true;trustServerCertificate=true;";
		String userName = "sa";
		String dbPassword = "123";
		String pass = "";
		char[] con = this.confirmPass.getPassword();
		String confirm = "";
		for (int i = 0; i < password.length; i++) {
			pass += password[i];
			confirm += con[i];
		}

		// Phương thức try-with-resources để tự động đóng kết nối
		try (Connection conn = DriverManager.getConnection(url, userName, dbPassword)) {
			if (accName == "" || username == "" || pass == "" || confirm == "") {
				feedback.setText("không thể để trống thông tin!");
				return;
			}

			else if (pass.equals(confirm) == false) {
				feedback.setText("mật khẩu không trùng khớp!");
				this.confirmPass.setText("");
				this.password.setText("");
				return;
			}

			else if (checkDuplicateUsername(conn, username)) {
				feedback.setText("Tên người dùng đã tồn tại! Vui lòng chọn tên người dùng khác.");
				return; // Nếu tên người dùng đã tồn tại, không thực hiện thêm tài khoản mới
			}
			String query = "INSERT INTO Users (UserNameInChat,Username, Password) VALUES (?, ?, ?)";

			// Sử dụng PreparedStatement để tránh SQL Injection và làm cho câu lệnh SQL dễ
			// đọc hơn
			try (PreparedStatement pstmt = conn.prepareStatement(query)) {
				// Thiết lập giá trị của các tham số trong câu lệnh SQL
				pstmt.setString(1, accName);
				pstmt.setString(2, username);
				pstmt.setString(3, new String(password));

				// Thực hiện truy vấn INSERT
				int rowsAffected = pstmt.executeUpdate();

				// Kiểm tra xem có bao nhiêu hàng đã được tác động bởi truy vấn INSERT
				if (rowsAffected > 0) {
					feedback.setText("Tài khoản đã được tạo thành công.");
					feedback.setForeground(Color.GREEN);
					Thread.sleep(2000);
					logination(username, new String(password));
				} else {
					feedback.setText("tên người dùng đã tồn tại");
					feedback.setForeground(Color.RED);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean checkDuplicateUsername(Connection conn, String username) throws SQLException {
		String query = "SELECT * FROM Users WHERE Username = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, username);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next(); // Trả về true nếu có ít nhất một bản ghi với tên người dùng đã cho
			}
		}
	}

	private void setToCreateForm() {
		this.setTitle("Đăng Ký Tài Khoản Mới :");
		login.setText("OK");
		create.setText("back");

		confirmlbl.setVisible(true);
		confirmPass.setVisible(true);
		this.accName.setVisible(true);
		this.user.setVisible(true);
	}

	private void setToLoginForm() {
		this.setTitle("Đăng Nhập :");
		login.setText("Login");
		create.setText("create account");

		confirmlbl.setVisible(false);
		confirmPass.setVisible(false);
		this.accName.setVisible(false);
		this.user.setVisible(false);

	}

	private void logination(String user, String pass) throws Exception {
		String userName = "sa";
		String password = "123";
		String DatabaseName = "ChatApp";
		String url = "jdbc:sqlserver://localhost:1433;DatabaseName=" + DatabaseName
				+ ";encrypt=true;trustServerCertificate=true;";
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		try (Connection conn = DriverManager.getConnection(url, userName, password);
				java.sql.Statement stm = conn.createStatement()) {
			String sql = "SELECT UserNameInChat FROM Users WHERE Username = '" + user + "' and Password = '" + pass
					+ "'";
//			System.out.println(sql);
			try (ResultSet rst = stm.executeQuery(sql)) {
				if (rst.next()) {
					String name = rst.getString("UserNameInChat");
					new ChatClient(name);
					this.dispose();
				} else {
					// Đăng nhập thất bại
					feedback.setText("sai tên đăng nhập hoặc mật khẩu!");
					this.password.setText("");

				}
			}
		}
	}
}
