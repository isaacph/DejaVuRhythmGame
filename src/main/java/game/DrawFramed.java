package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class DrawFramed {

    private static final float[] points = {
            -0.5f, -0.5f, 0.0f, 0.0f,
            -0.5f, +0.5f, 0.0f, 1.0f,
            +0.5f, +0.5f, 1.0f, 1.0f,
            +0.5f, +0.5f, 1.0f, 1.0f,
            +0.5f, -0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.0f, 0.0f
    };

    public int program;
    public int vao;
    public int vbo;
    public int uniformMatrix;
    public int uniformColor;
    public int uniformSampler;
    public int uniformMinTex;
    public int uniformMaxTex;

    public DrawFramed() {
        int vshader = ShaderUtil.createShader("framed_v.glsl", GL_VERTEX_SHADER);
        int fshader = ShaderUtil.createShader("texture_f.glsl", GL_FRAGMENT_SHADER);
        program = glCreateProgram();
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glBindAttribLocation(program, ShaderUtil.Attribute.POSITION.position, "position");
        glBindAttribLocation(program, ShaderUtil.Attribute.TEXTURE.position, "texCoord");
        glLinkProgram(program);
        ShaderUtil.checkLinking(program);
        glUseProgram(program);
        ShaderUtil.checkGLError("Simple shader program");
        glDeleteShader(vshader);
        glDeleteShader(fshader);

        uniformMatrix = glGetUniformLocation(program, "matrix");
        uniformColor = glGetUniformLocation(program, "color");
        uniformSampler = glGetUniformLocation(program, "sampler");
        uniformMinTex = glGetUniformLocation(program, "minTex");
        uniformMaxTex = glGetUniformLocation(program, "maxTex");

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, points, GL_STATIC_DRAW);

        glEnableVertexAttribArray(ShaderUtil.Attribute.POSITION.position);
        glVertexAttribPointer(ShaderUtil.Attribute.POSITION.position, 2, GL_FLOAT, false, 4 * 4, 0);
        glEnableVertexAttribArray(ShaderUtil.Attribute.TEXTURE.position);
        glVertexAttribPointer(ShaderUtil.Attribute.TEXTURE.position, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
    }

    public void draw(Matrix4f matrix, Vector4f color, int frameCountX, int frameCountY, int frameIndex) {
        float frameSizeX = 1.0f / frameCountX, frameSizeY = 1.0f / frameCountY;
        float frameX = (frameIndex % frameCountX) * frameSizeX;
        float frameY = (frameIndex % frameCountY) * frameSizeY;
        draw(matrix, color, new Vector2f(frameX, frameY), new Vector2f(frameX + frameSizeX, frameY + frameSizeY));
    }

    public void draw(Matrix4f matrix, Vector4f color, Vector2f frameMin, Vector2f frameMax) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matBuffer = stack.mallocFloat(16);

            glUseProgram(program);
            glUniformMatrix4fv(uniformMatrix, false, matrix.get(matBuffer));
            glUniform4f(uniformColor, color.x, color.y, color.z, color.w);
            glUniform1i(uniformSampler, 0); // use GL_TEXTURE0
            glUniform2f(uniformMinTex, frameMin.x, frameMin.y);
            glUniform2f(uniformMaxTex, frameMax.x, frameMax.y);

            glBindVertexArray(vao);

            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
    }

    public void destroy() {
        glDeleteProgram(program);
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
    }
}
