package mythx.launcher.xml;

public class XMLReader {
	
	private static Feed news;
	private static Feed archives;

	/*public static void init(NewsBox newsBox, JLabel date) {
		try {
			news = getNews();
			archives = getArchivedNews();

			if(news != null) {
				date.setText(news.getMessages().get(0).getTitleDate());
			}
			
			newsBox.getTitle().setText(news == null ? "No news available" : news.getMessages().get(0).getTitle());
			String newsText = news == null ? "-----" : news.getMessages().get(0).getCleanDesc();
			
			for (String line : newsText.split("<br>")) {
				newsBox.append("<p style='color:#AAAAAA;margin:0;font-size:8px;font-family:arial;'> "+line+" </p>");
			}
			
			if (archives == null) {
				return;
			}
			
			int startY = 0;
			
			for (FeedMessage msg : archives.getMessages()) {
				newsBox.add(new ArchiveBox(msg, new Rectangle(246, startY, 194, 39)));
				startY += 50;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Feed getArchivedNews() {
		RSSFeedParser parser = new RSSFeedParser(Constants.NEWS_URL);
		Feed feed = parser.readFeed();
		return feed;
	}
	
	public static Feed getNews() {
		RSSFeedParser parser = new RSSFeedParser(Constants.NEWS_URL);
		Feed feed = parser.readFeed();
		return feed;
	}*/

}
