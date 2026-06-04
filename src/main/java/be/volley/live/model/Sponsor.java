package be.volley.live.model;

import java.io.Serial;
import java.io.Serializable;

public class Sponsor implements Serializable  {

	@Serial
	private static final long serialVersionUID = 1L;
	private String name ;
	private String logo;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	
}
