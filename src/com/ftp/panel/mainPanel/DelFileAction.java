package com.ftp.panel.mainPanel;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.ftp.panel.local.LocalPanel;
import sun.net.ftp.FtpClient;
import com.ftp.utils.FtpFile;
import sun.net.ftp.FtpProtocolException;

/**
 * FTP����ɾ����ť�Ķ���������
 */
class DelFileAction extends AbstractAction {
	private FtpPanel ftpPanel;

	/**
	 * ɾ�������������Ĺ��췽��
	 *
	 * @param ftpPanel
	 *            - FTP��Դ�������
	 * @param name
	 *            - ��������
	 * @param icon
	 *            - ͼ��
	 */
	public DelFileAction(FtpPanel ftpPanel, String name, Icon icon) {
		super(name, icon);
		this.ftpPanel = ftpPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// ��ȡ��ʾFTP��Դ�б�ı�������ǰѡ���������
		final int[] selRows = ftpPanel.ftpDiskTable.getSelectedRows();
		if (selRows.length < 1)
			return;
		int confirmDialog = JOptionPane.showConfirmDialog(ftpPanel, "ȷ��Ҫɾ����");
		if (confirmDialog == JOptionPane.YES_OPTION) {
			Runnable runnable = new Runnable() {

				/**
				 * ɾ���������ļ��ķ���
				 * @param file - �ļ�����
				 */
				private void delFile(FtpFile file) {
					FtpClient ftpClient = ftpPanel.ftpClient; // ��ȡftpClientʵ��
					try {
						if (file.isFile()) { // ���ɾ�������ļ�
							ftpClient.deleteFile(file.getName()); // ����ɾ���ļ�������
							ftpClient.getLastReplyCode(); // ���շ��ر���
						} else if (file.isDirectory()) { // ���ɾ�������ļ���
							ftpClient.changeDirectory(file.getName()); // ���뵽���ļ���

							InputStream inputStream=ftpClient.list(ftpClient.getWorkingDirectory());
							byte[]names=new byte[2048];
							int bufsize=0;
							bufsize=inputStream.read(names, 0, names.length);
							int i=0,j=0;
							while(i<bufsize){
								//�ַ�ģʽΪ10��������ģʽΪ13
//								if (names[i]==10) {
								if (names[i]==13) {
									//�ļ����������п�ʼ������Ϊj,i-jΪ�ļ����ĳ��ȣ��ļ����������еĽ����±�Ϊi-1
									String fileMessage = new String(names,j,i-j);
									if(fileMessage.length() == 0){
										System.out.println("fileMessage.length() == 0");
										break;
									}
									//���տո�fileMessage��Ϊ������ȡ�����Ϣ
									// ������ʽ  \s��ʾ�ո񣬣�1������ʾ1һ������
									if(!fileMessage.split("\\s+")[8].equals(".") && !fileMessage.split("\\s+")[8].equals("..")){
										/**�ļ���С*/
										String sizeOrDir="";
										if (fileMessage.startsWith("d")) {//�����Ŀ¼
											sizeOrDir="<DIR>";
										}else if (fileMessage.startsWith("-")) {//������ļ�
											sizeOrDir=fileMessage.split("\\s+")[4];
										}
										/**�ļ���*/
										String fileName=fileMessage.split("\\s+")[8];
										/**�ļ�����*/
										String dateStr =fileMessage.split("\\s+")[5] +fileMessage.split("\\s+")[6] +fileMessage.split("\\s+")[7];
										FtpFile ftpFile = new FtpFile();
										// ��FTPĿ¼��Ϣ��ʼ����FTP�ļ�������
										ftpFile.setLastDate(dateStr);
										ftpFile.setSize(sizeOrDir);
										ftpFile.setName(fileName);
										ftpFile.setPath(file.getAbsolutePath());
										// �ݹ�ɾ���ļ����ļ���
										delFile(ftpFile);
									}
									//j=i+1;//��һ��λ��Ϊ�ַ�ģʽ
									j=i+2;//��һ��λ��Ϊ������ģʽ
								}
								i=i+1;
							}
							ftpClient.changeToParentDirectory(); // �����ϲ��ļ���
							ftpClient.removeDirectory(file.getName()); // ����ɾ���ļ���ָ��
							ftpClient.getLastReplyCode(); // ���շ�����
						}
					} catch (Exception ex) {
						Logger.getLogger(LocalPanel.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				}

				/**
				 * �̵߳����巽��
				 */
				public void run() {
					// ������ʾFTP��Դ�ı�������ѡ����
					for (int i = 0; i < selRows.length; i++) {
						// ��ȡÿ�еĵ�һ����Ԫֵ����ת��ΪFtpFile����
						final FtpFile file = (FtpFile) ftpPanel.ftpDiskTable
								.getValueAt(selRows[i], 0);
						if (file != null) {
							delFile(file); // ����ɾ���ļ��ĵݹ鷽��
							try {
								// ���������ɾ���ļ��еķ���
								ftpPanel.ftpClient.removeDirectory(file.getName());
								// ��ȡFTP�������ķ�����
								ftpPanel.ftpClient.getLastReplyCode();
							} catch (IOException | FtpProtocolException e) {
								e.printStackTrace();
							}
						}
					}
					// ˢ��FTP��������Դ�б�
					DelFileAction.this.ftpPanel.refreshCurrentFolder();
					JOptionPane.showMessageDialog(ftpPanel, "ɾ���ɹ���");
				}
			};
			new Thread(runnable).start();
		}
	}
}