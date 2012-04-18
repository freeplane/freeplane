package org.docear.plugin.services.features.elements;

import java.net.URL;
import java.sql.Date;

public class Version {
	private final int id;
	private final URL href;
	
	private Date releaseDate;	
	private Integer buildNumber;
	private Integer majorVersion;
	private Integer middleVersion;
	private Integer minorVersion;
	private String status;
	private Integer statusNumber;
	private String releaseNotes;
	
	public Version(int id, URL href) {
		this.id = id;
		this.href = href;
	}

	public int getId() {
		return id;
	}

	public URL getHref() {
		return href;
	}
	
	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Integer getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(Integer buildNumber) {
		this.buildNumber = buildNumber;
	}

	public Integer getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(Integer majorVersion) {
		this.majorVersion = majorVersion;
	}

	public Integer getMiddleVersion() {
		return middleVersion;
	}

	public void setMiddleVersion(Integer middleVersion) {
		this.middleVersion = middleVersion;
	}

	public Integer getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(Integer minorVersion) {
		this.minorVersion = minorVersion;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getStatusNumber() {
		return statusNumber;
	}

	public void setStatusNumber(Integer statusNumber) {
		this.statusNumber = statusNumber;
	}

	public String getReleaseNotes() {
		return releaseNotes;
	}

	public void setReleaseNotes(String releaseNotes) {
		this.releaseNotes = releaseNotes;
	}	
}
