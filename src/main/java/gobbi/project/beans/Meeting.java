package gobbi.project.beans;

public class Meeting {

	private String title;
	private String dateTime;
	private Integer duration;
	private Integer max;
	
	public Meeting(String title, String dateTime, Integer duration, Integer max) {
		this.title=title;
		this.dateTime=dateTime;
		this.duration=duration;
		this.max=max;
	}
	
	public Meeting() {
		
	}
	
	public void setTitle(String title) {
		this.title=title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setDateTime(String dateTime) {
		this.dateTime=dateTime;
	}
	
	public String getDateTime() {
		return dateTime;
	}
	
	public void setDuration(Integer duration) {
		this.duration=duration;
	}
	
	public Integer getDuration() {
		return duration;
	}
	
	public void setMax(Integer max) {
		this.max=max;
	}
	
	public Integer getMax() {
		return max;
	}
	
}
