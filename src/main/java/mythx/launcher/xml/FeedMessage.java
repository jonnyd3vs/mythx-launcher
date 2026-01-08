package mythx.launcher.xml;

/*
 * Represents one RSS message
 */
public class FeedMessage {

	String item;
	String title;
	String description;
	String link;
	String author;
	String guid;
	String pubDate;
	
	public String getTitle() {
		return title;
	}

	public String getPubDate() {
		return pubDate;
	}
	
	public void setItem(String item) {
		this.item = item;
	}
	
	public String getItem() {
		return item;
	}
	
	public String getSplitDate() {
		String[] parts = pubDate.split(" ");
		return ""+parts[2]+" "+parts[1]+" "+parts[3]+" "+parts[4]+"";
	}

	public String getTitleDate() {
		String[] parts = pubDate.split(" ");
		return ""+parts[2]+" "+parts[1]+" "+parts[3]+"";
	}
	
	public void setPubDate(String date) {
		this.pubDate = date;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	
	public String getCleanDesc() {
		return description.replace("<p>", "\n")
				.replace("</p>", "")
				.replaceAll("\\<.*?\\>", "")
				.replace("\n", "<br>")
				.replace("&nbsp;", " ")
				.replace("<br><br>", "<br>");
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public String toString() {
		return "FeedMessage [title=" + title + ", description=" + description + ", link=" + link + ", author=" + author
				+ ", guid=" + guid + "]";
	}

}