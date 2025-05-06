package io.hynix.managers.checking.version.ui;

import net.minecraft.util.Util;

import javax.swing.*;
import java.awt.*;

public class Window {
    public static void window() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Графический интерфейс не доступен.");
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Предупреждение");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(null);
        dialog.setSize(300, 150);
        dialog.setResizable(false);

        // Установка модальности
        dialog.setModal(true); // Блокирует следующие действия до закрытия окна

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        iconLabel.setBounds(20, 22, 32, 32);
        dialog.add(iconLabel);

        JLabel textLabel = new JLabel("<html>Доступно более новое обновление!<br>Пожалуйста, обновите чит до новой версии.</html>");
        textLabel.setBounds(60, 15, 200, 50);
        dialog.add(textLabel);

        JButton okButton = new JButton("OK");
        okButton.setBounds(100, 85, 80, 25);
        okButton.addActionListener((ex) -> {
            Util.getOSType().openURI("https://hynix.fun/");
            dialog.dispose(); // Закрываем диалог
            System.exit(0); // Закрытие программы
        });
        dialog.add(okButton);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true); // Открывает диалог и блокирует выполнение кода до его закрытия
    }
}