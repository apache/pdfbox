package org.apache.pdfbox.tools.pdfdebugger.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.tools.gui.ArrayEntry;
import org.apache.pdfbox.tools.gui.MapEntry;

/**
 * Created by khyrul Bashar.
 */
public class TreeStatusPane extends JPanel implements MouseListener
{
    private Object rootNode;
    private JTree tree;

    private String statusString;
    private JTextField statusTextEditField;
    private JLabel statusLabel;
    private Border textFieldDefaultBorder;
    private Border textFieldErrorBorder;
    private Action textInputAction;

    public TreeStatusPane(JTree _tree)
    {
        tree = _tree;
        init();
    }

    private void init()
    {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        statusString = new String();
        statusTextEditField = new JTextField();
        statusLabel = new JLabel();
        this.add(statusLabel, BorderLayout.PAGE_END);
        this.addMouseListener(this);
        textFieldDefaultBorder = new BevelBorder(BevelBorder.LOWERED);
        textFieldErrorBorder = new BevelBorder(BevelBorder.LOWERED, Color.RED, Color.RED);
        textInputAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {

                    TreePath path = getPath(statusTextEditField.getText());
                    tree.setSelectionPath(path);
                }
                catch (RuntimeException e)
                {
                    statusTextEditField.setBorder(textFieldErrorBorder);
                }
            }
        };
        statusTextEditField.setAction(textInputAction);
    }

    public void updateRootNode(Object obj)
    {
        rootNode = obj;
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

    private TreePath getPath(String statusString) throws RuntimeException
    {
        ArrayList<String> nodes = parsePathString(statusString);
        Object obj = rootNode;
        TreePath path = new TreePath(obj);
        for (String node : nodes)
        {
            obj = searchNode(obj, node);
            path = path.pathByAddingChild(obj);
        }
        return path;
    }

    private ArrayList<String> parsePathString(String path)
    {
        ArrayList<String> nodes = new ArrayList<String>();
        for (String node : path.split(">"))
        {
            node = node.trim();
            if (node.startsWith("["))
            {
                node = node.replace("]", "").replace("[", "");
            }
            node = node.trim();
            if (node.isEmpty())
            {
                throw new RuntimeException("an empty node is not permitted");
            }
            nodes.add(node);
        }
        return nodes;
    }

    private Object searchNode(Object obj, String searchStr)
    {
        if (obj instanceof MapEntry)
        {
            obj = ((MapEntry) obj).getValue();
        }
        else if (obj instanceof ArrayEntry)
        {
            obj = ((ArrayEntry) obj).getValue();
        }
        if (obj instanceof COSObject)
        {
            obj = ((COSObject) obj).getObject();
        }
        if (obj instanceof COSDictionary)
        {
            COSDictionary dic = (COSDictionary) obj;
            if (dic.containsKey(searchStr))
            {
                MapEntry entry = new MapEntry();
                entry.setKey(COSName.getPDFName(searchStr));
                entry.setValue(dic.getItem(searchStr));
                return entry;
            }
        }
        else if (obj instanceof COSArray)
        {
            int index = Integer.parseInt(searchStr);
            COSArray array = (COSArray) obj;
            if (index <= array.size() - 1)
            {
                ArrayEntry entry = new ArrayEntry();
                entry.setIndex(index);
                entry.setValue(array.getObject(index));
                return entry;
            }
        }
        else
        {
            return null;
        }
        return null;
    }

    public void showPath(TreePath path)
    {
        StringBuilder pathStringBuilder = new StringBuilder();
        while (path.getParentPath() != null)
        {
            Object object = path.getLastPathComponent();
            pathStringBuilder.insert(0, " > " + getObjectName(object));
            path = path.getParentPath();
        }
        pathStringBuilder.delete(0, 2);
        this.setStatusString(pathStringBuilder.toString());
    }

    private String getObjectName(Object object)
    {
        String name = "";
        if (object instanceof MapEntry)
        {
            MapEntry entry = (MapEntry) object;
            COSName key = (COSName) entry.getKey();
            name = key.getName();
        }
        else if (object instanceof ArrayEntry)
        {
            ArrayEntry entry = (ArrayEntry) object;
            name = "[" + entry.getIndex() + "]";
        }
        else
        {
            throw new RuntimeException("Unknown COS type " + object.getClass().getName());
        }
        return name;
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
            statusTextEditField.setText(statusString);
            statusTextEditField.setBorder(textFieldDefaultBorder);
            this.add(statusTextEditField, BorderLayout.PAGE_END);
            this.validate();
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
