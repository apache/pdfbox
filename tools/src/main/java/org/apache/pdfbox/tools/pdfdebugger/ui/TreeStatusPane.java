package org.apache.pdfbox.tools.pdfdebugger.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Created by kbashar on 5/5/15.
 */
public class TreeStatusPane extends JPanel implements MouseListener
{
    private String statusString;
    private JTextField statusTextEditField;
    private JLabel statusLabel;

    public TreeStatusPane()
    {
        init();
    }

    private void init()
    {
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        statusString = new String();
        statusTextEditField = new JTextField();
        statusLabel = new JLabel();
        this.add(statusLabel);
        this.addMouseListener(this);
    }

    public String getStatusString()
    {
        return statusString;
    }

    public void setStatusString(String _statusString)
    {
        if (_statusString != null)
        {
            statusString = _statusString;
            setText(statusString);
        }
    }

    private void setText(String statusString)
    {
        if (this.getComponent(0) == statusTextEditField)
        {
            this.removeAll();
            this.revalidate();
            this.repaint();
            this.add(statusLabel);
            statusLabel.setText(statusString);
        }
        else
        {
            statusLabel.setText(statusString);
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent)
    {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent)
    {
        if (mouseEvent.getClickCount() == 2 && !mouseEvent.isConsumed())
        {
            statusLabel.setText("");
            mouseEvent.consume();
            this.remove(statusLabel);
            this.validate();
            statusTextEditField.setText(statusString);
            this.add(statusTextEditField);
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent)
    {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent)
    {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent)
    {
    }
}
