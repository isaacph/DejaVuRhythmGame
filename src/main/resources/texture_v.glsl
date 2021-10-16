#version 330 core

in vec2 position;
in vec2 texCoord;

out vec2 textureCoord;

uniform mat4 matrix;

void main() {
    gl_Position = matrix * vec4(position, 0, 1);
    textureCoord = texCoord;
}
