/*
 *	This file is part of DJ_Helper	*
 *	Copyright (C) 2017 Andrew Li				*
 *											*
 *	This program is free software: you can redistribute it and/or modify		*
 *	it under the terms of the GNU General Public License as published by		*
 *	the Free Software Foundation, either version 3 of the License, or		*
 *	(at your option) any later version.						*
 *											*
 *	This program is distributed in the hope that it will be useful,			*
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of			*
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			*
 *	GNU General Public License for more details.					*
 *											*
 *	You should have received a copy of the GNU General Public License		*
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.		*
 *											*
 *	email: andrew.w.li1990@gmail.com						*
 ****************************************************************************************/

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.TextFields;

public class LibraryGUI {
	static Utility util;
	VBox searchMenuGUI,configMenuGUI,addMenuGUI, songLibraryGUI, followSongListGUI, leftGUI, rightGUI;
	VBox testVList;
	TextField searchField, addField;
	ProgressBar progress;
	Button addButton, reInitButton, updateButton, deleteButton, openConfigButton, goToNextButton;
	ScrollPane followScrollPane;
	static ListView<SongEntry> followListView;
	VBoxListView vListView;
	CheckBox appendDirectionBox;
	ObservableList<SongEntry> fill;
	static ObservableList<SongEntry> followFill;

	public final int appendImgWidth = 82;
	public final int appendImgHeight = 31;
	
	public static final String REGEX_TITLE = "(.*) <!";
	public static final String REGEX_ARTIST = "<!(.*)!>";
	
	public static final String ADD_LABEL = "Add follow songs";
	public static final String SEARCH_LABEL = "Search your library";
	public static final String ALERT_REINIT_CONFIRMATION = "WARNING\n\nThis will rebuild your entire library save file.\n"
			+ "You will lose ALL follow song data.\n"
			+ "*Your actual songs are safe, don't worry*\n"
			+ "This will also take a few minutes.\n"
			+ "This program will close upon completion and will need to be restarted.\n\n"
			+ "Continue?";
	public static final String ALERT_REINIT_HEADER = "Reinitialization Confirmation";
	public static final String ALERT_UPDATE_CONFIRMATION = "This will attempt to update your library save file"
			+ " with songs created/downloaded later than last update.\n"
			+ "This will take a few minutes.\n"
			+ "This program will close upon completion and will need to be restarted.\n\n"
			+ "Continue?";
	public static final String ALERT_UPDATE_HEADER = "Update Confirmation";
	
	/**
	 * constructor, connects the Utility object, instantiates menus and populates the GUI song list
	 */
	public LibraryGUI(Utility util){
		this.util = util;		
		
		searchMenuGUI = makeSearchBar();
		searchMenuGUI.getStyleClass().add("general_menu_with_label");
		
		vListView = new VBoxListView(util);
		songLibraryGUI = vListView.getVList();
		
		addMenuGUI = makeAddBar();
		addMenuGUI.getStyleClass().add("general_menu_with_label");
		
		followSongListGUI = new VBox();
		initFollowListView();
		
		configMenuGUI = makeConfigBar();
		configMenuGUI.getStyleClass().add("entire_config_menu");
		
		//initListView();
		
		leftGUI = new VBox(searchMenuGUI, songLibraryGUI);
		leftGUI.setSpacing(5);

		rightGUI = new VBox(addMenuGUI, followSongListGUI, configMenuGUI);
		//rightGUI = new VBox(addMenuGUI, configMenuGUI);
		rightGUI.setSpacing(5);
	}
	
	/**
	 * TODO remove, unused listview from 1.3
	 */
	/*
	public void initListView(){
		listView = new ListView<SongEntry>(); //fill ListView with that string list
	
		//Customized ListView output code block
        listView.setCellFactory(new Callback<ListView<SongEntry>, ListCell<SongEntry>>() {
        	
            @Override
            public ListCell<SongEntry> call(ListView<SongEntry> arg0) {
                return new ListCell<SongEntry>() {

                    @Override
                    protected void updateItem(SongEntry item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null || !bln) {
                            ImageView albumArtImageView;
                            
                            //read the image art (if possible) and display it/placeholder image in ImageView
	                        BufferedImage img = util.getAlbumArt(item.getFilePath());
	                        if(img != null){
		                        WritableImage wr = null; 		
		                        albumArtImageView = new ImageView(SwingFXUtils.toFXImage(img, wr));
	                        }else{
	                            Image im = new Image("/empty_album_art.png", 50, 50, false, false);
	                            albumArtImageView = new ImageView(im);
	                        }

                            //labels for title and artist
                            Label labelTitle = new Label(item.getTitle());
                            labelTitle.getStyleClass().add("title_label");
                            
                            Label labelArtist = new Label(item.getArtist());
                            labelArtist.getStyleClass().add("artist_label");
                            
                            HBox hBox = new HBox(albumArtImageView, new VBox(labelTitle, labelArtist));
                            hBox.setSpacing(10);
                            setGraphic(hBox);
                        }else{
                        	//this prevents listview duplication bug
                        	setText(null);
                        	setGraphic(null);
                        }
                    }//updateItem
                };//return new  ListCell;
            }//public ListCell call
        });//set cell factory
		
		//event handler for ListView
		listView.setOnMouseClicked(e -> {
			if(listView.getItems().size() > 0){
				String selectedSong = listView.getSelectionModel().getSelectedItem().getTitle();
				refreshFollowListView(selectedSong);
			}else{
				//do nothing
			}
		});
		
		//press enter to do same thing as clicks (above)
		listView.setOnKeyReleased(new EventHandler<KeyEvent>()
	    {
	        @Override
	        public void handle(KeyEvent ke)
	        {
	            if(ke.getCode() == KeyCode.ENTER){
	            	if(listView.getItems().size() > 0 && listView.getSelectionModel() != null){
	            		String selectedSong = listView.getSelectionModel().getSelectedItem().getTitle();
						refreshFollowListView(selectedSong);
	            	}
	            }
	        }//handle
	    });//onKeyReleased
		
		listView.setMinSize(450, 600);
		songLibraryGUI.getChildren().add(listView);
	}//populate listview
	*/
	
	/**
	 * rereads the xml song list with a regex filter and then re-sets the items in the list view
	 */
	public void refreshListView(String regex){
		fill = util.readXmlSongs(regex);
		vListView.setSongList(fill);
		VBoxListView.setSelectedSong(0);
	}
	
	/**
	 * initializes handlers for the follow song gui
	 */
	public void initFollowListView(){
		followListView = new ListView<SongEntry>();
		
		followListView.setOnKeyReleased(new EventHandler<KeyEvent>()
	    {
	        @Override
	        public void handle(KeyEvent ke)
	        {
	            if(ke.getCode() == KeyCode.ENTER){
	            	searchCurrentFollowSong();
	            }//if
	        }//handle
	    });//onKeyReleased
		
		//event handler for ListView
		followListView.setOnMouseClicked(e -> {
			if(followListView.getItems().size() > 0 && followListView.getSelectionModel().getSelectedItem() != null){
				deleteButton.setDisable(false);
				goToNextButton.setDisable(false);
			}else{
				//
			}
		});
		
		followListView.setMinSize(450, 450);
		followSongListGUI.getChildren().add(followListView);
		
	}
	
	/**
	 * uses the current follow song selection as the new input for searching
	 */
	public void searchCurrentFollowSong(){
		
		
		if(followListView.getItems().size() > 0 && followListView.getSelectionModel().getSelectedItem() != null){
    		refreshListView(followListView.getSelectionModel().getSelectedItem().getTitle());
    		searchField.clear();	
    	}
		
		//disable buttons if no follow songs are found
		if(followListView.getItems().size() == 0 || followListView.getSelectionModel().getSelectedItem() == null){
			deleteButton.setDisable(true);
			goToNextButton.setDisable(true);
		}
	}
	
	/**
	 * refresh followListViewGUI upon clicking a listview item
	 */
	public static void refreshFollowListView(String selectedSong){
		followFill = util.readXmlFollowSongs(selectedSong);
		followListView.setItems(followFill);
	}
	
	/**
	 * returns a VBox containing instantiated and functional search and progress bars
	 */
	public VBox makeSearchBar(){

		Label searchLabel = new Label(SEARCH_LABEL); 
		searchLabel.getStyleClass().add("label_menus");
		
		searchField = TextFields.createClearableTextField();
		searchField.getStyleClass().add("textfield_bar");
		
		//typing event handler
		searchField.setOnKeyReleased(new EventHandler<KeyEvent>()
	    {
			
	        @Override
	        public void handle(KeyEvent ke)
	        {
	            if(searchField.getText().length() > 2){
	            	refreshListView(searchField.getText());
	            }//if
	        }//handle
	    });//onKeyReleased
		
		HBox searchAndProg = new HBox(searchField);
		searchAndProg.getStyleClass().add("general_bar_menu");
		VBox entireSearchBar = new VBox(searchLabel, searchAndProg);
		
		return entireSearchBar;
	}
	
	/**
	 * instantiates and gives function to add search and button
	 */
	public VBox makeAddBar(){
		//add label
		Label addLabel = new Label(ADD_LABEL);
		addLabel.getStyleClass().add("label_menus");
		
		//add textfield
		addField = TextFields.createClearableTextField();
		addField.getStyleClass().add("textfield_bar");
		TextFields.bindAutoCompletion(addField, util.readXmlSongs("")); 
		addField.setOnKeyPressed(new EventHandler<KeyEvent>()
	    {
	        @Override
	        public void handle(KeyEvent ke)
	        {
	            if (ke.getCode().equals(KeyCode.ENTER))
	            {
	            	if(!VBoxListView.isEmpty() && VBoxListView.selected != null){
		            	String lineInput = addField.getText();
		            	String title = util.doRegex(lineInput, REGEX_TITLE);
		            	//String artist = util.doRegex(lineInput, REGEX_ARTIST);
		            	
		            	util.appendFollowSong(VBoxListView.selected, title);
		            	refreshFollowListView(VBoxListView.selected);
	            	}
	            }
	        }
	    });
		
		//append direction image
		Image appendImage;

		if(Utility.APPEND_BOTH_WAYS){
			appendImage = new Image("two_way.png", appendImgWidth, appendImgHeight, false, false);
		}else{
			appendImage = new Image("one_way.png", appendImgWidth, appendImgHeight, false, false);
		}
		ImageView appendImageView = new ImageView(appendImage);
		
		//append direction check box
		appendDirectionBox = new CheckBox();
		appendDirectionBox.setSelected(true);
		appendDirectionBox.setOnAction(new EventHandler<ActionEvent>() {
			  
	    	 public void handle(ActionEvent event) {
	    		 if(appendDirectionBox.isSelected()){
	    			 Utility.APPEND_BOTH_WAYS = true;
	    			 appendImageView.setImage(new Image("two_way.png", appendImgWidth, appendImgHeight, false, false));
	    			 System.out.println(VBoxListView.selected);
	    		 }else{
	    			 Utility.APPEND_BOTH_WAYS = false;
	    			 appendImageView.setImage(new Image("one_way.png", appendImgWidth, appendImgHeight, false, false));
	    			 System.out.println(VBoxListView.selected);
	    		 }
	    	 }
	     });
		
		
		
		//add button
		addButton = new Button("Submit");
	    addButton.setOnAction(new EventHandler<ActionEvent>() {
	  
	    	 public void handle(ActionEvent event) {
	    		 if(!VBoxListView.isEmpty() && VBoxListView.selected != null){
	    		 	String lineInput = addField.getText();
	            	String title = util.doRegex(lineInput, REGEX_TITLE);
	            	//String artist = util.doRegex(lineInput, REGEX_ARTIST);
	            	
	            	util.appendFollowSong(VBoxListView.selected, title);
	            	refreshFollowListView(VBoxListView.selected);
	    		 }
	    	 }
	     });
	    
	    //delete button
	    deleteButton = new Button("Delete Follow Song");
	    deleteButton.setDisable(true);
	    deleteButton.setOnAction(new EventHandler<ActionEvent>() {
	    	 public void handle(ActionEvent event) {
	    		 if(followListView.getSelectionModel().getSelectedItem() != null && VBoxListView.selected != null){
		    		 String songTitle = VBoxListView.selected;
		    		 String fSongTitle = followListView.getSelectionModel().getSelectedItem().getTitle();
		    		 util.deleteFollowSong(songTitle, fSongTitle);
		    		 refreshFollowListView(songTitle);
		    		 deleteButton.setDisable(true);
	    		 }
	    	 }
	     });
		
	    goToNextButton = new Button("Search Follow Song");
	    goToNextButton.setDisable(true);
	    goToNextButton.setOnAction(new EventHandler<ActionEvent>() {
	    	 public void handle(ActionEvent event) {
	    		 searchCurrentFollowSong();
	    	 }
	     });
	    
		HBox hbox1 = new HBox(addField, addButton);
		hbox1.getStyleClass().add("add_and_submit");
		
		HBox hbox2 = new HBox(appendDirectionBox, appendImageView);
		hbox2.getStyleClass().add("append_box");
		
		HBox hbox3 = new HBox(hbox2, deleteButton, goToNextButton);
		hbox3.getStyleClass().add("add_menu_options");
		
		VBox entireAddBar = new VBox(addLabel, hbox1, hbox3);
		return entireAddBar;
	}
	
	/**
	 * instantiates reinitialization, open config, and update buttons
	 */
	public VBox makeConfigBar(){
		
		reInitButton = new Button("Reinitialize Entire Library");
	    reInitButton.setOnAction(new EventHandler<ActionEvent>() {
	    	 public void handle(ActionEvent event) {
	    		 showAlertWindow(ALERT_REINIT_HEADER, ALERT_REINIT_CONFIRMATION);
	    	 }
	     });
		
		updateButton = new Button("Update Library");
		updateButton.setOnAction(new EventHandler<ActionEvent>() {
	    	 public void handle(ActionEvent event) {
	    		 showAlertWindow(ALERT_UPDATE_HEADER, ALERT_UPDATE_CONFIRMATION);
	    	 }
	     });
		
		openConfigButton = new Button("Open Config File Location");
		openConfigButton.setOnAction(new EventHandler<ActionEvent>() {
	    	 public void handle(ActionEvent event) {
	    		 try {
					Desktop.getDesktop().open(new File(".")); //TODO instructions for editting config
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	 }
	     });
		Label configLabel = new Label("Options");
		configLabel.getStyleClass().add("label_config");
		
		HBox configMenu = new HBox(reInitButton, updateButton, openConfigButton);
		configMenu.getStyleClass().add("config_menu");
		configMenu.setAlignment(Pos.CENTER);
		
		VBox entireConfigMenu = new VBox(configLabel, configMenu);	
		
		return entireConfigMenu;
	}//makeConfigBar()
	
	/**
	 * shows an alert confirmation window
	 */
	public void showAlertWindow(String header, String contentText){
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText(header);
		alert.setContentText(contentText);
		

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			
			if(header.equals(ALERT_REINIT_HEADER)){
				util.initializeSongList(util.readPropertiesFile(Utility.MUSIC_FOLDER_KEY), util.readPropertiesFile(Utility.XML_FOLDER_KEY));
				Platform.exit();   
	   		 	System.exit(0);	
			}else if(header.equals(ALERT_UPDATE_HEADER)){
				util.updateXmlFile(util.readPropertiesFile(Utility.MUSIC_FOLDER_KEY), util.getXmlLastModified(), util.getXmlFile());
				Platform.exit();   
	   		 	System.exit(0);	
			}
			
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
		

	}

	//getters
	public VBox getleftGUI(){
		return leftGUI;
	}
	
	public VBox getRightGUI(){
		return rightGUI;
	}
	
	
}
