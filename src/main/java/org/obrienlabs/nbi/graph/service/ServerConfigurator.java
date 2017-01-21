/*
 * Michael O'Brien (2017)  Overly Enthusiastic - Science | Software | Hardware | Experimentation
 * michael at obrienlabs.org
 * https://github.com/obrienlabs
 * https://twitter.com/_mikeobrien
 * http://eclipsejpa.blogspot.ca/
 */
package org.obrienlabs.nbi.graph.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;

public class ServerConfigurator implements Map<String,String> {
    private Configuration config ;

    private Map<String,String> properties = new HashMap<>();
    public ServerConfigurator( Map<String,Object>  aInMap)
    {
        config = new MapConfiguration(aInMap);
        for (Map.Entry<String, Object> lEntry : aInMap.entrySet())
        {
             properties.put(lEntry.getKey(),lEntry.getValue()+"");
        }

    }
    //@Override
    public Configuration configuration()
    {
        return config;
    }

    //@Override
    public Map<String, String> getDatabaseTuningProperties()
    {
        return properties;
    }

    public void init() {

    }

    @Override
    public int size()
    {
        return properties.size();
    }

    @Override
    public boolean isEmpty()
    {
        return properties.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return properties.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return properties.containsValue(value);
    }

    @Override
    public String get(Object key)
    {
        return properties.get(key);
    }

    @Override
    public String put(String key, String value)
    {
        config.setProperty(key,value);
        return properties.put(key,value);
    }

    @Override
    public String remove(Object key)
    {
        config.clearProperty(key.toString());
        return properties.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m)
    {
        for (Entry<? extends String, ? extends String> lEntry : m.entrySet())
        {
            config.setProperty(lEntry.getKey(),lEntry.getValue());
        }
        properties.putAll(m);
    }

    @Override
    public void clear()
    {
        config.clear();
        properties.clear();
    }

    @Override
    public Set<String> keySet()
    {
        return properties.keySet();
    }

    @Override
    public Collection<String> values()
    {
        return properties.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet()
    {
        return properties.entrySet();
    }
}