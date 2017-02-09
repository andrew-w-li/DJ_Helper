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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import net.coobird.thumbnailator.Thumbnails;

public class Utility {
	
	public static final String CONFIG = "config.properties";
	public static final String MUSIC_FOLDER_KEY = "music_folder_directory";
	public static final String XML_FOLDER_KEY = "XML_save_directory";
	public static final String DEFAULT_EXTENSIONS = "valid_extensions";
	
	public static final String ROOT_TAG = "SongLibrary";
	public static final String SONG_TAG = "song";
	public static final String NAME_TAG = "name";
	public static final String ARTIST_TAG = "artist";
	public static final String FOLLOW_SONG_TAG = "follow_song";
	public static final String ATTR_TITLE = "title";
	public static final String FILE_LOCATION = "file_location";
	public static final String LAST_MODIFIED = "last_modified";
	public static boolean APPEND_BOTH_WAYS = true;
	
	/**
	 * Returns the last modified date of the xml file
	 */
	public Date getXmlLastModified(){
		Document doc = getXmlFile();
		Date date = new Date(doc.getRootElement().getAttributeValue(LAST_MODIFIED));
		return date;
	}
	
	/**
	 * sets the last modified attribute of the song library to current time
	 */
	public void setXmlLastModified(Document document){
		document.getRootElement().getAttribute(LAST_MODIFIED).setValue(new Date().toString());
	}
	
	/**
	 * returns the file's creation time
	 */
	public Date getSongCreationTime(String filepath){
	    Path f = Paths.get(filepath);
	    BasicFileAttributes basicFileAttributes = null;
	    
		try {
			basicFileAttributes = Files.readAttributes(f,BasicFileAttributes.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    Date creationTime = new Date(basicFileAttributes.creationTime().toMillis());
	    
	    return creationTime;
	}
	
	/**
	 * Creates the XML file containing all music files for the first time
	 */
	public void initializeSongList(String musicDir, String xmlPath){
			//root element and document
			Element songLibrary = new Element(ROOT_TAG);
			Document doc = new Document(songLibrary);
			songLibrary.setAttribute(new Attribute(LAST_MODIFIED, new Date().toString()));
			
			getFileNames(musicDir, doc);
			writeXml(doc);
	}//initializeSongList
	
	/**
	 * update the XML file with new song info (keeps old info)
	 * 
	 */
	public void updateXmlFile(String musicDir, Date xmlLastModified, Document document){
		File[] listOfFiles = new File(musicDir).listFiles();
		
		for(File f : listOfFiles){
			//verify its a file and its CREATION DATE is AFTER XML LAST MODIFIED DATE
			if(f.isFile() && hasValidExtension(f.getName()) && xmlLastModified.before(getSongCreationTime(f.getAbsolutePath()))){
				String artist = getSongArtist(f.getAbsolutePath());
				document.getRootElement().addContent(makeNewElement(f.getName(), f.getAbsolutePath(), artist));	
			}else if(f.isDirectory()){
				updateXmlFile(f.getPath(), xmlLastModified, document); //recursion
			}//else if
		}//for
		setXmlLastModified(document);
		writeXml(document);
	}
	
	/**
	 * Traverses the music folder and stores each song name as an element
	 */
	public 	void getFileNames(String musicDir, Document document){
		File directory = new File(musicDir);
		File[] listOfFiles = directory.listFiles();

		for(File f : listOfFiles){
			

			if(f.isFile() && hasValidExtension(f.getName())){
				
				//error logging
				try {
					writeOutPathLog("Last written file: " + f.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				String artist = getSongArtist(f.getAbsolutePath());
				document.getRootElement().addContent(makeNewElement(f.getName(), f.getAbsolutePath(), artist)); //TODO get name via metadata	
				
			}else if(f.isDirectory()){
				getFileNames(f.getPath(), document); //recursion
			}//else
		}//for
	}//getFileNames
	
	/**
	 * simple error log, prints out the location of the last read file
	 */
	public void writeOutPathLog(String lastFilename) throws IOException{
		
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(
						new File("path_log.txt")), "UTF8"));
		//string to write out
		out.write(lastFilename);
		
		//close writer
		if(out!=null){
			out.flush();
			out.close();
		}
	}
	
	/**
	 * returns the song's artist info, checks the metatag version first
	 */
	public String getSongArtist(String filepath){
		String artist = "";

		if(filepath.toLowerCase().endsWith(".mp3")){
			File file = new File(filepath);
			try {
				MP3File mp3File = (MP3File)AudioFileIO.read(file);
				if(mp3File != null){
					if(mp3File.hasID3v1Tag()) {artist = mp3File.getID3v1Tag().getArtist().toString();}
					if(mp3File.hasID3v2Tag()) {artist = mp3File.getID3v2Tag().getFirst(ID3v24Frames.FRAME_ID_ARTIST);}
					else{ }
				}else{
					//do nothing
				}
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				e.printStackTrace();
			}

		}else if(filepath.toLowerCase().endsWith(".m4a")) {
			File file = new File(filepath);
			try {
				AudioFile f  = AudioFileIO.read(file);
				if(f != null){
					Tag tag = f.getTag();
					
					if(tag != null){
						artist = tag.getFirst(FieldKey.ARTIST);
					}else{
						//do nothing
					}
					
				}else{
					//do nothing
				}
				
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				e.printStackTrace();
			}
		}
		
		return artist;
	}//getSongArtist
	
	/**
	 * returns album art as buffered image
	 *TODO update with Jaudiotagger library
	 */
	public BufferedImage getAlbumArt(String filepath){
		BufferedImage img = null;
		
		if(filepath.toLowerCase().endsWith(".mp3")){
			
			try {
				Mp3File mp3file = new Mp3File(filepath);
				
				if(mp3file.hasId3v2Tag()){
					byte[] imageData = mp3file.getId3v2Tag().getAlbumImage();
					if(imageData != null){
						img = ImageIO.read(new ByteArrayInputStream(imageData));
						if(img != null){
							if(Thumbnails.of(img) != null){
								img = Thumbnails.of(img).size(75, 75).asBufferedImage();
							}else{
								System.out.println("!!!_FOR: " + filepath + "[thumbnail] was NULL_!!!");
							}
						}else{
							System.out.println("!!_FOR: " + filepath + " [img] was NULL_!!");
						}
					}
				}
			} catch (UnsupportedTagException e) {
				e.printStackTrace();
			} catch (InvalidDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}else if(filepath.toLowerCase().endsWith(".m4a")) {
			File file = new File(filepath);
			try {
				AudioFile f  = AudioFileIO.read(file);
				Tag tag = f.getTag();
				
				//extract artwork if it exists
				if (tag.getFirstArtwork() != null){
					byte[] imageData = tag.getFirstArtwork().getBinaryData();
					if(imageData != null){
						img = ImageIO.read(new ByteArrayInputStream(imageData));
						if(img != null){
							if(Thumbnails.of(img) != null){
								img = Thumbnails.of(img).size(50, 50).asBufferedImage();
							}else{
								System.out.println("!!_FOR: " + filepath + " [img] was NULL_!!");
							}
						}else{
							System.out.println("!!_FOR: " + filepath + " [img] was NULL_!!");
						}
					}
				}
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				e.printStackTrace();
			}
		}
		
		return img;
	}//getAlbumArt
	
	/**
	 * makes a song element for the XML file
	 */
	public Element makeNewElement(String name, String fileLocation, String artist){
		Element newSong = new Element(SONG_TAG);
		newSong.setAttribute(new Attribute(ATTR_TITLE, name));
		newSong.addContent(new Element(FILE_LOCATION).setText(fileLocation));
		newSong.addContent(new Element(ARTIST_TAG).setText(artist));
		return newSong;
	}//makeNewElement
	
	/**
	 * parse the XML file for info
	 * TODO increase this for further metatag data
	 * TODO need not be reliant on songList
	 */
	public String[] getInfoFromXml(List<Element> songList, String songName){
		String[] info = new String[2];
		
		for(Element e : songList){///
			if(e.getAttributeValue(ATTR_TITLE).equals(songName)){
				info[0] = e.getChildText(ARTIST_TAG);
				break;
			}
		}
		
		return info;
	}
	
	/**
	 * appends a follow song element to a Song element in the XML file
	 */
	public void appendFollowSong(String songTitle, String followSongTitle){
	
			Document doc = getXmlFile();
			List<Element> songList = doc.getRootElement().getChildren("song");
			int offSwitch = 0;
			for(Element e : songList){
				
				//adds a <followsong> tag to the desired song, as well as in reverse if checkbox is checked
				if(offSwitch < 2){
					if(e.getAttributeValue(ATTR_TITLE).equals(songTitle)){
						Element newFollowSong1 = new Element(FOLLOW_SONG_TAG);
						newFollowSong1.setAttribute(new Attribute(ATTR_TITLE, followSongTitle));
						newFollowSong1.addContent(new Element(ARTIST_TAG)
								.setText(getInfoFromXml(songList, followSongTitle)[0]));
						e.addContent(newFollowSong1);
						offSwitch++;
					}
					if(APPEND_BOTH_WAYS){
						if(e.getAttributeValue(ATTR_TITLE).equals(followSongTitle)){
							Element newFollowSong2 = new Element(FOLLOW_SONG_TAG);
							newFollowSong2.setAttribute(new Attribute(ATTR_TITLE, songTitle));
							newFollowSong2.addContent(new Element(ARTIST_TAG)
									.setText(getInfoFromXml(songList, songTitle)[0]));
							e.addContent(newFollowSong2);
							offSwitch++;
						}
					}//if append both ways
				}else{
					break;
					//nothing to do if song not found
				}
			}//for
			writeXml(doc);
	}//appendFollowSong
	
	/**
	 * Gets song information from the xml file
	 */
	public ObservableList<SongEntry> readXmlSongs(String regex){
		String regex_search = regex;
		ObservableList<SongEntry> songsFromXml = FXCollections.observableArrayList();
		
		Document doc = getXmlFile();
		List<Element> songList = doc.getRootElement().getChildren("song");
		/*
		if(regex_search.isEmpty()){
			
			for(int i = 0; i < songList.size(); i++){
				Element e1 = songList.get(i);
				String title = e1.getAttributeValue(ATTR_TITLE);
				String artist = e1.getChildText(ARTIST_TAG);
				String filepath = e1.getChildText(FILE_LOCATION);
				songsFromXml.add(new SongEntry(title, artist, filepath));
			}//for
		}else{
	*/		
				for(int j = 0; j < songList.size(); j++){
					Element e2 = songList.get(j);
					String titleFromXml = e2.getAttributeValue(ATTR_TITLE);
					String artistFromXml = e2.getChildText(ARTIST_TAG);
					if(checkRegex(titleFromXml, regex_search) || checkRegex(artistFromXml, regex_search)){
						String filepath = e2.getChildText(FILE_LOCATION);
						songsFromXml.add(new SongEntry(titleFromXml, artistFromXml, filepath));
					}else{
						//nothing to do if regex doesn't work
					}//else
				}//for
		//}//else
		return songsFromXml;
	}//readXmlSongs
	
	/**
	 * gets follow song information for a given song
	 */
	public ObservableList<SongEntry> readXmlFollowSongs(String selectedSong){
		ObservableList<SongEntry> followSongsFromXml = FXCollections.observableArrayList();
		Document doc = getXmlFile();
		List<Element> songList = doc.getRootElement().getChildren("song");
		
		for(Element e : songList){
			if(e.getAttributeValue(ATTR_TITLE).equals(selectedSong)){
				List<Element> followSongList = e.getChildren(FOLLOW_SONG_TAG);
				
				for(Element fe : followSongList){
					String title = fe.getAttributeValue(ATTR_TITLE);
					String artist = fe.getChildText(ARTIST_TAG);
					followSongsFromXml.add(new SongEntry(title, artist));
				}
				
				break;
			}
		}
		
		return followSongsFromXml;
	}
	
	/**
	 * deletes a follow song from the XML file 
	 */
	public void deleteFollowSong(String songTitle, String fSongToDelete){
		
		Document doc = getXmlFile();
		List<Element> songList = doc.getRootElement().getChildren("song");
		int offSwitch = 0;
		for(Element e : songList){
			
			//need to optimize this
			//adds a <followsong> tag to the desired song, as well as in reverse
			if(offSwitch < 2){
				
				//delete followsong from initial song
				if(e.getAttributeValue(ATTR_TITLE).equals(songTitle)){
					Element toBeDeleted = null;
					List<Element> followList = e.getChildren(FOLLOW_SONG_TAG);
	
					for (Element fe : followList){
						if(fe.getAttributeValue(ATTR_TITLE).equals(fSongToDelete)){
							toBeDeleted = fe;
							break;
						}
					}//for
					
					if(toBeDeleted != null){
						e.removeContent(toBeDeleted);
					}
					offSwitch++;
				}//if
				
				//delete initial song from followsong
				if(APPEND_BOTH_WAYS){
					if(e.getAttributeValue(ATTR_TITLE).equals(fSongToDelete)){
						Element toBeDeletedReverse = null;
						List<Element> followListReverse = e.getChildren(FOLLOW_SONG_TAG);
		
						for (Element fe : followListReverse){
							if(fe.getAttributeValue(ATTR_TITLE).equals(songTitle)){
								toBeDeletedReverse = fe;
								break;
							}
						}//for
						
						if(toBeDeletedReverse != null){
							e.removeContent(toBeDeletedReverse);
						}
						offSwitch++;
					} //if title = title
				}//if append both ways
			}else{
				break;
			}
		}//for
		writeXml(doc);
		
	}//deleteFollowSong
	
	/**
	 * Writes out XML file
	 */
	public void writeXml(Document doc){
		try {
			XMLOutputter xmlOutput = new XMLOutputter();

			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			OutputStreamWriter fstream = new OutputStreamWriter(
					new FileOutputStream(readPropertiesFile(XML_FOLDER_KEY)), StandardCharsets.UTF_8);
			xmlOutput.output(doc, fstream);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * returns the xml file
	 */
	public Document getXmlFile(){
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(readPropertiesFile(XML_FOLDER_KEY));
		Document doc = null;
		
		try {
			doc = (Document) builder.build(xmlFile);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return doc;
	}//getXmlFile
	
	/**
	 * does regex to split the addField TITLE and <!ARTIST!>
	 */
	public String doRegex(String line, String pattern){
		String match = "";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(line);
		
		if(m.find()){
			match = m.group(1);
		}else{
		}
		return match;
	}
	/**
	 * does regex for the searchField, compares what the user has typed with the entire xml
	 */
	public boolean checkRegex(String line, String pattern){
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
		
		Matcher m = p.matcher(line);
		
		if(m.find()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * create the properties file, ONLY TO BE USED IF IT DOESN'T ALREADY EXIST
	 */
	public void writePropertiesFile(String musicDir, String extensions){
		Properties prop = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream(CONFIG);

			// set the properties value
			prop.setProperty(MUSIC_FOLDER_KEY, musicDir); //to be filled in by user
			prop.setProperty(DEFAULT_EXTENSIONS, extensions);
			prop.setProperty(XML_FOLDER_KEY, "songlib.xml");

			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}//writePropertiesFile
	
	/**
	 * read properties file for file location info
	 */
	public String readPropertiesFile(String key){
		Properties prop = new Properties();
		InputStream input = null;
		String output= "";
		
		try {
			input = new FileInputStream(CONFIG);

			// load a properties file
			prop.load(input);
			output = prop.getProperty(key);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return output;
	}//readPropertiesFile
	
	/**
	 * checks if the properties file exists where expected
	 */
	public boolean propertiesFileExists(){
		File file = new File(CONFIG);
		
		if(file.exists()){
			return true;
		}else{
			return false;
		}
	}//propertiesFileExists
	
	/**
	 * sets progress bar fraction
	 */
	public void updateProgressBar(ProgressBar progress, double fraction){
		progress.setProgress(fraction);
		System.out.println("progressbar: " + progress.getProgress() + " fraction: " + fraction);
	}

	/**
	 * cross checks file extension with valid_extensions from config file
	 */
	public boolean hasValidExtension(String filename){
		Boolean verdict = false;
		String validExtensions = readPropertiesFile(DEFAULT_EXTENSIONS);
		String[] patterns = validExtensions.split(",");
		
		for(String s : patterns){
			if(checkRegex(filename, s)){
				verdict = true;
				break;
			}else{
				verdict = false;
			}
		}
		return verdict;
	}
	
	/**
	 * checks to see if the config.properties and songlib.xml file exist
	 */
	public boolean configExists(){
		File saveFile = new File("songlib.xml");
		File configFile = new File("config.properties");
		
		if(saveFile.exists() && configFile.exists()){
			return true;
		}else{
			return false;
		}
	}

}
