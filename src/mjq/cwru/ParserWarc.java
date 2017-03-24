package mjq.cwru;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by majunqi0102 on 7/8/16.
 */

public class ParserWarc {
    private static final Logger logger = LoggerFactory
            .getLogger(ParserWarc.class);

    private File file = null;
    private GZIPInputStream gzInputStream = null;
    private DataInputStream inStream = null;
    private WarcRecord thisWarcRecord = null;

    public ParserWarc(File file) {
        super();
        this.file = file;
        try {
            gzInputStream = new GZIPInputStream(new FileInputStream(this.file));
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        inStream = new DataInputStream(gzInputStream);
    }

    public HashSet<HtmlEntity> getHtmlSet() {
        HashSet<HtmlEntity> htmlEntitySet = new HashSet<HtmlEntity>();
        int i = 1;
        try {
            while (((thisWarcRecord = WarcRecord.readNextWarcRecord(inStream)) != null) && i <= 10000)  {
                try {
                    // see if it's a response record
                    if (thisWarcRecord.getHeaderRecordType().equals("response")
                            && thisWarcRecord.getHeaderMetadataItem(
                            "Content-Type").indexOf("application/http") != -1) {
                        // it is - create a WarcHTML record
                        WarcHTMLResponseRecord htmlRecord = new WarcHTMLResponseRecord(thisWarcRecord);
                        // get our TREC ID and target URI
                        String thisTargetURI = htmlRecord.getTargetURI();

                        InputStreamReader in = new InputStreamReader(
                                new ByteArrayInputStream(
                                        thisWarcRecord.getContent()));

                        BufferedReader br = new BufferedReader(in);

                        String line = br.readLine();
                        Map<String, String> httpHeaderMap = new HashMap<String, String>();
                        while (!(line = br.readLine()).isEmpty()) {
                            int temp = line.indexOf(":");
                            httpHeaderMap.put(line.substring(0, temp).trim(),
                                    line.substring(temp + 1).trim());
                        }

                        StringBuffer sb = new StringBuffer();
                        while ((line = br.readLine()) != null)
                            sb.append(line);

                        // System.out.println(thisWarcRecord.getContentUTF8());
                        if (httpHeaderMap.get("Content-Type").indexOf(
                                "text/html") == -1)
                            continue;

                        /*
                         * for (Entry<String, String> per :
                         * httpHeaderMap.entrySet()) {
                         * System.out.println(per.getKey()+":"+per.getValue());
                         * }
                         */

                        HtmlEntity htmlEntity = new HtmlEntity();

                        htmlEntity.setHeaderMap(httpHeaderMap);
                        htmlEntity.setContent(sb.toString());
                        htmlEntity.setUrl(thisTargetURI);
                        br.close();
                        in.close();

                        htmlEntitySet.add(htmlEntity);
                        i++;
                    }

                } catch (Exception e) {
                    System.out.println("There is not Content in this HTML");
                    e.printStackTrace();
                    logger.error(e.getMessage());

                }
            }

            try {
                if (inStream!= null)
                    inStream.close();
                if (gzInputStream != null)
                    gzInputStream.close();
            } catch (IOException e) {
                System.out.println("inStream exceiption");
                logger.error(e.getMessage());
                e.printStackTrace();
            }

            return htmlEntitySet;
        } catch (Exception e) {
            System.out.println("最外面的exception");
            e.printStackTrace();
            return null;
        }
    }
}
