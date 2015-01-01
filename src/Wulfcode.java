
import javax.sound.midi.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.util.Properties;


import com.sun.awt.AWTUtilities;

import themidibus.*;



public class Wulfcode {


	/**
	 *	Wulfcode v0.2
	 * 
	 */

	String infostring;
	public Sequence portaseq;
	byte[] mididata;
	Properties props;
	boolean close = false;
	boolean finished = false;
	boolean altpressed = false;
	boolean ctrlpressed = false;
	boolean shiftpressed = false;
	JTextArea textarea = new JTextArea();
	JTextArea textoutput = new JTextArea();
	JFrame jframe = new JFrame();
	JFrame jframe2 = new JFrame();
	UndoManager undoManager = new UndoManager();
	MidiBus myMidi;
	int clockcount = 0;
	int randnote = 0;
	public NoteMachine noter;
	public CCMachine ccer;
	public ArrayList<NoteMachine> machinelist;
	public ArrayList<CCMachine> cclist;
	public ArrayList<Chord> chordlist;
	public ArrayList<Velo> velocs;
	public int initialroot = 60;
	public int beatmarker = 1;
	public int lastselectedstart = 0;
	public int lastselectedend = 0;
	String[] beatnumbers = {"ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN", "TWELVE"};
	public String inputbgcol = "0x080808";
	public String inputtextcol = "0xc6a8c1";
	public String outputbgcol = "0xcaa5c1";
	public String outputtextcol = "0x080808";
	public int inputtextsize = 18;
	public int outputtextsize = 15;
	public int initialopacity = 80;
	public int inputwidth = 640;
	public int outputwidth = 640;
	public int inputheight = 800;
	public int outputheight = 800;
	public String windoworientation = "horizontal";
	public int windoworder = 1;
	public int startposx = 0;
	public int startposy = 0;
	public int midiin = -1; 
	public int midiout = -1;


	boolean locationset = false;


	int activenote = initialroot;

	public static void main(String args[]) {

		Wulfcode wulf = new Wulfcode();
		try {
			wulf.setup();
			wulf.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	public void setup() {
		
		try {
			props = new Properties();
			props.load(new FileInputStream(System.getProperty("user.home") + "/wulfcode_config.txt"));
		} catch (IOException e1) {
			System.out.println("Couldn't find a config file in user's home directory");
		}
		if (props != null) {
			if(props.containsKey("midiin")) {midiin = Integer.parseInt(props.getProperty("midiindevice", "./"));	}
			if(props.containsKey("midiout")) {midiout = Integer.parseInt(props.getProperty("midioutdevice", "./")); 	}
			if(props.containsKey("initialopacity")) {initialopacity = Integer.parseInt(props.getProperty("initialopacity", "./"));	}
			if(props.containsKey("windoworder")) {windoworder = Integer.parseInt(props.getProperty("windoworder", "./"));	}
			if(props.containsKey("windoworientation")) {windoworientation = props.getProperty("windoworientation", "./");	}
			if(props.containsKey("startposx")) {startposx = Integer.parseInt(props.getProperty("startposx", "./"));	}
			if(props.containsKey("startposy")) {startposy = Integer.parseInt(props.getProperty("startposy", "./"));	}
			if(props.containsKey("outputbgcol")) {outputbgcol = "0x" + props.getProperty("outputbgcol", "./");			}
			if(props.containsKey("outputtextcol")) {outputtextcol = "0x" + props.getProperty("outputtextcol", "./");	}
			if(props.containsKey("outputtextsize")) {outputtextsize = Integer.parseInt(props.getProperty("outputtextsize", "./"));	}
			if(props.containsKey("outputwidth")) {outputwidth = Integer.parseInt(props.getProperty("outputwidth", "./"));	}
			if(props.containsKey("outputheight")) {outputheight = Integer.parseInt(props.getProperty("outputheight", "./"));	}
			if(props.containsKey("inputbgcol")) {inputbgcol = "0x" + props.getProperty("inputbgcol", "./");	}
			if(props.containsKey("inputtextcol")) {inputtextcol = "0x" + props.getProperty("inputtextcol", "./");	}
			if(props.containsKey("inputtextsize")) {inputtextsize = Integer.parseInt(props.getProperty("inputtextsize", "./"));	}
			if(props.containsKey("inputwidth")) {inputwidth = Integer.parseInt(props.getProperty("inputwidth", "./"));	}
			if(props.containsKey("inputheight")) {inputheight = Integer.parseInt(props.getProperty("inputheight", "./"));	}
		}

		
		prepareExitHandler();
		machinelist = new ArrayList<NoteMachine>();
		cclist = new ArrayList<CCMachine>();
		chordlist = new ArrayList<Chord>();
		velocs = new ArrayList<Velo>();

		textarea.setBackground(Color.decode(inputbgcol));
		textarea.setForeground(Color.decode(inputtextcol)); 
		textarea.setFont(new Font("Courier", Font.BOLD, inputtextsize));
		textarea.setSelectionColor(Color.decode("0x24ab94"));
		textarea.setSize(inputwidth, inputheight);
		textarea.setBounds(inputwidth, 0, inputwidth, inputheight);
		textarea.setCaretColor(Color.decode(inputtextcol));
		textarea.setMargin(new Insets(4, 4, 4, 4));
		textarea.getDocument().addUndoableEditListener(
				new UndoableEditListener() {
					public void undoableEditHappened(UndoableEditEvent e) {
						undoManager.addEdit(e.getEdit());						
					}
				}
				);

		textoutput.setBackground(Color.decode(outputbgcol));
		textoutput.setForeground(Color.decode(outputtextcol));
		textoutput.setFont(new Font("Courier", Font.PLAIN, outputtextsize));
		textoutput.setSelectionColor(Color.decode("0x24ab94"));
		textoutput.setSize(outputwidth, outputheight);
		textoutput.setBounds(0, 0, outputwidth, outputheight);
		textoutput.setCaretColor(Color.decode(outputtextcol));
		textoutput.setMargin(new Insets(4, 4, 4, 4));

		//JScrollPane scroll = new JScrollPane(textoutput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//scroll.setBounds(640, 0, 1280, 800);
		//scroll.setSize(640, 800);		
		//jframe2.add(scroll);
		jframe.add(textarea);
		jframe2.add(textoutput);
		jframe.setSize(inputwidth, inputheight);
		jframe2.setSize(outputwidth, outputheight);
		//jframe.setBackground(Color.decode("0x080808"));
		//jframe.setBounds(0, 0, 1280, 800);
		if(windoworientation.equals("horizontal")) {
			if(windoworder < 2) {
				jframe.setBounds(startposx, startposy, inputwidth, inputheight);
				jframe2.setBounds(startposx+inputwidth, startposy, outputwidth, outputheight);
			} else {
				jframe.setBounds(startposx+outputwidth, startposy, inputwidth, inputheight);
				jframe2.setBounds(startposx, startposy, outputwidth, outputheight);			
			}
		} else if(windoworientation.equals("vertical")) {
			if(windoworder < 2) {
				jframe.setBounds(startposx, startposy, inputwidth, inputheight);
				jframe2.setBounds(startposx, startposy+inputheight, outputwidth, outputheight);
			} else {
				jframe.setBounds(startposx, startposy+outputheight, inputwidth, inputheight);
				jframe2.setBounds(startposx, startposy, outputwidth, outputheight);			
			}
		}
		//jframe.setUndecorated(true); //- removes OS border, but means you can't move the window around! Only use if defining position		
		jframe.setVisible(true);	
		jframe2.setVisible(true);
		jframe2.toFront();
		jframe.toFront(); // Give text entry the focus
		float opac = initialopacity * 0.01f;
		AWTUtilities.setWindowOpacity(jframe, opac);
		AWTUtilities.setWindowOpacity(jframe2, opac);
		//p.frame.toBack(); // Give text entry the focus
		jframe.setTitle("Wulfcode Input");
		jframe2.setTitle("Wulfcode Output");

		textoutput.setEditable(false);
		textoutput.setCaretPosition(textoutput.getDocument().getLength());




		textoutput.setText("Wulfcode v0.2 by Brendan Ratliff\n\n");		
		MidiBus.list();		
		String[] list_ins = MidiBus.availableInputs();
		String[] list_outs = MidiBus.availableOutputs();		


		String inname = "";
		String outname = "";
		
		// This is all very Mac-specific...favours IAC virtual MIDI bus interfaces. Needs sorted for cross-platform.
		if(midiin < 0) {
			for (int i=0; i < list_ins.length; i++) {
				//textoutput.setText(textoutput.getText() + "\nInput["+i+"]: " + list_ins[i]);
				textoutput.append("\nInput["+i+"]: " + list_ins[i]);
				textoutput.setCaretPosition(textoutput.getText().length()-1);
				if(list_ins[i].toLowerCase().contains("iac bus 1") &! list_ins[i].toLowerCase().contains("driver")) {
					midiin = i;
					inname = list_ins[i];
				}
			}
		}
		if(midiout < 0) {
			for (int i=0; i < list_outs.length; i++) {
				//textoutput.setText(textoutput.getText() + "\nOutput["+i+"]: " + list_outs[i]);
				textoutput.append("\nOutput["+i+"]: " + list_outs[i]);
				textoutput.setCaretPosition(textoutput.getText().length()-1);
				if(list_outs[i].toLowerCase().contains("iac bus 2") &! list_outs[i].toLowerCase().contains("driver")) {
					midiout = i;
					outname = list_outs[i];
				}
			}
		}
		myMidi = new MidiBus(this, midiin, midiout);
		myMidi.sendTimestamps(false); // Does this help or hinder? Try to replicate Java midi->IAC bug...
		System.out.println("In: " + inname + "   Out: " + outname);

		textarea.addKeyListener(new KeyListener () {

			public void keyPressed(java.awt.event.KeyEvent event) {
				if(event.getKeyCode() == 18) {
					altpressed = true;				
				}
				if(event.getKeyCode() == 38) {
					if(altpressed) {
						//cmdParse(textarea.getSelectedText());

					}
				}

				if(event.getKeyCode() == 157) {
					ctrlpressed = true;
				}

				if(event.getKeyCode() == 16) {
					shiftpressed = true;
				}

				if(event.getKeyCode() == 50) {
					if(ctrlpressed) {

					}
				}


				if(event.getKeyCode() == 90) {
					if(ctrlpressed) {
						if(shiftpressed) {
							try {
								undoManager.redo();
							} catch (CannotRedoException e) {
								System.out.println("Nothing to redo");
							}
						} else {
							try {
								undoManager.undo();
							} catch (CannotUndoException e) {
								System.out.println("Nothing to undo");
							}
						}
					}
				}

				if(event.getKeyCode() == 49 ) {
					if(ctrlpressed) {
						String input = textarea.getSelectedText();
						lastselectedstart = textarea.getSelectionStart();
						lastselectedend = textarea.getSelectionEnd();
						if(input.contains(";")) {
							String[] commands = input.split(";");
							for (int i=0; i < commands.length; i++ ) {
								//println(commands[i]);
								commands[i] = commands[i].trim();
								cmdParse(commands[i]);
							}
						} else if (input.contains("\n")){
							String[] commands = input.split("\n");
							for (int i=0; i < commands.length; i++) {
								commands[i] = commands[i].trim();
								cmdParse(commands[i]);
							}
						} else if (input.contains("\r")){
							String[] commands = input.split("\r");
							for (int i=0; i < commands.length; i++) {
								commands[i] = commands[i].trim();
								cmdParse(commands[i]);
							}
						} else {
							cmdParse(textarea.getSelectedText());
						}
					}
				}


			}

			public void keyReleased(java.awt.event.KeyEvent event) {
				if(event.getKeyCode() == 18) {
					altpressed = false;
				}
				if(event.getKeyCode() == 157) {
					ctrlpressed = false;
				}				
				if(event.getKeyCode() == 16) {
					shiftpressed = false;
				}				

			}

			public void keyTyped(java.awt.event.KeyEvent e) {

			}
		});

	}



	public void run() {
		while(!finished){
			if(close) {				
				break;
			}				
		}				
		System.exit(0);	
	}



	public void cmdParse(String cmd) {		

		String [] parts = cmd.split(" ");
		if(parts[0].equals("chord") && parts.length > 2) {
			boolean foundexisting = false;
			for (int i=0; i<chordlist.size(); i++ ) {
				if(chordlist.get(i).name.equals(parts[1])){
					chordlist.set(i, new Chord(parts[1], parts[2].substring(1, parts[2].length()-1), Integer.parseInt(parts[3])));
					foundexisting = true;
					System.out.println("Created chord " + chordlist.get(i).name + " with notes: " + chordlist.get(i).notes[0] + ", " + chordlist.get(i).notes[1] + ", etc.");
				} 
			}
			if(!foundexisting){
				chordlist.add(new Chord(parts[1], parts[2].substring(1, parts[2].length()-1), Integer.parseInt(parts[3])));

			}							

		} else if (parts[0].substring(0,1).equals("*") && parts.length > 1) {
			// Multiply the multiplier by value
			boolean foundnoter = false;
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					foundnoter = true;
					machinelist.get(i).kill();
					machinelist.get(i).multiplier *= Integer.parseInt(parts[0].substring(1));
				}
			}			
			if(!foundnoter) {
				for (int i=0; i < cclist.size(); i++) {
					if(cclist.get(i).name.equals(parts[1])) {
						cclist.get(i).kill();
						cclist.get(i).multiplier *= Integer.parseInt(parts[0].substring(1));
					}
				}				
			}

		} else if (parts[0].substring(0,1).equals("/") && parts.length > 1) {
			boolean foundnoter = false;
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					machinelist.get(i).kill();
					if(Integer.parseInt(parts[0].substring(1)) > 0 && machinelist.get(i).multiplier > 1) { //prevent div zero
						machinelist.get(i).multiplier /= Integer.parseInt(parts[0].substring(1));
					}
				}
			}		
			if(!foundnoter) {
				for (int i=0; i < cclist.size(); i++) {
					if(cclist.get(i).name.equals(parts[1])) {
						cclist.get(i).kill();
						if(Integer.parseInt(parts[0].substring(1)) > 0 && cclist.get(i).multiplier > 1) { //prevent div zero
							cclist.get(i).multiplier /= Integer.parseInt(parts[0].substring(1));
						}
					}
				}					
			}

		} else if (parts[0].equals("hoof") && parts.length > 1) {
			if(parts.length > 2 && parts[1].equals("velo")) {
				for (int j=0; j < velocs.size(); j++) {
					if(velocs.get(j).name.equals(parts[2])) {
						velocs.get(j).shuffle();
						int cursorline = textarea.getCaretPosition();
						String updated = "";
						String[] lines = textarea.getText().split(System.getProperty("line.separator"));
						for (int t = 0; t < lines.length; t++) {							
							if(lines[t].length() > parts[1].length() && lines[t].substring(0, parts[1].length()).contains(parts[2])) {
								String chunk1 = lines[t].substring(0, lines[t].indexOf("{"));
								String chunk2 = lines[t].substring(lines[t].indexOf("}"), lines[t].length());
								String newvelos = "";
								for (int k=0; k < velocs.get(j).invelos.length; k++) {
									newvelos += velocs.get(j).invelos[k];
								}
								updated += chunk1 + "{" + newvelos + chunk2 + System.getProperty("line.separator");
							}	else {
								updated += lines[t] + System.getProperty("line.separator");
							}
						}						
						textarea.setText(updated);
						textarea.setCaretPosition(cursorline);
						textarea.setSelectionStart(lastselectedstart);
						textarea.setSelectionEnd(lastselectedend);	
					}
				}
			} else {
				boolean foundnoter = false;
				for (int i=0; i < machinelist.size(); i++) {
					if(machinelist.get(i).name.equals(parts[1])) {
						foundnoter = true;
						machinelist.get(i).kill();
						machinelist.get(i).shuffle();
						int cursorline = textarea.getCaretPosition();
						String updated = "";
						String[] lines = textarea.getText().split(System.getProperty("line.separator"));
						for (int t = 0; t < lines.length; t++) {							
							if(lines[t].length() > parts[1].length() && lines[t].substring(0, parts[1].length()).contains(parts[1])) {
								String chunk1 = lines[t].substring(0, lines[t].indexOf("{"));
								String chunk2 = lines[t].substring(lines[t].indexOf("}"), lines[t].length());
								String newnotes = "";
								for (int k=0; k < machinelist.get(i).notestring.length; k++) {
									newnotes += machinelist.get(i).notestring[k];
								}
								updated += chunk1 + "{" + newnotes + chunk2 + System.getProperty("line.separator");
							}	else {
								updated += lines[t] + System.getProperty("line.separator");
							}
						}
						textarea.setText(updated);
						textarea.setCaretPosition(cursorline);
						textarea.setSelectionStart(lastselectedstart);
						textarea.setSelectionEnd(lastselectedend);						
					}
				}	

				if(!foundnoter) { // Now search the CC objects
					for (int i=0; i < cclist.size(); i++) {
						if(cclist.get(i).name.equals(parts[1])) {
							cclist.get(i).kill();
							cclist.get(i).shuffle();
							int cursorline = textarea.getCaretPosition();
							String updated = "";
							String[] lines = textarea.getText().split(System.getProperty("line.separator"));
							for (int t = 0; t < lines.length; t++) {							
								if(lines[t].substring(0).contains("cc ") && lines[t].contains(" " + (parts[1]) + " ")) {
									String chunk1 = lines[t].substring(0, lines[t].indexOf("{"));
									String chunk2 = lines[t].substring(lines[t].indexOf("}"), lines[t].length());
									String newvals = "";
									for (int k=0; k < cclist.get(i).valstring.length; k++) {
										newvals += cclist.get(i).valstring[k];
									}
									updated += chunk1 + "{" + newvals + chunk2 + System.getProperty("line.separator");
								}	else {
									updated += lines[t] + System.getProperty("line.separator");
								}
							}
							textarea.setText(updated);
							textarea.setCaretPosition(cursorline);
							textarea.setSelectionStart(lastselectedstart);
							textarea.setSelectionEnd(lastselectedend);
						}
					}
				}
			}

		} else if (parts[0].equals("<<") && parts.length > 1) { // Nudge phrase left in time
			boolean foundnoter = false;
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					foundnoter = true;
					if(machinelist.get(i).seqcounter > 0) {
						machinelist.get(i).seqcounter--;
					} else {
						machinelist.get(i).seqcounter = machinelist.get(i).ticklength-1;
					}
				}
			}			
			if(!foundnoter) { //look for cc objects
				for (int i=0; i < cclist.size(); i++) {
					if(cclist.get(i).name.equals(parts[1])) {
						if(cclist.get(i).seqcounter > 0) {
							cclist.get(i).seqcounter--;
						} else {
							cclist.get(i).seqcounter = cclist.get(i).ticklength-1;
						}
					}
				}
			}

		} else if (parts[0].equals(">>") && parts.length > 1) { //Nudge phrase right in time
			boolean foundnoter = false;
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					foundnoter = true;
					if(machinelist.get(i).seqcounter < machinelist.get(i).ticklength-1) {
						machinelist.get(i).seqcounter++;
					} else {
						machinelist.get(i).seqcounter = 0;
					}
					/*
					if(machinelist.get(i).globvoloffset - 4 >= 0) {
						machinelist.get(i).velocity -= 4;
					}
					 */
				}
			}			
			if(!foundnoter) {
				for (int i=0; i < cclist.size(); i++) {
					if(cclist.get(i).name.equals(parts[1])) {
						if(cclist.get(i).seqcounter < cclist.get(i).ticklength-1) {
							cclist.get(i).seqcounter++;
						} else {
							cclist.get(i).seqcounter = 0;
						}
					}
				}
			}

		} else if(parts[0].equals("start")) {
			myMidi.sendMessage(javax.sound.midi.ShortMessage.START); // This might work on Windows/Linux, but not on OSX's fucking disastrous Java MIDI implementation			
		} else if(parts[0].equals("stop")) {
			myMidi.sendMessage(javax.sound.midi.ShortMessage.STOP); // This might work on Windows/Linux, but not on OSX's fucking disastrous Java MIDI implementation			
		}
		else if (parts[0].equals("velo") && parts.length > 2) {
			// String name, int future, divr, looping
			boolean exists = false;
			for (int i=0; i < velocs.size(); i++) {
				if(velocs.get(i).name.equals(parts[1])) {
					velocs.set(i, new Velo(parts[1], parts[2].substring(1, parts[2].length()-1)));
					exists = true;
				}
			}
			if(!exists) {
				velocs.add( new Velo(parts[1], parts[2].substring(1, parts[2].length()-1)));
			}


		} else if(parts[0].equals("help")) {
			// Spit out a list of commands and whatnot
			
		} else if(parts[0].equals("list") && parts.length > 1) {
			// List objects (note, cc, velo, chord) with current playing/muting state, target channel, etc.
			
			
		} else if (parts[0].equals("key") && parts.length > 1) {
			int key = Integer.parseInt(parts[1]);
			if(key > -64 && key < 64) {
				initialroot+= key;
			}

		}  else if (parts[0].equals("pause") && parts.length > 1) {
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					machinelist.get(i).kill();
					machinelist.get(i).playing = false;
				}
			}			

		}  else if (parts[0].equals("unpause") && parts.length > 1) {
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					machinelist.get(i).playing = true;
				}
			}		

		}  else if (parts[0].equals("mute") && parts.length > 1) {
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					machinelist.get(i).muted = true;
				}
			}		

		}  else if (parts[0].equals("unmute") && parts.length > 1) {
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					machinelist.get(i).muted = false ;
				}
			}		

		} else if(parts[0].equals("ptoggle") && parts.length > 1){ // ptoggle - toggle playing or not playing
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					if (machinelist.get(i).playing){
						machinelist.get(i).playing = false;
					} else {
						machinelist.get(i).playing = true;
					}
				}
			}

		} else if(parts[0].equals("mtoggle") && parts.length > 1){ // ptoggle - toggle muted or not
			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					if (machinelist.get(i).muted){
						machinelist.get(i).muted = false;
					} else {
						machinelist.get(i).muted = true;
					}
				}
			}

		} else if(parts[0].equals("mididevice")){
			if(parts.length > 1){
				if(parts[1].toLowerCase().equals("list")) {
					for (int i=0; i < MidiBus.availableInputs().length; i++) {
						textoutput.setText(textoutput.getText() + "\nInput["+i+"]: " + MidiBus.availableInputs()[i]);
					}
					for (int i=0; i < MidiBus.availableOutputs().length; i++) {
						textoutput.setText(textoutput.getText() + "\nOutput["+i+"]: " + MidiBus.availableOutputs()[i]);
					}		
				} else {
					if(Integer.parseInt(parts[1]) <= MidiBus.availableInputs().length && Integer.parseInt(parts[2]) <= MidiBus.availableOutputs().length ) {
						myMidi = new MidiBus(this, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
						System.out.println("Updated MIDI devices");
						textoutput.setText(textoutput.getText()+ "\nUpdated MIDI device");
					}
				}
			}

		} else if(parts[0].equals("opacity") && parts.length > 1) {

			if(Integer.parseInt(parts[1]) <= 100 && Integer.parseInt(parts[1]) > 0) {
				float opac = Integer.parseInt(parts[1]) * 0.01f;
				AWTUtilities.setWindowOpacity(jframe, opac);
				AWTUtilities.setWindowOpacity(jframe2, opac);
			}

		} else if (parts[0].equals("kill") && parts.length > 1) {

			for (int i=0; i < machinelist.size(); i++) {
				if(machinelist.get(i).name.equals(parts[1])) {
					machinelist.get(i).kill();
					machinelist.remove(i);
				}
			}
			for (int i=0; i < cclist.size(); i++) {
				if(cclist.get(i).name.equals(parts[1])) {		
					cclist.get(i).kill();
					cclist.remove(i);
				}
			}

		} else if (parts[0].equals("killquit") && parts.length > 1) {
			// TO DO			

		} else if(parts[0].equals("cc") && parts.length > 4) {
			boolean foundexisting = false;
			for (int i = 0; i < cclist.size(); i++) {
				if(cclist.get(i).name.equals(parts[1])) {
					cclist.get(i).channel = Integer.parseInt(parts[2].substring(1));
					cclist.get(i).cc = Integer.parseInt(parts[3]);
					cclist.get(i).loopcounter = 0;
					String[] tempvals = parts[4].substring(1, parts[4].length()-1).split("(?!^)");
					if(tempvals.length < cclist.get(i).ticklength){
						cclist.get(i).seqcounter = 0;
					}
					cclist.get(i).ticklength = tempvals.length;
					cclist.get(i).valstring = new String[tempvals.length];
					for (int p = 0; p < tempvals.length; p++) {
						cclist.get(i).valstring[p] = tempvals[p];					
					}
					if(parts.length > 5 && Integer.parseInt(parts[5]) > 0) {
						cclist.get(i).multiplier = Integer.parseInt(parts[5]);
					}
					if(parts.length > 6) {
						cclist.get(i).loops = Integer.parseInt(parts[6]);	
					}
					cclist.get(i).playing = true;

					foundexisting = true;
				}
			}
			if(!foundexisting) {
				// If it's new, grab control by sending 0, 127, then the first value in case DAW has knob pickup mode enabled
				myMidi.sendControllerChange(Integer.parseInt(parts[2].substring(1)), Integer.parseInt(parts[3]), 0);
				myMidi.sendControllerChange(Integer.parseInt(parts[2].substring(1)), Integer.parseInt(parts[3]), 64);
				String[] tempvals = parts[4].substring(1, parts[4].length()-1).split("(?!^)");
				myMidi.sendControllerChange(Integer.parseInt(parts[2].substring(1)), Integer.parseInt(parts[3]), (int)map((float)Integer.parseInt(String.valueOf(tempvals[0]), 16), 0f, 15f, 0f, 127f));
				try {
					if(parts.length > 6 && Integer.parseInt(parts[5]) > 0) {
						cclist.add(new CCMachine(parts[1], Integer.parseInt(parts[2].substring(1)), Integer.parseInt(parts[3]), parts[4].substring(1, parts[4].length()-1), Integer.parseInt(parts[5]), Integer.parseInt(parts[6]))); 
						//						name			channel number							CC number					value sequence								multiplier					loopcount												
					} else if(parts.length > 5 && Integer.parseInt(parts[5]) > 0) {
						cclist.add(new CCMachine(parts[1], Integer.parseInt(parts[2].substring(1)), Integer.parseInt(parts[3]), parts[4].substring(1, parts[4].length()-1), Integer.parseInt(parts[5]))); 
						//						name			channel number							CC number					value sequence								multiplier					loopcount						
					} else if(parts.length > 4) {												
						cclist.add(new CCMachine(parts[1], Integer.parseInt(parts[2].substring(1)), Integer.parseInt(parts[3]), parts[4].substring(1, parts[4].length()-1))); 
						//						name			channel number							CC number					value sequence								multiplier					loopcount						
					}

				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
		} else {

			if( parts.length > 2) {
				// If lfo, chord etc not specified, create a notemachine
				boolean foundexisting = false;
				for (int i=0; i < machinelist.size(); i++) {
					if(machinelist.get(i).name.equals(parts[0])) {
						// Don't overwrite, just change values in existing				
						//machinelist.set(i, new NoteMachine(parts[0], Integer.parseInt(parts[1]), parts[2].substring(1, parts[2].length()-1), Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
						machinelist.get(i).channel = Integer.parseInt(parts[1].substring(1));
						//machinelist.get(i).kill();
						String[] tempnotes = parts[2].substring(1, parts[2].length()-1).split("(?!^)");
						// Check whether the sequencecounter's in dangerous territory, for the previous phrase's sequence (if longer than the new one)
						if(tempnotes.length < machinelist.get(i).ticklength){
							machinelist.get(i).seqcounter = 0;
						}
						machinelist.get(i).ticklength = tempnotes.length;
						machinelist.get(i).notestring = new String[tempnotes.length];
						for (int p = 0; p < tempnotes.length; p++) {
							machinelist.get(i).notestring[p] = tempnotes[p];
						}
						if(parts.length > 3 && Integer.parseInt(parts[3]) > 0) {
							machinelist.get(i).multiplier = Integer.parseInt(parts[3]);
						}
						if(parts.length > 4) {
							machinelist.get(i).transpose = Integer.parseInt(parts[4]);
						}
						machinelist.get(i).playing = true;
						foundexisting = true;
					}
				}
				if(!foundexisting) {
					//name, midichan, sequence, multiplier, tranpose
					try {
						if(parts.length > 4) {
							machinelist.add(new NoteMachine(parts[0], Integer.parseInt(parts[1].substring(1)), parts[2].substring(1, parts[2].length()-1), Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
						} else
							if(parts.length > 3) {
								machinelist.add(new NoteMachine(parts[0], Integer.parseInt(parts[1].substring(1)), parts[2].substring(1, parts[2].length()-1), Integer.parseInt(parts[3])));
							} else
								if(parts.length > 2) {
									machinelist.add(new NoteMachine(parts[0], Integer.parseInt(parts[1].substring(1)), parts[2].substring(1, parts[2].length()-1)));							
								}



					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public float map(float value, float istart, float istop, float ostart, float ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}

	public void keyPressed() {
		// Processing's keylistener - not used for now		
	}

	public void keyReleased(KeyEvent event) {
		// Processing's keylistener - not used for now
	}


	public void focusGained(FocusEvent e) {
		jframe.setVisible(true);
		jframe.toFront();
	}

	public void midiMessage(MidiMessage message) {
		if(message.getStatus() == 0xFA) {
			clockcount = 1; // Start the clock!
			sync();
		}
		if(message.getStatus() == 0xF8) {
			//sync(System.currentTimeMillis());
			sync();
		}
		if(message.getStatus() == 0xF2) {
			// We've got a song position message - doesn't matter what it is, but it means playback's just been started. Reset clock.
			clockcount = 1;
			sync();
		}

	}

	public void sync() {
		clockcount++;
		if(clockcount > 0 && cclist != null && machinelist != null) {


			for (int i=0; i < machinelist.size(); i++) {
				machinelist.get(i).update(clockcount);
			}

			for (int i=0; i < cclist.size(); i++) {
				cclist.get(i).update(clockcount);
			}			

		}

		if(clockcount % 24 == 0) {
			String beatmarks = "Beat: ";
			if(beatmarker < 4) {
				beatmarker++;
			} else {
				beatmarker = 1;
			}


			for (int i=0; i < beatmarker; i++) {
				beatmarks = "BEAT: " + beatnumbers[i] + " ";
			}			

			textoutput.setText(beatmarks+ textoutput.getText().substring(textoutput.getText().indexOf("\n")));
		}
	}


	public class Chord {
		String name = "";
		public int[] notes;
		public int transpose = 0;

		public Chord(String chordname, String chordnotes, int transp) {
			name = chordname;
			transpose = transp;
			notes = new int[chordnotes.length()];
			for ( int i = 0; i < chordnotes.length(); i++ ) {					
				notes[i] = Integer.parseInt(String.valueOf(chordnotes.charAt(i)), 16) + transpose; //parse hex
			}			
		}
	}

	public class Velo {
		String name = "";
		public int[] velos;
		public String[] invelos;

		public Velo(String targetname, String velocities) {
			name = targetname;
			invelos = velocities.split("(?!^)");;
			velos = new int[velocities.length()];
			for ( int i = 0; i < velocities.length(); i++ ) {					
				velos[i] = Integer.parseInt(String.valueOf(velocities.charAt(i)), 16); //parse hex
			}			
		}

		public void shuffle () {
			for (int i=0; i < velos.length; i++) {

				Random rnd = new Random();
				for (int j = invelos.length - 1; j > 0; j--)
				{
					int index = rnd.nextInt(j + 1);
					// Simple swap
					String a = invelos[index];
					invelos[index] = invelos[j];
					invelos[j] = a;
				}

			}
		}
	}


	public class NoteMachine {
		public int ticklength = 0;
		public int lastcount = 0;
		public int seqcounter = 0;
		public int velocounter = 0;
		public int globvoloffset = 0;
		public String[] notestring;
		public String[] orignotestring;
		public int[] notes;	
		public String name = "";
		public int channel = 0;
		int multiplier = 12; //default 12
		public int transpose = 0;
		public boolean playing = true;
		public boolean muted = false;
		public int velocity = 100;
		public int offnote = 0;
		public ArrayList<String> oldnotes = new ArrayList<String>();
		public ArrayList<Chord> oldchords = new ArrayList<Chord>();

		public NoteMachine(String machinename, int chan, String phrase) {
			ticklength = phrase.length();
			notestring = phrase.split("(?!^)");
			orignotestring = phrase.split("(?!^)");
			notes = new int[notestring.length];			
			name = machinename;
			channel = chan;		
		}

		public NoteMachine(String machinename, int chan, String phrase, int multi) {
			ticklength = phrase.length();
			notestring = phrase.split("(?!^)");
			orignotestring = phrase.split("(?!^)");			
			notes = new int[notestring.length];
			name = machinename;
			channel = chan;
			multiplier = multi;		
		}

		public NoteMachine(String machinename, int chan, String phrase, int multi, int transp) {
			ticklength = phrase.length();
			notestring = phrase.split("(?!^)"); 
			orignotestring = phrase.split("(?!^)");			
			notes = new int[notestring.length];
			name = machinename;
			channel = chan;
			transpose = transp;
			multiplier = multi;		
		}


		public void wrapright () {
			String last = notestring[notestring.length-1];
			for (int i=0; i < notestring.length-1; i++ ) {
				notestring[i+1] = orignotestring[i];
			}
			notestring[0] = last;
		}

		public void wrapleft () {
			String first = notestring[0];
			for (int i=1; i < notestring.length; i++ ) {
				notestring[i] = orignotestring[i-1];
			}
			notestring[notestring.length-1] = first;
		}

		public void shuffle () {
			for (int i=0; i < notestring.length; i++) {
				//notestring[i] = Integer.toString((int)random(0, 16));
				//notes[i] = notes[(int)random(0, notes.length)];

				Random rnd = new Random();
				for (int j = notestring.length - 1; j > 0; j--)
				{
					int index = rnd.nextInt(j + 1);
					// Simple swap
					String a = notestring[index];
					notestring[index] = notestring[j];
					notestring[j] = a;
				}

			}
		}

		public void update(int clockcount) {
			if(clockcount % multiplier == 0 && playing) {

				for (int i=0; i < velocs.size(); i++ ) {
					if(velocs.get(i).name.equals(this.name)) {
						if(velocounter < velocs.get(i).velos.length) {
							velocity = (int)map((float)velocs.get(i).velos[velocounter], (float)0x00, (float)0x0F, (float)0, (float)127);
						}
						if(velocounter+1 >= velocs.get(i).velos.length){
							velocounter = 0;
						} else {
							velocounter++;
						}
					}
				}


				if(!notestring[seqcounter].matches("g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z")) {
					if(!notestring[seqcounter].equals("-")) { // Skip note if there's a hyphen
						if(!notestring[seqcounter].equals(".")) { // Send a kill if there's a dot
							int thisnote = Integer.parseInt(notestring[seqcounter], 16) + transpose; //parse hex
							//if(seqcounter > 0) { offnote = Integer.parseInt(notestring[seqcounter-1], 16) + transpose; }
							if(muted) {
								myMidi.sendNoteOn(channel, initialroot + thisnote, 0);
							} else {
								myMidi.sendNoteOn(channel, initialroot + thisnote, velocity + globvoloffset);	
							}

							oldnotes.add(Integer.toString(thisnote));
							activenote = initialroot+thisnote;
						} else {
							kill();
						}

					}
				} else {
					for(int j=0; j < chordlist.size(); j++) {
						//Chord tempchord = chordlist.get(j);
						if(chordlist.get(j).name.equals(notestring[seqcounter])) {

							for( int k = 0; k < chordlist.get(j).notes.length; k++ ) {
								chordlist.get(j).notes[k] += transpose;
								if(muted) {
									myMidi.sendNoteOn(channel,  initialroot + chordlist.get(j).notes[k], 0);
								} else {
									myMidi.sendNoteOn(channel,  initialroot + chordlist.get(j).notes[k], velocity + globvoloffset);	
								}

								activenote = initialroot+chordlist.get(j).notes[k];
							}
							oldchords.add(chordlist.get(j));

						}

					}
				}

				if(seqcounter < ticklength-1) {
					seqcounter++;
				} else {
					seqcounter = 0;

				}
			}
		}


		// might be grossly inefficient...
		public void kill() {
			for (int i=0; i < 128; i++) {
				myMidi.sendNoteOff(channel, i, 0);
			}
		}

		public void schedulekill(int chan, int note, int velo, int clockseq) {


		}


	}


	public class CCMachine {
		public int ticklength = 0;
		public int lastcount = 0;
		public int seqcounter = 0;

		public String[] valstring;
		public String[] origvalstring;
		public int[] vals;	
		public String name = "";
		public int channel = 0;
		public int loopcounter = 0;
		int multiplier = 12; //defaults to 12
		public int loops = 0; // 0 = infinite
		public int cc = 10; // Defaults to pan, no reason
		public boolean playing = true;
		public boolean muted = false;
		public ArrayList<String> oldvals = new ArrayList<String>();

		public CCMachine(String machinename, int chan, int ccnum, String phrase) {					
			loopcounter = 0;
			name = machinename;
			channel = chan;
			cc = ccnum;
			ticklength = phrase.length();
			valstring = phrase.split("(?!^)");
			vals = new int[valstring.length];	
			origvalstring = phrase.split("(?!^)");
		}

		public CCMachine(String machinename, int chan, int ccnum, String phrase, int multi) {		
			loopcounter = 0;
			name = machinename;
			channel = chan;
			cc = ccnum;
			ticklength = phrase.length();
			valstring = phrase.split("(?!^)");
			vals = new int[valstring.length];				
			origvalstring = phrase.split("(?!^)");
			multiplier = multi;
		}

		public CCMachine(String machinename, int chan, int ccnum, String phrase, int multi, int loopcount) {					
			loopcounter = 0;
			name = machinename;
			channel = chan;
			cc = ccnum;
			ticklength = phrase.length();
			valstring = phrase.split("(?!^)");
			vals = new int[valstring.length];			
			origvalstring = phrase.split("(?!^)");
			multiplier = multi;
			loops = loopcount;
		}		

		public void wrapright () {
			String last = valstring[valstring.length-1];
			for (int i=0; i < valstring.length-1; i++ ) {
				valstring[i+1] = origvalstring[i];
			}
			valstring[0] = last;
		}

		public void wrapleft () {
			String first = valstring[0];
			for (int i=1; i < valstring.length; i++ ) {
				valstring[i] = origvalstring[i-1];
			}
			valstring[valstring.length-1] = first;
		}

		public void shuffle () {
			for (int i=0; i < valstring.length; i++) {

				Random rnd = new Random();
				for (int j = valstring.length - 1; j > 0; j--)
				{
					int index = rnd.nextInt(j + 1);
					// Simple swap
					String a = valstring[index];
					valstring[index] = valstring[j];
					valstring[j] = a;
				}

			}
		}

		public void update(int clockcount) {
			if(clockcount % multiplier == 0 && playing) {


				if(!valstring[seqcounter].matches("g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z")) {
					if(!valstring[seqcounter].equals("-")) { // Skip note if there's a hyphen
						if(!valstring[seqcounter].equals(".")) { // Send a kill if there's a dot (doesn't do anything much in CCs)
							int thisval = Integer.parseInt(valstring[seqcounter], 16); //parse hex
							//if(seqcounter > 0) { offnote = Integer.parseInt(notestring[seqcounter-1], 16) + transpose; }
							//send pan first
							myMidi.sendControllerChange(channel, cc, (int)map((float)thisval, 0f, 15f, 0f, 127f));									
							oldvals.add(Integer.toString(thisval));
						} else {
							kill();
						}

					}
				}

				if(seqcounter < ticklength-1) {
					seqcounter++;
				} else {
					seqcounter = 0;
					if(loops > 0) { 
						loopcounter++;
					}
				}
			}
			if(loops > 0) {
				if(loopcounter >= loops) {
					playing = false;
				}
			}
		}

		// 

		// might be grossly inefficient...
		public void kill() {

		}


	}


	// must add "prepareExitHandler();" in setup() for Processing sketches 
	private void prepareExitHandler () {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run () {
				for(int i=0; i < machinelist.size(); i++) {
					machinelist.get(i).kill();
				}

			}
		}));
	}


}
