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

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class SongEntryGUI {
	
	Utility util;
	SongEntry song;
	HBox record;
	Label labelTitle, labelArtist;
	ImageView art;
	Color bgColour;
	public static int row_color;
	boolean pressed = false;
	
	public SongEntryGUI(SongEntry song, Utility util){
		this.song = song;
		this.util = util;
		
        Label labelTitle = new Label(song.getTitle());
        labelTitle.getStyleClass().add("title_label");
        
        Label labelArtist = new Label(song.getArtist());
        labelArtist.getStyleClass().add("artist_label");
        
        //read the image art (if possible) and display it/placeholder image in ImageView
        BufferedImage img = util.getAlbumArt(song.getFilePath());
        
        if(img != null){
            WritableImage wr = null; 		
            art = new ImageView(SwingFXUtils.toFXImage(img, wr));
        }else{
            art = new ImageView(VBoxListView.placeholder);
        }
		

        //record = new HBox(vbox1);
        
        VBox vbox1 = new VBox(labelTitle, labelArtist);
        record = new HBox(art, vbox1);
        record.getStyleClass().add("v_list_item");
        
        //event handler
		record.setOnMouseClicked(e -> {
			if(!VBoxListView.isEmpty()){
				
				VBoxListView.resetAllBackgrounds();
				pressed = true; //TODO does this even do anything
				record.getStyleClass().clear();
				record.getStyleClass().add("v_list_item_selected");
				LibraryGUI.refreshFollowListView(song.getTitle());
				VBoxListView.selected = song.getTitle();
			
			}else{
				//do nothing
			}

		});
	}
	
	public HBox getRecordGUI(){
		return record;
	}
	
	public SongEntry getSong(){
		return song;
	}
	
	public boolean isPressed(){
		return pressed;
	}
	
	public void setPressedStatus(boolean b){
		pressed = b;
	}
}
