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

    public void setId(String id)
    {
         m_id = id;
    }
    
    public String getId() 
    {
    	return m_id; 
    }

    public void setName(String name)
    {
         m_name = name;
    }
    
    public String getName() 
    {
    	return m_name; 
    }

    public void setVersion(String version)
    {
         m_version = version;
    }
    
    public String getVersion() 
    {
    	return m_version; 
    }

    public void setHost(String host)
    {
         m_host = host;
    }
    
    public String getHost() 
    {
    	return m_host;
    }

    public void setIpAddress(String ipAddress)
    {
         m_idAddress = ipAddress;
    }
    
    public String getIpAddress() 
    {
    	return m_idAddress; 
    }

    public void setPort(String port)
    { 
    	 m_port = port;
    }
    
    public String getPort() 
    {
    	return m_port; 
    }

    public void setTimeStamp(String timestamp)
    {
        m_timestamp = timestamp;
    }
    
    public String getTimeStamp() 
    {
    	return m_timestamp; 
    }
}
