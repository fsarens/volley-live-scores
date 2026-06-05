package be.volley.live.model;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "teams")
public class Team implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Indexed(unique = true)

	private String code;

	private String name;

	private League league;


	/* team color (hex) for the scoring UI */
	private String color = "#1565c0";

	/* soft delete — inactive teams are hidden from game planning and ranking */
	private boolean active = true;

	/* code of the reeks for this team with volleyscore */
	private String reeks;

	public String getReeks() {
		return reeks;
	}

	public Team(String code, String reeks) {
		super();
		this.code = code;
		this.reeks = reeks;
	}

	public Team() {
		super();
	}

	public void setReeks(String reeks) {
		this.reeks = reeks;
	}

	public Sponsor getSponsor() {
		return sponsor;
	}

	public void setSponsor(Sponsor sponsor) {
		this.sponsor = sponsor;
	}

	/* the sponsor for this team */
	private Sponsor sponsor;
	private Sponsor sponsor2;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj instanceof Team)
			return this.id.equals(((Team) obj).id);

		return false;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public League getLeague() {
		return league;
	}

	public void setLeague(League league) {
		this.league = league;
	}


	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }

	public Sponsor getSponsor2() {
		return sponsor2;
	}

	public void setSponsor2(Sponsor sponsor2) {
		this.sponsor2 = sponsor2;
	}


}
