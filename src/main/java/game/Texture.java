package game;

import org.joml.Vector2i;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class Texture {

    private final int handle;

    public Texture(int handle) {
        this.handle = handle;
    }

    public Texture(ByteBuffer data, Vector2i size, Settings settings) {
        handle = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, handle);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x, size.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, settings.filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, settings.filter);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public Texture(FloatBuffer data, Vector2i size, Settings settings) {
        handle = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, handle);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x, size.y, 0, GL_RGBA, GL_FLOAT, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, settings.filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, settings.filter);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, handle);
    }

    public void destroy() {
        glDeleteTextures(handle);
    }

    public static Texture makeTexture(String path) {
        try {
            Vector2i size = new Vector2i();
            ByteBuffer data = loadFromFile(path, size);
            return new Texture(data, size, new Settings());
        } catch (IOException e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
            try(MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer data = stack.malloc(16);
                data.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) -1);
                data.put((byte) -1).put((byte) -1).put((byte) -1).put((byte) -1);
                data.put((byte) -1).put((byte) -1).put((byte) -1).put((byte) -1);
                data.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) -128);
                data.flip();
                Vector2i size = new Vector2i(2, 2);
                return new Texture(data, size, new Settings());
            }
        }
    }

    public static ByteBuffer loadFromFile(String path, Vector2i destSize) throws IOException {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.ints(0);
            IntBuffer h = stack.ints(0);
            IntBuffer bpp = stack.ints(0);
            ByteBuffer data = FileUtil.loadDataFromFile(path);
            ByteBuffer bitmap = STBImage.stbi_load_from_memory(data, w, h, bpp, 4);
            destSize.x = w.get();
            destSize.y = h.get();
            return bitmap;
        }
    }

    public static class Settings {
        public int wrap, filter;

        public Settings() {//default
            this(GL_CLAMP_TO_EDGE, GL_NEAREST);
        }

        public Settings(int wrap, int filter) {
            this.wrap = wrap;
            this.filter = filter;
        }
    }
}
