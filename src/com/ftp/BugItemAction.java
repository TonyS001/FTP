package com.ftp;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

//点击  帮助(H)-->错误报告(U)  调用系统的电子邮件程序发送邮件
class BugItemAction implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				URI uri = new URI("mailto:850379795@qq.com");
				desktop.mail(uri);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}