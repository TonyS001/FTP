package com.ftp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

//���  ����(H)-->����(A) ��Ӧ��ʾһ����ͼƬ�����
class AboutItemAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null,
                "�������塢������������֣�","��ʦ����߷֣�",JOptionPane.INFORMATION_MESSAGE);
    }
}