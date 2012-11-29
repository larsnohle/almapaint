package se.nohle.kurser.java2.paint;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import javax.swing.filechooser.*;

/**
 * The class containing the JFrame of this application.
 */
public class MainFrame extends JFrame
{
  //----------------------------------------------------------
  // Constants
  //---------------------------------------------------------- 
  private static final int WINDOW_WIDTH = 500;
  private static final int WINDOW_HEIGHT = 500;
  private static final String RECTANGLE = "Rektangel";
  private static final String FREE_HAND = "Frihand";
  private Color DEFAULT_COLOR = Color.RED;
  private ToolType DEFAULT_SHAPE_TYPE = ToolType.FREEHAND;
  private static String LANGUAGE_BUNDLE_FILENAME =
  "se/nohle/kurser/java2/paint/languageBundle.properties";  
  private final static Color PURPLE = new Color(128, 0, 128);

  //----------------------------------------------------------
  // Fields
  //---------------------------------------------------------- 
  private JPanel greenPanel;
  private JPanel bluePanel;
  private JPanel blackPanel;
  private JPanel redPanel;
  private JPanel yellowPanel;
  private JPanel pinkPanel;
  private JPanel purplePanel;

  private JPanel palettePanel;
  private JPanel topPanel;
  private JPanel statusPanel;
  private JPanel coordinatePanel;
  private JPanel colorChoiceLabelAndPanelPanel;
  private JPanel colorChoicePanel;
  private JPanel strokeWidthPanel;

  private ShapePanel shapePanel;

  private JComboBox<ToolType> toolCB;
  private JLabel coordinateMessageLabel;
  private JLabel coordinateLabel;
  private JLabel colorChoiceMessageLabel;
  private JLabel strokeWidthMessageLabel;
  private JLabel strokeWidthLabel;

  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenu editMenu;
  private JMenu optionsMenu;
  private JMenu strokeWidthMenu;
  private JMenu helpMenu;

  private JCheckBoxMenuItem optionFillMenuItem;

  private Action newAction;
  private Action undoAction;
  private Action redoAction;
  private Action saveAction;
  private Action saveAsAction;
  private Action openAction;
  private Action exitAction;

  private Action optionFillAction;

  private Action aboutAction;

  private int currentXCoordinate;
  private int currentYCoordinate;
  private boolean fillSelected;

  private File saveFile;
  private int hashCodeOfShapesLastOpen;
  private final static FileNameExtensionFilter AAR_FILE_FILTER = 
  new FileNameExtensionFilter("AAR", "aar");

  private static Properties languageBundle;

  //----------------------------------------------------------
  // STATIC INITIALIZER.
  //---------------------------------------------------------- 
  static
  {
    loadBundle();    
  }

  //----------------------------------------------------------
  // CONSTRUCTOR
  //---------------------------------------------------------- 

  /**
   * Constructor
   *
   * @param title The title of the frame.
   */
  private MainFrame(String title)
  {
    super(title);

    setupFrameProperties();
    createActions();
    createComponents();
    createMenus();
    initializeComponents();
    setupLayouts();
    addComponentsToTopPanel();
    addComponentsToStatusPanel();
    addComponentsToThisFrame();
    setEnabledStateOfActions();
    customizeComponents();
    addListeners();
  }

  //----------------------------------------------------------
  // STATIC METHODS.
  //---------------------------------------------------------- 

  /**
   * Loads the bundle containing the localized texts.
   */
  private static void loadBundle()
  {
    Reader reader = null;
    InputStream is = null;
    try 
    {
      is = ClassLoader.getSystemClassLoader().
       getResourceAsStream(LANGUAGE_BUNDLE_FILENAME);

      languageBundle = new Properties();
      languageBundle.load(is);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        if (is != null)
        {
          is.close();
        }
      }
      catch (Exception ee)
      {
        ee.printStackTrace();
      } 
    } 
  }

  /**
   * Returna a localized string for the specified key.
   *
   * @param key The key to return string for.
   * @return A localized string for key.
   */
  private static String getString(String key)
  {
    return languageBundle.getProperty(key);
  }  


  //----------------------------------------------------------
  // INSTANCE METODS.
  //---------------------------------------------------------- 
  
  /**
   * Sets dome properties of this JFrame.
   */
  private void setupFrameProperties()
  {
    // We want to handle shutdown ourselves.
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

    // Center the frame on the screen.
    setLocationRelativeTo(null);
  }

  /**
   * Create the actions used in the menus and for the button.
   */
  private void createActions()
  {
    newAction = new AbstractAction(getString("NEW"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          shapePanel.removeAllShapes();
          setEnabledStateOfActions();
        }
      };

    undoAction = new AbstractAction(getString("UNDO"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          shapePanel.removeLastShape();
          setEnabledStateOfActions();
        }
      };

    redoAction = new AbstractAction(getString("REDO"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          shapePanel.addLastRemovedShape();
          setEnabledStateOfActions();
        }
      };


    saveAction = new AbstractAction(getString("SAVE"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          saveInvoked();
          setEnabledStateOfActions();
        }
      };

    saveAsAction = new AbstractAction(getString("SAVE_AS"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          saveAsInvoked();
          setEnabledStateOfActions();
        }
      };

    openAction = new AbstractAction(getString("OPEN"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          openInvoked();
          setEnabledStateOfActions();
        }
      };

    exitAction = new AbstractAction(getString("EXIT"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          exitInvoked();
        }
      };

    aboutAction = new AbstractAction(getString("ABOUT"))
      {
        public void actionPerformed(ActionEvent ae)
        {
        }
      };

    optionFillAction = new AbstractAction(getString("FILL"))
      {
        public void actionPerformed(ActionEvent ae)
        {
          fillSelected = optionFillMenuItem.getState();
        }
      };
  }
  
  /**
   * Adds listeners to this frame.
   */
  private void addListeners()
  {
    // Handle the window closing manually.
    this.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent we)
        {
          exitInvoked();
        }
      });

    // Listener to tell the ShapePanel when the selected shape type changes.
    toolCB.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          Object o = e.getItem();
          shapePanel.setCurrentToolType((ToolType)o);
        }
      });

    greenPanel.addMouseListener(new ColorMouseListener(Color.GREEN));
    bluePanel.addMouseListener(new ColorMouseListener(Color.BLUE));
    blackPanel.addMouseListener(new ColorMouseListener(Color.BLACK));
    redPanel.addMouseListener(new ColorMouseListener(Color.RED));
    yellowPanel.addMouseListener(new ColorMouseListener(Color.YELLOW));    
    pinkPanel.addMouseListener(new ColorMouseListener(Color.PINK));    
    purplePanel.addMouseListener(new ColorMouseListener(PURPLE));    

  }


  /**
   * Performs the operations that should be performed when the user tries to
   * exit the application.
   */
  private void exitInvoked()
  {
    int answer = JOptionPane.showConfirmDialog(this, 
getString("CONFIRM_EXIT_DIALOG_MESSAGE"), 
      getString("CONFIRM_EXIT_DIALOG_TITLE"), 
JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (answer == JOptionPane.YES_OPTION)
    {
      System.exit(0);
    }
  }

  /**
   * Creates the components of this application.
   */
  private void createComponents()
  {
    greenPanel = new JPanel();
    bluePanel = new JPanel();
    blackPanel = new JPanel();
    redPanel = new JPanel();
    yellowPanel = new JPanel();
    pinkPanel = new JPanel();
    purplePanel = new JPanel();

    topPanel = new JPanel();
    palettePanel = new JPanel();
    statusPanel = new JPanel();
    coordinatePanel = new JPanel();
    colorChoiceLabelAndPanelPanel = new JPanel();
    colorChoicePanel = new JPanel();
    strokeWidthPanel = new JPanel();

    shapePanel = new ShapePanel(new ShapePanelCallback());

    coordinateMessageLabel = new JLabel(getString("COORDINATES") + " ");
    coordinateLabel = new JLabel("");
    colorChoiceMessageLabel= new JLabel(getString("COLOR_CHOICE"));
    strokeWidthMessageLabel = new JLabel(getString("STROKE_WIDTH") + ": ");
    strokeWidthLabel = new JLabel("1");

    toolCB = new JComboBox<ToolType>();

  }

  /**
   * Creates the menubar, the menus and adds actions to the menus.
   * Also sets mnemonics and accelerators.
   */
  private void createMenus()
  {    
    KeyStroke acceleratorNew = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
    KeyStroke acceleratorUndo = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
    KeyStroke acceleratorRedo = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK);
    KeyStroke acceleratorSave = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
    KeyStroke acceleratorSaveAs = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
    KeyStroke acceleratorOpen = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK);
    KeyStroke acceleratorExit = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK);
    KeyStroke acceleratorFill = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth4 = KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth5 = KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth6 = KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth7 = KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth8 = KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth9 = KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_MASK);
    KeyStroke acceleratorStrokeWidth10 = KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK);

    menuBar = new JMenuBar();
    fileMenu = new JMenu(getString("FILE"));
    JMenuItem menuItem = fileMenu.add(newAction);
    menuItem.setMnemonic(KeyEvent.VK_N);
    menuItem.setAccelerator(acceleratorNew);
    
    fileMenu.addSeparator();

    menuItem = fileMenu.add(saveAction);
    menuItem.setMnemonic(KeyEvent.VK_S);
    menuItem.setAccelerator(acceleratorSave);

    menuItem = fileMenu.add(saveAsAction);
    menuItem.setAccelerator(acceleratorSaveAs);

    menuItem = fileMenu.add(openAction);
    menuItem.setMnemonic(KeyEvent.VK_O);
    menuItem.setAccelerator(acceleratorOpen);

    fileMenu.addSeparator();

    menuItem = fileMenu.add(exitAction);
    menuItem.setMnemonic(KeyEvent.VK_E);
    menuItem.setAccelerator(acceleratorExit);

    fileMenu.setMnemonic(KeyEvent.VK_F);

    editMenu = new JMenu(getString("EDIT"));
    editMenu.setMnemonic(KeyEvent.VK_E);

    menuItem = editMenu.add(undoAction);
    menuItem.setMnemonic(KeyEvent.VK_U);
    menuItem.setAccelerator(acceleratorUndo);
    menuItem = editMenu.add(redoAction);
    menuItem.setMnemonic(KeyEvent.VK_R);
    menuItem.setAccelerator(acceleratorRedo);

    strokeWidthMenu = new JMenu(getString("STROKE_WIDTH"));

    ButtonGroup strokeWidthButtonGroup = new ButtonGroup();
    JRadioButtonMenuItem sw1 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(1));
    JRadioButtonMenuItem sw2 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(2));
    JRadioButtonMenuItem sw3 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(3));
    JRadioButtonMenuItem sw4 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(4));
    JRadioButtonMenuItem sw5 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(5));
    JRadioButtonMenuItem sw6 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(6));
    JRadioButtonMenuItem sw7 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(7));
    JRadioButtonMenuItem sw8 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(8));
    JRadioButtonMenuItem sw9 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(9));
    JRadioButtonMenuItem sw10 = new JRadioButtonMenuItem(
     new StrokeWidthSetterAction(10));
    
    strokeWidthButtonGroup.add(sw1);
    strokeWidthButtonGroup.add(sw2);
    strokeWidthButtonGroup.add(sw3);
    strokeWidthButtonGroup.add(sw4);
    strokeWidthButtonGroup.add(sw5);
    strokeWidthButtonGroup.add(sw6);
    strokeWidthButtonGroup.add(sw7);
    strokeWidthButtonGroup.add(sw8);
    strokeWidthButtonGroup.add(sw9);
    strokeWidthButtonGroup.add(sw10);

    menuItem = strokeWidthMenu.add(sw1);
    menuItem.setAccelerator(acceleratorStrokeWidth1);

    menuItem = strokeWidthMenu.add(sw2);
    menuItem.setAccelerator(acceleratorStrokeWidth2);
    menuItem = strokeWidthMenu.add(sw3);
    menuItem.setAccelerator(acceleratorStrokeWidth3);
    menuItem = strokeWidthMenu.add(sw4);
    menuItem.setAccelerator(acceleratorStrokeWidth4);
    menuItem = strokeWidthMenu.add(sw5);
    menuItem.setAccelerator(acceleratorStrokeWidth5);
    menuItem = strokeWidthMenu.add(sw6);
    menuItem.setAccelerator(acceleratorStrokeWidth6);
    menuItem = strokeWidthMenu.add(sw7);
    menuItem.setAccelerator(acceleratorStrokeWidth7);
    menuItem = strokeWidthMenu.add(sw8);
    menuItem.setAccelerator(acceleratorStrokeWidth8);
    menuItem = strokeWidthMenu.add(sw9);
    menuItem.setAccelerator(acceleratorStrokeWidth9);
    menuItem = strokeWidthMenu.add(sw10);
    menuItem.setAccelerator(acceleratorStrokeWidth10);

    sw1.setSelected(true);

    optionsMenu = new JMenu(getString("OPTIONS"));
    optionFillMenuItem = new JCheckBoxMenuItem(optionFillAction);
    optionFillMenuItem.setAccelerator(acceleratorFill);
    optionsMenu.add(optionFillMenuItem);


    optionsMenu.add(strokeWidthMenu);
    
    helpMenu = new JMenu(getString("HELP"));

    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(optionsMenu);
    menuBar.add(helpMenu);
    this.setJMenuBar(menuBar);
  }

  /**
   * Sets values of some of the components.
   */
  private void initializeComponents()
  {
    toolCB.addItem(ToolType.FREEHAND);
    toolCB.addItem(ToolType.RECTANGLE);
    toolCB.addItem(ToolType.LINE);
    toolCB.addItem(ToolType.CIRCLE);
    toolCB.addItem(ToolType.MOVE);

    toolCB.setSelectedItem(DEFAULT_SHAPE_TYPE);
  }

  /**
   * Sets up the layouts of the containers used in this application.
   */
  private void setupLayouts()
  {
    palettePanel.setLayout(new GridBagLayout());
    topPanel.setLayout(new GridBagLayout());
    statusPanel.setLayout(new GridBagLayout());
    coordinatePanel.setLayout(new GridBagLayout());
    colorChoiceLabelAndPanelPanel.setLayout(new GridBagLayout());
    strokeWidthPanel.setLayout(new GridBagLayout());
  }

  /**
   * Adds components to the top panel.
   */
  private void addComponentsToTopPanel()
  {
    GridBagConstraints gbc = new GridBagConstraints(); 

    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    palettePanel.add(greenPanel, gbc);

    gbc.gridx = 2;
    palettePanel.add(bluePanel, gbc);

    gbc.gridx = 3;
    palettePanel.add(blackPanel, gbc);

    gbc.gridx = 4;
    palettePanel.add(redPanel, gbc);

    gbc.gridx = 5;
    palettePanel.add(yellowPanel,gbc);

    gbc.gridx = 6;
    palettePanel.add(pinkPanel,gbc);

    gbc.gridx = 7;
    palettePanel.add(purplePanel,gbc);
    
    gbc = new GridBagConstraints(); 
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;    
    topPanel.add(palettePanel, gbc);
    
    gbc.gridx = 2;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;    
    topPanel.add(toolCB, gbc);      
  }

  /**
   * Adds components to the status panel.
   */
  private void addComponentsToStatusPanel()
  {
    //----------------------------------------------------------
    // Coordinate panel. 
    //---------------------------------------------------------- 
    GridBagConstraints gbc = new GridBagConstraints(); 

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.SOUTHWEST;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    coordinatePanel.add(coordinateMessageLabel, gbc);

    gbc.gridx = 2;
    gbc.weightx = 1;
    gbc.weighty = 1;
    coordinatePanel.add(coordinateLabel, gbc);
    
    //----------------------------------------------------------
    // Stroke width panel.
    //---------------------------------------------------------- 
    gbc = new GridBagConstraints(); 
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    strokeWidthPanel.add(strokeWidthMessageLabel, gbc);

    gbc.gridx = 2;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weightx = 1;
    gbc.weighty = 1;
    strokeWidthPanel.add(strokeWidthLabel, gbc);

    //----------------------------------------------------------
    // Color choice panel.
    //---------------------------------------------------------- 
    gbc = new GridBagConstraints(); 
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.SOUTHEAST;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    colorChoiceLabelAndPanelPanel.add(colorChoiceMessageLabel, gbc);

    gbc.gridx = 2;
    gbc.weightx = 1;
    gbc.weighty = 1;
    colorChoiceLabelAndPanelPanel.add(colorChoicePanel, gbc);

    //----------------------------------------------------------
    // Status panel.
    //---------------------------------------------------------- 
    Insets insets = new Insets(4, 5, 4, 5);
    gbc = new GridBagConstraints(); 
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.SOUTHWEST;
    gbc.insets = insets;
    gbc.weightx = 1;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    statusPanel.add(coordinatePanel, gbc);    

    gbc.gridx = 2;
    gbc.anchor = GridBagConstraints.SOUTHEAST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = insets;
    gbc.weightx = 0;
    statusPanel.add(strokeWidthPanel, gbc);

    gbc.gridx = 3;
    gbc.anchor = GridBagConstraints.SOUTHEAST;
    gbc.fill = GridBagConstraints.NONE; 
    gbc.insets = insets;
    statusPanel.add(colorChoiceLabelAndPanelPanel, gbc);
  }
 
  /**
   * Adds components to the frame.
   */
  private void addComponentsToThisFrame()
  {
    add(topPanel, BorderLayout.PAGE_START);
    add(statusPanel, BorderLayout.PAGE_END);
    add(shapePanel, BorderLayout.CENTER);
  }

  /**
   * Enables/disables the undo action depending on if some shapes are created or not.
   */
  private void setEnabledStateOfActions() 
  {
    undoAction.setEnabled(shapePanel.hasAtLeastOneShape());
    redoAction.setEnabled(shapePanel.hasAtLeastOneShapeInRedoList());
    saveAction.setEnabled(saveFile != null && 
      hashCodeOfShapesLastOpen != shapePanel.hashCodeOfShapes()); 
    saveAsAction.setEnabled(shapePanel.hasAtLeastOneShape()); // TODO: improve
  }

  /**
   * Customizes some of the components a bit.
   */
  private void customizeComponents()
  {
    greenPanel.setBackground(Color.GREEN);
    bluePanel.setBackground(Color.BLUE);
    blackPanel.setBackground(Color.BLACK);
    redPanel.setBackground(Color.RED);
    yellowPanel.setBackground(Color.YELLOW);
    pinkPanel.setBackground(Color.PINK);
    purplePanel.setBackground(PURPLE);

    shapePanel.setBackground(Color.WHITE);
    shapePanel.setCurrentColor(DEFAULT_COLOR);
    shapePanel.setCurrentToolType(DEFAULT_SHAPE_TYPE);

    colorChoicePanel.setBackground(DEFAULT_COLOR);

    // The width of the coordinates label may change when different coordinates are displayed.
    // Set the minimum width of the coordinates display explicitly to prevent this.
    Dimension d = new Dimension(WINDOW_WIDTH / 5, colorChoicePanel.getPreferredSize().height + 10);
    colorChoicePanel.setPreferredSize(d);
    colorChoicePanel.setMaximumSize(d);
    colorChoicePanel.setMinimumSize(d);

    coordinatePanel.setPreferredSize(d);
    coordinatePanel.setMinimumSize(d);
  }

  /**
   * Called when the user has selected a color from the top panel.
   *
   * @param color The selected color.
   */
  private void colorSelected(Color color)
  {
    shapePanel.setCurrentColor(color);
    colorChoicePanel.setBackground(color);
    colorChoicePanel.repaint();
  }

  /**
   * Displays the specified coordinates in the desinated label
   *
   * @param point The coordinates to display.
   */
  private void displayCoordinates(CoordinatePair point) 
  {
    int xCoordinate = point.x;
    int yCoordinate = point.y;
    if (xCoordinate != currentXCoordinate || 
        yCoordinate != currentYCoordinate)
    {
      currentXCoordinate = xCoordinate;
      currentYCoordinate = yCoordinate;
      coordinateLabel.setText("" + currentXCoordinate +", " + currentYCoordinate);
    } 
  }

  /**
   * Sets the displayed stroke width in the status panel.
   *
   * @param width The stroke width to display in the status panel.
   */ 
  private void setStrokeWidthInStatusPanel(int width)
  {
    strokeWidthLabel.setText("" + width);
  }

  /**
   * Called when the save action has been triggered.
   */
  private void saveInvoked()
  {    
    try
    {      
      FileHandler.save(saveFile, shapePanel.getShapes());
      storeSaveFile(saveFile); // To update the hash code of the shapes.
    }
    catch (Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, 
                                    getString("SAVE_ERROR"),
                                    getString("ERROR"), JOptionPane.ERROR_MESSAGE);
    }       
  }

  /**
   * Called when the save action has been triggered.
   */
  private void saveAsInvoked()
  {    
    // Set up filechooser.
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(AAR_FILE_FILTER);
    
    // Display it.
    int option = fileChooser.showSaveDialog(MainFrame.this);

    // Any file selected?
    if (option == JFileChooser.APPROVE_OPTION) {      
      File file = fileChooser.getSelectedFile();
      // Save the content in the file.
      try
      {
        FileHandler.save(file, shapePanel.getShapes());
        storeSaveFile(file);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
         getString("SAVE_ERROR"), 
         getString("ERROR"), JOptionPane.ERROR_MESSAGE);
      }       
    }    
  }

  /**
   * Called when the open action has been triggered.
   */
  private void openInvoked()
  {    
    // Set up filechooser.
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(AAR_FILE_FILTER);

    // Display it.
    int option = fileChooser.showOpenDialog(MainFrame.this);

    // Any file selected?
    if (option == JFileChooser.APPROVE_OPTION) {      
      File file = fileChooser.getSelectedFile();
      // Open the content in the file.
      try
      {
        shapePanel.setShapes(FileHandler.load(file));
        storeSaveFile(file);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
         "Caught exception when loading file", 
         "Error", JOptionPane.ERROR_MESSAGE);
      }       
    }    
  }

  /**
   *
   *
   * @file The file to use to store in.
   */
  private void storeSaveFile(File file)
  {
    saveFile = file;

    hashCodeOfShapesLastOpen = shapePanel.hashCodeOfShapes();
    
    String title = getString("FRAME_TITLE");
    if (saveFile != null)
    {
      title += ": " + saveFile.getName();
    }

    this.setTitle(title);
  }

  /**
   * M A I N
   */
  public static void main(String[] args)
  {
    MainFrame mf = new MainFrame(getString("FRAME_TITLE"));
    mf.setVisible(true);
  } 

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  // 
  // INNER CLASS
  // 
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH 

  /**
   * Listener for color selections in the top panel.
   */
  private class ColorMouseListener extends MouseAdapter
  {
    private Color colorToSet;
    private ColorMouseListener(Color colorToSet)
    {
      this.colorToSet = colorToSet;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {   
      colorSelected(colorToSet);
    }
  }


  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  // 
  // INNER CLASS
  // 
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH 

  /**
   * Callback invoked from the ShapePanel when the user has performed some action
   * that affects this frame. E.g. moved the mouse pointer so that the displayed 
   * coordinates need to be changed.
   */
  private class ShapePanelCallback implements Callback
  {
    public void numberOfShapesHasChanged()
    {
      setEnabledStateOfActions();
    }

    public void mousePointerCoordinatesChanged(CoordinatePair point)
    {
      displayCoordinates(point);
    }


    public boolean fillSelected()
    {
      return MainFrame.this.fillSelected;
    }
  }

  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  // 
  // INNER CLASS.
  // 
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH 

  private class StrokeWidthSetterAction extends AbstractAction
  {
    private int width;
    private StrokeWidthSetterAction(int width)
    {
      super("" + width);
      this.width = width;
    }

    @Override
    public void actionPerformed(ActionEvent ae)
    {
      shapePanel.setStrokeWidth(width);
      setStrokeWidthInStatusPanel(width);
    }
  }

}
