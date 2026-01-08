package com.mythx.launcher.cache;

import com.mythx.launcher.Launch;
import com.mythx.launcher.utility.Utilities;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Jonny on 8/21/2019
 **/
public class CacheDownloader {

    public static void init() throws IOException {

        loadAllData();

        //download();

        saveSizes();
    }

    public static void loadAllData() throws MalformedURLException {
        loadSizes();

        String url = Launch.USE_SECONDARY_WEBSITE ? "http://therealmrsps.com/realm/realm.zip" : "http://runelinks.com/mythx.zip";

        remoteSize = getFileSize(new URL(url));
        remoteLastModified = getLastModified(new URL(url));

    }

    public static long personalSize;
    public static long personalLastModified;

    public static long remoteSize;
    public static long remoteLastModified;

    /**
     * Starts the initial download process
     * @param cache
     */
    public static boolean download() {

        if(!needsUpdate()) {
            return false;
        }

        try {
            download("realm.zip", Launch.USE_SECONDARY_WEBSITE ? "http://therealmrsps.com/realm/realm.zip" : "http://runelinks.com/realm/realm.zip");

           // new Thread(() -> {
                unZip("realm.zip");
           // }).start();

            System.out.println("Attempting to download.. ");

            File downloaded = new File(Utilities.getCacheDirectory() + File.separator + "realm.zip");

            if(downloaded != null && downloaded.exists()) {
                downloaded.delete();
            }


            saveSizes();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    public static boolean needsUpdate() {

        return (((remoteSize != personalSize) || (remoteLastModified != personalLastModified)));
    }


    /**
     * Downloads the cache
     * @param fileName
     * @param downloadUrl
     * @throws IOException
     */
    public static void download(String fileName, String downloadUrl) throws IOException {
        URL url = new URL(downloadUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.addRequestProperty("User-Agent", "Mozilla/4.76");
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = Utilities.getCacheDirectory() + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[1024 * 64];
            long startTime = System.currentTimeMillis();
            int downloaded = 0;
            long numWritten = 0;
            int length = httpConn.getContentLength();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                numWritten += bytesRead;
                downloaded += bytesRead;
                int percentage = (int)(((double)numWritten / (double)length) * 100D);
                @SuppressWarnings("unused")
                int downloadSpeed = (int) ((downloaded / 1024) / (1 + ((System.currentTimeMillis() - startTime) / 1000)));
             //   Client.instance.drawLoadingBar(percentage, (new StringBuilder()).append("Downloading "+fileName+" ("+downloadSpeed+"kb/s): "+percentage+"%").toString());
            }

            outputStream.close();
            inputStream.close();

        } else {
            System.out.println("runelinks.com replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    /**
     * Starts the intiial file unzipping process
     * @param fileName
     */
    public static void unZip(String fileName) {
        try {

            File outFile = new File(Utilities.getCacheDirectory());

            String zipFile = Utilities.getCacheDirectory() + fileName;

        //    Client.instance.drawLoadingBar(0, (new StringBuilder()).append("Preparing to unzip "+fileName).toString());

            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));

            ZipEntry e;

            long max = 0;
            long curr = 0;

            while ((e = zin.getNextEntry()) != null) {
                max += e.getSize();
            }

            zin.close();

            ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));

            while ((e = in.getNextEntry()) != null) {
                if (e.isDirectory()) {
                    new File(outFile, e.getName()).mkdirs();
                } else {
                    FileOutputStream out = new FileOutputStream(new File(outFile, e.getName()));

                    byte[] b = new byte[1024];

                    int len;

                    while ((len = in.read(b)) != -1) {
                        curr += len;
                        out.write(b, 0, len);
                    }

                  //  Client.instance.drawLoadingBar((int) ((curr * 100) / max), (new StringBuilder()).append("Extracting "+fileName+": "+(int) ((curr * 100) / max)+"%").toString());

                    out.flush();
                    out.close();
                }
            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getFileSize(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            return conn.getContentLengthLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static long getLastModified(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            return conn.getLastModified();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static void saveSizes() {
        try {
            PrintWriter writer = new PrintWriter(Utilities.getCacheDirectory() + File.separator + "versions", "UTF-8");

            writer.println(personalSize+":size");
            writer.println(personalLastModified+":modified");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadSizes() {
        File file = new File(Utilities.getCacheDirectory() + File.separator + "versions");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch(FileNotFoundException e) {
            return;
        }
        String line;
        try {
            while ((line = reader.readLine()) != null) {

                try {
                    String[] args = line.split(":");
                    if (args.length <= 1)
                        continue;
                    String value = args[0], type = args[1];

                    switch (type) {
                        case "size":
                            long size = Long.parseLong(value);
                            personalSize = size;
                            break;
                        case "modified":
                            long modified = Long.parseLong(value);
                            personalLastModified = modified;
                            break;
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
