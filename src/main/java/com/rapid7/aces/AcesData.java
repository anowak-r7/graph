package com.rapid7.aces;

/**
 * Created by root on 11/17/15.
 */
public class AcesData
{
    public AcesData()
    {}

    private String m_id;
    private String m_version;
    private String m_name;
    private String m_host;
    private String m_idAddress;
    private String m_port;
    private String m_timestamp;

    public String setId(String id)
    {
       return  m_id = id;
    }
    
    public String getId() 
    {
    	return m_id; 
    }

    public String setName(String name)
    {
        return m_name = name;
    }
    
    public String getName() 
    {
    	return m_name; 
    }

    public String setVersion(String version)
    {
        return m_version = version;
    }
    
    public String getVersion() 
    {
    	return m_version; 
    }

    public String setHost(String host)
    {
        return m_host = host;
    }
    
    public String getHost() 
    {
    	return m_host;
    }

    public String setIpAddress(String ipAddress)
    {
        return m_idAddress = ipAddress;
    }
    
    public String getIpAddress() 
    {
    	return m_idAddress; 
    }

    public String setPort(String port) 
    { 
    	return m_port = port;
    }
    
    public String getPort() 
    {
    	return m_port; 
    }

    public String setTimeStamp(String timestamp)
    {
       return m_timestamp = timestamp;
    }
    
    public String getTimeStamp() 
    {
    	return m_timestamp; 
    }
}
