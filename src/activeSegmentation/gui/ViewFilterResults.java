package activeSegmentation.gui;


import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import activeSegmentation.Common;
import activeSegmentation.IFeatureManager;
import activeSegmentation.IFeatureManagerNew;
import activeSegmentation.IProjectManager;
import activeSegmentation.LearningType;
import activeSegmentation.feature.FeatureManagerNew;
import activeSegmentation.io.ProjectInfo;
import activeSegmentation.io.ProjectManagerImp;

public class ViewFilterResults extends ImageWindow  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int IMAGE_CANVAS_DIMENSION = 560; //same width and height	
	private IProjectManager projectManager;
	private IFeatureManagerNew featureManager;
	private ProjectInfo projectInfo;
	private String filterString;
	private Composite transparency050 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f );
	/** array of roi list overlays to paint the transparent rois of each class */
	private Map<String,RoiListOverlay> roiOverlayList;

	public static final Font FONT = new Font( "Arial", Font.BOLD, 10 );

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	ActionEvent NEXT_BUTTON_PRESSED;

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	ActionEvent SLICE_NEXT_BUTTON_PRESSED;
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	ActionEvent PREVIOUS_BUTTON_PRESSED ;

	ActionEvent SLICE_PREVIOUS_BUTTON_PRESSED;
	private Map<String, JList> exampleList;
	private Map<String, JList> allexampleList;
	JPanel imagePanel,roiPanel;
	JTextField imageNum;
	JTextField sliceField;
	JLabel totalSlice;
	JLabel total;
	List<String> featuresList;
	List<String> images;
	int sliceNum,featureNum,totalSlices,totalFeatures;
	JFrame frame;
	private JComboBox<LearningType> learningType;
	private ImagePlus displayImage;
	public ViewFilterResults(IProjectManager projectManager,IFeatureManagerNew featureManager) {
		//super(image);
		super(featureManager.getCurrentImage());
		this.projectManager = projectManager;
		this.featureManager=featureManager;
		this.featuresList=new ArrayList<String>();
		this.images=new ArrayList<>();
		this.exampleList = new HashMap<String, JList>();
		this.allexampleList = new HashMap<String, JList>();
		this.roiOverlayList = new HashMap<String, RoiListOverlay>();
		this.projectInfo=this.projectManager.getMetaInfo();
		//this.images=loadImages(projectString);
		this.filterString=this.projectInfo.getProjectDirectory().get(Common.FILTERSDIR);
		this.hide();
		showPanel();
	}



	private static ImagePlus  createImageIcon(String path) {
		java.net.URL imgURL = ViewFilterResults.class.getResource(path);
		if (imgURL != null) {
			return new ImagePlus(imgURL.getPath());
		} else {            
			//System.err.println("Couldn't find file: " + path);
			return null;
		}
	}   

	private int loadImages(String directory){
		featuresList.clear();
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isFile()) {
				featuresList.add(file.getName());
			}
		}
		return featuresList.size();
	}

	private int loadSlices(String directory){
		int count=0;
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isDirectory()) {
				count++;
				this.images.add(file.getName());
			}
		}
		return count;
	}


	public void showPanel() {

		frame = new JFrame("VISUALIZATION");
		this.roiPanel=new JPanel();
		NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );
		PREVIOUS_BUTTON_PRESSED= new ActionEvent( this, 1, "Previous" );
		SLICE_NEXT_BUTTON_PRESSED = new ActionEvent( this, 3, "Next" );
		SLICE_PREVIOUS_BUTTON_PRESSED= new ActionEvent( this, 4, "Previous" );
		this.totalSlices=loadSlices(filterString);
		if(totalSlices>0){
			this.sliceNum=1;
			this.totalFeatures=loadImages(filterString+images.get(0));
		}

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JList frameList= Util.model();
		frameList.setForeground(Color.BLACK);
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setFont(FONT);
		panel.setBackground(Color.GRAY);
		imagePanel = new JPanel();	
		imagePanel.setLayout(new BorderLayout());
		imagePanel.setBackground(Color.GRAY);
		ic=new CustomCanvas(featureManager.getCurrentImage());
		//ic.setBounds(10, 10, IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION);
		//ic.setMaximumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
		//ic.setSize(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION);
	     ic.setMinimumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
		if(totalFeatures>0){
			featureNum=1;
			loadImage(sliceNum, featureNum);
			
		}
		
		//imagePanel.add(ic);
		imagePanel.add(ic,BorderLayout.CENTER);
		imagePanel.setBounds( 10, 10, IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION );
		//imagePanel.setMinimumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
		
		panel.add(imagePanel);

		JPanel slicePanel= new JPanel();
		slicePanel.setBounds(605,20,350,80);
		slicePanel.setBorder(BorderFactory.createTitledBorder("SLICES"));
		addButton(new JButton(), "PREVIOUS",null , 610, 70, 120, 20,slicePanel,SLICE_PREVIOUS_BUTTON_PRESSED,null );
		sliceField= new JTextField();
		sliceField.setColumns(5);
		sliceField.setBounds( 630, 70, 10, 20 );
		JLabel sliceLine= new JLabel("/");
		sliceLine.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		sliceLine.setForeground(Color.BLACK);
		sliceLine.setBounds( 670, 70, 10, 20 );
		totalSlice= new JLabel("--");
		totalSlice.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		totalSlice.setForeground(Color.BLACK);
		totalSlice.setBounds( 730, 600, 80, 30);
		if(sliceNum>0){
			sliceField.setText("1");
			totalSlice.setText(Integer.toString(totalSlices));
		}
		slicePanel.add(sliceField);
		slicePanel.add(sliceLine);
		slicePanel.add(totalSlice);
		addButton(new JButton(), "NEXT",null , 800, 50, 80, 20,slicePanel,SLICE_NEXT_BUTTON_PRESSED,null );
		panel.add(slicePanel);
		JPanel features= new JPanel();
		features.setBounds(605,120,350,80);
		features.setBorder(BorderFactory.createTitledBorder("FEATURES"));
		addButton(new JButton(), "PREVIOUS",null , 610, 130, 120, 20,features,PREVIOUS_BUTTON_PRESSED,null );
		imageNum= new JTextField();
		imageNum.setColumns(5);
		imageNum.setBounds( 630, 130, 10, 20 );
		JLabel dasedLine= new JLabel("/");
		dasedLine.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		dasedLine.setForeground(Color.BLACK);
		dasedLine.setBounds(  670, 130, 10, 20 );
		total= new JLabel("Total");
		total.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		total.setForeground(Color.BLACK);
		total.setBounds( 500, 600, 80, 30);		
		if(this.totalFeatures>0){
			imageNum.setText("1");
			total.setText(Integer.toString(totalFeatures));

		}
		features.add(imageNum);
		features.add(dasedLine);
		features.add(total);
		addButton(new JButton(), "NEXT",null , 800, 130, 80, 20,features,NEXT_BUTTON_PRESSED,null );
		frame.add(features);
		JPanel dataJPanel = new JPanel();
		learningType = new JComboBox<LearningType>(LearningType.values());
		learningType.setVisible(true);
		learningType.addItemListener( new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {			 
				//updateGui();	
			}
		});
		dataJPanel.setBounds(720,240,100,40);
		learningType.setSelectedIndex(0);
		learningType.setFont( FONT );
		learningType.setBackground(new Color(192, 192, 192));
		learningType.setForeground(Color.WHITE);
		dataJPanel.add(learningType);
		dataJPanel.setBackground(Color.GRAY);
		panel.add(dataJPanel);
		roiPanel.setBorder(BorderFactory.createTitledBorder("Region Of Interests"));
		//roiPanel.setPreferredSize(new Dimension(200, 400));
		roiPanel.setPreferredSize(new Dimension(350, 175*featureManager.getNumOfClasses()));
		JScrollPane scrollPane = new JScrollPane(roiPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);	
		scrollPane.setBounds(605,300,350,250);
		panel.add(scrollPane);
		frame.add(panel);
		frame.pack();
		frame.setSize(1000,600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
        refreshPanel();
        updateGui();
	}

	private void refreshPanel() {
		roiPanel.removeAll();
		for(String key: featureManager.getClassKeys()){
			//System.out.println();
			String label=featureManager.getClassLabel(key);
			Color color= featureManager.getClassColor(key);
			addSidePanel(color,key,label);
		}		
	}

	private void addSidePanel(Color color,String key,String label){
		JPanel panel= new JPanel();
		JList current=Util.model();

		current.setForeground(color);
		exampleList.put(key,current);
		JList all=Util.model();
		all.setForeground(color);
		allexampleList.put(key,all);	
		RoiListOverlay roiOverlay = new RoiListOverlay();
		roiOverlay.setComposite( transparency050 );
		((OverlayedImageCanvas)ic).addOverlay(roiOverlay);
		roiOverlayList.put(key,roiOverlay);
		JPanel buttonPanel= new JPanel();
		buttonPanel.setName(key);
		ActionEvent addbuttonAction= new ActionEvent(buttonPanel, 1,"AddButton");
		JButton addButton= new JButton();
		addButton.setName(key);
		JButton upload= new JButton();
		upload.setName(key);
		JButton download= new JButton();
		download.setName(key);
		addButton(addButton, label, null, 605,280,350,250, buttonPanel, addbuttonAction, null);
		
		panel.add(Util.addScrollPanel(exampleList.get(key),null));
		panel.add(Util.addScrollPanel(allexampleList.get(key),null));
		roiPanel.add(panel );
		exampleList.get(key).addMouseListener(mouseListener);
		allexampleList.get(key).addMouseListener(mouseListener);
	}

	private void loadImage(int sliceNum, int featureNum){

		this.displayImage= new ImagePlus(filterString+images.get(sliceNum-1)+"/"+featuresList.get(featureNum-1));
	   
		setImage(this.displayImage);
		updateImage(this.displayImage);
		
	}

	public void doAction( final ActionEvent event )
	{

		if(event == PREVIOUS_BUTTON_PRESSED && featureNum >1){

			//System.out.println("BUTTON PRESSED");
			featureNum=featureNum-1;
			imageNum.setText(Integer.toString(featureNum));
			loadImage(sliceNum, featureNum);
			/*if(ic.getWidth()>IMAGE_CANVAS_DIMENSION) {
				int x_centre = ic.getWidth()/2+ic.getX();
				int y_centre = ic.getHeight()/2+ic.getY();
				ic.zoomIn(x_centre,y_centre);
			}	*/
			
			updateGui();

		}
		if(event==NEXT_BUTTON_PRESSED && featureNum<totalFeatures ){
			//	System.out.println("IN NEXT BUTTOn");
			featureNum=featureNum+1;
			imageNum.setText(Integer.toString(featureNum));
		
			/*if(ic.getWidth()>IMAGE_CANVAS_DIMENSION) {
				int x_centre = ic.getWidth()/2+ic.getX();
				int y_centre = ic.getHeight()/2+ic.getY();
				ic.zoomIn(x_centre,y_centre);
			}*/
			loadImage(sliceNum, featureNum);
			//imagePanel.add(ic);
			updateGui();
		}

		if(event==SLICE_PREVIOUS_BUTTON_PRESSED && sliceNum>1){
			featureNum=1;
			sliceNum= sliceNum-1;
			this.totalFeatures=loadImages(filterString+images.get(sliceNum-1)+"/");
			imageNum.setText(Integer.toString(featureNum));
			sliceField.setText(Integer.toString(sliceNum));
			total.setText(Integer.toString(totalFeatures));
			loadImage(sliceNum, featureNum);
			/*if(ic.getWidth()>IMAGE_CANVAS_DIMENSION) {
				int x_centre = ic.getWidth()/2+ic.getX();
				int y_centre = ic.getHeight()/2+ic.getY();
				ic.zoomIn(x_centre,y_centre);
			}*/
			//imagePanel.add(ic);
			updateGui();
		}
		if(event==SLICE_NEXT_BUTTON_PRESSED && sliceNum< totalSlices){
			featureNum=1;
			sliceNum= sliceNum+1;
			this.totalFeatures=loadImages(filterString+images.get(sliceNum-1)+"/");
			imageNum.setText(Integer.toString(featureNum));
			sliceField.setText(Integer.toString(sliceNum));
			total.setText(Integer.toString(totalFeatures));
			loadImage(sliceNum, featureNum);
			/*if(ic.getWidth()>IMAGE_CANVAS_DIMENSION) {
				int x_centre = ic.getWidth()/2+ic.getX();
				int y_centre = ic.getHeight()/2+ic.getY();
				ic.zoomIn(x_centre,y_centre);
			}*/
			//imagePanel.add(ic);
			updateGui();

		}


	}




	private  MouseListener mouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			JList theList = ( JList) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 1) {
				int index = theList.getSelectedIndex();

				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					String[] arr= item.split(" ");
					//System.out.println("Class Id"+ arr[0].trim());
					int sliceNum=Integer.parseInt(arr[2].trim());
					showSelected( arr[0].trim(),index);

				}
			}

			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				String type= learningType.getSelectedItem().toString();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					//System.out.println("ITEM : "+ item);
					String[] arr= item.split(" ");
					//int classId= featureManager.getclassKey(arr[0].trim())-1;
					featureManager.deleteExample(arr[0], Integer.parseInt(arr[1].trim()), type);
					updateGui();
				}
			}
		}
	};

	private JButton addButton(final JButton button ,final String label, final Icon icon, final int x,
			final int y, final int width, final int height,
			JComponent panel, final ActionEvent action,final Color color )
	{
		panel.add( button );
		button.setText( label );
		button.setIcon( icon );
		button.setFont( FONT );
		button.setBorderPainted(false); 
		button.setFocusPainted(false); 
		button.setBackground(new Color(192, 192, 192));
		button.setForeground(Color.WHITE);
		if(color!=null){
			button.setBackground(color);
		}
		button.setBounds( x, y, width, height );
		//System.out.println("ADDED");
		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				//System.out.println("CLICKED");
				doAction(action);
			}
		});

		return button;
	}

	/**
	 * Draw the painted traces on the display image
	 */
	private void drawExamples(){
		for(String key: featureManager.getClassKeys()){
			ArrayList<Roi> rois=(ArrayList<Roi>) featureManager.
					getExamples(key,learningType.getSelectedItem().toString());
			this.roiOverlayList.get(key).setColor(featureManager.getClassColor(key));
			this.roiOverlayList.get(key).setRoi(rois);
			//System.out.println("roi draw"+ key);
		}

		getImagePlus().updateAndDraw();
	}
	private void updateGui(){	
		try{
			drawExamples();
			updateExampleLists();
			//updateallExampleLists();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Select a list and deselect the others
	 * @param e item event (originated by a list)
	 * @param i list index
	 */
	private void showSelected(String classKey,int index ){
		updateGui();
		displayImage.setColor(Color.YELLOW);
		String type= learningType.getSelectedItem().toString();
		//System.out.println(classKey+"--"+index+"---"+type);
		final Roi newRoi = featureManager.getRoi(classKey, index,type);		
		//System.out.println(newRoi);
		newRoi.setImage(displayImage);
		displayImage.setRoi(newRoi);
		displayImage.updateAndDraw();
	} 
	private void updateExampleLists()	{
		LearningType type=(LearningType) learningType.getSelectedItem();
		for(String key:featureManager.getClassKeys()){
			exampleList.get(key).removeAll();
			Vector<String> listModel = new Vector<String>();

			for(int j=0; j<featureManager.getRoiListSize(key, learningType.getSelectedItem().toString()); j++){	
				listModel.addElement(key+ " "+ j + " " +
						featureManager.getCurrentSlice()+" "+type.getLearningType());
			}
			exampleList.get(key).setListData(listModel);
			exampleList.get(key).setForeground(featureManager.getClassColor(key));
		}
	}		
	/*public static void main(String[] args) {
		new ImageJ();

		//projectInfo.setProjectName("testproject");
		IProjectManager projectManager= new ProjectManagerImp();
		projectManager.loadProject("C:\\Users\\sumit\\Documents\\testproject\\testproject.json");
		IFeatureManagerNew featureManagerNew = new FeatureManagerNew(projectManager);
		new ViewFilterResults(projectManager,featureManagerNew,createImageIcon("no-image.jpg"));
		//new ImageWindow(new ImagePlus("C:/Users/HP/Documents/DATASET/Stack-2-0001.tif"));
	}*/

}