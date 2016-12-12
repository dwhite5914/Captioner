package com.stanley.captioner;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Captioner
{
    public static void main(String args[])
    {
        try
        {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            System.out.println("Failed to apply look and feel.");
        }

        MainFrame frame = new MainFrame();
        frame.setVisible(true);
    }
}
