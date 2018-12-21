package com.ftp.panel.remote;

import javax.swing.table.DefaultTableModel;
/***
 * FTP����ģ��
 */
class FtpTableModel extends DefaultTableModel {
	Class[] types = new Class[] { java.lang.Object.class,
			java.lang.String.class, java.lang.String.class };
	boolean[] canEdit = new boolean[] { false, false, false };

	FtpTableModel() {
		super(new Object[][] {}, new String[] { "�ļ���", "��С", "����" });
	}

	public Class getColumnClass(int columnIndex) {
		return types[columnIndex];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return canEdit[columnIndex];
	}
}