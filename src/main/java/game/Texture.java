package game;

import org.joml.Vector2i;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
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
                data.put((byte) 0);
                data.put((byte) -128);
                data.put((byte) -128);
                data.put((byte) 0);
                data.flip();
                Vector2i size = new Vector2i(2, 2);
                return new Texture(data, size, new Settings());
            }
        }
    }

    public static ByteBuffer loadFromFile(String path, Vector2i destSize) throws IOException {
        InputStream file = FileUtil.getInputStream(path);
        ByteBuffer data = MemoryUtil.memAlloc(file.available());
        Channels.newChannel(file).read(data);
        int[] w = {0};
        int[] h = {0};
        int[] channels = {0};
        stbi_load_from_memory(data, w, h, channels, 4);
        if (channels[0] != 4) {
            System.err.println("Weird number of channels in " + path + ": " + channels[0]);
        }
        return data;
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
