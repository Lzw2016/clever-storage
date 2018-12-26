//package org.clever.storage.test;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPClientConfig;
//import org.apache.commons.net.ftp.FTPFile;
//import org.clever.storage.utils.FTPClientTemplate;
//import org.junit.Test;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.Charset;
//
///**
// * 作者： lzw<br/>
// * 创建时间：2018-12-26 10:44 <br/>
// */
//@Slf4j
//public class FTPClientTemplateTest {
//
//    private static final String host = "47.98.40.187";
//    private static final int port = 31021;
//    private static final String username = "ftpuser";
//    private static final String password = "";
//
//    @Test
//    public void t01() throws IOException {
//
//
//        FTPClientTemplate ftpClientTemplate = new FTPClientTemplate(host, port, username, password);
//        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_NT);
//        ftpClientTemplate.getFtpclient().configure(config);
//        log.info("### getSystemType ============ {}", ftpClientTemplate.getFtpclient().getSystemType());
////        log.info("### feat ============ {}", ftpClientTemplate.getFtpclient().feat());
////        log.info("### pwd ============ {}", ftpClientTemplate.getFtpclient().pwd());
//        log.info("### pasv ============ {}", ftpClientTemplate.getFtpclient().pasv());
////        log.info("### mlsd ============ {}", ftpClientTemplate.getFtpclient().mlsd());
//
//        ftpClientTemplate.getFtpclient().setCharset(Charset.forName("UTF-8"));
//        log.info("### printWorkingDirectory ============ {}", ftpClientTemplate.getFtpclient().printWorkingDirectory());
//        log.info("### ============ {}", ftpClientTemplate.existsFile("壁纸01.jpg"));
//
//        InputStream inputStream = new ByteArrayInputStream("123456789壁纸01".getBytes());
//        log.info("### uploadFile ============ {}", ftpClientTemplate.uploadFile("/123.txt", inputStream));
//        inputStream.close();
//        log.info("### getReplyCode ============ {}", ftpClientTemplate.getFtpclient().getReplyCode());
//
//        FTPFile[] ftpFiles = ftpClientTemplate.getFtpclient().listFiles("/");
//        for (FTPFile ftpFile : ftpFiles) {
//            log.info("### ============ {}", ftpFile.getName());
//        }
//        // ftpClientTemplate.mkdir("/work/Java/web");
//        ftpClientTemplate.close();
//    }
//
//    @Test
//    public void t02() throws IOException {
//        FTPClient ftpClient = new FTPClient();
//
//        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_NT);
////        config.set
//        ftpClient.configure(config);
//
//        ftpClient.connect(host, port);
//        ftpClient.login(username, password);
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//        ftpClient.setCharset(Charset.forName("UTF-8"));
//        ftpClient.setControlEncoding("UTF-8");
//        ftpClient.enterLocalPassiveMode();
////        ftpClient.enterRemotePassiveMode();
//        log.info("### printWorkingDirectory ============ {}", ftpClient.printWorkingDirectory());
//
//        FTPFile[] ftpFiles = ftpClient.listFiles("/");
//        for (FTPFile ftpFile : ftpFiles) {
//            log.info("### ============ {}", ftpFile.getName());
//        }
//
//        ftpClient.logout();
//        ftpClient.disconnect();
//    }
//}
