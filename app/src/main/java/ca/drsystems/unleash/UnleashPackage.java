package ca.drsystems.unleash;

import java.io.Serializable;

public class UnleashPackage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 42L;
	private int header;
	private User data;
	
	public UnleashPackage(int h, User d){
		this.header = h;
		
		//changing this back allowed the devices to communicate to the point where a socket was created and data was 
		//passed for a short time, and then both devices crashed at the exact same time (instead of the usual where
		//just the host crashes)
		this.data = d;
		/*data = new User();
		data.setLon(d.getLon());
		data.setLat(d.getLat());
		data.setName(d.getName());
		data.setNumber(d.getNumber());*/
	}
	
	public void setHeader(int h){
		this.header = h;
	}
	
	public int getHeader(){
		return this.header;
	}
	
	public void setData(User d){
		this.data.setLat(d.getLat());
		this.data.setLon(d.getLon());
		this.data.setName(d.getName());
		this.data.setNumber(d.getNumber());
	}
	
	public User getData(){
		return this.data;
	}
}
