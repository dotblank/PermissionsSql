package com.nijiko.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nijiko.permissions.EntryType;

public class SqlGroupStorage extends SqlEntryStorage implements GroupStorage {

    private String defaultGroup = null;
    
    private static final String defGroupText = "SELECT defaultid FROM PrWorldBase WHERE worldid = ?;";
    private static final String trackListText = "SELECT trackname FROM PrTracks WHERE worldid = ?;";
    private static final String trackGetText = "SELECT PrWorlds.worldname, PrEntries.name FROM PrWorlds, PrEntries, PrTracks, PrTrackGroups WHERE PrTrackGroups.trackid = PrTracks.trackid AND PrTracks.worldid = ? AND PrTracks.trackname = ? AND PrEntries.entryid = PrTrackGroups.gid AND PrWorlds.worldid = PrEntries.worldid ORDER BY PrTrackGroups.groupOrder;";
        
    public SqlGroupStorage(String world, int id) {
        super(world, id);
    }

    @Override
    public EntryType getType() {
        return EntryType.GROUP;
    }

    @Override
    public boolean isDefault(String name) {
        if(defaultGroup != null) {
            return defaultGroup.equals(name);
        }
        List<Map<Integer, Object>> results = SqlStorage.runQuery(defGroupText, new Object[]{worldId}, true, 1);
        Iterator<Map<Integer, Object>> iter = results.iterator();
        if(iter.hasNext()) {
            Object def = iter.next().get(1);
            if(def instanceof Integer) {
            	try {
					if(this.getId(name) == (Integer) def)
	            	{
		                defaultGroup = name;
		                return defaultGroup.equals(name);
		            }
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        return false;
    }
    public String getDefault()
    {
    	return defaultGroup;
    }

    @Override
    public Set<String> getTracks() {
        List<Map<Integer, Object>> results = SqlStorage.runQuery(trackListText, new Object[]{worldId}, false, 1);
        Iterator<Map<Integer, Object>> iter = results.iterator();
        Set<String> tracks = new LinkedHashSet<String>();
        while(iter.hasNext()) {
            Object o = iter.next().get(1);
            if(o instanceof String) {
                tracks.add((String) o);
            }
        }
        return tracks;
    }

    @Override
    public LinkedList<GroupWorld> getTrack(String track) {
        List<Map<Integer, Object>> results = SqlStorage.runQuery(trackGetText, new Object[]{worldId, track}, false, 1);
        Iterator<Map<Integer, Object>> iter = results.iterator();
        LinkedList<GroupWorld> trackGroups = new LinkedList<GroupWorld>();
        while(iter.hasNext()) {
            Object oWorld = iter.next().get(1);
            Object oName = iter.next().get(2);
            if(oWorld instanceof String && oName instanceof String) {                
                trackGroups.add(new GroupWorld((String)oWorld, (String)oName));
            }
        }
        return null;
    }

    @Override
    protected int getId(String name) throws SQLException {
        int gid = -1;
        if (idCache.containsKey(name))
            gid = idCache.get(name);
        else {
            gid = SqlStorage.getEntry(world, name, true);
            idCache.put(name, gid);
        }
        return gid;
    }
    
}
