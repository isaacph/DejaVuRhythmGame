package game;

import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileUtil {

    public static InputStream getInputStream(String path) throws IOException {
//        return new FileInputStream("src/main/resources/" + path);
        InputStream stream = FileUtil.class.getClassLoader().getResourceAsStream("" + path);
        try {
            if(stream == null) stream = new FileInputStream("" + path);
        } catch(IOException e) {
            e.printStackTrace();
            stream = null;
        }
        if(stream == null) {
            throw new IOException("Resource not found at " + path);
        }
        return stream;
    }

    public static OutputStream getOutputStream(String path) throws IOException {
        return new FileOutputStream(path);
    }

    public static String readFile(String path) throws IOException {
        InputStream stream = getInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder content = new StringBuilder();
        while(reader.ready()) {
            content.append(reader.readLine()).append("\n");
        }
        stream.close();
        return content.toString();
    }

    public static ByteBuffer loadDataFromFile(String path) {
        ByteBuffer data;
        try {
            InputStream stream = FileUtil.getInputStream(path);
            ReadableByteChannel channel = Channels.newChannel(stream);
            data = MemoryUtil.memAlloc(stream.available());
            channel.read(data);
            channel.close();
            data.flip();
        } catch(Exception e) {
            System.err.println("Error loading texture " + path);
            e.printStackTrace();
            return null;
        }
        return data;
    }
}
