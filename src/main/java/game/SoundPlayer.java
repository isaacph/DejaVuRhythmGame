package game;

import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;


public class SoundPlayer {

    private long device, context;

    private static class SoundInfo {
        public int buffer;

        public SoundInfo(int b) {
            this.buffer = b;
        }
    }

    private final Map<SoundHandle, SoundInfo> sounds = new HashMap<>();

    private final int[] sources = new int[100];
    private final double[] sourceEnd = new double[sources.length];

    public SoundPlayer() {

        this.device = alcOpenDevice((ByteBuffer) null);
        if (device == 0L) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        this.context = alcCreateContext(device, (IntBuffer) null);
        if (context == 0L) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        alGenSources(sources);
        for(int i = 0; i < sourceEnd.length; ++i) {
            sourceEnd[i] = glfwGetTime();
        }
    }

    public void play(SoundHandle sound, float duration) {
        int minSourceIndex = 0;
        double minSourceTime = Double.MAX_VALUE;

        for(int i = 0; i < sources.length; ++i) {
            if(sourceEnd[i] < minSourceTime) {
                minSourceTime = sourceEnd[i];
                minSourceIndex = i;
            }
        }

        System.out.print(sound + ": ");
        System.out.println(sounds.get(sound).buffer + ", " + sources[minSourceIndex]);
        alSourceUnqueueBuffers(sources[minSourceIndex]);
        alSourcei(sources[minSourceIndex], AL_BUFFER, sounds.get(sound).buffer);
        alSourcePlay(sources[minSourceIndex]);

        sourceEnd[minSourceIndex] = glfwGetTime() + duration;
    }

    public SoundHandle loadSound(String path) {
        ByteBuffer data = FileUtil.loadDataFromFile(path);
        if(data == null) {
            throw new RuntimeException("Sound file not found: " + path);
        }

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channelsB = stack.ints(0);
            IntBuffer sampleRateB = stack.ints(0);

            ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(data, channelsB, sampleRateB);
            checkALError("start " + path);
            if(rawAudioBuffer == null) {
                throw new RuntimeException("Failed to decode sound file: " + path);
            }

            int channels = channelsB.get();
            int sampleRate = sampleRateB.get();

            //Find the correct OpenAL format
            int format = -1;
            if(channels == 1) {
                format = AL_FORMAT_MONO16;
            } else if(channels == 2) {
                format = AL_FORMAT_STEREO16;
            }

            SoundInfo info = new SoundInfo(alGenBuffers());
            checkALError(path);

            alBufferData(info.buffer, format, rawAudioBuffer, sampleRate);
            checkALError(path);

            free(rawAudioBuffer);

            SoundHandle handle = new SoundHandle(path);
            sounds.put(handle, info);
            return handle;
        }
    }

    public void destroy() {
        for(SoundInfo info : sounds.values()) {
            alDeleteBuffers(info.buffer);
        }
        alcDestroyContext(context);
        alcCloseDevice(device);
    }

    public static void checkALError(String filename)
    {
        int error = alGetError();
        if(error != AL_NO_ERROR)
        {
            StringBuilder err = new StringBuilder();
            err.append("***ERROR*** (").append(filename).append(")\n");
            switch(error)
            {
                case AL_INVALID_NAME:
                    err.append("AL_INVALID_NAME: a bad name (ID) was passed to an OpenAL function");
                    break;
                case AL_INVALID_ENUM:
                    err.append("AL_INVALID_ENUM: an invalid enum value was passed to an OpenAL function");
                    break;
                case AL_INVALID_VALUE:
                    err.append("AL_INVALID_VALUE: an invalid value was passed to an OpenAL function");
                    break;
                case AL_INVALID_OPERATION:
                    err.append("AL_INVALID_OPERATION: the requested operation is not valid");
                    break;
                case AL_OUT_OF_MEMORY:
                    err.append("AL_OUT_OF_MEMORY: the requested operation resulted in OpenAL running out of memory");
                    break;
                default:
                    err.append("UNKNOWN AL ERROR: ").append(error);
            }
            err.append("\n");
            throw new RuntimeException("OpenAL " + err.toString());
        }
    }
}
