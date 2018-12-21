package com.ftp.panel.remote;

import sun.net.ftp.FtpProtocolException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 * �����ļ��а�ť�Ķ���������
 */
class CreateFolderAction extends AbstractAction {
	private FtpPanel ftpPanel;

	/**
	 * ���췽��
	 *
	 * @param ftpPanel
	 *            - FTP��Դ�������
	 * @param name
	 *            - ��������
	 * @param icon
	 *            - ����ͼ��
	 */
	public CreateFolderAction(FtpPanel ftpPanel, String name, Icon icon) {
		super(name, icon); // ���ø��๹�췽��
		this.ftpPanel = ftpPanel; // ��ֵFTP��Դ������������
	}

	/**
	 * �����ļ��е��¼�������
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// �����û�������½��ļ��е�����
		String folderName = JOptionPane.showInputDialog("�������ļ������ƣ�");
		if (folderName == null)
			return;
		int read = -1;
		try {
			// ���ʹ����ļ��е�����
			ftpPanel.ftpClient.makeDirectory(folderName);
			// ��ȡFTP���������������
			read = ftpPanel.ftpClient.getLastReplyCode().getValue();
		} catch (IOException | FtpProtocolException e1) {
			e1.printStackTrace();
		}
		if (read == 257) {// ������������257��·����������ɣ�
			// ��ʾ�ļ��д����ɹ�
			JOptionPane.showMessageDialog(ftpPanel, folderName + "�ļ��У������ɹ���",
					"�����ļ���", JOptionPane.INFORMATION_MESSAGE);
		}else{
			// ���� ��ʾ�û����ļ����޷�����
			JOptionPane.showMessageDialog(ftpPanel, folderName + "�ļ����޷���������",
					"�����ļ���", JOptionPane.ERROR_MESSAGE);
		}
		this.ftpPanel.refreshCurrentFolder();
	}
}