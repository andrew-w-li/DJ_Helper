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

public class SongEntry {
	String title;
	String artist;
	String filepath;
	
	public SongEntry(String title, String artist, String filepath){
		this.title = title;
		this.artist = artist;
		this.filepath = filepath;
	}
	
	public SongEntry(String title, String artist){
		this.title = title;
		this.artist = artist;
	}
	
	//getters
	public String getTitle(){
		return title;
	}
	
	public String getArtist(){
		return artist;
	}
	
	public String getFilePath(){
		return filepath;
	}
	
	@Override
	public String toString(){
		return title + " <!" + artist + "!>";
	}
	
	
	
	
}
