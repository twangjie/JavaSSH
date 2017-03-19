package javassh;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.*;
import org.apache.commons.vfs2.provider.sftp.*;

import java.io.*;

import org.apache.commons.net.io.Util;
import org.apache.commons.net.io.CopyStreamListener;
import org.junit.Test;

/**
 * Created by 王杰 on 2017/03/18.
 */
public class Scp implements CopyStreamListener {

    /**
     * http://stackoverflow.com/questions/11101267/how-do-i-monitor-the-progress-of-a-file-transfer-in-apache-commons-vfs
     * @param sourceFile
     * @param destinationFile
     * @param progressMonitor
     * @throws IOException
     */
    public void copy(FileObject sourceFile, FileObject destinationFile, CopyStreamListener progressMonitor) throws IOException {

        InputStream sourceFileIn = sourceFile.getContent().getInputStream();
        try {
            OutputStream destinationFileOut = destinationFile.getContent().getOutputStream();
            try {
                Util.copyStream(sourceFileIn, destinationFileOut, Util.DEFAULT_COPY_BUFFER_SIZE, sourceFile.getContent().getSize(), progressMonitor);
            } finally {
                destinationFileOut.close();
            }
        } finally {
            sourceFileIn.close();
        }
    }

    public void bytesTransferred(CopyStreamEvent event) {

        bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
    }

    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
        System.out.println( String.format("totalBytesTransferred:%d, bytesTransferred:%d, streamSize: %d", totalBytesTransferred, bytesTransferred, streamSize));
    }

    public static boolean isSftpRemotePath(String path){
        return  path.toLowerCase().startsWith("sftp");
    }

    public static void ScpTransfer(String sourceUri, String destUri){

        StandardFileSystemManager manager = new StandardFileSystemManager();

        try {
            //Initializes the file manager
            manager.init();

            //Setup our SFTP configuration
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
            SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

            FileObject sourcefileObject;
            if(isSftpRemotePath(sourceUri)) {
                sourcefileObject = manager.resolveFile(sourceUri, opts);
            }else {
                sourcefileObject = manager.resolveFile(new File(sourceUri).getAbsolutePath());
            }

            FileObject destFileObject;

            if(isSftpRemotePath(destUri)) {
                destFileObject = manager.resolveFile(destUri, opts);
            }else {
                destFileObject = manager.resolveFile(new File(destUri).getAbsolutePath());
            }

            destFileObject.copyFrom(sourcefileObject, Selectors.SELECT_ALL);

            System.out.println("File upload successful");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            manager.close();
        }
    }

    @Test
    public void testUpload() {

        String sourceUri = "D:/spark-1.5.1/examples/src/main/resources";
        //Create the SFTP URI using the host name, userid, password,  remote path and file name
        String destUri = "sftp://mahjong:mahjong123@202.5.16.25//home/mahjong/wangjie/resources";

        ScpTransfer(sourceUri, destUri);
    }

    @Test
    public void testDownload() {

        String destUri = "D:/spark-1.5.1/examples/src/main/resources";
        //Create the SFTP URI using the host name, userid, password,  remote path and file name
        String sourceUri = "sftp://mahjong:mahjong123@202.5.16.25//home/mahjong/wangjie/resources";

        ScpTransfer(sourceUri, destUri);
    }

    public static void main(String[] args) throws Exception {

        if(args.length != 0){

            System.out.println("Usage:\n\tjava -cp JavaSSH.jar sourceUri destUri\n\t" +
                    "e.g. java -cp JavaSSH.jar ");

            System.exit(1);
        }

    }
}
