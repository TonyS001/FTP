package com.ftp.panel.remote;

import java.io.*;

import javax.swing.*;

import com.ftp.panel.queue.*;
import com.ftp.utils.*;
import sun.net.ftp.FtpClient;

/**
 * FTP�ļ�����ģ���FTP�ļ����ض��е��߳�
 */
public class DownThread extends Thread {
	private final FtpPanel ftpPanel; // FTP��Դ�������
	private FtpClient ftpClient = null; // FTP������
	private boolean conRun = true; // �̵߳Ŀ��Ʊ���
	private String path; // FTP��·����Ϣ
	private Object[] queueValues; // �������������

	/**
	 * ���췽��
	 *
	 * @param ftpPanel
	 *            - FTP��Դ�������
	 */
	public DownThread(FtpPanel ftpPanel) {
		this.ftpPanel = ftpPanel;
		ftpClient = ftpPanel.ftpClient;
	}

	public void stopThread() {// ֹͣ�̵߳ķ���
		conRun = false;
	}

	/**
	 * �����̵߳ĵݹ鷽�����û�̽��FTP�����ļ��е��������ļ��к�����
	 * @param file  FTP�ļ�����
	 * @param localFolder  �����ļ��ж���
	 */
	private void downFile(FtpFile file, File localFolder) {
		// �ж϶�������Ƿ�ִ����ͣ����
		while (ftpPanel.frame.getQueuePanel().isStop()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Object[] args = ftpPanel.queue.peek();
		// �ж϶��ж��Ƿ�Ϊ�������һ������
		if (queueValues == null || args == null
				|| !queueValues[0].equals(args[0]))
			return;
		try {
			String ftpFileStr = file.getAbsolutePath().replaceFirst(path + "/",
					"");
			if (file.isFile()) {
				// ��ȡ������ָ���ļ���������
				InputStream ftpIs = ftpClient.getFileStream(file.getName());
				if (ftpIs == null) {
					JOptionPane.showMessageDialog(this.ftpPanel, file.getName()
							+ "�޷�����");
					return;
				}
				// ���������ļ�����
				File downFile = new File(localFolder, ftpFileStr);
				// ���������ļ��������
				FileOutputStream fout = new FileOutputStream(downFile, true);
				// �����ļ���С
				double fileLength = file.getLongSize() / Math.pow(1024, 2);
				ProgressArg progressArg = new ProgressArg((int) (file
						.getLongSize() / 1024), 0, 0); //���Ȳ���
				String size = String.format("%.4f MB", fileLength);
				//"�ļ���", "��С", "�����ļ���","����", "״̬"
				Object[] row = new Object[] { ftpFileStr, size,
						downFile.getAbsolutePath(), ftpClient.getServerAddress(),
						progressArg };
				DownloadPanel downloadPanel = ftpPanel.frame.getDownloadPanel(); //���ض������
				downloadPanel.addRow(row);  //�����
				byte[] data = new byte[1024]; // ���建��
				int read = -1;
				while ((read = ftpIs.read(data)) > 0) { // ��ȡFTP�ļ����ݵ�����
					Thread.sleep(0, 30); // �߳�����
					fout.write(data, 0, read); // ����������д�뱾���ļ�
					// �ۼӽ�����
					progressArg.setValue(progressArg.getValue() + 1);
				}
				progressArg.setValue(progressArg.getMax());// ����������
				fout.close(); // �ر��ļ������
				ftpIs.close(); // �ر�FTP�ļ�������
			} else if (file.isDirectory()) { // ������ص����ļ���
				// ���������ļ��ж���
				File directory = new File(localFolder, ftpFileStr);
				directory.mkdirs(); // �������ص��ļ���
				ftpClient.changeDirectory(file.getName()); // �ı�FTP�������ĵ�ǰ·��
				// ��ȡFTP���������ļ��б���Ϣ
				InputStream inputStream=ftpClient.list(null);
				ByteArrayOutputStream temp = clone(inputStream);
				inputStream.close();
				ftpClient.completePending();
				inputStream = new ByteArrayInputStream(temp.toByteArray());
				byte[]names=new byte[2048];
				int bufsize=0;
				bufsize=inputStream.read(names, 0, names.length);
				int i=0,j=0;
				while(i<bufsize){
					//�ַ�ģʽΪ10��������ģʽΪ13
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
							FtpFile ftpFile = new FtpFile();
							// ��FTPĿ¼��Ϣ��ʼ����FTP�ļ�������
							ftpFile.setSize(sizeOrDir);
							ftpFile.setName(fileName);
							ftpFile.setPath(file.getAbsolutePath());
							// �ݹ�ִ�����ļ��е�����
							downFile(ftpFile, localFolder);
						}
						//j=i+1;//��һ��λ��Ϊ�ַ�ģʽ
						j=i+2;//��һ��λ��Ϊ������ģʽ
					}
					i=i+1;
				}
				ftpClient.changeToParentDirectory(); // ����FTP�ϼ�·��
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void run() { // �߳�ҵ�񷽷�
		while (conRun) {
			try {
				Thread.sleep(1000);
				queueValues = ftpPanel.queue.peek();
				if (queueValues == null) {
					continue;
				}
				FtpFile file = (FtpFile) queueValues[0];
				File localFolder = (File) queueValues[1];
				if (file != null) {
					path = file.getPath();
					ftpClient.changeDirectory(path);
					downFile(file, localFolder);
					path = null;
					ftpPanel.frame.getLocalPanel().refreshCurrentFolder();
				}
				Object[] args = ftpPanel.queue.peek();
				// �ж϶��ж��Ƿ�Ϊ�������һ������
				if (queueValues == null || args == null
						|| !queueValues[0].equals(args[0]))
					continue;
				ftpPanel.queue.poll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static ByteArrayOutputStream clone(InputStream input){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = input.read(buffer)) > -1 ) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
			return baos;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}