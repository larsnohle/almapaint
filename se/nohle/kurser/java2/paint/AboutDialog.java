/*****************************************************************
 * Small about dialog
 *
 * 01-03-16  ehslano Created
 * 01-10-08  ehslano  Added the createRootPane method.
 *****************************************************************/ 
package se.nohle.kurser.java2.paint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class AboutDialog extends JDialog
{
  //----------------------------------------------------------------
  // Constants
  //---------------------------------------------------------------- 
  private final static int WIDTH = 300;
  private final static int HEIGHT = 130;

  //----------------------------------------------------------------
  // Components
  //---------------------------------------------------------------- 
  private JLabel logoLabel;
  private JLabel versionLabel;
  private JLabel byLabel;
  private JLabel nameLabel;
  private JLabel dateLabel;
  private JButton oKButton;

  //----------------------------------------------------------------
  // Other fields
  //---------------------------------------------------------------- 
  private JFrame parentFrame;
  private final Properties languageBundle;
  

  /*****************************************************************
   * Constructor                                                                                           0
   *****************************************************************/ 
  public AboutDialog(JFrame parent, Properties languageBundle)
  {
    super(parent, languageBundle.getProperty("ABOUT_TITLE"));

    parentFrame = parent;
    this.languageBundle = languageBundle;

    //----------------------------------------------------------------
    // Create components
    //---------------------------------------------------------------- 
    logoLabel = new JLabel(languageBundle.getProperty("TITLE"));
    versionLabel = new JLabel(languageBundle.getProperty("VERSION"));
    byLabel = new JLabel(languageBundle.getProperty("BY"));
    nameLabel = new JLabel(languageBundle.getProperty("MYNAME"));
    dateLabel = new JLabel(languageBundle.getProperty("DATE"));
    oKButton = new JButton(languageBundle.getProperty("OK"));

    //----------------------------------------------------------------
    // Font & colour
    //---------------------------------------------------------------- 
    logoLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
    logoLabel.setForeground(Color.yellow);

    //----------------------------------------------------------------
    // Add listeners
    //---------------------------------------------------------------- 
    oKButton.addActionListener(new OKButtonListener());

    //----------------------------------------------------------------
    // Layout
    //---------------------------------------------------------------- 
    Container cp = this.getContentPane();
    cp.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    
    //----------------------------------------------------------------
    // Add components
    //---------------------------------------------------------------- 
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    cp.add(logoLabel, gbc);

    gbc.gridy = 1;
    cp.add(versionLabel, gbc);

    gbc.gridy = 2;
    cp.add(byLabel, gbc);

    gbc.gridy = 3;
    cp.add(nameLabel, gbc);

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.gridy = 4;
    gbc.insets = new Insets(5,0,0,0);
    gbc.weighty = 1;
    cp.add(dateLabel, gbc);

    gbc.anchor = GridBagConstraints.SOUTH;
    gbc.gridy = 5;
    gbc.insets = new Insets(5,0,5,0);
    gbc.weighty = 0;
    cp.add(oKButton, gbc);

    this.setSize(WIDTH, HEIGHT);
  }

  /*****************************************************************
   * Configure
   *****************************************************************/ 
  public void configure()
  {
    // Set the default button. It seems that this cannot be done in 
    // the constructor if it is to work more than the first time the
    // dialog is displayed.
    this.getRootPane().setDefaultButton(oKButton);   

    this.setLocationRelativeTo(parentFrame);
    this.setSize(WIDTH, HEIGHT);
  }

  //****************************************************************
  // The createRootPane method. Overrides JDialog.
  // Lets the escape button dispose of this dialog.
  //**************************************************************** 
  protected JRootPane createRootPane()
  {
    //----------------------------------------------------------
    // Create a new JRootPane. We're going to modify it a bit
    // and then return it.
    //---------------------------------------------------------- 
    JRootPane rootPane = new JRootPane();

    //----------------------------------------------------------
    // Retrieve the KeyStroke for the escape key.
    //---------------------------------------------------------- 
    KeyStroke keyStroke = KeyStroke.getKeyStroke("ESCAPE");

    //----------------------------------------------------------
    // Create an action that disposes of this dialog when 
    // invoked.
    //---------------------------------------------------------- 
    Action al = new AbstractAction()
    {
      public void actionPerformed(ActionEvent ae)
      {
        AboutDialog.this.dispose();
      }
    };

    //----------------------------------------------------------
    // Retrieve the InputMap for the JRootPane.
    //---------------------------------------------------------- 
    InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

    //----------------------------------------------------------
    // Tie the escape KeyStroke to the action string 'ESCAPE'.
    //---------------------------------------------------------- 
    im.put(keyStroke, "ESCAPE");

    //----------------------------------------------------------
    // In the action map for the root pane, tie the 'ESCAPE' 
    // action string to the Action we defined above.
    //---------------------------------------------------------- 
    rootPane.getActionMap().put("ESCAPE", al);

    //----------------------------------------------------------
    // Return the JRootPane.
    //---------------------------------------------------------- 
    return rootPane;
  }


  /*****************************************************************
   * I  N  N  E  R    C  L  A  S  S  E  S
   *****************************************************************/ 

  /*****************************************************************
   * Listener for the OK button
   *****************************************************************/ 
  private class OKButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      AboutDialog.this.dispose();
    }
  }
}
