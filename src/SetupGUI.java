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

import java.io.File;
import java.util.Optional;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SetupGUI {

	Utility util;
	VBox entireSetupGui;
	TextField musicDirPath, validExtensions;
	Button submit;
	Label setupHeader, musicDirLabel, validExtensionsLabel, goAheadLabel;
	ProgressBar progress;
	public static boolean initFinished = false;
	public final String HEADER_LABEL = "First time initialization.";
	public final String MUSIC_LABEL = "Enter your music folder's full filepath: ";
	public final String EXTENSIONS_LABEL = "Valid Extensions: ";
	public final String ERROR_HEADER_NO_EXIST = "Location does not exist";
	public final String ERROR_MESSAGE_NO_EXIST = "Could not find the specified directory, try typing it out.";
	public final String ERROR_HEADER_NOT_DIRECTORY = "Location is not a directory";
	public final String ERROR_MESSAGE_NOT_DIRECTORY = "Make sure the location is a folder, not a file";
	public final String CONFIRMATION_HEADER = "Okay to initialize?";
	public final String CONFIRMATION_MESSAGE = "DJ Helper will now read your music folder and save song information.\n"
			+ "This can take several minutes or longer depending on the size of your library.\n"
			+ "This program will close upon save completion, re-open it afterward.\n\n"
			+ "Continue?";
	public final String GO_AHEAD = "Initalizing...";
	
	public SetupGUI(Utility util){
		this.util = util;
		
		setupHeader = new Label(HEADER_LABEL);
		setupHeader.getStyleClass().add("setup_label");
		
		musicDirLabel = new Label(MUSIC_LABEL);
		musicDirPath = new TextField();
		musicDirPath.getStyleClass().add("textfield_bar_setup");
		
		validExtensionsLabel = new Label(EXTENSIONS_LABEL);
		validExtensions = new TextField();
		validExtensions.setText(".mp3,.m4a,.wav");
		validExtensions.getStyleClass().add("textfield_bar_setup");
		
		submit = new Button("Submit");
		initSubmitButton();
		
		HBox musicHBox = new HBox(musicDirLabel, musicDirPath);
		musicHBox.getStyleClass().add("general_bar_menu");
		
		HBox extensionsHBox = new HBox(validExtensionsLabel, validExtensions);
		extensionsHBox.getStyleClass().add("general_bar_menu");
		
		entireSetupGui = new VBox(setupHeader, musicHBox, extensionsHBox, submit);
		entireSetupGui.getStyleClass().add("general_menu_with_label_setup");
	}
	
	/**
	 * sets up event handler for the submit button
	 */
	private void initSubmitButton(){
	    submit.setOnAction(new EventHandler<ActionEvent>() {
	  	  
	    	 public void handle(ActionEvent event) {
	    		String musicDir = musicDirPath.getText().trim();
	    		String extensions = validExtensions.getText().trim();
	    		
	    		if(musicDir.isEmpty() || extensions.isEmpty()){
	    			showInvalidInputAlert();
	    		}else{
	    			showConfirmationAlert(musicDir, extensions);
	    		}
	    		
	    	 }
	     });
	}
	
	private void showInvalidInputAlert(){
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText("Invalid Input");
		alert.setContentText("Both fields must be filled out");
		alert.showAndWait();
	}
	
	private void showConfirmationAlert(String dir, String ext){
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText(CONFIRMATION_HEADER);
		alert.setContentText(CONFIRMATION_MESSAGE);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			File directory = new File(dir);
			
			if(directory.exists()){

				if(directory.isDirectory()){
					//exists and is a directory, everything OK
					
					//disable the GUI and add a small progress bar
					addProgressVisual();
					
					//put initialization in another thread so that application doesn't freeze
			        Task longTask = new Task<Void>() {
			            @Override
			            protected Void call() throws Exception {
			            	util.writePropertiesFile(directory.getAbsolutePath(), ext);
			                util.initializeSongList(util.readPropertiesFile(Utility.MUSIC_FOLDER_KEY), util.readPropertiesFile(Utility.XML_FOLDER_KEY));
			                return null;
			            }
			        };
			        longTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			            @Override
			            public void handle(WorkerStateEvent t) {
			            	progressFinished();
			            }
			        });
			        new Thread(longTask).start();
					
				}else{
					//exists but not a directory
					showInvalidDirectoryError(ERROR_HEADER_NOT_DIRECTORY, ERROR_MESSAGE_NOT_DIRECTORY);
				}
			}else{
				//doesn't exist
				showInvalidDirectoryError(ERROR_HEADER_NO_EXIST, ERROR_MESSAGE_NO_EXIST);
			}
		} else {
			//if they click cancel, do nothing
		}
	}
	
	public void addProgressVisual(){		
		musicDirPath.setDisable(true);
		validExtensions.setDisable(true);
		submit.setDisable(true);
		musicDirLabel.getStyleClass().add("disabled_text");
		validExtensionsLabel.getStyleClass().add("disabled_text");
		setupHeader.getStyleClass().add("disabled_text");
		progress = new ProgressBar();
		goAheadLabel = new Label(GO_AHEAD);
		entireSetupGui.getChildren().addAll(goAheadLabel, progress);
	}
	public void progressFinished(){
		progress.setProgress(1);
		progress.setDisable(true);
		goAheadLabel.getStyleClass().add("disabled_text");
		showSuccessWindow();
    	Platform.exit();   
	   	System.exit(0);	
	}
	
	private void showInvalidDirectoryError(String header, String message){
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	private void showSuccessWindow(){
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Initialization Complete");
		alert.setHeaderText("Initialization Complete");
		alert.setContentText("Initialization successfully completed, please re-open this application to begin.");
		alert.showAndWait();
	}
	
	//getters
	public VBox getSetupGui(){
		return entireSetupGui;
	}
	
}
