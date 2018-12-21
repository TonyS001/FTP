package com.ftp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

//点击  帮助(H)-->关于(A) 响应显示一个带图片的面板
class AboutItemAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null,
                "由严晓峰、胡秉晖倾情呈现！","老师请给高分！",JOptionPane.INFORMATION_MESSAGE);
    }
}