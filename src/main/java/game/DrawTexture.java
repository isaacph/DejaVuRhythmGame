package game;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class DrawTexture {

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

    public DrawTexture() {
        int vshader = ShaderUtil.createShader("texture_v.glsl", GL_VERTEX_SHADER);
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

    public void draw(Matrix4f matrix, Vector4f color) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matBuffer = stack.mallocFloat(16);

            glUseProgram(program);
            glUniformMatrix4fv(uniformMatrix, false, matrix.get(matBuffer));
            glUniform4f(uniformColor, color.x, color.y, color.z, color.w);
            glUniform1i(uniformSampler, 0); // use GL_TEXTURE0

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
