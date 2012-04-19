package org.docear.plugin.services.features.elements;

import java.net.URL;
import java.sql.Date;

public class Version implements Comparable<Version> {
	private final int id;
	private final URL href;
	
	private Date releaseDate;	
	private Integer buildNumber;
	private String releaseNotes;
	
	int[] versionNumber = new int[5];
	
	public enum VersionField {
		MAJOR (0), MIDDLE(1), MINOR(2), STATUS(3), STATUS_NO(4);		
		public final int index;
		
		VersionField(int index) {
			this.index = index;			
		}
	}
	
	public enum CompareCode {
		LOWER(-1), EQUALS(0), DEVEL(1), BETA(2), MINOR(3), MIDDLE(4), MAJOR(5);		
		public final int code;
		
		CompareCode(int code) {
			this.code = code;
		}
	}
	
	public enum StatusName {
		devel(1), alpha(2), beta(3), rc(4), stable(5);
		public final int code;
		
		StatusName(int code) {
			this.code = code;
		}
	}
	
	public Version() {
		this.id = -1;
		this.href = null;
	}
	
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
		return versionNumber[VersionField.MAJOR.index];
	}

	public void setMajorVersion(Integer majorVersion) {
		versionNumber[VersionField.MAJOR.index] = majorVersion;
	}

	public Integer getMiddleVersion() {
		return versionNumber[VersionField.MIDDLE.index];
	}

	public void setMiddleVersion(Integer middleVersion) {
		versionNumber[VersionField.MIDDLE.index] = middleVersion;
	}

	public Integer getMinorVersion() {
		return versionNumber[VersionField.MINOR.index];
	}

	public void setMinorVersion(Integer minorVersion) {
		this.versionNumber[VersionField.MINOR.index] = minorVersion;
	}

	public String getStatus() {
		for(StatusName status : StatusName.values()) {
			if(status.code == versionNumber[VersionField.STATUS.index]) {
				return status.name();
			}
		}
		return null;
	}

	public void setStatus(String status) {
		versionNumber[VersionField.STATUS.index] = StatusName.valueOf(status).code;
	}

	public Integer getStatusNumber() {
		return versionNumber[VersionField.STATUS_NO.index];
	}

	public void setStatusNumber(Integer statusNumber) {
		this.versionNumber[VersionField.STATUS_NO.index] = statusNumber;
	}

	public String getReleaseNotes() {
		return releaseNotes;
	}

	public void setReleaseNotes(String releaseNotes) {
		this.releaseNotes = releaseNotes;
	}

	protected long getComputableVersionNumber() {
		long result = 0;		
		for (int field : versionNumber) {
			result = result*1000 + field; 
		}
		
		return result;
	}
	
	public int compareTo(Version o) {
		long me = this.getComputableVersionNumber();
		long other = o.getComputableVersionNumber();
		
		if (me < other) {
			return CompareCode.LOWER.code;
		}		
		else if (me == other) {
			if (this.getBuildNumber() < o.getBuildNumber()) {
				return CompareCode.LOWER.code;
			}
			else if (this.getBuildNumber() > o.getBuildNumber()) {
				return CompareCode.DEVEL.code;
			}
			else {
				return CompareCode.EQUALS.code;
			}
		}
		
		//ME > OTHER
		int index = 0;
		
		for (int i=VersionField.STATUS_NO.index; i>=VersionField.MAJOR.index; i--) {
			if((other%1000) < versionNumber[i]) {
				index = i;
			}
			other /= 1000;
		}
		
		if (index == VersionField.STATUS_NO.index) {
			return CompareCode.BETA.code;
		}
		else if (index == VersionField.STATUS.index) {
			return CompareCode.MAJOR.code;
		}
		else if (index == VersionField.MINOR.index) {
			return CompareCode.MINOR.code;
		}
		else if (index == VersionField.MIDDLE.index) {
			return CompareCode.MIDDLE.code;
		}
		
		return CompareCode.MAJOR.code;
	}	
}
