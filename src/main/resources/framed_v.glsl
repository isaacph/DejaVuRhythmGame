#version 330 core

in vec2 position;
in vec2 texCoord;

out vec2 textureCoord;

uniform mat4 matrix;
uniform vec2 minTex;
uniform vec2 maxTex;

void main() {
    gl_Position = matrix * vec4(position, 0, 1);
    textureCoord = vec2(texCoord.x * (maxTex.x - minTex.x), texCoord.y * (maxTex.y - minTex.y)) + minTex;
}
