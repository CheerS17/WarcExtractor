package mjq.cwru;

import org.apache.commons.io.IOUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) throws IOException {


        File warcPath = new File(pathname);
        File[] warcs = warcPath.listFiles();
        for (int i = 1; i < warcs.length; i++) {
            System.out.println(warcs[i]);
            String fileName = warcs[i].toString().split("/")[6];
            fileName = fileName.split("\\.")[0];
            String output = output_dir + fileName;
            System.out.println(output);

            findHtmlBodyInWarchRecord(warcs[i].toString(), output);
        }

    }
    private static String output_dir = "/Users/majunqi0102/Research/HTMLandWARC/HTMLs/";
//    private static String pathname = "/Users/majunqi0102/Research/HTMLs/CC-MAIN-20161202170900-00001-ip-10-31-129-80.ec2.internal.warc.gz";
    private static String pathname = "/Users/majunqi0102/Research/HTMLandWARC/WARCs";

    private static void findHtmlBodyInWarchRecord(String pathname, String output) {
        File f = new File(pathname);
        File dir = new File(output);
        //if HTMLs from this warc doesn't exist, make dir
        //if not return;
        if (dir.exists() == false)
            dir.mkdir();
        else return;

        ParserWarc pw = new ParserWarc(f);
        HashSet<HtmlEntity> htmlEntityHashSet = pw.getHtmlSet();

        MJQFileWriter fw = new MJQFileWriter();
        Iterator it = htmlEntityHashSet.iterator();
        int i  = 0;
        while (it.hasNext()) {
            HtmlEntity html = (HtmlEntity) it.next();
            fw.openFile( output+"/"+i + ".html");
            fw.writeFile(html.getContent());
//            System.out.println(html.getContent());
            i++;
        }
    }

    private static void findWarcRecordHeader() throws IOException {
        // Set up a local compressed WARC file for reading
//        String fn = "data/CC-MAIN-20131204131715-00000-ip-10-33-133-15.ec2.internal.warc.gz";
        String fn = "CC-MAIN-20151124205404-00008-ip-10-71-132-137.ec2.internal.warc.gz";
        FileInputStream is = new FileInputStream(fn);
        // The file name identifies the ArchiveReader and indicates if it should be decompressed
        ArchiveReader ar = WARCReaderFactory.get(fn, is, true);

        // Once we have an ArchiveReader, we can work through each of the records it contains
        int i = 0;
        int j = 0;
        for (ArchiveRecord r : ar) {
            // The header file contains information such as the type of record, size, creation time, and URL
            System.out.println(r.getHeader());
            System.out.println(r.getHeader().getUrl());
            System.out.println();

            // If we want to read the contents of the record, we can use the ArchiveRecord as an InputStream
            // Create a byte array that is as long as the record's stated length
            byte[] rawData = IOUtils.toByteArray(r, r.available());

            // Why don't we convert it to a string and print the start of it? Let's hope it's text!
            String content = new String(rawData);
//            System.out.println(content.substring(0, Math.min(500, content.length())));
//            System.out.println((content.length() > 500 ? "..." : ""));
            System.out.println(content);

            // Pretty printing to make the output more readable
            System.out.println(j + "=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=");
            j++;
            if (i++ > 4) break;
        }

    }
}

