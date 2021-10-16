package game;

import java.io.*;

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
}
