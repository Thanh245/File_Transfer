import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FileTransferServer extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String strHostAddress = "";
	public static int intPortNumber = 0, intMaxClients = 0;
	@SuppressWarnings("rawtypes")
	public static Vector vecConnectionSockets = null;
	public static FileTransferServer objFileTransfer;
	public static String strFileName = "", strFilePath = "";
	public static Socket clientSocket = null;
	public static ObjectOutputStream outToServer = null;
	public static ObjectInputStream inFromServer = null;

	public static void main(String[] args) throws IOException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Chon so hieu cong de ket noi: ");
		System.out.flush();
		intPortNumber = Integer.parseInt(stdin.readLine());
		System.out.print("So luong may Client co the ket noi: ");
		System.out.flush();
		intMaxClients = Integer.parseInt(stdin.readLine());
		objFileTransfer = new FileTransferServer();
	}

	public Label lblSelectFile;
	public Label lblTitle;
	public Label lblStudentName;
	public Label lblStudentClass;
	public Label lblStudentId;
	public TextField tfFile;
	public Button btnBrowse;
	public Button btnSend;
	public Button btnReset;

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public FileTransferServer() {
		setTitle("Chương trình truyền File phía Server");
		setSize(500, 400);
		setLayout(null);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		lblTitle = new Label("Chương trình truyền File qua mạng dựa trên Socket TCP ");
		add(lblTitle);
		lblTitle.setBounds(80, 30, 450, 50);
		lblSelectFile = new Label("Đường dẫn file cần truyền:");
		add(lblSelectFile);
		lblSelectFile.setBounds(80, 100, 200, 20);
		lblStudentName = new Label("Sinh viên thực hiện : Mai Văn Thành");
		add(lblStudentName);
		lblStudentName.setBounds(150, 300, 250, 20);
		lblStudentClass = new Label("Lớp : 17T3");
		add(lblStudentClass);
		lblStudentClass.setBounds(150, 320, 100, 20);
		lblStudentId = new Label("MSV : 102170192");
		add(lblStudentId);
		lblStudentId.setBounds(150, 340, 250, 20);
		tfFile = new TextField("");
		add(tfFile);
		tfFile.setBounds(80, 134, 200, 20);
		btnBrowse = new Button("Chọn File");
		btnBrowse.addActionListener(new buttonListener());
		add(btnBrowse);
		btnBrowse.setBounds(300, 133, 70, 20);
		btnSend = new Button("Gửi");
		btnSend.addActionListener(new buttonListener());
		add(btnSend);
		btnSend.setBounds(140, 200, 50, 20);
		btnReset = new Button("Xóa");
		btnReset.addActionListener(new buttonListener());
		add(btnReset);
		btnReset.setBounds(210, 200, 50, 20);
		show();
		vecConnectionSockets = new Vector();
		try {
			@SuppressWarnings("resource")
			ServerSocket welcomeSocket = new ServerSocket(intPortNumber, intMaxClients);
			while (true) {
				vecConnectionSockets.addElement(new ThreadedConnectionSocket(welcomeSocket.accept()));
				Thread.yield();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	@SuppressWarnings("deprecation")
	public static String showDialog() {
		FileDialog fd = new FileDialog(new Frame(), "Select File...", FileDialog.LOAD);
		fd.show();
		return fd.getDirectory() + fd.getFile();
	}

	private class buttonListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			byte[] arrByteOfSentFile = null;
			if (ae.getSource() == btnBrowse) {
				strFilePath = showDialog();
				tfFile.setText(strFilePath);
				int intIndex = strFilePath.lastIndexOf("\\");
				strFileName = strFilePath.substring(intIndex + 1);
			}
			if (ae.getSource() == btnSend) {
				try {
					@SuppressWarnings("resource")
					FileInputStream inFromHardDisk = new FileInputStream(strFilePath);
					int size = inFromHardDisk.available();
					arrByteOfSentFile = new byte[size];
					inFromHardDisk.read(arrByteOfSentFile, 0, size);
					for (int i = 0; i < vecConnectionSockets.size(); i++) {
						ThreadedConnectionSocket tempConnectionSocket = (ThreadedConnectionSocket) vecConnectionSockets
								.elementAt(i);
						tempConnectionSocket.outToClient.writeObject("IsFileTransfered");
						tempConnectionSocket.outToClient.flush();
						tempConnectionSocket.outToClient.writeObject(strFileName);
						tempConnectionSocket.outToClient.flush();
						tempConnectionSocket.outToClient.writeObject(arrByteOfSentFile);
						tempConnectionSocket.outToClient.flush();
					}
					JOptionPane.showMessageDialog(null, "Bạn đã gửi File thành công tới Client", "Xác nhận",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
				}
			}
			if (ae.getSource() == btnReset) {
				tfFile.setText("");
			}
		}
	}

	class ThreadedConnectionSocket extends Thread {
		public Socket connectionSocket;
		public ObjectInputStream inFromClient;
		public ObjectOutputStream outToClient;

		public ThreadedConnectionSocket(Socket s) {
			connectionSocket = s;
			try {
				outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
				outToClient.flush();
				inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
			} catch (Exception e) {
				System.out.println(e);
			}
			start();
		}

		public void run() {
			try {
				int intFlag = 0;
				String strFileName = "";
				while (true) {
					Object objRecieved = inFromClient.readObject();
					switch (intFlag) {
					case 0:
						if (objRecieved.equals("IsFileTransfered")) {
							intFlag++;
						}
						break;
					case 1:
						strFileName = (String) objRecieved;
						int intOption = JOptionPane.showConfirmDialog(null,
								connectionSocket.getInetAddress().getHostName() + " Đang gửi " + strFileName
										+ "!\n Bạn có chắc chắn muốn nhận không?",
								"Thông báo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (intOption == JOptionPane.YES_OPTION) {
							intFlag++;
						} else {
							intFlag = 0;
						}
						break;
					case 2:
						byte[] arrByteOfReceivedFile = (byte[]) objRecieved;
						FileOutputStream outToHardDisk = new FileOutputStream(strFileName);
						outToHardDisk.write(arrByteOfReceivedFile);
						outToHardDisk.close();
						intFlag = 0;
						JOptionPane.showMessageDialog(null, "Bạn đã nhận File thành công từ Client", "Xác nhận",
								JOptionPane.INFORMATION_MESSAGE);
						break;
					}
					Thread.yield();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
