package game;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class DrawSimple {

    private static final float[] points = {
            -0.5f, -0.5f,
            -0.5f, +0.5f,
            +0.5f, +0.5f,
            +0.5f, +0.5f,
            +0.5f, -0.5f,
            -0.5f, -0.5f
    };

    public int program;
    public int vao;
    public int vbo;
    public int uniformMatrix;
    public int uniformColor;

    public DrawSimple() {
        int vshader = ShaderUtil.createShader("simple_v.glsl", GL_VERTEX_SHADER);
        int fshader = ShaderUtil.createShader("simple_f.glsl", GL_FRAGMENT_SHADER);
        program = glCreateProgram();
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glBindAttribLocation(program, ShaderUtil.Attribute.POSITION.position, "position");
        glLinkProgram(program);
        ShaderUtil.checkLinking(program);
        glUseProgram(program);
        ShaderUtil.checkGLError("Simple shader program");
        glDeleteShader(vshader);
        glDeleteShader(fshader);

        uniformMatrix = glGetUniformLocation(program, "matrix");
        uniformColor = glGetUniformLocation(program, "color");

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, points, GL_STATIC_DRAW);

        glEnableVertexAttribArray(ShaderUtil.Attribute.POSITION.position);
        glVertexAttribPointer(ShaderUtil.Attribute.POSITION.position, 2, GL_FLOAT, false, 2 * 4, 0);
    }

    public void draw(Matrix4f matrix, Vector4f color) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matBuffer = stack.mallocFloat(16);

            glUseProgram(program);
            glUniformMatrix4fv(uniformMatrix, false, matrix.get(matBuffer));
            glUniform4f(uniformColor, color.x, color.y, color.z, color.w);

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
