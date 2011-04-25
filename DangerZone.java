import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.*;

public class DangerZone extends Plugin 
{
	// Base Plugin Variables
	private static String name = "DangerZone";
	private static String version = "0.24";
	private boolean debug = false;
	static final Logger log = Logger.getLogger("Minecraft");
	private Random randGen;
	private boolean suppressMonsters = false;
	private ArrayList<ExclusionZone> authoritativeZones = new ArrayList<ExclusionZone>();
	private ArrayList<ExclusionZone> cumulativeZones = new ArrayList<ExclusionZone>();
	
	public void enable() {
		log.info(name + " v" + version + " enabled.");
	}

	public void disable() {
		log.info(name + " v" + version + " disabled.");
	}

	public void initialize() {
		DangerZoneListener listener = new DangerZoneListener();
		randGen = new Random();
		
		PropertiesFile properties = new PropertiesFile("server.properties");
		
		if (properties.keyExists("dangerzone-debug") && properties.getBoolean("dangerzone-debug")) {
			debug = true;
		}

		String configFile = properties.getString("dangerzone-config", "dangerzone.csv");
		loadConfig(configFile);

		etc.getLoader().addListener(PluginLoader.Hook.MOB_SPAWN, listener, this, PluginListener.Priority.HIGH);
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.LOW);
		etc.getLoader().addListener(PluginLoader.Hook.SERVERCOMMAND, listener, this, PluginListener.Priority.LOW);
	}
	
	public void debug(String msg) {
		if (debug == true) {
			log.info("[DEBUG] " + msg);
		}
	}
	
	public String colorize(String msg) {
		// Replaces !@0 - !@f with the appropriate color code
		return(msg.replaceAll("\\!\\@([0-9a-f])", "§$1"));
	}

	public String join(String[] s, String glue) {
		int k=s.length;
		if (k==0) {
			return null;
		}
		
		StringBuilder out=new StringBuilder();
		out.append(s[0]);
		
		for (int x=1;x<k;++x) {
			out.append(glue).append(s[x]);
		}
		
		return out.toString();
	}
	
	protected double getProbability(double rX, double rZ) {
		double currentProbability = 1;
		Iterator<ExclusionZone> zoneIterator = authoritativeZones.iterator();
		
		if (suppressMonsters) { return 0; }
		
		while (zoneIterator.hasNext()) {
			currentProbability = zoneIterator.next().spawnProbability(rX, rZ);
			if (currentProbability >= 0) {
				return currentProbability;
			}
		}
		zoneIterator = null;
		
		currentProbability = 1;
		zoneIterator = cumulativeZones.iterator();
		while (zoneIterator.hasNext()) {
			currentProbability = currentProbability * Math.abs(zoneIterator.next().spawnProbability(rX, rZ));
		}

		return currentProbability;
	}
	
	private void loadConfig(String filename) {
		debug("Loading config file.");
		authoritativeZones.clear();
		cumulativeZones.clear();
		File file=new File(filename);
		
		if (!file.exists()) { createDefaultFile(filename); }
		
        BufferedReader bufferedReader = null;
        
        try {
            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
            	line = line.trim();
                //Process the data, here we just print it out
            	if (!line.startsWith("#") && !line.equalsIgnoreCase("")) {
            		ExclusionZone tempZone = ExclusionZone.fromString(line);
            		if (tempZone != null) {
            			if (tempZone.isAuthoritative) {
            				debug("Adding authoritative zone " + tempZone.toString());
            				authoritativeZones.add(tempZone);
            			} else {
            				debug("Adding cumulative zone " + tempZone.toString());
            				cumulativeZones.add(tempZone);
            			}
            		} else {
            			debug("Ignoring line: " + line);
            		}
            	}
            }
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	}
	
	private void createDefaultFile(String filename) {
		try {
			debug("Creating default config file.");
			FileWriter outFile = new FileWriter(filename);
	    	PrintWriter out = new PrintWriter(outFile);
	    	// Also could be written as follows on one line
	    	// Printwriter out = new PrintWriter(new FileWriter(args[0]));
	    	
	    	// Write text to file
	    	out.println("# DangerZone Config File ");
	    	out.println("# This CSV is read by the DangerZone pluign.");
	    	out.println("# Lines that begin with a hash are comments.");
	    	out.println("# Generic format:");
	    	out.println("# type,authoritative,x,z,<type-specific data>");
	    	out.println("# Zone types:");
	    	out.println("# ring,[yes|no],x,z,innerRadius,innerProbability (0..1),outerRadius,outerProbability (0..1)");
	    	out.println("# rectangle,[yes|no],x1,z1,x2,z2,Probability (0..1)");
	    	out.println("# line,[yes|no],x,z,Axis [X|Z],Distance, innerProbability (0..1), outerProbability");
	    	out.close();
	    } catch (IOException e){
	    	e.printStackTrace();
	    }		
	}
	
	public class DangerZoneListener extends PluginListener // start
	{
	    /**
	     * @param mob Mob attempting to spawn.
	     * @return true if you don't want mob to spawn.
	     */
	    public boolean onMobSpawn(Mob mob) {
	    	double allowChance = getProbability(mob.getX(), mob.getY());
	    	if (allowChance >= randGen.nextDouble()) {
	    		debug("Allowing " + mob.getName() + " to spawn.");
	    		return false;
	    	} else {
	    		debug("Keeping " + mob.getName() + " from spawning in a safe zone.");
	    		return true;
	    	}
	    }
	    
	    /**
	     * Called before the command is parsed. Return true if you don't want the
	     * command to be parsed.
	     * 
	     * @param player
	     * @param split
	     * @return false if you want the command to be parsed.
	     */
	    public boolean onCommand(Player player, String[] split) {
			// The plug-in will get any command issued in the game,
			// not just the ones we specifically request, so we have to
			// check to see if we should be acting.
	    	if (!player.canUseCommand(split[0])) {
	    		return false;
	    	}
	    	
	    	if (split[0].equalsIgnoreCase("/monsterProbability") || split[0].equalsIgnoreCase("/mprob")) {
	    		player.sendMessage("Monster spawn probability: " + Colors.Rose + getProbability(player.getX(), player.getZ()));
	    		return true;
	    	}
	    	return false;
	    } //onCommand
	    
	    /**
	     * Called before the console command is parsed. Return true if you don't
	     * want the server command to be parsed by the server.
	     * 
	     * @param split
	     * @return false if you want the command to be parsed.
	     */
	    public boolean onConsoleCommand(String[] split) {
	    	if (split[0].equalsIgnoreCase("suppressMonsters")) {
	    		if (split.length >= 2) {
	    			if (split[1].equalsIgnoreCase("on")) {
	    				log.info("[DangerZone] Monsters are now suppressed.");
	    				suppressMonsters = true;
	    				return true;
	    			}
	    			
	    			if (split[1].equalsIgnoreCase("off")) {
	    				log.info("[DangerZone] Monsters are no longer suppressed.");
	    				suppressMonsters = false;
	    				return true;
	    			}
	    		}
	    		
	    		log.info("Incorrect syntax: suppressMonsters [on|off]");
	    		return true;
	    	}
			return false;
	    } //onConsoleCommand
	} //DanterZoneListener
} //DangerZone