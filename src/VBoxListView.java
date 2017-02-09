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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VBoxListView {

	ObservableList<SongEntry> songList;
	static ObservableList<SongEntryGUI> songGuiList;
	public static VBox vList, vListViewAndButtons;
	Utility util;
	Button forward, backward;
	ScrollPane scrollpane;
	Label resultsLabel, pageLabel;
	static ObservableList<Node> obsListGUI;

	public static String selected = null;
	public static Image placeholder;
	
	private int currentCount;
	private int NUM_PER_PAGE = 6;
	private int currentPage;
	private int currentMax;
	private int total;
	private int totalPages;
	
	public VBoxListView(Utility util){
		this.songList = FXCollections.observableArrayList();
		this.util = util;
		placeholder = new Image("/empty_album_art.png", 75, 75, false, false);
		obsListGUI = FXCollections.observableArrayList();
		songGuiList = FXCollections.observableArrayList();
		
		//labels
		resultsLabel = new Label("Search to begin");
		pageLabel = new Label("- - - - - - -");
		
		//buttons
		forward = new Button(">");
		backward = new Button("<");
		initButtonHandlers();
		
		//listview
		vList = new VBox();
		vList.getStyleClass().add("v_list_view");
		scrollpane = new ScrollPane(vList);
		scrollpane.setOnKeyPressed(new EventHandler<KeyEvent>(){
	        @Override
	        public void handle(KeyEvent ke){
	            if 		(ke.getCode().equals(KeyCode.D) || ke.getCode().equals(KeyCode.RIGHT))	{pushForwardButton();}
	            else if(ke.getCode().equals(KeyCode.A) || ke.getCode().equals(KeyCode.LEFT))	{pushBackButton();}
	            else if(ke.getCode().equals(KeyCode.W) || ke.getCode().equals(KeyCode.UP))		{selectUpOrDown("UP");}
	            else if(ke.getCode().equals(KeyCode.S) || ke.getCode().equals(KeyCode.DOWN))	{selectUpOrDown("DOWN");}
	        }
	    });
		scrollpane.getStyleClass().add("v_list_view_scrollpane");
		
		//placement
		VBox vbox1 = new VBox(resultsLabel, pageLabel);
		vbox1.getStyleClass().add("page_navigation_labels");

		HBox hbox1 = new HBox(backward, vbox1, forward);
		hbox1.getStyleClass().add("page_navigation_menu");
		
		vListViewAndButtons = new VBox(hbox1, scrollpane);
		vListViewAndButtons.setSpacing(5);
	}
	
	/**
	 * event handlers for forward and backward buttons
	 */
	public void initButtonHandlers(){
		//forward button
		forward.setOnAction(new EventHandler<ActionEvent>() {
	    	 public void handle(ActionEvent event) {
	    		 pushForwardButton();
	    	 }
	     });
		//backward button
		backward.getStyleClass().add("backward_button");
		backward.setOnAction(new EventHandler<ActionEvent>() {
	    	 public void handle(ActionEvent event) {
	    		 pushBackButton();
	    	 }
	     });
	}
	
	/**
	 * go forward a page
	 */
	public void pushForwardButton(){
		 if(songList.size() > 0){
    		 updateNumbers(NUM_PER_PAGE, 1);
    		 vList.getChildren().clear();
    		 loadVBoxListGUI();
		 }else{
			 System.out.println("songlist is empty");
		 }
	}
	
	/**
	 * go back a page
	 */
	public void pushBackButton(){
		 if(songList.size() > 0){
    		 updateNumbers(-NUM_PER_PAGE, -1);
    		 vList.getChildren().clear();
    		 loadVBoxListGUI();	 
		 }else{
			 System.out.println("songlist is empty");
		 }
	}
	
	/**
	 * sets the selected song to a specific index (index should be previously known before using this method)
	 */
	public static void setSelectedSong(int index){
		if(!songGuiList.isEmpty()){
			resetAllBackgrounds();
			selected = songGuiList.get(index).getSong().getTitle();
			songGuiList.get(index).pressed = true;
			obsListGUI.get(index).getStyleClass().clear();
			obsListGUI.get(index).getStyleClass().add("v_list_item_selected");
			LibraryGUI.refreshFollowListView(selected);
		}else{
			selected = null;
			LibraryGUI.refreshFollowListView("");
		}
	}//setDefaultSelectedSong
	
	/**
	 * keyboard event handler for pushing up or down
	 */
	public void selectUpOrDown(String direction){ //TODO prob can keep track of which is pressed when it is pressed
		int pressedIndex = 0;
		if(!songGuiList.isEmpty()){
			for(int i = 0; i < songGuiList.size(); i++){
				if(songGuiList.get(i).pressed){
					pressedIndex = i;
					break;
				}//if
			}//for
			
			if(direction.equals("DOWN")){
				if(pressedIndex + 1 < songGuiList.size()){
					pressedIndex++;
					setSelectedSong(pressedIndex);
				}
			}else if(direction.equals("UP")){
				if(pressedIndex - 1 >= 0){
					pressedIndex--;
					setSelectedSong(pressedIndex);
				}
			}
		}//if
	}
	
	/**
	 * load GUI elements for current page
	 */
	public void loadVBoxListGUI(){
		//update labels
		resultsLabel.setText("Found: " + songList.size());
		if(totalPages == 0){
			pageLabel.setText("Page: " + 0 + "/" + totalPages);
		}else{
			pageLabel.setText("Page: " + currentPage + "/" + totalPages);
		}

		obsListGUI.clear();
		songGuiList.clear();

   		for(int i = currentCount; i < currentMax; i++){
   			songGuiList.add(new SongEntryGUI(songList.get(i), util));
   		}
   		 
   		for(SongEntryGUI s : songGuiList){
   			obsListGUI.add(s.getRecordGUI());
   		}
			
		vList.getChildren().addAll(obsListGUI);
		setSelectedSong(0);
	}//loadVBoxListGUI

	/**
	 * resets all backgrounds for songs in the list for selection purposes
	 */
	public static void resetAllBackgrounds(){
		for(SongEntryGUI s : songGuiList){	
			if(s.isPressed()){
				s.setPressedStatus(false);
				s.getRecordGUI().getStyleClass().clear();
				s.getRecordGUI().getStyleClass().add("v_list_item");				
			}
		}
	}
	
	/**
	 * initialize the loop variables for the current search output
	 */
	public void initNumbers(){
		 total = songList.size();
		 currentCount = 0;
		 currentMax = (NUM_PER_PAGE > total) ?  total : NUM_PER_PAGE;
		 currentPage = 1;
		 totalPages = (int) Math.ceil((double)total/(double)NUM_PER_PAGE);
	}
	
	/**
	 * update the loop variables depending on what page the user is on
	 */
	public void updateNumbers(int countChange, int pageChange){

		//update current Page
		if((currentPage + pageChange > totalPages) || (currentPage + pageChange < 1)){
			//do nothing if it exceeds max or below min
		}else{
			currentPage += pageChange;
			
			//update current Max
			if(currentPage * NUM_PER_PAGE > total){
				currentMax = total;
			}else{
				currentMax = currentPage*NUM_PER_PAGE;
			}
			
			//update current Count
			if(currentCount + countChange > total || currentCount + countChange < 0){
				//do nothing
			}else{
				currentCount += countChange;
			}
			
		}
	}//updateNumbers

	/**
	 * refills the songlist from search and refreshes vList
	 */
	public void setSongList(ObservableList<SongEntry> newList){
		songList.setAll(newList);
		initNumbers();
		vList.getChildren().clear();
		loadVBoxListGUI();
	}
	
	public static boolean isEmpty(){
		int numChildren = vList.getChildren().size();
		
		if(numChildren > 0){
			return false;
		}else{
			return true;
		}
	}
	/**
	 * getter
	 */
	public VBox getVList(){
		return vListViewAndButtons;
	}
}//class
