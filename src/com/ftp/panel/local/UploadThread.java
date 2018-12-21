package com.ftp.panel.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.OutputStream;
import java.util.Queue;

import com.ftp.panel.remote.FtpPanel;
import com.ftp.panel.queue.UploadPanel;
import com.ftp.utils.FtpFile;
import com.ftp.utils.ProgressArg;

import sun.net.ftp.FtpClient;

/**
 * FTP�ļ�����ģ��ı����ļ��ϴ����е��߳�
 */
class UploadThread extends Thread {
	private LocalPanel localPanel;
	String path = "";// �ϴ��ļ��ı������·��
	String selPath;// ѡ��ı����ļ���·��
	private boolean conRun = true; // �̵߳Ŀ��Ʊ���
	private FtpClient ftpClient; // FTP������
	private Object[] queueValues; // ������������

	/**
	 * �����ϴ������̵߳Ĺ��췽��
	 *
	 * @param localPanel
	 *            - ������Դ�������
	 */
	public UploadThread(LocalPanel localPanel, FtpClient ftpClient) {
		this.localPanel = localPanel;
		this.ftpClient = ftpClient;
	}

	public void stopThread() { // ֹͣ�̵߳ķ���
		conRun = false;
	}

	/**
	 * �ϴ��̵߳ĵݹ鷽�����ϴ��ļ��е��������ļ��к�����
	 * @param file
	 *            - FTP�ļ�����
	 * @param ftpFile
	 *            - �����ļ��ж���
	 */
	private void copyFile(File file, FtpFile ftpFile) { // �ݹ�����ļ��еķ���
		// �ж϶�������Ƿ�ִ����ͣ����
		while (localPanel.frame.getQueuePanel().isStop()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Object[] args = localPanel.queue.peek();
		// �ж϶��ж��ǲ�����һ�����������
		if (queueValues == null || args == null
				|| !queueValues[0].equals(args[0]))
			return;
		try {
			path = file.getParentFile().getPath().replace(selPath, "");
			ftpFile.setName(path.replace("\\", "/"));
			path = ftpFile.getAbsolutePath();
			if (file.isFile()) {
				UploadPanel uploadPanel = localPanel.frame.getUploadPanel();//�ϴ����
				String remoteFile = path + "/" + file.getName(); // Զ��FTP���ļ�������·��
				double fileLength = file.length() / Math.pow(1024, 2);
				ProgressArg progressArg = new ProgressArg((int) (file.length() / 1024), 0, 0);//���Ȳ���
				String size = String.format("%.4f MB", fileLength);
				Object[] row = new Object[] { file.getAbsoluteFile(), size, remoteFile, ftpClient.getServerAddress().toString(), progressArg };
				uploadPanel.addRow(row); //�����
				OutputStream put = ftpClient.putFileStream(remoteFile); // ��ȡ�������ļ��������
				FileInputStream fis = null; // �����ļ���������
				try {
					fis = new FileInputStream(file); // ��ʼ���ļ���������
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				int readNum = 0;
				byte[] data = new byte[1024]; // �����С
				while ((readNum = fis.read(data)) > 0) { // ��ȡ�����ļ�������
					Thread.sleep(0, 30); // �߳�����
					put.write(data, 0, readNum); // �����������
					progressArg.setValue(progressArg.getValue() + 1);// �ۼӽ�����
				}
				progressArg.setValue(progressArg.getMax()); // ����������
				fis.close(); // �ر��ļ�������
				put.close(); // �رշ����������
			} else if (file.isDirectory()) {
				path = file.getPath().replace(selPath, "");
				ftpFile.setName(path.replace("\\", "/"));
				/**��Ŀ¼�л�����ǰFTP�������ĵ�ǰĿ¼*/
				ftpClient.changeDirectory(this.localPanel.frame.getFtpPanel().getPwd());     //  /mediaĿ¼
				/**
				 * ����д����ļ��е�Ȩ�ޣ����ڵ�ǰFTP�������ĵ�ǰĿ¼�´����ļ���
				 * ����Ҫ�д����ļ��е�Ȩ�ޣ�����ᱨ��
				 */
				ftpClient.makeDirectory(path);   //����  /media/audio Ŀ¼
				ftpClient.getLastReplyCode();

				//����һ���ļ��ж��󣬼����ļ��Ƿ����
				File fileRemote=new File(this.localPanel.frame.getFtpPanel().getPwd()+path);  //path��audio
				//��Ŀ¼������
				if (!fileRemote.exists()) {
					path=this.localPanel.frame.getFtpPanel().getPwd();
				}

				File[] listFiles = file.listFiles();
				for (File subFile : listFiles) {
					Thread.sleep(0, 50);
					copyFile(subFile, ftpFile);
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(0);
			// JOptionPane.showMessageDialog(localPanel, e1.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * �̵߳����巽��
	 */
	public void run() { // �̵߳����巽��
		while (conRun) {
			try {
				Thread.sleep(1000); // �߳�����1��
				Queue<Object[]> queue = localPanel.queue; // ��ȡ�������Ķ��ж���
				queueValues = queue.peek(); // ��ȡ�����׵Ķ���
				if (queueValues == null) { // ����ö���Ϊ��
					continue; // ������һ��ѭ��
				}
				File file = (File) queueValues[0]; // ��ȡ�����еı����ļ�����
				FtpFile ftpFile = (FtpFile) queueValues[1]; // ��ȡ�����е�FTP�ļ�����
				if (file != null) {
					selPath = file.getParent();
					copyFile(file, ftpFile); // ���õݹ鷽���ϴ��ļ�
					FtpPanel ftpPanel = localPanel.frame.getFtpPanel();
					ftpPanel.refreshCurrentFolder(); // ˢ��FTP����е���Դ
				}
				Object[] args = queue.peek();
				// �ж϶��ж��Ƿ�Ϊ�������һ������
				if (queueValues == null || args == null
						|| !queueValues[0].equals(args[0])) {
					continue;
				}
				queue.remove(); // �Ƴ�������Ԫ��
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}