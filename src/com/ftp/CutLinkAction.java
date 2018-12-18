package com.ftp;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Icon;

//�Ͽ���ť�Ķ���������
class CutLinkAction extends AbstractAction {
	private FTPClientFrame frame; // ����������ö���

	/**
	 * ���췽��
	 *
	 * @param client_Frame
	 *            �����������
	 * @param string
	 *            ���������ƣ�������ʾ�ڰ�ť��˵��������
	 * @param icon
	 *            ������ͼ�꣬������ʾ�ڰ�ť��˵��������
	 */
	public CutLinkAction(FTPClientFrame client_Frame, String string, Icon icon) {
		super(string, icon); // ���ø���Ĺ��췽��
		frame = client_Frame; // ��ֵ���������ö���
		setEnabled(false); // ���ò�����״̬
	}

	/**
	 * ����Ͽ���ť�İ�ť�����¼��ķ���
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			frame.ftpPanel.stopDownThread(); // ֹͣ�����߳�
			frame.localPanel.stopUploadThread(); // ֹͣ�ϴ��߳�
			frame.getFtpPanel().getQueue().clear(); // ����������
			frame.getFtpPanel().clearTable(); // ���FTP��Դ�������
			frame.getLocalPanel().getQueue().clear(); // ����������Ķ���
			// ���FTP���Ӷ�����ڣ������Ѿ�����FTP������
			if (frame.ftpClient != null && frame.ftpClient.isLoggedIn()) {
				frame.ftpClient.close();
				frame.ftpClient.getLastReplyCode(); // ��ȡ���ر���
				frame.ftpClient = null;
			}
			// �����ϴ���ť������
			frame.localPanel.getActionMap().get("uploadAction").setEnabled(
					false);
			// �������ذ�ť������
			frame.ftpPanel.getActionMap().get("downAction").setEnabled(false);
			setEnabled(false); // ���ñ���ť���Ͽ���������
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}