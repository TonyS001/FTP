package com.ftp.panel.remote;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.ftp.utils.FtpFile;
import sun.net.ftp.FtpProtocolException;

/**
 * ��������ť�Ķ���������
 */
class RenameAction extends AbstractAction {
	private FtpPanel ftpPanel;

	/**
	 * ���췽��
	 *
	 * @param ftpPanel
	 *            - FTP��Դ�������
	 * @param name
	 *            - ����������
	 * @param icon
	 *            - ������ͼ��
	 */
	public RenameAction(FtpPanel ftpPanel, String name, Icon icon) {
		super(name, icon); // ���ø���Ĺ��췽��
		this.ftpPanel = ftpPanel; // ��ֵFTP��Դ������������
	}

	/**
	 * ������FTP�ļ����¼�������
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// ��ȡ��ʾFTP��Դ�ı��ǰѡ���к�
		int selRow = ftpPanel.ftpDiskTable.getSelectedRow();
		if (selRow < 0)
			return;
		// ��ȡ��ǰ�еĵ�һ�����Ԫֵ����ת����FtpFile���͵Ķ���
		FtpFile file = (FtpFile) ftpPanel.ftpDiskTable.getValueAt(selRow, 0);
		// ʹ�öԻ�������û���������ļ����ļ�������
		String newName = JOptionPane.showInputDialog(ftpPanel, "�����������ơ�");
		if (file.getName().equals(".") || file.getName().equals("..")
				|| newName == null)
			return;
		try {
			// �������������������ָ��
			ftpPanel.ftpClient.rename(file.getName(), newName);
			ftpPanel.ftpClient.getLastReplyCode();
			ftpPanel.refreshCurrentFolder(); // ˢ�µ�ǰ�ļ���
		} catch (IOException | FtpProtocolException e1) {
			e1.printStackTrace();
		}
	}
}