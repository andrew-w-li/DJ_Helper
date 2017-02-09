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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application{
	
	Utility util;
	Stage primeStage;
	Scene sceneDefault;
	HBox hBoxAll;
	public static LibraryGUI libraryGui;


	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		util = new Utility();
		//first check if config file exists (suggests initialization has been done)
		if(util.configExists()){
			libraryGui = new LibraryGUI(util);
			
			hBoxAll = new HBox(libraryGui.getleftGUI(), libraryGui.getRightGUI());
			hBoxAll.getStyleClass().add("primary_box");
			
			//setting the default scene
			sceneDefault = new Scene(hBoxAll, 1024, 768);
			sceneDefault.getStylesheets().add("styles.css");
			
			primeStage = primaryStage;
			primeStage.getIcons().add(new Image("icon.png"));
			primeStage.setTitle("DJ Helper");
			primeStage.setScene(sceneDefault);
			primeStage.show();
		}else{
			//first time setup
			SetupGUI setupGui = new SetupGUI(util);
			
			hBoxAll = new HBox(setupGui.getSetupGui());
			
			//setting the default scene
			sceneDefault = new Scene(hBoxAll, 450, 300);
			sceneDefault.getStylesheets().add("styles.css");
			//sceneDefault.getStylesheets().add()
			primeStage = primaryStage;
			primeStage.getIcons().add(new Image("icon.png"));
			primeStage.setTitle("DJ Helper");
			primeStage.setScene(sceneDefault);
			primeStage.show();
		}
	}//start

}//class
