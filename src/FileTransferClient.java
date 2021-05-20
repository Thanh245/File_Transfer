import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class FileTransferClient extends Frame {
	public static String strHostAddress = "";
	public static int intPortNumber = 0, intMaxClients = 0;
	@SuppressWarnings("rawtypes")
	public static Vector vecConnectionSockets = null;
	public static FileTransferClient objFileTransfer;
	public static String strFileName = "", strFilePath = "";
	public static Socket clientSocket = null;
	public static ObjectOutputStream outToServer = null;
	public static ObjectInputStream inFromServer = null;

	public static void main(String[] args) throws IOException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Nhap dia chi cua may server de ket noi: ");
		System.out.flush();
		strHostAddress = stdin.readLine();
		System.out.print("Nhap dia chi cong de ket noi voi may server: ");
		System.out.flush();
		intPortNumber = Integer.parseInt(stdin.readLine());
		objFileTransfer = new FileTransferClient();
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

	@SuppressWarnings("deprecation")
	public FileTransferClient() {
		setTitle("Chương trình truyền File phía Client");
		setSize(500, 400);
		setLayout(null);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		lblTitle = new Label("Chương trình truyền File máy Client ");
		add(lblTitle);
		lblTitle.setBounds(80, 30, 450, 50);
		lblSelectFile = new Label("Đường dẫn file cần truyền:");
		add(lblSelectFile);
		lblSelectFile.setBounds(80, 100, 200, 20);
		
		lblStudentName = new Label("Sinh viên thực hiện: Mai Văn Thành");
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

		try {
			clientSocket = new Socket(strHostAddress, intPortNumber);
			outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			outToServer.flush();
			inFromServer = new ObjectInputStream(clientSocket.getInputStream());
			int intFlag = 0;
			while (true) {
				Object objRecieved = inFromServer.readObject();
				switch (intFlag) {
				case 0:
					if (objRecieved.equals("IsFileTransfered")) {
						intFlag++;
					}
					break;
				case 1:
					strFileName = (String) objRecieved;
					int intOption = JOptionPane.showConfirmDialog(this,
							clientSocket.getInetAddress().getHostName() + " đang gửi " + strFileName
									+ "!\nBạn có chắc chắn nhận không?",
							"Thông báo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);// xac nhan
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
					JOptionPane.showMessageDialog(this, "Bạn đã nhận File thành công từ Server", "Thông báo",
							JOptionPane.INFORMATION_MESSAGE);// file dc nhan;su chung thuc, su xac thuc
					break;
				}
				Thread.yield();
			}
		} catch (Exception e) {
			System.out.println(e);
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
					outToServer.writeObject("IsFileTransfered");
					outToServer.flush();
					outToServer.writeObject(strFileName);
					outToServer.flush();
					outToServer.writeObject(arrByteOfSentFile);
					outToServer.flush();
					JOptionPane.showMessageDialog(null, "Bạn đã gửi File thành công tới Server", "Xác nhận",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
				}
			}
			if (ae.getSource() == btnReset) {
				tfFile.setText("");
			}
		}
	}
}